/* *********************************************************************** *
 * project: org.matsim.*
 * NonSpatialInteractor.java
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

package playground.jhackney.interactions;

import org.matsim.gbl.Gbl;

import playground.jhackney.socialnet.SocialNetEdge;
import playground.jhackney.socialnet.SocialNetwork;

/**
 * This Interactor lets agents exchange knowledge.
 *
 * These are wrappers for the person knowledge exchange methods
 * 
 * @author J.Hackney
 */
public class NonSpatialInteractor{
    SocialNetwork net;
    Object links[];

    double proportionOfLinksToActivate;// [0.0,1.0]
    double fract_intro;//[0.0,1.0]

    int numInteractionsPerLink;// [0.0,1.0]

    PersonExchangeKnowledge pxk; // the actual workhorse

    public NonSpatialInteractor(SocialNetwork snet) {
	this.net=snet;

	pxk = new PersonExchangeKnowledge(net);
	proportionOfLinksToActivate = Double.parseDouble(Gbl.getConfig().socnetmodule().getFractNSInteract());
	numInteractionsPerLink = Integer.parseInt(Gbl.getConfig().socnetmodule().getSocNetNSInteractions());
	fract_intro=Double.parseDouble(Gbl.getConfig().socnetmodule().getTriangles());
    }

    public void exchangeGeographicKnowledge(String facType, int iteration) {
	System.out.println("  |Exchanging knowledge about "+facType+" factivity");


	java.util.Collections.shuffle(net.getLinks());
	links = net.getLinks().toArray();
	int numPicks = (int) (proportionOfLinksToActivate * links.length);

//	Pick a random link
	for(int i=0;i<numPicks;i++){
	    int rndInt1 = (int) Gbl.random.nextInt(Integer.MAX_VALUE);
	    int linkno = rndInt1 % links.length;
	    SocialNetEdge mySocialLink = (SocialNetEdge) links[linkno];

//	    Interact numInteractions times if chosen link is to be activated
	    for (int k = 0; k < numInteractionsPerLink; k++) {

//		Exchange random knowledge if that is the algorithm to be used in interaction
		pxk.exchangeRandomFacilityKnowledge(mySocialLink, facType);

//		Else insert other interactions here
//		pxk.otherCoolInteraction(net,curLink, XXXX);
	    }
	}
    }
    /**
     * This interact method exchanges knowledge about a person in Ego's egonet with one
     * of his friends: C is B's friend. B is A's friend. B "tells" C and A about each other.
     * Closes triangles. It might be strange for people to say they know each other without
     * having met face to face. However don't take it so literally. This model is an abstraction
     * (consider these to be trips not observed). It is the same mechanism as in JinGirNew,
     * which however uses a more direct and efficient "friends introducing friends" algorithm.
     * Without persons being introduced to persons via friends of friends in this
     * manner, it is not certain that enough triangles would form by spatial meeting. Essentially
     * we introduce people and THEN see if the relationship can be supported by the geography.
     * If not (i.e. if the people do not visit each other any more), the relationship disappears
     * after a time, anyway.
     * 
     * @author jhackney
     * @param iteration
     */
    public void exchangeSocialNetKnowledge(int iteration) {

	java.util.Collections.shuffle(net.getLinks());
	links = net.getLinks().toArray();
	int numPicks = (int) (fract_intro * links.length);

	for(int i=0;i<numPicks;i++){

//	    Get a random social link
	    int rndInt1 = (int) Gbl.random.nextInt(Integer.MAX_VALUE);
	    int linkno = rndInt1 % links.length;
	    SocialNetEdge myLink = (SocialNetEdge) links[linkno];

	    for (int j = 0; j < numInteractionsPerLink; j++) {

//		Random exchange of alters: note, results in adding to social network!
//		pxk.randomlyIntroduceAtoCviaB(myLink, iteration);
		pxk.randomlyIntroduceBtoCviaA(myLink, iteration);

//		Could be replaced with other means of choosing friends to introduce
//		pxk.introduceCoolestNFriends(net,myLink,iteration);
	    }
	}
    }
}
