/* *********************************************************************** *
 * project: org.matsim.*
 * PreProcessEuclidean.java
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

package org.matsim.router.util;

import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;

/**
 * Pre-processes a given network, gathering information which can be used by a
 * AStarEuclidean when computing least-cost paths between a start and an end
 * node. Specifically, computes the minimal travel cost per length unit over all
 * links, which is used by AStarEuclidean's heuristic function during routing.
 * 
 * @author lnicolas
 */
public class PreProcessEuclidean extends PreProcessDijkstra {

	private static final Logger log = Logger.getLogger(PreProcessEuclidean.class);

	// Must be initialized to MAX_VALUE, otherwise updateMaxFreeSpeed(...) does
	// not change minTravelCostPerLength
	private double minTravelCostPerLength = Double.POSITIVE_INFINITY;

	protected TravelMinCostI costFunction;

	/**
	 * @param costFunction
	 *          Returns the minimal possible cost for each link.
	 */
	public PreProcessEuclidean(final TravelMinCostI costFunction) {
		this.costFunction = costFunction;
	}

	@Override
	public void run(final NetworkLayer network) {
		super.run(network);

		if (checkLinkLengths(network) == false) {
			log
					.warn("PreProcessAStar.run(...) There are links with stored length smaller than their euclidean distance in this network. Thus, NetworkAStar cannot guarantee to calculate the least-cost paths between two nodes!");
		}

		updateMinTravelCostPerLength(network);
	}

	void updateMinTravelCostPerLength(final NetworkLayer network) {
		for (Object link : network.getLinks()) {
			double minCost = this.costFunction.getLinkMinimumTravelCost((Link) link)
					/ ((Link) link).getLength();
			if (getMinTravelCostPerLength() > minCost) {
				setMinTravelCostPerLength(minCost);
			}
		}
	}

	private boolean checkLinkLengths(final NetworkLayer network) {
		Set links = network.getLinks();
		for (Object obj : links) {
			Link l = (Link) obj;
			double linkLength = l.getLength();
			double eucDist = l.getFromNode().getCoord().calcDistance(
					l.getToNode().getCoord());
			if (linkLength < eucDist) {
				if (log.isDebugEnabled()) {
					log.debug("link " + l.getId() + " has length " + linkLength + " which is smaller than the euclidean distance " + eucDist);
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * @param minTravelCostPerLength
	 *          the minTravelCostPerLength to set
	 */
	void setMinTravelCostPerLength(final double maxFreeSpeed) {
		this.minTravelCostPerLength = maxFreeSpeed;
	}

	/**
	 * @return the minimal travel cost per length unit over all links in the
	 *         network.
	 */
	public double getMinTravelCostPerLength() {
		return this.minTravelCostPerLength;
	}

	/**
	 * @return The cost function that was used to calculate the minimal travel
	 *         cost per length unit over all links in the network.
	 */
	public TravelCostI getCostFunction() {
		return this.costFunction;
	}
}
