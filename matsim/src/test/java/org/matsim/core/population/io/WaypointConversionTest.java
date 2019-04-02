package org.matsim.core.population.io;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.PtConstants;
import org.matsim.testcases.MatsimTestUtils;

public class WaypointConversionTest {
	@Rule
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	public void testPtInteractionConvertedOnImport() {
		testWaypointsConvertedOnImport(PtConstants.TRANSIT_ACTIVITY_TYPE);
	}

	@Test
	public void testArbitraryInteractionConvertedOnImport() {
		testWaypointsConvertedOnImport("random interaction");
	}

	public void testWaypointsConvertedOnImport(final String interactionType) {
		final String plansFile = utils.getOutputDirectory()+"/plans.xml";

		final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		final PopulationFactory factory = scenario.getPopulation().getFactory();

		// create an interaction-based plan and write it to file
		final Id<Link> waypointLinkId = Id.createLinkId("spaceport");
		final Id<Person> personId = Id.createPersonId("luke");

		final Person person = factory.createPerson(personId);
		final Plan plan = factory.createPlan();
		person.addPlan(plan);
		scenario.getPopulation().addPerson(person);


		plan.addActivity(factory.createActivityFromLinkId("home", Id.createLinkId("home")));
		plan.addLeg(factory.createLeg("pt"));
		plan.addActivity(factory.createActivityFromLinkId(interactionType, waypointLinkId));
		plan.addLeg(factory.createLeg("pt"));
		plan.addActivity(factory.createActivityFromLinkId("home", Id.createLinkId("home")));

		new PopulationWriter(scenario.getPopulation()).writeV6(plansFile);

		// Read and check: interaction should now be a waypoint
		final Scenario rereadScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(rereadScenario).readFile(plansFile);
		final Person rereadPerson = rereadScenario.getPopulation().getPersons().get(personId);

		final Plan rereadPlan = rereadPerson.getPlans().get(0);
		final PlanElement expectedWaypoint = rereadPlan.getPlanElements().get(2);

		Assert.assertEquals(
				"unexpected plan length for "+rereadPlan,
				plan.getPlanElements().size(),
				rereadPlan.getPlanElements().size());

		Assert.assertTrue(
				"Waypoint conversion seems to have failed for "+expectedWaypoint,
				expectedWaypoint instanceof Waypoint);

		final Waypoint waypoint = (Waypoint) expectedWaypoint;

		Assert.assertEquals(
				"wrong link ID in waypoint",
				waypointLinkId,
				waypoint.getLinkId());
	}
}
