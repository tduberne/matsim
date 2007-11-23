/* *********************************************************************** *
 * project: org.matsim.*
 * PlansFileSnapshotWriter.java
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

package org.matsim.mobsim.snapshots;


import org.matsim.gbl.Gbl;
import org.matsim.plans.Act;
import org.matsim.plans.Person;
import org.matsim.plans.Plan;
import org.matsim.plans.Plans;
import org.matsim.plans.PlansWriter;
import org.matsim.utils.misc.Time;

/**
 * Writes the current position of all vehicles into a plans file.
 *
 * @author glaemmel
 *
 */
public class PlansFileSnapshotWriter implements SnapshotWriterI {

	private String filePrefix;
	private String fileSuffix;

	private String version = null;
	private String filename = null;

	private double currenttime = -1;

	private Plans plans = null;

	public PlansFileSnapshotWriter(String snapshotFilePrefix, String snapshotFileSuffix){
		this.filePrefix = snapshotFilePrefix;
		this.fileSuffix = snapshotFileSuffix;

		this.version = Gbl.getConfig().plans().getOutputVersion();
	}

	public void beginSnapshot(double time) {
		this.plans = new Plans(Plans.NO_STREAMING);
		this.filename = this.filePrefix + Time.strFromSec((int)time) + "." + this.fileSuffix;
		this.currenttime = time;
	}

	public void endSnapshot() {
		writePlans();
		this.plans = null;
		this.currenttime = -1;
	}

	/**
	 * Writes the position infos as plans to a file using
	 * {@link org.matsim.plans.PlansWriter}
	 */
	private void writePlans() {
		PlansWriter pw = new PlansWriter(this.plans, this.filename, this.version);
		pw.write();
	}

	public void addAgent(PositionInfo position) {
		Person pers = new Person(position.getAgentId().toString(), null, null, null, "always", null);

		Plan plan = new Plan(pers);
		Act actA = new Act("h", position.getEasting(), position.getNorthing(),
				position.getLink(), 0.0, this.currenttime, this.currenttime, true);
		plan.addAct(actA);

		pers.addPlan(plan);

		try {
			this.plans.addPerson(pers);
		} catch (Exception e) { e.printStackTrace(); };

	}

	public void finish() {
	}

}
