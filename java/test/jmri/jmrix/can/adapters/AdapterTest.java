// AdapterTest.java

package jmri.jmrix.can.adapters;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters package.
 * @author      Bob Jacobsen  Copyright 2009
 * @version   $Revision: 1.2 $
 */
public class AdapterTest extends TestCase {

    public void testDefinitions() {
    }
    
    // from here down is testing infrastructure

    public AdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", AdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.can.adapters.AdapterTest");
        suite.addTest(jmri.jmrix.can.adapters.lawicell.LawicellTest.suite());
        suite.addTest(jmri.jmrix.can.adapters.gridconnect.GridConnectTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
