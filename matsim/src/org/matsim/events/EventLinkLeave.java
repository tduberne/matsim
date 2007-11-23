/* *********************************************************************** *
 * project: org.matsim.*
 * EventLinkLeave.java
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

package org.matsim.events;

import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.matsim.plans.Person;
import org.matsim.plans.Plans;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class EventLinkLeave extends LinkEvent {

	private static final long serialVersionUID = -6876760768039952024L;

	public EventLinkLeave(double time, String agentId, String linkId, Person agent, Link lnk) {
		super(time, agentId, linkId, agent, lnk);
	}

	public EventLinkLeave(double time, String agentId, String linkId) {
		super(time, agentId, linkId);
	}

	@Override
	public Attributes getAttributes() {
		AttributesImpl impl = getAttributesImpl();
		//impl.addAttribute("","","Flag", "", Integer.toString(2));
		impl.addAttribute("","","type", "", "left link");
		return impl;
	}

	@Override
	public String toString() {
		return asString() + "2\tleft link";
	}

	@Override
	public void rebuild(Plans population, NetworkLayer network) {
		rebuildLinkData(population, network);
	}

}
