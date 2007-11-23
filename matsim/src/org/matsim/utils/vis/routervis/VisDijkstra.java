/* *********************************************************************** *
 * project: org.matsim.*
 * VisDijkstra.java
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

package org.matsim.utils.vis.routervis;




import java.io.IOException;
import java.util.Iterator;

import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.matsim.network.Node;
import org.matsim.plans.Route;
import org.matsim.router.Dijkstra;
import org.matsim.router.util.LeastCostPathCalculator;
import org.matsim.router.util.PriorityQueueBucket;
import org.matsim.router.util.TravelCostI;
import org.matsim.router.util.TravelTimeI;

/**
 * @author laemmel
 *
 */
public class VisDijkstra extends Dijkstra implements LeastCostPathCalculator, VisLeastCostPathCalculator {

	private RouterNetStateWriter writer;

	private final int DUMP_INTERVAL = 1;

	private int explCounter;
	private int dumpCounter;

	public VisDijkstra(NetworkLayer network, TravelCostI costFunction, TravelTimeI timeFunction, RouterNetStateWriter writer) {
		super(network, costFunction, timeFunction);
		this.writer = writer;
		this.explCounter = 0;
		this.dumpCounter = 0;
	}
	/**
	 * Calculates the cheapest route from Node 'fromNode' to Node 'toNode' at
	 * starting time 'startTime'.
	 *
	 * @param fromNode
	 *            The Node at which the route should start.
	 * @param toNode
	 *            The Node at which the route should end.
	 * @param startTime
	 *            The time at which the route should start.
	 * @see org.matsim.router.util.LeastCostPathCalculator#calcLeastCostPath(org.matsim.network.Node,
	 *      org.matsim.network.Node, double)
	 */
	@Override
	public Route calcLeastCostPath(final Node fromNode, final Node toNode, final double startTime) {
		doSnapshot();
		Route route = super.calcLeastCostPath(fromNode, toNode, startTime);

		this.writer.reset();
		Link [] links = route.getLinkRoute();
		for (int i =0; i < links.length; i++){
			this.writer.setLinkColor(links[i].getId(), 0.1);
			doSnapshot();
		}


		return route;
	}

	/**
	 * Adds some parameters to the given Node then adds it to the set of pending
	 * nodes.
	 *
	 * @param l
	 *            The link from which we came to this Node.
	 * @param n
	 *            The Node to add to the pending nodes.
	 * @param pendingNodes
	 *            The set of pending nodes.
	 * @param currTime
	 *            The time at which we started to traverse l.
	 * @param currCost
	 *            The cost at the time we started to traverse l.
	 * @param outNode
	 *            The Node from which we came to n.
	 * @param toNode
	 *            The target Node of the route.
	 */
	@Override
	protected boolean addToPendingNodes(final Link l, final Node n,
			final PriorityQueueBucket<Node> pendingNodes, final double currTime,
			final double currCost, final Node outNode, final Node toNode) {
		boolean succ = super.addToPendingNodes(l, n, pendingNodes, currTime, currCost, outNode, toNode);

		if (succ){
			//test if the node was revisited - if so the former shortest
			//path has to be canceled...
			Iterator it = l.getToNode().getInLinks().iterator();
			while (it.hasNext()){
				Link link = (Link) it.next();
				if (this.writer.getLinkDisplValue(link,0) == 0.25){
					this.writer.setLinkColor(link.getId(), 0.9);
				}
			}
			this.writer.setLinkColor(l.getId(), 0.25);
		} else {
			this.writer.setLinkColor(l.getId(), 0.9);
		}

		this.explCounter++;

		if (this.explCounter >= this.DUMP_INTERVAL) {
			this.explCounter = 0;
			doSnapshot();
		}

		return succ;
	}

	private void doSnapshot() {
		try {
			this.writer.dump(this.dumpCounter++);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
