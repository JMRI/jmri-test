/**
 * LocoNetTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet package
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.loconet;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.jmrix.loconet.locogen.*;

public class LocoNetTest extends TestCase {

	// from here down is testing infrastructure

	public LocoNetTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoNetTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrix.loconet.LocoNetTest");  // no tests in this class itself
		suite.addTest(jmri.jmrix.loconet.locoio.LocoIOTest.suite());
		suite.addTest(new TestSuite(LocoNetMessageTest.class));
		suite.addTest(new TestSuite(LnTrafficControllerTest.class));
		suite.addTest(new TestSuite(LnTrafficRouterTest.class));
		suite.addTest(new TestSuite(LnPacketizerTest.class));
		suite.addTest(LnPowerManagerTest.suite());
		suite.addTest(LnTurnoutTest.suite());
		suite.addTest(LnTurnoutManagerTest.suite());
		suite.addTest(LnSensorTest.suite());
		suite.addTest(LnSensorAddressTest.suite());
		suite.addTest(LnSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.loconet.locoio.LocoIOTest.suite());
 		suite.addTest(jmri.jmrix.loconet.locogen.LocoGenFrameTest.suite());
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetTest.class.getName());

}
