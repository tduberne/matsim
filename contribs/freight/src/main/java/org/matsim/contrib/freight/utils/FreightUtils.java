/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

package org.matsim.contrib.freight.utils;

import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierImpl;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

/**
 * Utils for the work with the freight contrib
 * 
 * @author kturner
 *
 */
public class FreightUtils {

	/**
	 * From the outside, rather use {@link FreightUtils#getCarriers(Scenario)} .  This string constant will eventually become private.
	 */
	@Deprecated
	public static final String CARRIERS = "carriers" ;
	private static final Logger log = Logger.getLogger(FreightUtils.class );

	/**
	 * Creates a new {@link Carriers} container only with {@link CarrierShipment}s for creating a new VRP.
	 * As consequence of the transformation of {@link CarrierService}s to {@link CarrierShipment}s the solution of the VRP can have tours with
	 * vehicles returning to the depot and load for another tour instead of creating another vehicle with additional (fix) costs.
	 * <br/>
	 * The method is meant for multi-depot problems.  Here, the original "services" input does not have an assignment of services
	 * to depots.  The solution to the problem, however, does.  So the assignment is taken from that solution, and each returned
	 * {@link Carrier} has that depot as pickup location in each shipment.
	 *
	 * @param carriers	carriers with a Solution (result of solving the VRP).
	 * @return Carriers carriersWithShipments
	 */
	public static Carriers createShipmentVRPCarrierFromServiceVRPSolution(Carriers carriers) {
		Carriers carriersWithShipments = new Carriers();
		for (Carrier carrier : carriers.getCarriers().values()){
			Carrier carrierWS = CarrierImpl.newInstance(carrier.getId());
			if (carrier.getShipments().size() > 0) {
				copyShipments(carrierWS, carrier);
			}
			//			copyPickups(carrierWS, carrier);	//Not implemented yet due to missing CarrierPickup in freight contrib, kmt Sep18
			//			copyDeliveries(carrierWS, carrier); //Not implemented yet due to missing CarrierDelivery in freight contrib, kmt Sep18
			if (carrier.getServices().size() > 0) {
				createShipmentsFromServices(carrierWS, carrier); 
			}
			carrierWS.setCarrierCapabilities(carrier.getCarrierCapabilities()); //vehicles and other carrierCapabilites
			carriersWithShipments.addCarrier(carrierWS);
		}
		return carriersWithShipments;
	}

	public static Carriers getCarriers( Scenario scenario ){
		Carriers carriers = (Carriers) scenario.getScenarioElement( CARRIERS );
		if ( carriers==null ) {
			carriers = new Carriers(  ) ;
			scenario.addScenarioElement( CARRIERS, carriers );
		}
		return carriers;
	}

	/**
	 * NOT implemented yet due to missing CarrierDelivery in freight contrib, kmt Sep18
	 * @param carrierWS
	 * @param carrier
	 */
	private void copyDeliveries(Carrier carrierWS, Carrier carrier) {
		log.error("Coping of Deliveries is NOT implemented yet due to missing CarrierDelivery in freight contrib");
	}

	/**
	 * NOT implemented yet due to missing CarrierPickup in freight contrib, kmt Sep18
	 * @param carrierWS
	 * @param carrier
	 */
	private void copyPickups(Carrier carrierWS, Carrier carrier) {
		log.error("Coping of Pickup is NOT implemented yet due to missing CarrierPickup in freight contrib");
	}

	/**
	 * Copy all shipments from the existing carrier to the new carrier with shipments.
	 * @param carrierWS		the "new" carrier with Shipments
	 * @param carrier		the already existing carrier
	 */
	private static void copyShipments(Carrier carrierWS, Carrier carrier) {
		for (CarrierShipment carrierShipment: carrier.getShipments()){
			log.debug("Copy CarrierShipment: " + carrierShipment.toString());
			carrierWS.getShipments().add(carrierShipment);
		}
		
	}

	/**
	 * Transform all services from the existing carrier to the new carrier with shipments.
	 * The location of the depot from which the "old" carrier starts the tour to the service is used as fromLocation for the new Shipment.
	 * @param carrierWS		the "new" carrier with Shipments
	 * @param carrier		the already existing carrier
	 */
	private static void createShipmentsFromServices(Carrier carrierWS, Carrier carrier) {
		TreeMap<Id<CarrierService>, Id<Link>> depotServiceIsdeliveredFrom = new TreeMap<Id<CarrierService>, Id<Link>>();
		try {
			carrier.getSelectedPlan();
		} catch (Exception e) {
			throw new RuntimeException("Carrier " + carrier.getId() + " has NO selectedPlan. --> CanNOT create a new carrier from solution");
		}
		try {
			carrier.getSelectedPlan().getScheduledTours();
		} catch (Exception e) {
			throw new RuntimeException("Carrier " + carrier.getId() + " has NO ScheduledTours. --> CanNOT create a new carrier from solution");
		}
		for (ScheduledTour tour : carrier.getSelectedPlan().getScheduledTours()) {
			Id<Link> depotForTour = tour.getVehicle().getLocation();
			for (TourElement te : tour.getTour().getTourElements()) {
				if (te instanceof ServiceActivity){
					ServiceActivity act = (ServiceActivity) te;
					depotServiceIsdeliveredFrom.put(act.getService().getId(), depotForTour);
				}
			}
		}
		for (CarrierService carrierService : carrier.getServices()) {
			log.debug("Converting CarrierService to CarrierShipment: " + carrierService.getId());
			CarrierShipment carrierShipment = CarrierShipment.Builder.newInstance(Id.create(carrierService.getId().toString(), CarrierShipment.class), 
					depotServiceIsdeliveredFrom.get(carrierService.getId()),
					carrierService.getLocationLinkId(),
					carrierService.getCapacityDemand())
					.setDeliveryServiceTime(carrierService.getServiceDuration())
					//						.setPickupServiceTime(pickupServiceTime)			//Not set yet, because in service we have now time for that. Maybe change it later, kmt sep18
					.setDeliveryTimeWindow(carrierService.getServiceStartTimeWindow())
					.setPickupTimeWindow(TimeWindow.newInstance(0.0, carrierService.getServiceStartTimeWindow().getEnd()))			// limited to end of delivery timeWindow (pickup later as latest delivery is not usefull)
					.build();
			carrierWS.getShipments().add(carrierShipment);
		}
	}

}
