/**
 * EasyDccTest.java
 *
 * Description:	    tests for the jmri.jmrix.easydcc package
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.easydcc;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

public class EasyDccTest extends TestCase {

	// from here down is testing infrastructure

	public EasyDccTest(String s) {
		super(s);
	}

	// a dummy test to avoid JUnit warning
	public void testDemo() {
		assertTrue(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.jmrix.nce.EasyDccTest");
		suite.addTest(jmri.jmrix.easydcc.EasyDccTurnoutTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccTurnoutManagerTest.suite());
		suite.addTest(jmri.jmrix.easydcc.easydccmon.EasyDccMonFrameTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccProgrammerTest.suite());
		suite.addTest(jmri.jmrix.easydcc.packetgen.EasyDccPacketGenFrameTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccTrafficControllerTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccMessageTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccReplyTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccPowerManagerTest.suite());
		return suite;
	}

}
