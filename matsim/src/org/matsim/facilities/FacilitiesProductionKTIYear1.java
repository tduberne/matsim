/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesProductionKTIYear1.java
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

package org.matsim.facilities;

import org.matsim.facilities.algorithms.FacilitiesAllActivitiesFTE;
import org.matsim.facilities.algorithms.FacilitiesOpentimesKTIYear1;
import org.matsim.facilities.algorithms.FacilitiesRandomizeHectareCoordinates;
import org.matsim.gbl.Gbl;

/**
 * Generates the facilities file for all of Switzerland from the Swiss
 * National Enterprise Census of the year 2000 (published 2001).
 * 
 * @author meisterk
 *
 */
public class FacilitiesProductionKTIYear1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Gbl.createConfig(args);
		Facilities facilities = (Facilities)Gbl.getWorld().createLayer(Facilities.LAYER_TYPE,null);
		
		facilities.setName(
				"Facilities based on the Swiss National Enterprise Census of the year 2000. Generated by org.matsim.demandmodeling.facilities.FacilitiesProductionKTIYear1"
				);
		
		System.out.println("  adding and running facilities algorithms... ");
		facilities.addAlgorithm(new FacilitiesAllActivitiesFTE());
		facilities.addAlgorithm(new FacilitiesOpentimesKTIYear1());
		facilities.addAlgorithm(new FacilitiesRandomizeHectareCoordinates());
		facilities.runAlgorithms();
		System.out.println("  done.");

		System.out.println("  writing facilities file... ");
		FacilitiesWriter facilities_writer = new FacilitiesWriter(facilities);
		facilities_writer.write();
		System.out.println("  done.");

	}

}
