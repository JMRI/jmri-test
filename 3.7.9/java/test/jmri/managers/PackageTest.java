// PackageTest.java

package jmri.managers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the jmri.managers
 * @author	Bob Jacobsen, Copyright (C) 2009
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", PackageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.managers.ManagersTest");  // no tests in this class itself
		suite.addTest(InternalLightManagerTest.suite());
		suite.addTest(DefaultSignalSystemManagerTest.suite());
		suite.addTest(DefaultUserMessagePreferencesTest.suite());
		suite.addTest(ProxyLightManagerTest.suite());
		suite.addTest(ProxySensorManagerTest.suite());
		suite.addTest(ProxyTurnoutManagerTest.suite());
		suite.addTest(jmri.managers.configurexml.PackageTest.suite());
                suite.addTest(DefaultIdTagManagerTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
