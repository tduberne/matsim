/* *********************************************************************** *
 * project: org.matsim.*
 * EventFilterPersonSpecific.java
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

import java.util.HashSet;
import java.util.Set;

import org.matsim.basic.v01.Id;
import org.matsim.events.BasicEvent;
import org.matsim.utils.identifiers.IdI;

/**
 * A EventFilterPersonSpecific lets the events, whose agentId belong to the
 * set PERSONIDS, pass. In order to save the resource of computer, i suggest
 * that the EventFilterPersonSpecific should be the first PersonFilterA after the
 * PersonFilterAlgorithm.
 *
 * @author ychen
 */
public class EventFilterPersonSpecific extends EventFilterA implements EventFilterI {
	/*-----------------------MEMBER VARIABLE-----------------*/
	/**
	 * Every EventFilterPersonSpecific must have a Set of Person-IDs
	 */
	private Set<IdI> personIds = new HashSet<IdI>();

	/*----------------------CONSTRUCTOR----------------------*/
	/**
	 * @param personIDs -
	 *            A set of Person- IDs, which is any Integer object
	 */
	public EventFilterPersonSpecific(Set<IdI> personIDs) {
		System.out.println("importing " + personIDs.size() + " Person- IDs.");
		setPersonIDs(personIDs);
	}

	/*------------------------SETTER---------------------------*/
	/**
	 * Sets a Set of Person- IDs in this class
	 *
	 * @param personIDs -
	 *            a Set of Person-IDs
	 */
	public void setPersonIDs(Set<IdI> personIDs) {
		this.personIds = personIDs;
	}

	/*-------------------------OVERRIDING METHOD----------------------*/
	/**
	 * Returns true if this set contains PERSONIDS contains an
	 * Integer-object,that represents the specified int value of the agent-ID of
	 * the event
	 *
	 * @param event -
	 *            an event, whose presence in this set is to be tested.
	 * @return true if this Set contains the Integer object, which corresponds
	 *         the agent- ID.
	 */
	@Override
	public boolean judge(BasicEvent event) {
		return this.personIds.contains(new Id(event.agentId));
	}
}
