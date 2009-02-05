package playground.wrashid.DES;

import org.matsim.mobsim.deqsim.TestEventLog;
import org.matsim.mobsim.deqsim.TestMessageFactory;
import org.matsim.mobsim.deqsim.TestMessageQueue;
import org.matsim.mobsim.deqsim.TestScheduler;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for playground.wrashid.DES");

		suite.addTest(playground.wrashid.DES.util.AllTests.suite());
		
		return suite;
	}

	

}
