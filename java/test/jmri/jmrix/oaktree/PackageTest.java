// PackageTest.java
package jmri.jmrix.oaktree;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.oaktree package.
 *
 * @author Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    public void testDefinitions() {
        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERNODE,
                SerialNode.MAXSENSORS + 1);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.oaktree.SerialTest");
        suite.addTest(SerialTurnoutTest.suite());
        suite.addTest(SerialTurnoutManagerTest.suite());
        suite.addTest(SerialSensorManagerTest.suite());
        suite.addTest(SerialNodeTest.suite());
        suite.addTest(SerialMessageTest.suite());
        suite.addTest(SerialTrafficControllerTest.suite());
        suite.addTest(SerialAddressTest.suite());
        return suite;
    }

}
