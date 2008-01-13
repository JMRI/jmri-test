// SerialTest.java

package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.grapevine package.
 * @author      Bob Jacobsen  Copyright 2003, 2007
 * @version   $Revision: 1.4 $
 */
public class SerialTest extends TestCase {

    // from here down is testing infrastructure

    public SerialTest(String s) {
        super(s);
    }

    public void testDefinitions() {
        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERNODE,
                                    SerialNode.MAXSENSORS+1);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.grapevine.SerialTest");
        suite.addTest(SerialTurnoutTest.suite());
        suite.addTest(SerialTurnoutTest1.suite());
        suite.addTest(SerialTurnoutTest2.suite());
        suite.addTest(SerialTurnoutTest3.suite());
        suite.addTest(SerialTurnoutManagerTest.suite());
        suite.addTest(SerialLightTest.suite());
        suite.addTest(SerialLightManagerTest.suite());
        suite.addTest(SerialSensorManagerTest.suite());
        suite.addTest(SerialNodeTest.suite());
        suite.addTest(SerialMessageTest.suite());
        suite.addTest(SerialReplyTest.suite());
        suite.addTest(SerialTrafficControllerTest.suite());
        suite.addTest(SerialAddressTest.suite());
        suite.addTest(jmri.jmrix.grapevine.serialmon.SerialMonTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
