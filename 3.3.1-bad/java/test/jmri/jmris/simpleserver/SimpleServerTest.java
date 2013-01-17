//SimpleServerTest.java

package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.simpleserver package
 * @author                      Paul Bender
 * @version                     $Revision$
 */
public class SimpleServerTest extends TestCase {

    public void testCtor() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
    }

    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
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
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.simpleserver.SimpleServerTest.class);
        suite.addTest(jmri.jmris.simpleserver.SimpleTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimplePowerServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleReporterServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleSensorServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleLightServerTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleOperationsServerTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
           // put any tests that require a UI here.
        }

        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleServerTest.class.getName());

}

