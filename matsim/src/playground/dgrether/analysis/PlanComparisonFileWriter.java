/* *********************************************************************** *
 * project: org.matsim.*
 * PlanComparisonFileWriter.java
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

package playground.dgrether.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/**
 * This class can be used to write a PlanComparison to a file.
 * @author dgrether
 *
 */
public class PlanComparisonFileWriter implements PlanComparisonWriter {

	private BufferedWriter writer;
	/**
	 * 
	 * @param filename
	 */
	public PlanComparisonFileWriter(String filename) {
		try {
			this.writer = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @see playground.dgrether.analysis.PlanComparisonWriter#write(playground.dgrether.analysis.PlanComparison)
	 */
	public void write(PlanComparison pc) {
		PlanComparisonStringWriter pcsw = new PlanComparisonStringWriter();
		pcsw.write(pc);
		try {
			this.writer.append(pcsw.getResult());
		  this.writer.flush();
		  this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
