/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * VolumesAnalyzerModule.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2018 by the members listed in the COPYING, *
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

package org.matsim.analysis;

import org.apache.log4j.Logger;
import org.matsim.core.controler.AbstractModule;


public class VTTSAnalysisModule extends AbstractModule {
	private final static Logger log = Logger.getLogger(VTTSAnalysisModule.class);

	@Override
    public void install() {
        if (getConfig().vspExperimental().getVTTSanalysisInterval() > 0) {
        	
        		if (getConfig().planCalcScore().getMarginalUtilityOfMoney() == 0.) {
        			log.warn("The marginal utility of money must not be 0.0. The VTTS is computed in Money per Time.");
        		}
        	
        		bind(VTTSAnalysis.class).asEagerSingleton();
            addControlerListenerBinding().to(VTTSAnalysis.class);
            addEventHandlerBinding().to(VTTSAnalysis.class);
        }
    }

}
