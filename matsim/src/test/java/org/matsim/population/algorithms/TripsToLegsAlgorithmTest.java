/* *********************************************************************** *
 * project: org.matsim.*
 * TripDetectionTest.java
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
package org.matsim.population.algorithms;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.algorithms.TripsToLegsAlgorithm;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.MainModeIdentifierImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author thibautd
 */
public class TripsToLegsAlgorithmTest {
	private static class Fixture {
		public final Plan plan;
		public final List<PlanElement> expectedPlanStructure;
		public final String name;

		public Fixture(
				final String name,
				final Plan plan,
				final List<PlanElement> structure) {
			this.name = name;
			this.plan = plan;
			this.expectedPlanStructure = structure;
		}
	}

	@Test
	public void testMonoLegPlan() throws Exception {
		final Plan plan = PopulationUtils.createPlan(PopulationUtils.getFactory().createPerson(Id.create("id", Person.class)));
		final List<PlanElement> structure = new ArrayList<PlanElement>();

		final Id<Link> id1 = Id.create( 1, Link.class );
		final Id<Link> id2 = Id.create( 2, Link.class );

		Activity act = PopulationUtils.createActivityFromLinkId("act_1", id1);
		plan.addActivity( act );
		structure.add( act );
		Leg leg = PopulationUtils.createLeg("mode_1");
		plan.addLeg( leg );
		structure.add( leg );
		act = PopulationUtils.createActivityFromLinkId("act_2", id2);
		plan.addActivity( act );
		structure.add( act );
		leg = PopulationUtils.createLeg("mode_2");
		plan.addLeg( leg );
		structure.add( leg );
		act = PopulationUtils.createActivityFromLinkId("act_3", id1);
		plan.addActivity( act );
		structure.add( act );

		performTest(
				new Fixture(
					"mono leg trips",
					plan,
					structure));
	}

	@Test
	public void testMultiLegPlan() throws Exception {
		final Plan plan = PopulationUtils.createPlan(PopulationUtils.getFactory().createPerson(Id.create("id", Person.class)));
		final List<PlanElement> structure = new ArrayList<PlanElement>();

		final Id<Link> id1 = Id.create( 1, Link.class );
		final Id<Link> id2 = Id.create( 2, Link.class );

		Activity act = PopulationUtils.createActivityFromLinkId("act_1", id1);
		plan.addActivity( act );
		structure.add( act );

		Leg leg = PopulationUtils.createLeg("mode_1");
		plan.addLeg( leg );
		structure.add( leg );

		leg = PopulationUtils.createLeg("mode_1bis");
		plan.addLeg( leg );

		act = PopulationUtils.createActivityFromLinkId("act_2", id2);
		plan.addActivity( act );
		structure.add( act );

		leg = PopulationUtils.createLeg("mode_2");
		plan.addLeg( leg );
		structure.add( leg );

		leg = PopulationUtils.createLeg("mode_2bis");
		plan.addLeg( leg );

		act = PopulationUtils.createActivityFromLinkId("act_3", id1);
		plan.addActivity( act );
		structure.add( act );

		performTest(
				new Fixture(
					"multi leg trips",
					plan,
					structure));	
	}

	@Test
	public void testDummyActsPlan() throws Exception {
		final Plan plan = PopulationUtils.createPlan(PopulationUtils.getFactory().createPerson(Id.create("id", Person.class)));
		final List<PlanElement> structure = new ArrayList<PlanElement>();

		final Id<Link> id1 = Id.create( 1, Link.class );
		final Id<Link> id2 = Id.create( 2, Link.class );
		final Id<Link> id3 = Id.create( 3, Link.class );

		Activity act = PopulationUtils.createActivityFromLinkId("act_1", id1);
		plan.addActivity( act );
		structure.add( act );

		Leg leg = PopulationUtils.createLeg("mode_1");
		plan.addLeg( leg );
		structure.add( leg );

		Waypoint waypoint = PopulationUtils.createWaypoint(null, id3);
		plan.addWaypoint( waypoint );

		leg = PopulationUtils.createLeg("mode_1bis");
		plan.addLeg( leg );

		act = PopulationUtils.createActivityFromLinkId("act_2", id2);
		plan.addActivity( act );
		structure.add( act );

		leg = PopulationUtils.createLeg("mode_2");
		plan.addLeg( leg );
		structure.add( leg );

		waypoint = PopulationUtils.createWaypoint(null, id3);
		plan.addWaypoint( waypoint );

		leg = PopulationUtils.createLeg("mode_2bis");
		plan.addLeg( leg );

		act = PopulationUtils.createActivityFromLinkId("act_3", id1);
		plan.addActivity( act );
		structure.add( act );

		performTest(
				new Fixture(
					"dummy act trips",
					plan,
					structure));		
	}

