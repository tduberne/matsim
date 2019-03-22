/* *********************************************************************** *
 * project: org.matsim.*
 * Plan.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2009 by the members listed in the COPYING,  *
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

package org.matsim.api.core.v01.population;

import java.util.List;

import org.matsim.api.core.v01.Customizable;
import org.matsim.core.api.internal.MatsimPopulationObject;
import org.matsim.utils.objectattributes.attributable.Attributable;

/**
 * A plan contains the intention of an agent.  In consequence, all information is <i>expected</i>.  For example,
 * travel times and travel distances in routes are expected.  Even something like mode could be expected, if the
 * plan is fed into a mobsim that is within-day replanning capable at the mode level.
 * <p></p>
 * The only thing which is not "expected" in the same sense is the score.
 *
 */
public interface Plan extends MatsimPopulationObject, Customizable, BasicPlan, Attributable {
	
	List<PlanElement> getPlanElements();

	void addLeg(final Leg leg);

	void addActivity(final Activity act);

	default void addWaypoint(final Waypoint waypoint) {
		// default implementation for classes implemented before waypoints were introduced.
		// classes that return an immutable collection will fail and have to implement this themselves.
		// classes that return a mutable copy will ignore the action... Hopefully there is none that does that.
		// td Mar 17
		getPlanElements().add(waypoint);
	}

	String getType();

	void setType(final String type);

	Person getPerson();

	/**
	 * Sets the reference to the person.
	 * This is done automatically if using Person.addPlan(). Make
	 * sure that the bidirectional reference is set correctly if
	 * you are using this method!.
	 */
	void setPerson(Person person);

}
