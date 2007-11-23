/* *********************************************************************** *
 * project: org.matsim.*
 * PopParser.java
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


/* *********************************************************************** *
 *                     org.matsim.demandmodeling.plans                     *
 *                             PopParser.java                              *
 *                          ---------------------                          *
 * copyright       : (C) 2006 by                                           *
 *                   Michael Balmer, Konrad Meister, Marcel Rieser,        *
 *                   David Strippgen, Kai Nagel, Kay W. Axhausen,          *
 *                   Technische Universitaet Berlin (TU-Berlin) and        *
 *                   Swiss Federal Institute of Technology Zurich (ETHZ)   *
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

package org.matsim.plans;

import java.util.Stack;

import org.matsim.gbl.Gbl;
import org.matsim.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

/**
 * A reader for population files generated by Martin Frick.
 *
 * @author mfrick
 * @author mrieser
 */
public class PopParser extends MatsimXmlParser {

	private final static String SYNPOP   = "synthetic_population";
	private final static String AGENT    = "agent";
	private final static String HOME     = "home";
	private final static String WORK     = "workplace";
	private final static String LOCATION = "location";
	private final static String AGE      = "age";
	private final static String SEX      = "sex";
	private final static String DLICENCE = "driver_licence_ownership";
	private final static String CARAVAIL = "car_availability";
	private final static String EMPLOYED = "employed";
	private final static String HALFFARE = "half_fare_ticket_ownership";
	private final static String GA       = "general_abonnement_ownership";
	private final static String INCOME   = "household_monthly_income";

	private PlansWriter planswriter = null;
	private static final int PLANS_STARTED   = 1;
	private static final int PERSON_FINISHED = 2;
	private static final int PLANS_FINISHED  = 3;

	private final Plans plans;
	private PopReaderHandler handler = null;
	
	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PopParser(final Plans plans, final PlansWriter planswriter) {
		this.plans = plans;
		this.planswriter = planswriter;
	}

	@Override
	protected void setDoctype(String doctype) {
		if (doctype.equals("synthetic_population.dtd")) {
			this.handler = new PopReaderHandlerImpl(plans);
		} else {
			Gbl.errorMsg(this + "[doctype=" + doctype + " not known]");
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// private methods
	//////////////////////////////////////////////////////////////////////

	private final void streamCommand(int command) {
		if (!Gbl.getConfig().plans().switchOffPlansStreaming()) {

			if (command == PLANS_STARTED) {
				this.planswriter.writeStartPlans();
			}
			else if (command == PERSON_FINISHED) {
				plans.runPersonAlgorithms();
				this.planswriter.writePersons();
				plans.clearPersons();
			}
			else if (command == PLANS_FINISHED) {
				this.planswriter.writeEndPlans();
			}
			else {
				Gbl.errorMsg(this + "[command=" + command + " not known]");
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	//
	// interface implementation
	//
	//////////////////////////////////////////////////////////////////////

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (SYNPOP.equals(name)) {
			handler.startSynPop(atts);
			// this is the message for the writer to write the plans header
			this.streamCommand(PLANS_STARTED);
		} else if (AGENT.equals(name)) {
			handler.startAgent(atts);
		} else if (HOME.equals(name)) {
			handler.startHome(atts);
		} else if (WORK.equals(name)) {
			handler.startWorkplace(atts);
		} else if (LOCATION.equals(name)) {
			handler.startLocation(atts);
		} else if (AGE.equals(name)) {
			handler.startAge(atts);
		} else if (SEX.equals(name)) {
			handler.startSex(atts);
		} else if (DLICENCE.equals(name)) {
			handler.startDLicence(atts);
		} else if (CARAVAIL.equals(name)) {
			handler.startCarAvail(atts);
		} else if (EMPLOYED.equals(name)) {
			handler.startEmployed(atts);
		} else if (HALFFARE.equals(name)) {
			handler.startHalfFare(atts);
		} else if (GA.equals(name)) {
			handler.startGA(atts);
		} else if (INCOME.equals(name)) {
			handler.startIncome(atts);
		} else {
			Gbl.errorMsg(this + "[lname=" + name + " not known]");
		}
	}

	//////////////////////////////////////////////////////////////////////

	@Override
	public void endTag(String name, String content, Stack<String> context) {
		if (SYNPOP.equals(name)) {
			handler.endSynPop();
			// this is the message for the writer to write the footer
			this.streamCommand(PLANS_FINISHED);
		} else if (AGENT.equals(name)) {
			handler.endAgent();
			// this is the message (1.) for the plans to run the algos, (2.) for
			// the writer to write that person and (3.) for the plans to erasure that
			// person is removed from memory
			this.streamCommand(PERSON_FINISHED);
		} else if (HOME.equals(name)) {
			handler.endHome();
		} else if (WORK.equals(name)) {
			handler.endWorkplace();
		} else if (LOCATION.equals(name)) {
			handler.endLocation();
		} else if (AGE.equals(name)) {
			handler.endAge();
		} else if (SEX.equals(name)) {
			handler.endSex();
		} else if (DLICENCE.equals(name)) {
			handler.endDLicence();
		} else if (CARAVAIL.equals(name)) {
			handler.endCarAvail();
		} else if (EMPLOYED.equals(name)) {
			handler.endEmployed();
		} else if (HALFFARE.equals(name)) {
			handler.endHalfFare();
		} else if (GA.equals(name)) {
			handler.endGA();
		} else if (INCOME.equals(name)) {
			handler.endIncome();
		} else {
			Gbl.errorMsg(this + "[lname=" + name + " not known]");
		}
	}
}
