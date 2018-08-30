package org.matsim.core.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Waypoint;
import org.matsim.utils.objectattributes.attributable.Attributes;

class WaypointImpl implements Waypoint {
	private Id<Link> linkId = null;
	private Coord coord = null;
	private final Attributes attributes = new Attributes();

	WaypointImpl(Coord coord, Id<Link> linkId) {
		this.linkId = linkId;
		this.coord = coord;
	}

	@Override
	public Id<Link> getLinkId() {
		return linkId;
	}

	@Override
	public void setLinkId(Id<Link> linkId) {
		this.linkId = linkId;
	}

	@Override
	public Coord getCoord() {
		return coord;
	}

	@Override
	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	@Override
	public Attributes getAttributes() {
		return attributes;
	}
}
