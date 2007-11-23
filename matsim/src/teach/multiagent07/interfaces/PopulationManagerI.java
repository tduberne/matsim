/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationManagerI.java
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

package teach.multiagent07.interfaces;

import teach.multiagent07.population.Person;


public interface PopulationManagerI {

	public void setHandler (PersonHandlerI handler);
	
	public void setReplanningFraction (double fraction);
	
	public void runHandler ();
	
	public void addPerson(Person person)throws Exception;
}
