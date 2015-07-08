package jmri.jmrix.roco.z21;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.jmrix.lenz.XNetThrottleTest;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.LenzCommandStation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * z21XNetThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.z21XNetThrottle class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class z21XNetThrottleTest extends XNetThrottleTest {

    static final int RELEASE_TIME = 100;

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        z21XNetThrottle t = new z21XNetThrottle(new XNetSystemConnectionMemo(tc), tc);
        Assert.assertNotNull(t);
    }

    // Test the constructor with an address specified.
    public void testCtorWithArg() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        z21XNetThrottle t = new z21XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public z21XNetThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", z21XNetThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(z21XNetThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(z21XNetThrottleTest.class.getName());

}
