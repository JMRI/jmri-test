// PackageTest.java
package jmri.jmrix.qsi;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.jmrix.qsi package
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.qsi.QsiTest");
        suite.addTest(jmri.jmrix.qsi.QsiTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiMessageTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiReplyTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.qsi.qsimon.QsiMonFrameTest.suite());
            suite.addTest(jmri.jmrix.qsi.packetgen.PacketGenFrameTest.suite());
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
