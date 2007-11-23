/* *********************************************************************** *
 * project: org.matsim.*
 * PlanMutateTimeAllocation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.plans.algorithms;

import org.matsim.gbl.Gbl;
import org.matsim.plans.Act;
import org.matsim.plans.Leg;
import org.matsim.plans.Plan;

public class PlanMutateTimeAllocation implements PlanAlgorithmI {

	private final int mutationRange;

	public PlanMutateTimeAllocation(int mutationRange) {
		this.mutationRange = mutationRange;
	}
	
	public void run(Plan plan) {
		mutatePlan(plan);
	}

	private void mutatePlan(Plan plan) {

		int max = plan.getActsLegs().size();
		
		int now = 0;
		
		// apply mutation to all activities except the last home activity
		for (int i = 0; i < max; i++ ) {

			if (i % 2 == 0) {
				Act act = (Act)(plan.getActsLegs().get(i));
				// invalidate previous activity times because durations will change
				act.setStartTime(Gbl.UNDEFINED_TIME);

				// handle first activity
				if (i == 0) {
					// set start to midnight
					act.setStartTime(now);
					// mutate the end time of the first activity
					act.setEndTime(mutateTime(act.getEndTime()));
					// calculate resulting duration
					act.setDur(act.getEndTime() - act.getStartTime());
					// move now pointer
					now += act.getEndTime();
					
				// handle middle activities	
				} else if ((i > 0) && (i < (max - 1))) {
					
					// assume that there will be no delay between arrival time and activity start time
					act.setStartTime(now);
					// mutate the durations of all 'middle' activities
					act.setDur(mutateTime(act.getDur()));
					now += act.getDur();
					// set end time accordingly
					act.setEndTime(now);
					
				// handle last activity
				} else if (i == (max - 1)) {
					
					// assume that there will be no delay between arrival time and activity start time
					act.setStartTime(now);
					// invalidate duration and end time because the plan will be interpreted 24 hour wrap-around
					act.setDur(Gbl.UNDEFINED_TIME);
					act.setEndTime(Gbl.UNDEFINED_TIME);
					
				}
				
			} else {
				
				Leg leg = (Leg)(plan.getActsLegs().get(i));
				
				// assume that there will be no delay between end time of previous activity and departure time
				leg.setDepTime(now);
				// let duration untouched. if defined add it to now
				if (leg.getTravTime() != Gbl.UNDEFINED_TIME) {
					now += leg.getTravTime();
				}
				// set planned arrival time accordingly
				leg.setArrTime(now);
				
			}
		}
	}

	private double mutateTime(final double time) {
		double t = time;
		if (t != Gbl.UNDEFINED_TIME) {		
			t = t + (int)((Gbl.random.nextDouble() * 2.0 - 1.0) * mutationRange);
			if (t < 0) t = 0;
			if (t > 24*3600) t = 24*3600;
		} else {
			t = Gbl.random.nextInt(24*3600);
		}
		return t;
	}

}
