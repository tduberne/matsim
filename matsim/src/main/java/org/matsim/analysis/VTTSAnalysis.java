/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package org.matsim.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;

import com.google.inject.Inject;

/**
 * This analysis computes the effective value of travel time savings (VTTS) for each agent and each trip.
 * The basic idea is to repeat the scoring for an earlier arrival time (or shorter travel time) and to compute the score difference.
 * The score difference is used to compute the agent's trip-specific VTTS applying a linearization.
 * 
 * @author ikaddoura
 *
 */
public class VTTSAnalysis implements AfterMobsimListener,IterationEndsListener,
ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler, TransitDriverStartsEventHandler {

	private final static Logger log = Logger.getLogger(VTTSAnalysis.class);
	private static int incompletedPlanWarning = 0;
		
	private final Set<Id<Person>> personIdsToBeIgnored = new HashSet<>(); // e.g. transit drivers		
	private String[] activitiesToBeSkipped = {"pt interaction"};
	private String[] modesToBeSkipped = {"transit_walk", "access_walk", "egress_walk"};
	
	private final Set<Id<Person>> departedPersonIds = new HashSet<>();
	private final Map<Id<Person>, Double> personId2currentActivityStartTime = new HashMap<>();
	private final Map<Id<Person>, Double> personId2firstActivityEndTime = new HashMap<>();
	private final Map<Id<Person>, String> personId2currentActivityType = new HashMap<>();
	private final Map<Id<Person>, String> personId2firstActivityType = new HashMap<>();
	private final Map<Id<Person>, Integer> personId2currentTripNr = new HashMap<>();
	private final Map<Id<Person>, String> personId2currentTripMode = new HashMap<>();
	
	private final Map<Id<Person>, List<Double>> personId2VTTSh = new HashMap<>();
	
	private final Map<Id<Person>, Map<Integer, Double>> personId2TripNr2VTTSh = new HashMap<>();
	private final Map<Id<Person>, Map<Integer, String>> personId2TripNr2Mode = new HashMap<>();	
	private final Map<Id<Person>, Map<Integer, Double>> personId2TripNr2DepartureTime = new HashMap<>();
			
	@Inject
	private Scenario scenario;
	
	@Inject
	private OutputDirectoryHierarchy controlerIO;

	@Override
	public void reset(int iteration) {
				
		incompletedPlanWarning = 0;
		
		this.personIdsToBeIgnored.clear();
		this.departedPersonIds.clear();
		this.personId2currentActivityStartTime.clear();
		this.personId2firstActivityEndTime.clear();
		this.personId2currentActivityType.clear();
		this.personId2firstActivityType.clear();
		this.personId2currentTripNr.clear();
		this.personId2currentTripMode.clear();
		
		this.personId2VTTSh.clear();
		this.personId2TripNr2VTTSh.clear();
		this.personId2TripNr2Mode.clear();
		
		this.personId2TripNr2DepartureTime.clear();
	}
	
	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		this.computeFinalVTTSforTripToOvernightActivity();
	}
	
	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		personIdsToBeIgnored.add(event.getDriverId());
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
	
		if (isModeToBeSkipped(event.getLegMode()) || this.personIdsToBeIgnored.contains(event.getPersonId())) {
			// skip
		
		} else {
			this.departedPersonIds.add(event.getPersonId());
			this.personId2currentTripMode.put(event.getPersonId(), event.getLegMode());
			
			if (this.personId2currentTripNr.containsKey(event.getPersonId())){
				this.personId2currentTripNr.put(event.getPersonId(), this.personId2currentTripNr.get(event.getPersonId()) + 1);
			} else {
				this.personId2currentTripNr.put(event.getPersonId(), 1);
			}
			
			if (this.personId2TripNr2DepartureTime.containsKey(event.getPersonId())) {
				this.personId2TripNr2DepartureTime.get(event.getPersonId()).put(this.personId2currentTripNr.get(event.getPersonId()), event.getTime());	
			} else {
				Map<Integer, Double> tripNr2departureTime = new HashMap<>();
				tripNr2departureTime.put(this.personId2currentTripNr.get(event.getPersonId()), event.getTime());
				this.personId2TripNr2DepartureTime.put(event.getPersonId(), tripNr2departureTime);
			}
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {	
		
		if (isActivityToBeSkipped(event.getActType()) || this.personIdsToBeIgnored.contains(event.getPersonId())) {
			// skip
		} else {
			this.personId2currentActivityStartTime.put(event.getPersonId(), event.getTime());
			this.personId2currentActivityType.put(event.getPersonId(), event.getActType());
		}
	
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
	
		if (isActivityToBeSkipped(event.getActType()) || this.personIdsToBeIgnored.contains(event.getPersonId())) {
			// skip			
		} else {
			if (this.personId2currentActivityStartTime.containsKey(event.getPersonId())) {
				// This is not the first activity...							
				computeVTTS(event.getPersonId(), event.getTime());
				
				// ... update the status of the 'current' activity...
				this.personId2currentActivityType.remove(event.getPersonId());
				this.personId2currentActivityStartTime.remove(event.getPersonId());
				
				this.departedPersonIds.remove(event.getPersonId());
		
			} else {
				// This is the first activity. The first and last / overnight activity are / is considered in a final step.
				// Therefore, the relevant information has to be stored.
				this.personId2firstActivityEndTime.put(event.getPersonId(), event.getTime());
				this.personId2firstActivityType.put(event.getPersonId(), event.getActType());
			}
		}		
		
	}
	
	void computeFinalVTTSforTripToOvernightActivity() {
		for (Id<Person> affectedPersonId : this.departedPersonIds) {
			computeVTTS(affectedPersonId, Time.getUndefinedTime());
		}
	}
	
	private void computeVTTS(Id<Person> personId, double activityEndTime) {
		
		if (this.personId2currentTripMode.get(personId) == null) {
			// No mode stored for this person and trip. This indicates that the current trip mode was skipped.
			// Thus, do not compute any VTTS for this trip.
		} else {
			double activityDelayDisutilityOneSec = 0.;
			
			final VTTSMarginalSumScoringFunction marginalSumScoringFunction = new VTTSMarginalSumScoringFunction(
					new ScoringParameters.Builder(scenario.getConfig().planCalcScore(), scenario.getConfig().planCalcScore().getScoringParameters(null), scenario.getConfig().scenario()).build());
			// TODO: account for different scoring parameters for different subpopulations
			
			// First, check if the agent has arrived at an activity
			if (this.personId2currentActivityType.containsKey(personId) && this.personId2currentActivityStartTime.containsKey(personId)) {
				
				if (activityEndTime == Time.getUndefinedTime()) {
					// The end time is undefined...
																
					// ... now handle the first and last OR overnight activity. This is figured out by the scoring function itself (depending on the activity types).
						
					Activity activityMorning = PopulationUtils.createActivityFromLinkId(this.personId2firstActivityType.get(personId), null);
					activityMorning.setEndTime(this.personId2firstActivityEndTime.get(personId));
					
					Activity activityEvening = PopulationUtils.createActivityFromLinkId(this.personId2currentActivityType.get(personId), null);
					activityEvening.setStartTime(this.personId2currentActivityStartTime.get(personId));
						
					activityDelayDisutilityOneSec = marginalSumScoringFunction.getOvernightActivityDelayDisutility(activityMorning, activityEvening, 1.);
					
				} else {
					// The activity has an end time indicating a 'normal' activity.
					
					Activity activity = PopulationUtils.createActivityFromLinkId(this.personId2currentActivityType.get(personId), null);
					activity.setStartTime(this.personId2currentActivityStartTime.get(personId));
					activity.setEndTime(activityEndTime);	
					activityDelayDisutilityOneSec = marginalSumScoringFunction.getNormalActivityDelayDisutility(activity, 1.);
				}
				
			} else {
				// No, there is no information about the current activity which indicates that the trip (with the delay) was not completed.
				
				if (incompletedPlanWarning <= 10) {
					log.warn("Agent " + personId + " has not yet completed the plan/trip (the agent is probably stucking). Cannot compute the disutility of being late at this activity. "
							+ "Something like the disutility of not arriving at the activity is required. Try to avoid this by setting a smaller stuck time period.");
					log.warn("Setting the disutilty of being delayed on the previous trip using the config parameters; assuming the marginal disutility of being delayed at the (hypothetical) activity to be equal to beta_performing: " + this.scenario.getConfig().planCalcScore().getPerforming_utils_hr());
				
					if (incompletedPlanWarning == 10) {
						log.warn("Additional warnings of this type are suppressed.");
					}
					incompletedPlanWarning++;
				}
				activityDelayDisutilityOneSec = (1.0 / 3600.) * this.scenario.getConfig().planCalcScore().getPerforming_utils_hr();
			}
			
			// Calculate the agent's trip delay disutility.
			// (Could be done similar to the activity delay disutility. As long as it is computed linearly, the following should be okay.)
			double tripDelayDisutilityOneSec = (1.0 / 3600.) * this.scenario.getConfig().planCalcScore().getModes().get(this.personId2currentTripMode.get(personId)).getMarginalUtilityOfTraveling() * (-1);
			
			// Translate the disutility into monetary units.
			double delayCostPerSec_usingActivityDelayOneSec = (activityDelayDisutilityOneSec + tripDelayDisutilityOneSec) / this.scenario.getConfig().planCalcScore().getMarginalUtilityOfMoney();

			// store the VTTS and mode for analysis purposes
			if (this.personId2VTTSh.containsKey(personId)) {
						
				this.personId2VTTSh.get(personId).add(delayCostPerSec_usingActivityDelayOneSec * 3600);
				this.personId2TripNr2VTTSh.get(personId).put(this.personId2currentTripNr.get(personId), delayCostPerSec_usingActivityDelayOneSec * 3600);
				this.personId2TripNr2Mode.get(personId).put(this.personId2currentTripNr.get(personId), this.personId2currentTripMode.get(personId));
		
			} else {

				List<Double> vTTSh = new ArrayList<>();
				vTTSh.add(delayCostPerSec_usingActivityDelayOneSec * 3600.);
				this.personId2VTTSh.put(personId, vTTSh);

				Map<Integer, Double> tripNr2VTTSh = new HashMap<>();
				tripNr2VTTSh.put(this.personId2currentTripNr.get(personId), delayCostPerSec_usingActivityDelayOneSec * 3600.);
				this.personId2TripNr2VTTSh.put(personId, tripNr2VTTSh);
				
				Map<Integer, String> tripNr2Mode = new HashMap<>();
				tripNr2Mode.put(this.personId2currentTripNr.get(personId), this.personId2currentTripMode.get(personId));
				this.personId2TripNr2Mode.put(personId, tripNr2Mode);
			}
		}
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
       
		if (this.scenario.getConfig().vspExperimental().getVTTSanalysisInterval() > 0) {
        	
			if (event.getIteration() == 0 || event.getIteration() % this.scenario.getConfig().vspExperimental().getVTTSanalysisInterval() == 0.) {
        			this.printVTTS(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_allTrips.csv"));
        			this.printVTTS(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_car.csv"), TransportMode.car);
        			this.printAvgVTTSperPerson(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_avgPerPerson.csv"));
        			
        			this.printVTTSdistribution(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_percentiles.csv"), null, null);
        			this.printVTTSdistribution(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_percentiles_car.csv"), TransportMode.car, null);

        			this.printVTTSdistribution(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_percentiles_car_7-9.csv"), "car", new Tuple<Double, Double>(7.0 * 3600., 9. * 3600.));
        			this.printVTTSdistribution(this.controlerIO.getIterationFilename(event.getIteration(), "vtts_percentiles_car_11-13.csv"), "car", new Tuple<Double, Double>(11.0 * 3600., 13. * 3600.));
        		}
        }

	}
	
	public void printVTTS(String fileName) {
		
		File file = new File(fileName);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("person Id;TripNr;Mode;VTTS [money/hour]");
			bw.newLine();
			
			for (Id<Person> personId : this.personId2TripNr2VTTSh.keySet()){
				for (Integer tripNr : this.personId2TripNr2VTTSh.get(personId).keySet()){
					bw.write(personId + ";" + tripNr + ";" + this.personId2TripNr2Mode.get(personId).get(tripNr) + ";" + this.personId2TripNr2VTTSh.get(personId).get(tripNr));
					bw.newLine();		
				}
			}
			
			bw.close();
			log.info("Output written to " + fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printVTTS(String fileName, String mode) {
		
		File file = new File(fileName);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("person Id;TripNr;Mode;VTTS [money/hour]");
			bw.newLine();
			
			for (Id<Person> personId : this.personId2TripNr2VTTSh.keySet()){
				for (Integer tripNr : this.personId2TripNr2VTTSh.get(personId).keySet()){
					if (this.personId2TripNr2Mode.get(personId).get(tripNr).equals(mode)) {
						bw.write(personId + ";" + tripNr + ";" + this.personId2TripNr2Mode.get(personId).get(tripNr) + ";" + this.personId2TripNr2VTTSh.get(personId).get(tripNr));
						bw.newLine();			
					}
				}
			}
			
			bw.close();
			log.info("Output written to " + fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printAvgVTTSperPerson(String fileName) {
		
		File file = new File(fileName);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("person Id;VTTS [money/hour]");
			bw.newLine();
			
			for (Id<Person> personId : this.personId2VTTSh.keySet()){
				double vttsSum = 0.;
				double counter = 0;
				for (Double vTTS : this.personId2VTTSh.get(personId)){
					vttsSum = vttsSum + vTTS;
					counter++;
				}
				bw.write(personId + ";" + (vttsSum / counter) );
				bw.newLine();	
			}
			
			bw.close();
			log.info("Output written to " + fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<Id<Person>, Map<Integer, Double>> getPersonId2TripNr2VTTSh() {
		return personId2TripNr2VTTSh;
	}

	public void printVTTSdistribution(String fileName, String mode, Tuple<Double, Double> fromToTime_sec) {
		
		List<Double> vttsFiltered = new ArrayList<>();
		
		for (Id<Person> personId : this.personId2TripNr2VTTSh.keySet()){
			for (Integer tripNr : this.personId2TripNr2VTTSh.get(personId).keySet()){
				
				boolean considerTrip = true;
				
				if (mode != null) {
					if (this.personId2TripNr2Mode.get(personId).get(tripNr).equals(mode)) {
						// consider this trip
					} else {
						considerTrip = false;
					}
				}
				
				if (fromToTime_sec != null) {
					if (this.personId2TripNr2DepartureTime.get(personId).get(tripNr) >= fromToTime_sec.getFirst()
							&& this.personId2TripNr2DepartureTime.get(personId).get(tripNr) < fromToTime_sec.getSecond()) {
						// consider this trip
					} else {
						considerTrip = false;
					}
				}
				
				if (considerTrip) {
					vttsFiltered.add(this.personId2TripNr2VTTSh.get(personId).get(tripNr));
				}
				
			}
		}
			
		double[] vttsArray = new double[vttsFiltered.size()];
		
		int counter = 0;
		for (Double vtts : vttsFiltered) {
			vttsArray[counter] = vtts;
			counter++;
		}
				
		File file = new File(fileName);
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));

			bw.write("5% percentile [money/hour] ; " + StatUtils.percentile(vttsArray, 5.0));
			bw.newLine();

			bw.write("25% percentile [money/hour] ; " + StatUtils.percentile(vttsArray, 25.0));
			bw.newLine();

			bw.write("50% percentile (median) [money/hour] ; " + StatUtils.percentile(vttsArray, 50.0));
			bw.newLine();

			bw.write("75% percentile [money/hour] ; " + StatUtils.percentile(vttsArray, 75.0));
			bw.newLine();

			bw.write("95% percentile [money/hour] ; " + StatUtils.percentile(vttsArray, 95.0));
			bw.newLine();

			bw.close();
			log.info("Output written to " + fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}
	
	private boolean isModeToBeSkipped(String legMode) {
		for (String modeToBeSkipped : this.modesToBeSkipped) {
			if (legMode.equals(modeToBeSkipped)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isActivityToBeSkipped(String actType) {
		for (String activityToBeSkipped : this.activitiesToBeSkipped) {
			if (actType.equals(activityToBeSkipped)) {
				return true;
			}
		}
		return false;
	}
}
