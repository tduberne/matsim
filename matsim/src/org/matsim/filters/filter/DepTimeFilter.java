/* *********************************************************************** *
 * project: org.matsim.*
 * DepTimeFilter.java
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

package org.matsim.filters.filter;

import java.util.List;

import org.matsim.gbl.Gbl;
import org.matsim.plans.Leg;
import org.matsim.plans.Person;
import org.matsim.plans.Plan;

public class DepTimeFilter extends PersonFilterA {
	private boolean result=false;
	
	private static double criterionMAX = Gbl.parseTime("09:00");

	private static double criterionMIN = Gbl.parseTime("06:40");

	@Override
	public boolean judge(Person person) {
		for (Plan plan : person.getPlans()) {
			List actsLegs = plan.getActsLegs();
			for (int i = 1; i < actsLegs.size(); i += 2) {
				Leg leg = (Leg) actsLegs.get(i);
				result=((criterionMIN < leg.getDepTime())&& (leg.getDepTime() < criterionMAX));
				if (result)return result;
			}
		}
		return result;
	}
}
