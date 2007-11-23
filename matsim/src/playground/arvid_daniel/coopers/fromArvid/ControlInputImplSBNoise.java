/* *********************************************************************** *
 * project: org.matsim.*
 * ControlInputImplSBNoise.java
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

package playground.arvid_daniel.coopers.fromArvid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.matsim.events.EventActivityEnd;
import org.matsim.events.EventActivityStart;
import org.matsim.events.EventAgentNoRoute;
import org.matsim.events.EventAgentStuck;
import org.matsim.events.EventAgentWait2Link;
import org.matsim.events.EventLinkEnter;
import org.matsim.events.EventLinkLeave;
import org.matsim.events.handler.EventHandlerActivityEndI;
import org.matsim.events.handler.EventHandlerActivityStartI;
import org.matsim.events.handler.EventHandlerAgentArrivalI;
import org.matsim.events.handler.EventHandlerAgentDepartureI;
import org.matsim.events.handler.EventHandlerAgentNoRouteI;
import org.matsim.events.handler.EventHandlerAgentStuckI;
import org.matsim.events.handler.EventHandlerAgentWait2LinkI;
import org.matsim.events.handler.EventHandlerLinkEnterI;
import org.matsim.events.handler.EventHandlerLinkLeaveI;
import org.matsim.interfaces.networks.basicNet.BasicLinkSetI;
import org.matsim.mobsim.SimulationTimer;
import org.matsim.network.Link;
import org.matsim.network.Node;
import org.matsim.plans.Route;
import org.matsim.withinday.trafficmanagement.AbstractControlInputImpl;
import org.matsim.withinday.trafficmanagement.Accident;
import org.matsim.withinday.trafficmanagement.ControlInput;

/*
 * FIXME [kn] Because this class was build to replace NashWriter, it inherits a serious flaw:
 * This class takes args of type Route in ctor, and returns arguments of
 * type route at getRoute, but these routes are of different type (one with FakeLink, the other
 * with behavioral links).
 */

/*
 * Does the same as ControlInputImplSB.java, but also:
 takes in- and outflows from other roads into the travel time calculations.
 The additional flows are measured and are assumed to be constant during the
 simulation time for the prediction. The additional agents are then included in the 
 travel time calculation
*/


//TODO: ADD AGENTS ONLY FROM LINKS BEFORE THE BOTTLENECK
//clean up, move some variables to Abstract, for example linkRoute...


