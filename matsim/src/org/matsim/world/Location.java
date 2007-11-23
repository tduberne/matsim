/* *********************************************************************** *
 * project: org.matsim.*
 * Location.java
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

package org.matsim.world;

import java.util.ArrayList;
import java.util.TreeMap;

import org.matsim.basic.v01.Id;
import org.matsim.facilities.Facility;
import org.matsim.gbl.Gbl;
import org.matsim.network.Link;
import org.matsim.utils.geometry.CoordI;
import org.matsim.utils.identifiers.IdI;

/**
 * Basic geographical class in MATSim.
 * @see Link
 * @see Facility
 * @see Zone
 * @author Michael Balmer
 */
public abstract class Location {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	// TODO [balmermi] The id should be unchangeable ('final'), but there
	// are modules which actually want to change ids of locations (see NetworkCleaner).
	// I'm not that happy the Ids can change (otherwise it would not be an id)!
	protected IdI id;
	protected final Layer layer;
	protected final CoordI center;

	// points to the zones of the lower resolution layer
	protected final TreeMap<IdI,Location> up_mapping = new TreeMap<IdI,Location>();

	// points to the zones of the higher resolution layer
	protected final TreeMap<IdI,Location> down_mapping = new TreeMap<IdI,Location>();

	//////////////////////////////////////////////////////////////////////
	// constructor
	//////////////////////////////////////////////////////////////////////

	/**
	 * A unique location for a given layer.
	 * @param layer The layer the location belongs to.
	 * @param id The unique id of that location.
	 * @param center The center of that location. Does not have to be the middle of the location object.
	 */
	protected Location(final Layer layer, final IdI id, final CoordI center) {
		this.layer = layer;
		this.id = id;
		this.center = center;
		if (this.center == null) {
			Gbl.errorMsg("Location id=" + id + " instanciate without coordinate!");
		}
		if (this.layer == null) {
			Gbl.errorMsg("Location id=" + id + " instanciate without layer!");
		}
	}

	/**
	 * @deprecated This constructor must not be used anymore. It will produce an error message anyway.
	 * @param id
	 */
	@Deprecated
	protected Location(final String id) {
		this(null,new Id(id),null);
	}

	/**
	 * A unique location for a given layer.
	 * @param layer The layer the location belongs to.
	 * @param id The unique id of that location.
	 * @param center_x
	 * @param center_y
	 */
	protected Location(final Layer layer, final IdI id, final double center_x, final double center_y) {
		this(layer,id,new Coord(center_x,center_y));
	}

	/**
	 * A unique location for a given layer.
	 * @param layer The layer the location belongs to.
	 * @param id The unique id of that location.
	 * @param center_x
	 * @param center_y
	 */
	protected Location(final Layer layer, final String id, final String center_x, final String center_y) {
		this(layer,id,new Coord(center_x,center_y));
	}

	/**
	 * A unique location for a given layer.
	 * @param layer The layer the location belongs to.
	 * @param id The unique id of that location.
	 * @param center The center of that location. Does not have to be the middle of the location object.
	 */
	protected Location(final Layer layer, final String id, final CoordI center) {
		this(layer,new Id(id),center);
	}

	//////////////////////////////////////////////////////////////////////
	// calc methods
	//////////////////////////////////////////////////////////////////////

	/**
	 * Calculates the distance from a given coordinate to that location.
	 * The interpretation of <em>distance</em> differ from the actual type of location.
	 * @param coord The coordinate from which the distance to that location should be calculated.
	 * @return the distance to that location
	 */
	public abstract double calcDistance(final CoordI coord);

	//////////////////////////////////////////////////////////////////////
	// add methods
	//////////////////////////////////////////////////////////////////////

	/**
	 * Connects two location of two different layers. The layers have to be 'neighbors' which
	 * means that the layers are connected via a MappingRule. The other layer have to be 'above'
	 * the one this location belongs to.
	 * @param other
	 */
	public final void addUpMapping(final Location other) {
		if (this.layer.getUpRule() == null) {
			Gbl.errorMsg(this.toString() + "[other=" + other + " has no up_rule]");
		}
		if (!this.layer.getUpRule().getUpLayer().equals(other.getLayer())) {
			Gbl.errorMsg(this.toString() + "[other=" + other + " has wrong layer]");
		}
		IdI other_id = other.getId();
		if (!this.up_mapping.containsKey(other_id)) {
			this.up_mapping.put(other_id,other);
		}
	}

	/**
	 * Connects two location of two different layers. The layers have to be 'neighbors' which
	 * means that the layers are connected via a MappingRule. The other layer have to be 'below'
	 * the one this location belongs to.
	 * @param other
	 */
	public final void addDownMapping(final Location other) {
		if (this.layer.getDownRule() == null) {
			Gbl.errorMsg(this.toString() + "[other=" + other + " has no down_rule]");
		}
		if (!this.layer.getDownRule().getDownLayer().equals(other.getLayer())) {
			Gbl.errorMsg(this.toString() + "[other=" + other + " has wrong layer]");
		}
		IdI other_id = other.getId();
		if (!this.down_mapping.containsKey(other_id)) {
			this.down_mapping.put(other_id,other);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// remove methods
	//////////////////////////////////////////////////////////////////////

	protected final boolean removeUpMapping(final IdI other_id) {
		if (this.up_mapping.get(other_id) == null) { return true; }
		Location other = this.up_mapping.get(other_id);
		if (other.down_mapping.remove(this.getId()) == null) { Gbl.errorMsg("This should never happen!"); }
		if (this.up_mapping.remove(other_id) == null) { Gbl.errorMsg("This should never happen!"); }
		return true;
	}

	protected final boolean removeDownMapping(final IdI other_id) {
		if (this.down_mapping.get(other_id) == null) { return true; }
		Location other = this.down_mapping.get(other_id);
		if (other.up_mapping.remove(this.getId()) == null) { Gbl.errorMsg("This should never happen!"); }
		if (this.down_mapping.remove(other_id) == null) { Gbl.errorMsg("This should never happen!"); }
		return true;
	}

	public final boolean removeAllUpMappings() {
		ArrayList<IdI> other_ids = new ArrayList<IdI>(this.up_mapping.keySet());
		for (int i=0; i<other_ids.size(); i++) { this.removeUpMapping(other_ids.get(i)); }
		return true;
	}

	public final boolean removeAllDownMappings() {
		ArrayList<IdI> other_ids = new ArrayList<IdI>(this.down_mapping.keySet());
		for (int i=0; i<other_ids.size(); i++) { this.removeDownMapping(other_ids.get(i)); }
		return true;
	}

	//////////////////////////////////////////////////////////////////////
	// set methods
	//////////////////////////////////////////////////////////////////////

	// TODO [balmermi] I do not like that (see above why)
	public final void setId(IdI id) {
		this.id = id;
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	public final IdI getId() {
		return this.id;
	}

	public final Layer getLayer() {
		return this.layer;
	}

	public final CoordI getCenter() {
		return this.center;
	}

	public final Location getUpLocation(IdI id) {
		return this.up_mapping.get(id);
	}

	public final Location downLocation(IdI id) {
		return this.down_mapping.get(id);
	}

	public final TreeMap<IdI,Location> getUpMapping() {
		return this.up_mapping;
	}

	public final TreeMap<IdI,Location> getDownMapping() {
		return this.down_mapping;
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return "[id=" + this.getId() + "]" +
		       "[layer=" + this.layer + "]" +
		       "[center=" + this.center + "]" +
		       "[nof_up_mapping=" + this.up_mapping.size() + "]" +
		       "[nof_down_mapping=" + this.down_mapping.size() + "]";
	}
}
