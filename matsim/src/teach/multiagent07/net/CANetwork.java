/* *********************************************************************** *
 * project: org.matsim.*
 * CANetwork.java
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

package teach.multiagent07.net;

import java.util.Iterator;

import org.matsim.basic.v01.BasicNet;

public class CANetwork extends BasicNet{

	public CANode newNode(String label) {
		CANode node = new CANode(label);
		return node;
	}

	public CALink newLink(String label) {
		CALink link = new CALink(label);
		return link;
	}

	public void connect() {
		// fill up outlinks/inlinks information in nodes
		Iterator i = links.iterator();
		while (i.hasNext()) {
			CALink link = (CALink)i.next();
			link.getToNode().addInLink(link);
			link.getFromNode().addOutLink(link);
		}
	}

	public void randomfill(double d) {
		for (Object l : links) ((CALink)l).randomFill(d);
	}
	
	public void build() {
		for (Object l : links) ((CALink)l).build();
		//for (Object n : nodes) ((CANode)n).build();
	}

	public void move(int time) {
		for (Object l : links) ((CALink)l).move(time);
		for (Object n : nodes) ((CANode)n).move(time);
	}

}