public class ControlInputImplSBNoise extends AbstractControlInputImpl implements EventHandlerLinkLeaveI,
		EventHandlerLinkEnterI, EventHandlerAgentWait2LinkI,
		EventHandlerAgentStuckI, EventHandlerAgentNoRouteI,
		EventHandlerAgentDepartureI, EventHandlerAgentArrivalI,
		EventHandlerActivityStartI, EventHandlerActivityEndI, ControlInput {
	
	private List<Link> inLinksMainRoute = new ArrayList<Link>();

	private Map<String, Double> inFlowDistancesMainRoute = new HashMap<String, Double>();

	private	Map<String, Double> inFlowsMainRoute = new HashMap<String, Double>();
	
	private Map<String, Integer> numbersPassedOnInAndOutLinks = new HashMap<String, Integer>();
	
	private List<Link> outLinksMainRoute = new ArrayList<Link>();

	private Map<String, Double> outFlowDistancesMainRoute = new HashMap<String, Double>();

	private	Map<String, Double> outFlowsMainRoute = new HashMap<String, Double>();
	
	private List<Link> inLinksAlternativeRoute = new ArrayList<Link>();

	private Map<String, Double> inFlowDistancesAlternativeRoute = new HashMap<String, Double>();

	private	Map<String, Double> inFlowsAlternativeRoute = new HashMap<String, Double>();
	
	private List<Link> outLinksAlternativeRoute = new ArrayList<Link>();

	private Map<String, Double> outFlowDistancesAlternativeRoute = new HashMap<String, Double>();
	
	private	Map<String, Double> outFlowsAlternativeRoute = new HashMap<String, Double>();
	
	private double predTTMainRoute;
	
	private double predTTAlternativeRoute;
	
	private ControlInputWriter writer;
	
	private List<Accident> accidents;
	
	private Link bottleNeckLinkMainRoute;
	
	private Link bottleNeckLinkAlternativeRoute;
	
	private double ttFreeSpeedAfterBottleNeckMainRoute;
	
	private double ttFreeSpeedAfterBottleNeckAlternativeRoute;
	
	private int additionalAgentsMainRoute = 0;
	
	private int additionalAgentsAlternativeRoute = 0;
	
	private Link [] routeLinksMainRoute;
	
	private Link [] routeLinksAlternativeRoute;
	
	private ArrayList<Node> routeNodesMainRoute = new ArrayList<Node>();
	
	private ArrayList<Node> routeNodesAlternativeRoute = new ArrayList<Node>();
	
	private List<Link> routeLinksListMainRoute = new ArrayList<Link>();
	
	private List<Link> routeLinksListAlternativeRoute = new ArrayList<Link>();

	public ControlInputImplSBNoise() {
		super();
		this.writer = new ControlInputWriter();
	}

	
	@Override
	public void init() {
		super.init();
				
		//init the mainRoute
		routeLinksMainRoute = mainRoute.getLinkRoute();	
		routeLinksListMainRoute = Arrays.asList(routeLinksMainRoute);
		routeNodesMainRoute = mainRoute.getRoute();
		routeNodesMainRoute.remove(0); //remove first Node since its inLinks shouldn't be included
		routeNodesMainRoute.remove(routeNodesMainRoute.size()-1);
		//all distances to in- and outLinks are stored in Maps <link id, distance>
		for(Node n : routeNodesMainRoute){	
			BasicLinkSetI inLinks = n.getInLinks();
			Iterator<Link> itIn = inLinks.iterator();
			while(itIn.hasNext()){
				Link inLink = (Link)itIn.next();
				if(!routeLinksListMainRoute.contains(inLink)){
					double d = getDistanceFromFirstNode(inLink.getToNode(), routeLinksMainRoute);
					inLinksMainRoute.add(inLink);
					inFlowDistancesMainRoute.put(inLink.getId().toString(), d);
					inFlowsMainRoute.put(inLink.getId().toString(), 0.0);
					numbersPassedOnInAndOutLinks.put(inLink.getId().toString(), 0);
				}
			}
			BasicLinkSetI outLinks = n.getOutLinks();
			Iterator<Link> itOut = outLinks.iterator();
			while(itOut.hasNext()){
				Link outLink = (Link)itOut.next();
				if(!routeLinksListMainRoute.contains(outLink)){ 
					double d = getDistanceFromFirstNode(outLink.getFromNode(), routeLinksMainRoute);
					outLinksMainRoute.add(outLink);
					outFlowDistancesMainRoute.put(outLink.getId().toString(), d);
					outFlowsMainRoute.put(outLink.getId().toString(), 0.0);
					numbersPassedOnInAndOutLinks.put(outLink.getId().toString(), 0);
				}
			}
		}
		
		//init the alternativeRoute
		routeLinksAlternativeRoute = alternativeRoute.getLinkRoute();	
		routeNodesAlternativeRoute = alternativeRoute.getRoute();
		routeNodesAlternativeRoute.remove(0); //first node's inLinks shouldn't be included in the model
		routeNodesAlternativeRoute.remove(routeNodesAlternativeRoute.size()-1);
		routeLinksListAlternativeRoute = Arrays.asList(routeLinksAlternativeRoute);
		//all distances to in- and outlinks are stored in Maps <link id, distance>
		for(Node n : routeNodesAlternativeRoute){	
			BasicLinkSetI inLinks = n.getInLinks();
			Iterator itIn = inLinks.iterator();
			while(itIn.hasNext()){
				Link inLink = (Link)itIn.next();
				if(!routeLinksListAlternativeRoute.contains(inLink)){
					double d = getDistanceFromFirstNode(inLink.getToNode(), routeLinksAlternativeRoute);
					inLinksAlternativeRoute.add(inLink);
					inFlowDistancesAlternativeRoute.put(inLink.getId().toString(), d);
					inFlowsAlternativeRoute.put(inLink.getId().toString(), 0.0);
					numbersPassedOnInAndOutLinks.put(inLink.getId().toString(), 0);
				}
				else{
					System.out.println("No additional inLinks");
				}
			}
			BasicLinkSetI outLinks = n.getOutLinks();
			Iterator itOut = outLinks.iterator();
			while(itOut.hasNext()){
				Link outLink = (Link)itOut.next();
				if(!routeLinksListAlternativeRoute.contains(outLink)){
					double d = getDistanceFromFirstNode(outLink.getFromNode(), routeLinksAlternativeRoute);
					outLinksAlternativeRoute.add(outLink);
					outFlowDistancesAlternativeRoute.put(outLink.getId().toString(), d);
					outFlowsAlternativeRoute.put(outLink.getId().toString(), 0.0);
					numbersPassedOnInAndOutLinks.put(outLink.getId().toString(), 0);
				}
				else{
					System.out.println("No additional outLinks");
				}
			}
		}
				
		this.writer.open();
	}
	
	
	private double getDistanceFromFirstNode(Node node, Link [] routeLinks) {
		double distance = 0;
		int i=0;
		while(!routeLinks[i].getToNode().equals(node)){
			distance += routeLinks[i].getLength();
			i++;
		}
		distance += routeLinks[i].getLength();
		return distance;
	}


	// memorize linkEnterEvents on the first links of the two alternative routes:
	@Override
	public void handleEvent(final EventLinkEnter event) {
		super.handleEvent(event);
		
		//handle flows on outLinks
		if (outLinksMainRoute.contains(event.link)){
			
			//increase numbersPassed
			int numbersPassed = numbersPassedOnInAndOutLinks.get(event.linkId) + 1;
			numbersPassedOnInAndOutLinks.put(event.linkId, numbersPassed);
		}
		else if (outLinksAlternativeRoute.contains(event.linkId)){
			
			//increase numbersPassed
			int numbersPassed = numbersPassedOnInAndOutLinks.get(event.linkId) + 1;
			numbersPassedOnInAndOutLinks.put(event.linkId, numbersPassed);
		}
	}

		@Override
	public void handleEvent(final EventLinkLeave event) {
		super.handleEvent(event);
		
		//handle flows on inLinks
		if (inLinksMainRoute.contains(event.link)) {
			
			//increase numbersPassed
			int numbersPassed = numbersPassedOnInAndOutLinks.get(event.linkId) + 1;
			numbersPassedOnInAndOutLinks.put(event.linkId, numbersPassed);
		}
		else if (inLinksAlternativeRoute.contains(event.linkId)){
			
			//increase numbersPassed
			int numbersPassed = numbersPassedOnInAndOutLinks.get(event.linkId) + 1;
			numbersPassedOnInAndOutLinks.put(event.linkId, numbersPassed);
		}
	}

	public void reset(final int iteration) {
		this.writer.close();
	}

	public void handleEvent(final EventAgentWait2Link event) {
		// TODO Auto-generated method stub
	}

	public void handleEvent(final EventAgentStuck event) {
		// TODO Auto-generated method stub
	}

	public void handleEvent(final EventAgentNoRoute event) {
		// TODO Auto-generated method stub
	}

	public void handleEvent(final EventActivityStart event) {
		// TODO Auto-generated method stub
	}

	public void handleEvent(final EventActivityEnd event) {
		// TODO Auto-generated method stub
	}

	// calculates the predictive NashTime with a single-bottle-neck-model.
	public double getPredictedNashTime() {
		
		String accidentLinkId = this.accidents.get(0).getLinkId();
		bottleNeckLinkMainRoute = searchAccidentsOnRoutes(accidentLinkId);
		bottleNeckLinkAlternativeRoute = routeLinksAlternativeRoute[0]; //first link on route as a default value
		additionalAgentsMainRoute = getAdditionalAgents(mainRoute);
		additionalAgentsAlternativeRoute = getAdditionalAgents(alternativeRoute);
		
		this.predTTMainRoute = getPredictedTravelTime(mainRoute, bottleNeckLinkMainRoute);
		this.predTTAlternativeRoute = getPredictedTravelTime(alternativeRoute, bottleNeckLinkAlternativeRoute);
			
		return this.predTTMainRoute - this.predTTAlternativeRoute;
	}

	
	private double getPredictedTravelTime(final Route route,
			final Link bottleNeckLink) {
		double ttFreeSpeedBeforeBottleNeck = 0.0;
		double ttFreeSpeedAfterBottleNeck;
		double bottleNeckCapacity = bottleNeckLink.getCapacity()/3600;
		double predictedTT;
		Link [] routeLinks;
		if(route == mainRoute){
			routeLinks = routeLinksMainRoute;
		}
		else{
			routeLinks = routeLinksAlternativeRoute;
		}
		
		// get the array index of the bottleneck link
		int bottleNeckLinkNumber = 0;
		for (int i = 0; i < routeLinks.length; i++) {
			if (bottleNeckLink.equals(routeLinks[i])) {
				bottleNeckLinkNumber = i;
				break;
			}
		}
		
		// count agents and free speed travel time BEFORE bottleneck
		double agentsBeforeBottleNeck;
		if(route == mainRoute){
			agentsBeforeBottleNeck = additionalAgentsMainRoute;
		}
		else{
			agentsBeforeBottleNeck = additionalAgentsAlternativeRoute;
		}
		
		for (int i = 0; i <= bottleNeckLinkNumber; i++) {
			agentsBeforeBottleNeck += this.numberOfAgents.get(routeLinks[i] 
					.getId().toString());
			
			ttFreeSpeedBeforeBottleNeck += this.ttFreeSpeeds.get(routeLinks[i]
					.getId().toString());
		}
		
		// sum up free speed travel time AFTER bottleneck
		ttFreeSpeedAfterBottleNeck = 0 ;
		for (int i = bottleNeckLinkNumber + 1; i <routeLinks.length ; i++) {
		ttFreeSpeedAfterBottleNeck += this.ttFreeSpeeds.get(routeLinks[i]
				.getId().toString());
		}
		if(route == mainRoute){
			ttFreeSpeedAfterBottleNeckMainRoute = ttFreeSpeedAfterBottleNeck;
		}
		else{
			ttFreeSpeedAfterBottleNeckAlternativeRoute = ttFreeSpeedAfterBottleNeck;
		}
		
		if (agentsBeforeBottleNeck / bottleNeckCapacity > ttFreeSpeedBeforeBottleNeck) {
				predictedTT = agentsBeforeBottleNeck / bottleNeckCapacity
					+ ttFreeSpeedAfterBottleNeck;
		} else {
			predictedTT = ttFreeSpeedBeforeBottleNeck
					+ ttFreeSpeedAfterBottleNeck;
				}
		return predictedTT;
	}
	
	private int getAdditionalAgents(final Route route){
		double weightedNetFlow = 0;
		double netFlow = 0;
		double distanceToBottleNeck = 0;
		double ttToBottleNeck = 0;
		
		//mainRoute
		if(route == mainRoute){
			//check distance from startNode to bottleneck
			distanceToBottleNeck = getDistanceFromFirstNode(bottleNeckLinkMainRoute.getToNode(), routeLinksMainRoute);
			
			//Sum up inflows, weighted with their distance to the start node.
			Iterator<Link> it1 = inLinksMainRoute.iterator();
			while(it1.hasNext()){
				Link inLink = it1.next();
				double inflow = getInFlow(inLink, mainRoute);  
				double weightedInFlow;
				//don't include Links after the bottleNeck. Alsa takes care of null-case
				if(inFlowDistancesMainRoute.get(inLink.getId().toString()) == null
						|| inFlowDistancesMainRoute.get(inLink.getId().toString()) > distanceToBottleNeck){
					weightedInFlow = 0;
				}
				else{
					weightedInFlow = inflow * inFlowDistancesMainRoute.get(inLink.getId().toString());
				}
				weightedNetFlow += weightedInFlow;
			}
			Iterator<Link> it2 = outLinksMainRoute.iterator();
			while(it2.hasNext()){
				Link outLink = it2.next();
				double outflow = getOutFlow(outLink, mainRoute);
				double weightedOutFlow;
				if(outFlowDistancesAlternativeRoute.get(outLink.getId().toString()) == null
						|| outFlowDistancesMainRoute.get(outLink.getId().toString()) > distanceToBottleNeck){
						weightedOutFlow = 0;
				}
				else{
					weightedOutFlow = outflow * outFlowDistancesAlternativeRoute.get(outLink.getId().toString());
				}
				weightedNetFlow -= weightedOutFlow;
			}
			netFlow = weightedNetFlow / distanceToBottleNeck;
			ttToBottleNeck = getPredictedTravelTime(mainRoute, bottleNeckLinkMainRoute) - ttFreeSpeedAfterBottleNeckMainRoute;
		}
		
		//AlternativeRoute
		else {
			distanceToBottleNeck = getDistanceFromFirstNode(bottleNeckLinkAlternativeRoute.getToNode(), routeLinksAlternativeRoute);
			Iterator<Link> it3 = inLinksAlternativeRoute.iterator();
			while(it3.hasNext()){
				Link inLink = it3.next();
				double inflow = getInFlow(inLink, alternativeRoute);
				double weightedInFlow;
				if(inFlowDistancesAlternativeRoute.get(inLink.getId().toString()) == null
						|| inFlowDistancesAlternativeRoute.get(inLink.getId().toString()) > distanceToBottleNeck){
					weightedInFlow = 0;
				}
				else{
					weightedInFlow = inflow * inFlowDistancesAlternativeRoute.get(inLink.getId().toString());
				}
				weightedNetFlow += weightedInFlow;
			}
			Iterator<Link> it4 = outLinksMainRoute.iterator();
			while(it4.hasNext()){
				Link outLink = it4.next();
				double outflow = getOutFlow(outLink, alternativeRoute);
				double weightedOutFlow;
				if(outFlowDistancesAlternativeRoute.get(outLink.getId().toString()) == null
						|| outFlowDistancesAlternativeRoute.get(outLink.getId().toString()) > distanceToBottleNeck){
						weightedOutFlow = 0;
				}
				else{
					weightedOutFlow = outflow * outFlowDistancesAlternativeRoute.get(outLink.getId().toString());
				}
				weightedNetFlow -= weightedOutFlow;
			}
			netFlow = weightedNetFlow / distanceToBottleNeck;
			ttToBottleNeck = getPredictedTravelTime(alternativeRoute, bottleNeckLinkAlternativeRoute) - ttFreeSpeedAfterBottleNeckAlternativeRoute;
		}
		return (int)(ttToBottleNeck * netFlow);
	}


	private double getOutFlow(Link outLink, Route route) {
		double flow;
		if(route == mainRoute){
			flow = outFlowsMainRoute.get(outLink.getId().toString());
		}
		else{
			flow = outFlowsAlternativeRoute.get(outLink.getId().toString());			
		}
		return flow;
	}


	private double getInFlow(Link inLink, Route route) {
		double flow;
		if(route == mainRoute){
			flow = inFlowsMainRoute.get(inLink.getId().toString());
		}
		else{
			flow = inFlowsAlternativeRoute.get(inLink.getId().toString());			
		}
		return flow;
	}


	private Link searchAccidentsOnRoutes(final String accidentLinkId) {
		Route r = this.mainRoute;
		for (int j = 0; j < 2; j++) {
			Link[] links = r.getLinkRoute();
			for (int i = 0; i < links.length; i++) {
				if (links[i].getId().toString().equalsIgnoreCase(accidentLinkId)) {
					return links[i];
				}
			}
			r = this.alternativeRoute;
		}
		throw new IllegalArgumentException("The set Accident has to be on one of the routes if using this implementation of ControlInput!");
	}


	// ContolInputI interface methods:

	public double getNashTime() {
		try {
			this.writer.writeAgentsOnLinks(this.numberOfAgents);
			this.writer.writeTravelTimesMainRoute(this.lastTime1,
					this.predTTMainRoute);
			this.writer.writeTravelTimesAlternativeRoute(this.lastTime2,
					this.predTTAlternativeRoute);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//set new in and outflows and reset numbersPassedOnInAndOutLinks every five minutes
		double simTime = SimulationTimer.getTime();
		if(simTime%10 == 0){
			//inLinksMainRoute
			Iterator<Link> iter1 = inLinksMainRoute.iterator();
			while(iter1.hasNext()){
				Link inLink = (Link)iter1.next();
				double flow = (double)numbersPassedOnInAndOutLinks.get(inLink.getId().toString())/10;
				inFlowsMainRoute.put(inLink.getId().toString(), flow);
				numbersPassedOnInAndOutLinks.put(inLink.getId().toString(), 0);
			}
			
			//outLinksMainRoute
			Iterator<Link> iter2 = outLinksMainRoute.iterator();
			while(iter2.hasNext()){
				Link outLink = (Link)iter2.next();
				double flow = numbersPassedOnInAndOutLinks.get(outLink.getId().toString())/10;
				outFlowsMainRoute.put(outLink.getId().toString(), flow);
				numbersPassedOnInAndOutLinks.put(outLink.getId().toString(), 0);
			}
			//inLinksAlternativeRoute
			Iterator<Link> iter3 = inLinksAlternativeRoute.iterator();
			while(iter3.hasNext()){
				Link inLink = (Link)iter3.next();
				double flow = numbersPassedOnInAndOutLinks.get(inLink.getId().toString())/10;
				inFlowsAlternativeRoute.put(inLink.getId().toString(), flow);
				numbersPassedOnInAndOutLinks.put(inLink.getId().toString(), 0);
			}
			
			//outLinksAlternativeRoute
			Iterator<Link> iter4 = inLinksAlternativeRoute.iterator();
			while(iter4.hasNext()){
				Link outLink = (Link)iter4.next();
				double flow = numbersPassedOnInAndOutLinks.get(outLink.getId().toString())/10;
				outFlowsAlternativeRoute.put(outLink.getId().toString(), flow);
				numbersPassedOnInAndOutLinks.put(outLink.getId().toString(), 0);
			}
		}
		
		// return timeDiffernce;
		return getPredictedNashTime();

	}

	public void setAccidents(final List<Accident> accidents) {
		this.accidents = accidents;
	}


}
