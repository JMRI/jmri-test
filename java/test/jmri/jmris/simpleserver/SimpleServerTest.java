//SimpleServerTest.java
package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.simpleserver package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SimpleServerTest extends NbTestCase {

    public void testCtor() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
    }

    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
        jmri.util.JUnitAppender.assertErrorMessage("Failed to connect to port 2048");
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public SimpleServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(jmri.jmris.simpleserver.SimpleServerTest.class);
        suite.addTest(jmri.jmris.simpleserver.SimpleTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimplePowerServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleReporterServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleSensorServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleLightServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleOperationsServerTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(SimpleServerTest.class.getName());

}
