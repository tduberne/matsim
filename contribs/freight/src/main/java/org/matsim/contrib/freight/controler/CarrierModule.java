/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * CarrierModule.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.contrib.freight.controler;

import com.google.inject.Inject;
import com.google.inject.Provides;
import org.matsim.contrib.freight.CarrierConfigGroup;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.mobsim.CarrierAgentTracker;
import org.matsim.contrib.freight.mobsim.FreightQSimFactory;
import org.matsim.contrib.freight.replanning.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

public class CarrierModule extends AbstractModule {

    private FreightConfigGroup freightConfig;

    private Carriers carriers = null;
    private CarrierPlanStrategyManagerFactory strategyManagerFactory;
    private CarrierScoringFunctionFactory scoringFunctionFactory;


    public CarrierModule() {
    }

    /**
     * CarrierPlanStrategyManagerFactory and CarrierScoringFunctionFactory must me bound separately
     * when this constructor is used.
     */
    public CarrierModule(Carriers carriers) {
        this.carriers = carriers;
    }

    public CarrierModule(Carriers carriers, CarrierPlanStrategyManagerFactory strategyManagerFactory, CarrierScoringFunctionFactory scoringFunctionFactory) {
        this.carriers = carriers;
        this.strategyManagerFactory = strategyManagerFactory;
        this.scoringFunctionFactory = scoringFunctionFactory;
    }

    @Override
    public void install() {
        this.freightConfig = (FreightConfigGroup) getConfig().getModules().get(FreightConfigGroup.GROUPNAME);

        if(this.carriers == null){
            this.carriers = new Carriers();
            new CarrierPlanXmlReader(carriers).readFile(freightConfig.getCarriersFile());
            CarrierVehicleTypes vehicleTypes = new CarrierVehicleTypes();
            new CarrierVehicleTypeReader(vehicleTypes).readFile(freightConfig.getCarriersVehicleTypesFile());
            new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(vehicleTypes);
        }

        bind(Carriers.class).toInstance(carriers);
        if (strategyManagerFactory != null) {
            bind(CarrierPlanStrategyManagerFactory.class).toInstance(strategyManagerFactory);
        }
        if (scoringFunctionFactory != null) {
            bind(CarrierScoringFunctionFactory.class).toInstance(scoringFunctionFactory);
        }

        // First, we need a ControlerListener.
        bind(CarrierControlerListener.class).asEagerSingleton();
        addControlerListenerBinding().to(CarrierControlerListener.class);

        // Set the Mobsim. The FreightQSimFactory needs the CarrierAgentTracker (see constructor).
        bindMobsim().toProvider(FreightQSimFactory.class);

        this.addControlerListenerBinding().toInstance( new ShutdownListener(){
            @Inject Config config ;
            @Override public void notifyShutdown( ShutdownEvent event ){
                writeAdditionalRunOutput( config, carriers );
            }
        } );

    }

    // We export CarrierAgentTracker, which is kept by the ControlerListener, which happens to re-create it every iteration.
    // The freight QSim needs it (see below).
    @Provides
    CarrierAgentTracker provideCarrierAgentTracker(CarrierControlerListener carrierControlerListener) {
        return carrierControlerListener.getCarrierAgentTracker();
    }

    private static void writeAdditionalRunOutput( Config config, Carriers carriers ) {
        // ### some final output: ###
        new CarrierPlanXmlWriterV2(carriers).write( config.controler().getOutputDirectory() + "/output_carriers.xml" ) ;
        new CarrierPlanXmlWriterV2(carriers).write( config.controler().getOutputDirectory() + "/output_carriers.xml.gz") ;
        new CarrierVehicleTypeWriter( CarrierVehicleTypes.getVehicleTypes(carriers )).write(config.controler().getOutputDirectory() + "/output_vehicleTypes.xml" );
        new CarrierVehicleTypeWriter(CarrierVehicleTypes.getVehicleTypes(carriers)).write(config.controler().getOutputDirectory() + "/output_vehicleTypes.xml.gz");
    }



}