	@Test
	public void testPtPlan() throws Exception {
		final Plan plan = PopulationUtils.createPlan(PopulationUtils.getFactory().createPerson(Id.create("id", Person.class)));
		final List<PlanElement> structure = new ArrayList<PlanElement>();

		final Id<Link> id1 = Id.create( 1, Link.class );
		final Id<Link> id2 = Id.create( 2, Link.class );
		final Id<Link> id3 = Id.create( 3, Link.class );

		Activity act = PopulationUtils.createActivityFromLinkId("act_1", id1);
		plan.addActivity( act );
		structure.add( act );

		Leg leg = PopulationUtils.createLeg(TransportMode.transit_walk);
		plan.addLeg( leg );

		Waypoint waypoint = PopulationUtils.createWaypoint(null, id3);
		plan.addWaypoint( waypoint );

		leg = PopulationUtils.createLeg(TransportMode.pt);
		plan.addLeg( leg );
		structure.add( leg );

		act = PopulationUtils.createActivityFromLinkId("act_2", id2);
		plan.addActivity( act );
		structure.add( act );

		act = PopulationUtils.createActivityFromLinkId("act_2bis", id2);
		plan.addActivity( act );
		structure.add( act );

		leg = PopulationUtils.createLeg(TransportMode.transit_walk);
		plan.addLeg( leg );
		structure.add( PopulationUtils.createLeg(TransportMode.pt) );

		waypoint = PopulationUtils.createWaypoint(null, id3);
		plan.addWaypoint( waypoint );

		leg = PopulationUtils.createLeg("mode_2bis");
		plan.addLeg( leg );

		act = PopulationUtils.createActivityFromLinkId("act_3", id1);
		plan.addActivity( act );
		structure.add( act );

		performTest(
				new Fixture(
					"dummy act trips",
					plan,
					structure));		
	}

	private static void performTest(final Fixture fixture) {

		final TripsToLegsAlgorithm algorithm = new TripsToLegsAlgorithm(EmptyStageActivityTypes.INSTANCE, new MainModeIdentifierImpl() );
		algorithm.run( fixture.plan );

		assertEquals(
				"wrong structure size for fixture <<"+fixture.name+">>",
				fixture.expectedPlanStructure.size(),
				fixture.plan.getPlanElements().size());

		final Iterator<PlanElement> expIter = fixture.expectedPlanStructure.iterator();
		final Iterator<PlanElement> actualIter = fixture.plan.getPlanElements().iterator();

		while ( expIter.hasNext() ) {
			final PlanElement expected = expIter.next();
			final PlanElement actual = actualIter.next();

			if ( actual instanceof Activity ) {
				assertTrue(
						"incompatible Activity/Leg sequence in fixture <<"+fixture.name+">>",
						expected instanceof Activity );

				assertEquals(
						"incompatible activity types in fixture <<"+fixture.name+">>",
						((Activity) expected).getType(),
						((Activity) actual).getType());
			}
			else if ( actual instanceof Leg ) {
				assertTrue(
						"incompatible types sequence in fixture <<"+fixture.name+">>",
						expected instanceof Leg );

				assertEquals(
						"incompatible leg modes in fixture <<"+fixture.name+">>",
						((Leg) expected).getMode(),
						((Leg) actual).getMode());
			}
			else {
				throw new RuntimeException( actual.getClass().getName() );
			}
		}
	}
}

