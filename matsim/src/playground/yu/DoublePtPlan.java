/* *********************************************************************** *
 * project: org.matsim.*
 * DoublePtPlan.java
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

package playground.yu;

import org.matsim.plans.Person;
import org.matsim.plans.Plans;

/**
 * increases the amount of Agents in a new MATSim plansfile, by copying the old
 * agents in the file and change only the Ids.
 * 
 * @author ychen
 * 
 */
public class DoublePtPlan extends NewAgentPtPlan {
	/**
	 * Construcktor
	 * 
	 * @param plans -
	 *            a Plans Object, which derives from MATSim plansfile
	 */
	public DoublePtPlan(Plans plans) {
		super(plans);
		// TODO Auto-generated constructor stub
	}

	/**
	 * writes an old Person and also new Persons in new plansfile.
	 * 
	 * @see org.matsim.plans.algorithms.PersonAlgorithm#run(org.matsim.plans.Person)
	 */
	@Override
	public void run(Person person) {
		pw.writePerson(person);
		// produce new Person with bigger Id
		for (int i = 1; i <= 9; i++) {
			int newPersonId = Integer.parseInt(person.getId().toString()) + 100;
			person.setId(Integer.toString(newPersonId));
			pw.writePerson(person);
		}
	}
}
