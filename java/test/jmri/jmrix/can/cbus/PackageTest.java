// PackageTest.java
package jmri.jmrix.can.cbus;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    public void testDefinitions() {
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.can.cbus.CbusTest");
        suite.addTest(jmri.jmrix.can.cbus.CbusAddressTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusProgrammerTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusProgrammerManagerTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusSensorTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.can.cbus.swing.SwingTest.suite());
        }

        return suite;
    }

}
