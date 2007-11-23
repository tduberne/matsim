/* *********************************************************************** *
 * project: org.matsim.*
 * Subsequent.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.matsim.interfaces.networks.basicNet.BasicLinkSetI;
import org.matsim.interfaces.networks.basicNet.BasicNodeSetI;
import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.matsim.network.Node;
import org.matsim.world.Coord;
import org.matsim.writer.MatsimXmlWriter;

/**
 * analyses MATSim networkfile
 * 
 * @author ychen
 * 
 */
public class Subsequent extends MatsimXmlWriter {

	private BasicLinkSetI links;

	/**
	 * criterion to judge Capacity
	 */
	private static double BETA = 0.0;

	/**
	 * the important intermediate result...
	 * 
	 * @param String
	 *            (args0) - ssLinkId: the "default next" linkId of the "current"
	 *            Link
	 * @param String
	 *            (args1) - linkId: the "current" Link
	 */
	private TreeMap<String, String> ssLinks = new TreeMap<String, String>();

	/**
	 * Constructor transfers the links of network to local links
	 * 
	 * @param network
	 */
	public Subsequent(NetworkLayer network) {
		links = network.getLinks();
	}

	/**
	 * @return Returns the ssLinks.
	 */
	public TreeMap<String, String> getSsLinks() {
		return ssLinks;
	}

	/**
	 * calculates the "default next" linkId of the current link with respect to
	 * geometry und Capacity (depending on BETA) and writes the result in
	 * ssLinks.
	 */
	public void compute() {
		/**
		 * @param String -
		 *            outNodeId;
		 * @param Double -
		 *            |deltaTheta of outLink-link|
		 */
		Map<String, Double> absDeltaThetas = new TreeMap<String, Double>();
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			Link l = (Link) iter.next();
			Node from = l.getFromNode();
			Node to = l.getToNode();
			BasicNodeSetI outNodes = to.getOutNodes();
			absDeltaThetas.clear();
			if (outNodes.size() > 1) {

				for (Iterator it = outNodes.iterator(); it.hasNext();) {
					Node out = (Node) it.next();
					Coord cFrom = from.getCoord();
					Coord cTo = to.getCoord();
					double xTo = cTo.getX();
					double yTo = cTo.getY();
					Coord cOut = out.getCoord();
					double deltaTheta = Math.atan2(cOut.getY() - yTo, cOut
							.getX()
							- xTo)
							- Math
									.atan2(yTo - cFrom.getY(), xTo
											- cFrom.getX());
					while (deltaTheta < -Math.PI)
						deltaTheta += 2.0 * Math.PI;
					while (deltaTheta > Math.PI)
						deltaTheta -= 2.0 * Math.PI;
					absDeltaThetas.put(out.getId().toString(), Math
							.abs(deltaTheta));
				}
				ssLinks.put(computeSubsequentLink(l, absDeltaThetas), l.getId()
						.toString());

			} else if (outNodes.size() == 1) {
				// Node[] outNodesArray = (Node[]) outNodes.toArray();----bad
				// code. throws ClassCastException
				// findOutLink(((Node)outNodesArray[0]).getId().toString(), l);
				ssLinks.put(findOutLink(
						((Node) outNodes.iterator().next()).getId().toString(),
						l).getId().toString(), l.getId().toString());
			}
		}
	}

	/**
	 * Calculates the "default next" linkId intermediately
	 * 
	 * @param thetas
	 * @return
	 */
	public String computeSubsequentLink(Link l, Map<String, Double> thetas) {
		String outLinkId = "";
		/**
		 * @param String -
		 *            id of outNode, whose |deltaTheta| is the smallest one.
		 */
		List<String> minThetaOutNodeIds = new ArrayList<String>();
		while (outLinkId.equals("")) {
			minThetaOutNodeIds.clear();
			double absMin = Collections.min(thetas.values());
			for (Iterator kVPairs = thetas.entrySet().iterator(); kVPairs
					.hasNext();) {
				Map.Entry entry = (Map.Entry) kVPairs.next();
				if (absMin == ((Double) entry.getValue()).doubleValue())
					minThetaOutNodeIds.add((String) entry.getKey());
			}
			if (minThetaOutNodeIds.size() == 1) {
				String outNodeId = minThetaOutNodeIds.get(0);
				Link outLink = findOutLink(outNodeId, l);
				if (outLink.getCapacity() >= BETA * l.getCapacity())
					outLinkId = outLink.getId().toString();
				else {
					thetas.remove(outNodeId);
				}
			} else if (minThetaOutNodeIds.size() == 2) {
				String outNodeIdA = minThetaOutNodeIds.get(0);
				String outNodeIdB = minThetaOutNodeIds.get(1);
				Link outLinkA = findOutLink(outNodeIdA, l);
				Link outLinkB = findOutLink(outNodeIdB, l);
				double capA = outLinkA.getCapacity();
				double capB = outLinkB.getCapacity();
				String outLinkAId = outLinkA.getId().toString();
				String outLinkBId = outLinkB.getId().toString();
				if (l.getCapacity() > Math.min(capA, capB)) {
					outLinkId = (capA == Math.max(capA, capB)) ? outLinkAId
							: outLinkBId;
				} else {
					outLinkId = (Math.random() < 0.5) ? outLinkAId : outLinkBId;
				}
			}
		}
		return outLinkId;
	}

	/**
	 * gets the link, who has suitable fromNode(Id) and toNode(Id)
	 * 
	 * @param outNodeId -
	 *            one of outNodeIds of a toNode
	 * @param l -
	 *            the link in process
	 * @return the link, whose toNodeId="outNodeId" and fromNodeId = l.toNode-Id
	 */
	public Link findOutLink(String outNodeId, Link l) {
		Link outL = null;
		BasicLinkSetI outLinks = l.getToNode().getOutLinks();
		for (Iterator linkIt = outLinks.iterator(); linkIt.hasNext();) {
			outL = (Link) linkIt.next();
			if (outL.getToNode().getId().toString().equals(outNodeId))
				return outL;
		}
		if (outL.equals(null))
			System.err
					.println("[WARNING]The link you are looking for doesn'/t exist in the network!");
		return outL;
	}

	/**
	 * writes linkId and the "default next" linkId into a .xml-file
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void writeFile(final String filename) throws IOException {
		System.out.println("@write beginning");
		openFile(filename);
		writeXmlHead();
		write();
		close();
	}

	/**
	 * writes contents (ssLinkId-linkId-pair) into the writer.
	 * 
	 * @throws IOException
	 */
	private void write() throws IOException {
		writer.write("<subsequent>\n");
		// links
		writer.write("\t<links>\n");
		for (Iterator it = ssLinks.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> next = (Entry<String, String>) it.next();
			writer.write("\t\t<link id=\"" + next.getValue()
					+ "\" subsequentLinkId=\"" + next.getKey() + "\" />\n");
		}
		writer.write("\t</links>\n" + "</subsequent>");
		System.out.println("@write done.");
	}
}
