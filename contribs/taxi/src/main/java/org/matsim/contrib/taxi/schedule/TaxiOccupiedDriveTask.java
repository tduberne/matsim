/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.schedule;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;
import org.matsim.contrib.taxi.passenger.TaxiRequest;

public class TaxiOccupiedDriveTask extends DriveTaskImpl implements TaxiTask {
	public TaxiOccupiedDriveTask(VrpPathWithTravelData path, TaxiRequest request) {
		super(path);

		if (request.getFromLink() != path.getFromLink() && request.getToLink() != path.getToLink()) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public TaxiTaskType getTaxiTaskType() {
		return TaxiTaskType.OCCUPIED_DRIVE;
	}

	@Override
	protected String commonToString() {
		return "[" + getTaxiTaskType().name() + "]" + super.commonToString();
	}
}
