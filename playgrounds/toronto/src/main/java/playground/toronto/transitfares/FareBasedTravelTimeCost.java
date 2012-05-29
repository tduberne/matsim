package playground.toronto.transitfares;

import java.util.Arrays;
import java.util.HashMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterNetwork.TransitRouterNetworkLink;
import org.matsim.pt.router.TransitRouterNetwork.TransitRouterNetworkNode;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.vehicles.Vehicle;

public class FareBasedTravelTimeCost implements TravelDisutility, TravelTime {

	private final static double MIDNIGHT = 24.0*3600;

	protected final TransitRouterConfig config;
	private Link previousLink = null;
	private double previousTime = Double.NaN;
	private double cachedTravelTime = Double.NaN;
	private Person agent;
	private Double tvm;
	private Double fare;
	
	/**
	 * A set of lookup tables, first organized by fareclass, then by a Tuple denoting the fromZone and ToZone.
	 * Contains Doubles of monetary fares.
	 * @author pkucirek
	 */
	private HashMap<String, HashMap<Tuple<String,String>, Double>> farelookuptable;
	
	//Additional attributes for stop facilities and persons.
	private ObjectAttributes StopZoneMap;
	private ObjectAttributes PersonFareClassMap;
	
	//--------------------------------------

	public FareBasedTravelTimeCost(final TransitRouterConfig config,
			HashMap<String, HashMap<Tuple<String,String>, Double>> farelookuptable,
			ObjectAttributes StopZoneMap,
			ObjectAttributes PersonFareClassMap,
			Double timevalueofmoney) {
		
		
		//super(config);
		
		this.config = config;
		this.farelookuptable = farelookuptable;
		this.StopZoneMap = StopZoneMap;
		this.PersonFareClassMap = PersonFareClassMap;
		this.tvm = timevalueofmoney;
	}
	
	
	// TODO currently a copy of the non-fare-based generalized cost method, needs to be replaced with the new updated TravelDisutiltiy
	public double getLinkGeneralizedTravelCost(Link link, double time) {
		double cost;

		if (((TransitRouterNetworkLink) link).getRoute() == null) {
			// it's a transfer link (walk)
			//cost = -getLinkTravelTime(link, time) * this.config.getEffectiveMarginalUtilityOfTravelTimeWalk_utl_s() + this.config.getUtilityOfLineSwitch_utl();
			double transfertime = getLinkTravelTime(link, time);
			double waittime = this.config.additionalTransferTime;
			double walktime = transfertime - waittime;
			cost = -walktime * this.config.getMarginalUtilityOfTravelTimeWalk_utl_s()
			       -waittime * this.config.getMarginalUtiltityOfWaiting_utl_s()
			       - this.config.getUtilityOfLineSwitch_utl();
		} else {
			cost = -getLinkTravelTime(link, time) * this.config.getMarginalUtilityOfTravelTimePt_utl_s() - link.getLength() * this.config.getMarginalUtilityOfTravelDistancePt_utl_m();
		}
		
		/* Looks up the 'zone' (or type) of the start-node/-stop and the end-node/-stop in the
		 * lookup table. Then, looks up the corresponding zone-to-zone transfer fare in the [NAME] table
		 * based on the agent class. This will return a transfer penalty (i.e, cost). 
		 */
		
		//snippet for looking up the stopFacility id (for finding the coresponding zone).
		//((TransitRouterNetworkNode) link.getFromNode()).getStop().getStopFacility().getId()
		
		String personfareclass = PersonFareClassMap.getAttribute(
				agent.getId().toString(),
				"fareclass")
				.toString();
		if(!farelookuptable.containsKey(personfareclass)){
			personfareclass = "default";
		}
		fare = farelookuptable.get(personfareclass).get(new Tuple<String,String>(
				StopZoneMap.getAttribute(
						((TransitRouterNetworkNode) link.getFromNode()).getStop().getStopFacility().getId().toString(),
						"zone").toString(),
				StopZoneMap.getAttribute(
						((TransitRouterNetworkNode) link.getToNode()).getStop().getStopFacility().getId().toString(),
						"zone").toString()));
		
		cost += fare * this.tvm;
		
		return cost;
	}
	
	@Override
	public double getLinkTravelTime(final Link link, final double time) {
		if ((link == this.previousLink) && (time == this.previousTime)) {
			return this.cachedTravelTime;
		}
		this.previousLink = link;
		this.previousTime = time;

		TransitRouterNetworkLink wrapped = (TransitRouterNetworkLink) link;
		TransitRouteStop fromStop = wrapped.fromNode.stop;
		TransitRouteStop toStop = wrapped.toNode.stop;
		if (wrapped.getRoute() != null) {
			// agent stays on the same route, so use transit line travel time
			double bestDepartureTime = getNextDepartureTime(wrapped.getRoute(), fromStop, time);

			double arrivalOffset = (toStop.getArrivalOffset() != Time.UNDEFINED_TIME) ? toStop.getArrivalOffset() : toStop.getDepartureOffset();
			double time2 = (bestDepartureTime - time) + (arrivalOffset - fromStop.getDepartureOffset());
			if (time2 < 0) {
				time2 += MIDNIGHT;
			}
			this.cachedTravelTime = time2;
			return time2;
		}
		// different transit routes, so it must be a line switch
		double distance = wrapped.getLength();
		double time2 = distance / this.config.getBeelineWalkSpeed() + this.config.additionalTransferTime;
		this.cachedTravelTime = time2;
		return time2;
	}
	
	private final HashMap<TransitRoute, double[]> sortedDepartureCache = new HashMap<TransitRoute, double[]>();
	
	public double getNextDepartureTime(final TransitRoute route, final TransitRouteStop stop, final double depTime) {
		double earliestDepartureTime = depTime - stop.getDepartureOffset();

		if (earliestDepartureTime >= MIDNIGHT) {
			earliestDepartureTime = earliestDepartureTime % MIDNIGHT;
		}

		double[] cache = this.sortedDepartureCache.get(route);
		if (cache == null) {
			cache = new double[route.getDepartures().size()];
			int i = 0;
			for (Departure dep : route.getDepartures().values()) {
				cache[i++] = dep.getDepartureTime();
			}
			Arrays.sort(cache);
			this.sortedDepartureCache.put(route, cache);
		}
		int pos = Arrays.binarySearch(cache, earliestDepartureTime);
		if (pos < 0) {
			pos = -(pos + 1);
		}
		if (pos >= cache.length) {
			pos = 0; // there is no later departure time, take the first in the morning
		}
		double bestDepartureTime = cache[pos];

		bestDepartureTime += stop.getDepartureOffset();
		while (bestDepartureTime < depTime) {
			bestDepartureTime += MIDNIGHT;
		}
		return bestDepartureTime;
	}


	@Override
	public double getLinkTravelDisutility(Link link, double time,
			Person person, Vehicle vehicle) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// TODO Auto-generated method stub
		return 0;
	}

}