package org.matsim.api.core.v01.population;

import org.matsim.api.core.v01.BasicAddress;
import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/**
 * This interface represents a "stay" in a plan, that is, the opposite of a movement ({@link Leg}).
 * This interface is not meant to be implemented directly, but is rather a way to group the two kind
 * of stays in MATSim plans:
 * <ul>
 *     <li>{@link Activity}, which are stays with a duration that represent, well, the classical "activities" as
 *          typically understood in activity-based analysis of transport systems</li>
 *     <li>{@link Waypoint}, which simply represents going through somewhere, mostly used to represent changes
 *          of vehicle inside a trip</li>
 * </ul>
 *
 * MATSim is designed in a way that relies on the strict alternance of stays and movements.
 * It might be acceptable to break this rule in some cases (for instance initial demand generation),
 * but if a part of the library chokes on it, it should be considered a feature, not a bug.
 *
 * @author thibautd
 */
public interface Stay extends PlanElement, BasicAddress, BasicLocation {
	void setLinkId(Id<Link> linkId);
	void setCoord(Coord coord);
}
