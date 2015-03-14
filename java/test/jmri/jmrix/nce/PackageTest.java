// PackageTest.java
package jmri.jmrix.nce;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * tests for the jmri.jmrix.nce package
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
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.nce.PackageTest");
        suite.addTest(jmri.jmrix.nce.NceTurnoutTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceAIUTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceMessageTest.suite());
        suite.addTest(jmri.jmrix.nce.NceReplyTest.suite());
        suite.addTest(jmri.jmrix.nce.NcePowerManagerTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.nce.ncemon.NceMonPanelTest.suite());
            suite.addTest(jmri.jmrix.nce.packetgen.NcePacketGenPanelTest.suite());
        }

        return suite;
    }

}
