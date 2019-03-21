/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.etaxi.optimizer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.etaxi.ETaxiScheduler;
import org.matsim.contrib.etaxi.optimizer.assignment.AssignmentETaxiOptimizer;
import org.matsim.contrib.etaxi.optimizer.assignment.AssignmentETaxiOptimizerParams;
import org.matsim.contrib.etaxi.optimizer.rules.RuleBasedETaxiOptimizer;
import org.matsim.contrib.etaxi.optimizer.rules.RuleBasedETaxiOptimizerParams;
import org.matsim.contrib.ev.data.ChargingInfrastructure;
import org.matsim.contrib.taxi.optimizer.TaxiOptimizer;
import org.matsim.contrib.taxi.run.TaxiConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ETaxiOptimizerProvider implements Provider<TaxiOptimizer> {
	public static final String TYPE = "type";

	public enum EOptimizerType {
		E_RULE_BASED, E_ASSIGNMENT;
	}

	private final EventsManager eventsManager;
	private final TaxiConfigGroup taxiCfg;
	private final Network network;
	private final Fleet fleet;
	private final MobsimTimer timer;
	private final TravelTime travelTime;
	private final TravelDisutility travelDisutility;
	private final ETaxiScheduler eScheduler;
	private final ChargingInfrastructure chargingInfrastructure;

	public ETaxiOptimizerProvider(EventsManager eventsManager, TaxiConfigGroup taxiCfg, Fleet fleet,
			@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network, MobsimTimer timer,
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime, TravelDisutility travelDisutility,
			ETaxiScheduler eScheduler, ChargingInfrastructure chargingInfrastructure) {
		this.eventsManager = eventsManager;
		this.taxiCfg = taxiCfg;
		this.fleet = fleet;
		this.network = network;
		this.timer = timer;
		this.travelTime = travelTime;
		this.travelDisutility = travelDisutility;
		this.eScheduler = eScheduler;
		this.chargingInfrastructure = chargingInfrastructure;
	}

	@Override
	public TaxiOptimizer get() {
		Configuration optimizerConfig = new MapConfiguration(taxiCfg.getOptimizerConfigGroup().getParams());
		EOptimizerType type = EOptimizerType.valueOf(optimizerConfig.getString(TYPE));

		switch (type) {
			case E_RULE_BASED:
				return RuleBasedETaxiOptimizer.create(eventsManager, taxiCfg, fleet, eScheduler, network, timer,
						travelTime, travelDisutility, new RuleBasedETaxiOptimizerParams(optimizerConfig),
						chargingInfrastructure);

			case E_ASSIGNMENT:
				return AssignmentETaxiOptimizer.create(eventsManager, taxiCfg, fleet, network, timer, travelTime,
						travelDisutility, eScheduler, chargingInfrastructure,
						new AssignmentETaxiOptimizerParams(optimizerConfig));

			default:
				throw new RuntimeException();
		}
	}
}
