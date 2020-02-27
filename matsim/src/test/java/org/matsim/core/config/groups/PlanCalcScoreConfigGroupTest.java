
/* *********************************************************************** *
 * project: org.matsim.*
 * PlanCalcScoreConfigGroupTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

 package org.matsim.core.config.groups;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.*;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ScoringParameterSet;
import org.matsim.testcases.MatsimTestUtils;

import java.util.Map;
import java.util.Random;

import static org.matsim.core.config.groups.PlanCalcScoreConfigGroup.*;

public class PlanCalcScoreConfigGroupTest {
	private static final Logger log = Logger.getLogger(PlanCalcScoreConfigGroupTest.class);

	@Rule
	public final MatsimTestUtils utils = new MatsimTestUtils();

	private void testAccessEgressParametersBeforeConsistencyCheck(Config config) {
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore();

		for (ScoringParameterSet scoringParameters : scoringConfig.getScoringParametersPerSubpopulation().values()) {
			// mode params are there for default modes:
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.car ) );
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.walk ) );
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.bike ) );
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.ride ) );
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.pt ) );
			Assert.assertNotNull( scoringParameters.getModes().get( TransportMode.other ) );

			// default stage/interaction params are there for pt and drt (as a service):
			Assert.assertNotNull( scoringParameters.getActivityParams( createStageActivityType( TransportMode.pt ) ) );
			Assert.assertNotNull( scoringParameters.getActivityParams( createStageActivityType( TransportMode.drt ) ) );
		}
	}

	private void testAccessEgressParametersAfterConsistencyCheck( Config config ) {
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore() ;

		// default stage/interaction params for modes routed on the network are now there:
		for( String networkMode : config.plansCalcRoute().getNetworkModes() ){
			for (ScoringParameterSet scoringParams : scoringConfig.getScoringParametersPerSubpopulation().values()) {
				Assert.assertNotNull( scoringParams.getActivityParams( createStageActivityType( networkMode ) ) );
			}
		}
	}

	@Test
	public void testAccessEgressParameterGetInserted() {
		Config config = ConfigUtils.loadConfig( utils.getClassInputDirectory() + "config_v2_w_scoringparams.xml" ) ;
		testAccessEgressParametersBeforeConsistencyCheck( config );
		PlanCalcScoreConfigGroup scoringConfig = config.planCalcScore() ;
		config.plansCalcRoute().setInsertingAccessEgressWalk( true );
		scoringConfig.checkConsistency( config );
		testAccessEgressParametersAfterConsistencyCheck( config );
	}


	@Test
	public void testAddActivityParams() {
		PlanCalcScoreConfigGroup c = new PlanCalcScoreConfigGroup();
		// null should always be there
		ScoringParameterSet scoringParameters = c.getScoringParameters(null);
        int originalSize = scoringParameters.getActivityParams().size();
		Assert.assertNull(scoringParameters.getActivityParams("type1"));
        Assert.assertEquals(originalSize, scoringParameters.getActivityParams().size());

		ActivityParams ap = new ActivityParams("type1");
		scoringParameters.addActivityParams(ap);
		Assert.assertEquals(ap, scoringParameters.getActivityParams("type1"));
        Assert.assertEquals(originalSize + 1, scoringParameters.getActivityParams().size());
	}


	//private void assertIdentical(
	//		final String msg,
	//		final PlanCalcScoreConfigGroup initialGroup,
	//		final PlanCalcScoreConfigGroup inputConfigGroup) {
	//	Assert.assertEquals(
	//			"wrong brainExpBeta "+msg,
	//			initialGroup.getBrainExpBeta(),
	//			inputConfigGroup.getBrainExpBeta(),
	//			1e-7);
 //       
	//	Assert.assertEquals(
	//			"wrong constantBike "+msg,
	//			initialGroup.getModes().get(TransportMode.bike).getConstant(),
	//			inputConfigGroup.getModes().get(TransportMode.bike).getConstant(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong constantCar "+msg,
	//			initialGroup.getModes().get(TransportMode.car).getConstant(),
	//			inputConfigGroup.getModes().get(TransportMode.car).getConstant(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong constantOther "+msg,
	//			initialGroup.getModes().get(TransportMode.other).getConstant(),
	//			inputConfigGroup.getModes().get(TransportMode.other).getConstant(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong constantPt "+msg,
	//			initialGroup.getModes().get(TransportMode.pt).getConstant(),
	//			inputConfigGroup.getModes().get(TransportMode.pt).getConstant(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong constantWalk "+msg,
	//			initialGroup.getModes().get(TransportMode.walk).getConstant(),
	//			inputConfigGroup.getModes().get(TransportMode.walk).getConstant(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong lateArrival_utils_hr "+msg,
	//			initialGroup.getLateArrival_utils_hr(),
	//			inputConfigGroup.getLateArrival_utils_hr(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong earlyDeparture_utils_hr "+msg,
	//			initialGroup.getEarlyDeparture_utils_hr(),
	//			inputConfigGroup.getEarlyDeparture_utils_hr(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong learningRate "+msg,
	//			initialGroup.getLearningRate(),
	//			inputConfigGroup.getLearningRate(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong marginalUtilityOfMoney "+msg,
	//			initialGroup.getMarginalUtilityOfMoney(),
	//			inputConfigGroup.getMarginalUtilityOfMoney() ,
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong marginalUtlOfDistanceOther "+msg,
	//			initialGroup.getModes().get(TransportMode.other).getMarginalUtilityOfDistance(),
	//			inputConfigGroup.getModes().get(TransportMode.other).getMarginalUtilityOfDistance(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong marginalUtlOfDistanceWalk "+msg,
	//			initialGroup.getModes().get(TransportMode.walk).getMarginalUtilityOfDistance(),
	//			inputConfigGroup.getModes().get(TransportMode.walk).getMarginalUtilityOfDistance(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong marginalUtlOfWaiting_utils_hr "+msg,
	//			initialGroup.getMarginalUtlOfWaiting_utils_hr(),
	//			inputConfigGroup.getMarginalUtlOfWaiting_utils_hr(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong marginalUtlOfWaitingPt_utils_hr "+msg,
	//			initialGroup.getMarginalUtlOfWaitingPt_utils_hr(),
	//			inputConfigGroup.getMarginalUtlOfWaitingPt_utils_hr(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong monetaryDistanceCostRateCar "+msg,
	//			initialGroup.getModes().get(TransportMode.car).getMonetaryDistanceRate(),
	//			inputConfigGroup.getModes().get(TransportMode.car).getMonetaryDistanceRate(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong monetaryDistanceCostRatePt "+msg,
	//			initialGroup.getModes().get(TransportMode.pt).getMonetaryDistanceRate(),
	//			inputConfigGroup.getModes().get(TransportMode.pt).getMonetaryDistanceRate(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong pathSizeLogitBeta "+msg,
	//			initialGroup.getPathSizeLogitBeta(),
	//			inputConfigGroup.getPathSizeLogitBeta(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong performing_utils_hr "+msg,
	//			initialGroup.getPerforming_utils_hr(),
	//			inputConfigGroup.getPerforming_utils_hr(),
	//			1e-7 );
	//	Assert.assertEquals(
	//			"wrong traveling_utils_hr "+msg,
	//			initialGroup.getModes().get(TransportMode.car).getMarginalUtilityOfTraveling(),
	//			inputConfigGroup.getModes().get(TransportMode.car).getMarginalUtilityOfTraveling(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong travelingBike_utils_hr "+msg,
	//			initialGroup.getModes().get(TransportMode.bike).getMarginalUtilityOfTraveling(),
	//			inputConfigGroup.getModes().get(TransportMode.bike).getMarginalUtilityOfTraveling(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong travelingOther_utils_hr "+msg,
	//			initialGroup.getModes().get(TransportMode.other).getMarginalUtilityOfTraveling(),
	//			inputConfigGroup.getModes().get(TransportMode.other).getMarginalUtilityOfTraveling(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong travelingPt_utils_hr "+msg,
	//			initialGroup.getModes().get(TransportMode.pt).getMarginalUtilityOfTraveling(),
	//			inputConfigGroup.getModes().get(TransportMode.pt).getMarginalUtilityOfTraveling(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong travelingWalk_utils_hr "+msg,
	//			initialGroup.getModes().get(TransportMode.walk).getMarginalUtilityOfTraveling(),
	//			inputConfigGroup.getModes().get(TransportMode.walk).getMarginalUtilityOfTraveling(),
	//			1e-7);
	//	Assert.assertEquals(
	//			"wrong utilityOfLineSwitch "+msg,
	//			initialGroup.getUtilityOfLineSwitch(),
	//			inputConfigGroup.getUtilityOfLineSwitch(),
	//			1e-7 );

	//	for ( ActivityParams initialSettings : initialGroup.getActivityParams() ) {
	//		final ActivityParams inputSettings =
	//			inputConfigGroup.getActivityParams(
	//					initialSettings.getActivityType() );
	//		Assert.assertEquals(
	//				"wrong type "+msg,
	//				initialSettings.getActivityType(),
	//				inputSettings.getActivityType() );
	//		Assert.assertEquals(
	//				"wrong closingTime "+msg,
	//				initialSettings.getClosingTime(),
	//				inputSettings.getClosingTime(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong earliestEndTime "+msg,
	//				initialSettings.getEarliestEndTime(),
	//				inputSettings.getEarliestEndTime(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong latestStartTime "+msg,
	//				initialSettings.getLatestStartTime(),
	//				inputSettings.getLatestStartTime(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong minimalDuration "+msg,
	//				initialSettings.getMinimalDuration(),
	//				inputSettings.getMinimalDuration(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong openingTime "+msg,
	//				initialSettings.getOpeningTime(),
	//				inputSettings.getOpeningTime(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong priority "+msg,
	//				initialSettings.getPriority(),
	//				inputSettings.getPriority(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong typicalDuration "+msg,
	//				initialSettings.getTypicalDuration(),
	//				inputSettings.getTypicalDuration(),
	//				1e-7 );
	//	}

	//	for ( ModeParams initialSettings : initialGroup.getModes().values() ) {
	//		final String mode = initialSettings.getMode();
	//		final ModeParams inputSettings = inputConfigGroup.getModes().get( mode );
	//		Assert.assertEquals(
	//				"wrong constant "+msg,
	//				initialSettings.getConstant(),
	//				inputSettings.getConstant(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong marginalUtilityOfDistance "+msg,
	//				initialSettings.getMarginalUtilityOfDistance(),
	//				inputSettings.getMarginalUtilityOfDistance(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong marginalUtilityOfTraveling "+msg,
	//				initialSettings.getMarginalUtilityOfTraveling(),
	//				inputSettings.getMarginalUtilityOfTraveling(),
	//				1e-7 );
	//		Assert.assertEquals(
	//				"wrong monetaryDistanceRate "+msg,
	//				initialSettings.getMonetaryDistanceRate(),
	//				inputSettings.getMonetaryDistanceRate(),
	//				1e-7 );
	//	}


	//}


	private PlanCalcScoreConfigGroup createTestConfigGroup() {
		final PlanCalcScoreConfigGroup group = new PlanCalcScoreConfigGroup();

		group.setBrainExpBeta( 124);
		ScoringParameterSet scoringParameters = group.getScoringParameters(null);
		scoringParameters.getModes().get(TransportMode.bike).setConstant((double) 98);
		scoringParameters.getModes().get(TransportMode.car).setConstant((double) 345);
		scoringParameters.getModes().get(TransportMode.other).setConstant((double) 345);
		scoringParameters.getModes().get(TransportMode.pt).setConstant((double) 983);
		scoringParameters.getModes().get(TransportMode.walk).setConstant((double) 89);
		scoringParameters.setLateArrival_utils_hr( 345 );
		scoringParameters.setEarlyDeparture_utils_hr( 5 );
		group.setLearningRate( 98 );
		scoringParameters.setMarginalUtilityOfMoney( 9 );
		scoringParameters.getModes().get(TransportMode.other).setMarginalUtilityOfDistance((double) 23);
		scoringParameters.getModes().get(TransportMode.walk).setMarginalUtilityOfDistance((double) 8675);
		scoringParameters.setMarginalUtlOfWaiting_utils_hr( 65798 );
		scoringParameters.setMarginalUtlOfWaitingPt_utils_hr( 9867d );
		scoringParameters.getModes().get(TransportMode.car).setMonetaryDistanceRate((double) 240358);
		scoringParameters.getModes().get(TransportMode.pt).setMonetaryDistanceRate((double) 9835);
		group.setPathSizeLogitBeta( 8 );
		scoringParameters.setPerforming_utils_hr( 678 );
		scoringParameters.getModes().get(TransportMode.car).setMarginalUtilityOfTraveling((double) 246);
		scoringParameters.getModes().get(TransportMode.bike).setMarginalUtilityOfTraveling((double) 968);
		scoringParameters.getModes().get(TransportMode.other).setMarginalUtilityOfTraveling((double) 206);
		scoringParameters.getModes().get(TransportMode.pt).setMarginalUtilityOfTraveling((double) 957);
		scoringParameters.getModes().get(TransportMode.walk).setMarginalUtilityOfTraveling((double) 983455);
		scoringParameters.setUtilityOfLineSwitch( 396 );

		final Random random = new Random( 925 );
		for ( int i=0; i < 10; i++ ) {
			final ActivityParams settings = new ActivityParams();
			settings.setActivityType( "activity-type-"+i );
			settings.setClosingTime( random.nextInt( 24*3600 ) );
			settings.setEarliestEndTime( random.nextInt( 24*3600 ) );
			settings.setLatestStartTime( random.nextInt( 24*3600 ) );
			settings.setMinimalDuration( random.nextInt( 24*3600 ) );
			settings.setOpeningTime( random.nextInt( 24*3600 ) );
			settings.setPriority( random.nextInt( 10 ) );
			settings.setTypicalDuration( random.nextInt( 24*3600 ) );

			scoringParameters.addActivityParams( settings );
		}

		for ( int i=0; i < 10; i++ ) {
			final ModeParams settings = new ModeParams();
			settings.setMode( "mode-"+i );
			settings.setConstant( random.nextDouble() );
			settings.setMarginalUtilityOfDistance( random.nextDouble() );
			settings.setMarginalUtilityOfTraveling( random.nextDouble() );
			settings.setMonetaryDistanceRate( random.nextDouble() );

			group.addParameterSet( settings );
		}


		return group;
	}
}
