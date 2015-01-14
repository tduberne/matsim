/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package playground.agarwalamit.congestionPricing.analysis;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.utils.io.IOUtils;

import playground.agarwalamit.analysis.LoadMyScenarios;
import playground.ikaddoura.internalizationCar.MarginalCongestionEvent;
import playground.ikaddoura.internalizationCar.MarginalCongestionEventHandler;
import playground.ikaddoura.internalizationCar.MarginalCongestionEventsReader;


/**
 * @author amit
 */

public class CompareCongestionEvents  {

	private String eventsFile_v3 = "/Users/amit/Documents/repos/runs-svn/siouxFalls/run203/implV3/ITERS/it.1000/1000.events.xml.gz";
	private String eventsFile_v4 = "/Users/amit/Documents/repos/runs-svn/siouxFalls/run203/implV3/ITERS/it.1000/1000.events_implV4.xml.gz";

	private List<MarginalCongestionEvent>  getCongestionEvents (String eventsFile){

		final List<MarginalCongestionEvent> congestionevents = new ArrayList<MarginalCongestionEvent>();

		EventsManager eventsManager = EventsUtils.createEventsManager();
		MarginalCongestionEventsReader reader = new MarginalCongestionEventsReader(eventsManager);

		eventsManager.addHandler(new MarginalCongestionEventHandler () {
			@Override
			public void reset(int iteration) {
				congestionevents.clear();
			}

			@Override
			public void handleEvent(MarginalCongestionEvent event) {
				congestionevents.add(event);
			}
		});
		reader.parse(eventsFile);

		return congestionevents;
	}

	public static void main(String[] args) {
		//		new CompareCongestionEvents().run("/Users/amit/Documents/repos/runs-svn/siouxFalls/run203/analysis/");
		new CompareCongestionEvents().compareTwoImplForSameRun();
	}

	List<String> wronglyChargedEventsList ;
	
	private void compareTwoImplForSameRun(){

		List<String> eventsImpl3_list = eventList2StringList(getCongestionEvents(eventsFile_v3));
		
		String runDir = "/Users/amit/Documents/repos/runs-svn/siouxFalls/run203/implV3/";
		Scenario scenario = LoadMyScenarios.loadScenarioFromOutputDir(runDir);
		
		CrossMarginalCongestionEventsWriter w =	new CrossMarginalCongestionEventsWriter(scenario);
		w.readAndWrite("implV4");
		
		List<String> eventsImpl4_list = eventList2StringList(w.getCongestionEventsList());

		System.out.println("V3 list size"+eventsImpl3_list.size());
		System.out.println("V4 list size"+eventsImpl4_list.size());

		Set<String> eventsImpl3 = new LinkedHashSet<String>();
		eventsImpl3.addAll(eventsImpl3_list);

		Set<String> eventsImpl4 = new LinkedHashSet<String>();
		eventsImpl4.addAll(eventsImpl4_list);

		System.out.println("V3 set size"+eventsImpl3.size());
		System.out.println("V4 set size"+eventsImpl4.size());

		wronglyChargedEventsList = new ArrayList<String>();
		
		for(String e3 : eventsImpl3){
			if(eventsImpl4.contains(e3)){
				eventsImpl4.remove(e3);
			} else {
				wronglyChargedEventsList.add(e3);
			}
		}
		
		
		
		System.out.println("Wrong events are "+wronglyChargedEventsList.size());
		System.out.println("Uncharged events are "+eventsImpl4.size());
		checkWrongEventsList();
	}
	
	private void checkWrongEventsList(){
		double wrongDelays = 0;
		Set<String> causingPersons = new HashSet<String>();
		Set<String> affectedPersons = new HashSet<String>();
		Set<Double> delays = new HashSet<Double>();
		
		for(String e:wronglyChargedEventsList){
			
			String CausingPerson = e.split(" ")[4];
			String affectedPerson = e.split(" ")[5];
			causingPersons.add(CausingPerson);
			affectedPersons.add(affectedPerson);
			
			String delay = (e.split(" ")[6]);
			String delayNumber = delay.substring(7,delay.length()-1);
			
			double d = Double.valueOf(delayNumber);
			delays.add(d);
			wrongDelays +=d;
		}
		
		System.out.println("Wrongly charged delays in hr is "+wrongDelays/3600);
		System.out.println("In wrongly events affected persons are "+ affectedPersons.size());
		System.out.println("In wrongly events causing persons are "+ causingPersons.size());
	}

	private List<String> eventList2StringList(List<MarginalCongestionEvent> l){
		List<String> outList = new ArrayList<String>();

		for(MarginalCongestionEvent e :l){
			outList.add(e.toString());	
		}
		return outList;
	}

	private void run(String outputFolder){

		List<MarginalCongestionEvent> eventsImpl3 = getCongestionEvents(eventsFile_v3);
		List<MarginalCongestionEvent> eventsImpl4 = getCongestionEvents(eventsFile_v4);

		BufferedWriter writer = IOUtils.getBufferedWriter(outputFolder+"/congestionEventsInfo.txt");
		try {
			writer.write("Particulars \t implV3 \t implV4 \n");

			writer.write("number of congestion events \t "+eventsImpl3.size()+"\t"+eventsImpl4.size()+"\n");

			writer.write("number of affected persons \t "+getAffectedPersons(eventsImpl3).size()+"\t"+getAffectedPersons(eventsImpl4).size()+"\n");
			writer.write("number of causing persons \t "+getCausingPersons(eventsImpl3).size()+"\t"+getCausingPersons(eventsImpl4).size()+"\n");
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException("Data is not written in file. Reason: "
					+ e);
		}
	}

	private Set<Id<Person>> getAffectedPersons(List<MarginalCongestionEvent> mce){
		Set<Id<Person>> affectedPersons = new HashSet<Id<Person>>();
		for(MarginalCongestionEvent e : mce) {
			affectedPersons.add(e.getAffectedAgentId());
		}
		return affectedPersons;
	}

	private Set<Id<Person>> getCausingPersons(List<MarginalCongestionEvent> mce){
		Set<Id<Person>> causingPersons = new HashSet<Id<Person>>();
		for(MarginalCongestionEvent e : mce) {
			causingPersons.add(e.getCausingAgentId());
		}
		return causingPersons;
	}

}
