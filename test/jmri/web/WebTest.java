// WebTest.java

package jmri.web;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.web tree
 *
 * @author	    Bob Jacobsen  Copyright 2008
 * @version         $Revision: 1.1 $
 */
public class WebTest extends TestCase {

    // from here down is testing infrastructure
    public WebTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {WebTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.web.WebTest");   // no tests in this class itself
        suite.addTest(jmri.web.miniserver.MiniServerTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
