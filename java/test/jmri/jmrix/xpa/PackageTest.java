// PackageTest.java
package jmri.jmrix.xpa;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.xpa package
 *
 * @author	Paul Bender
 * @version	$Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.xpa.XpaTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(XpaMessageTest.class));
        suite.addTest(new NbTestSuite(XpaTurnoutTest.class));
        suite.addTest(new NbTestSuite(XpaThrottleTest.class));
        //suite.addTest(new NbTestSuite(XpaPacketizerTest.class));
        //suite.addTest(new NbTestSuite(jmri.jmrix.xpa.packetgen.PacketGenFrameTest.class));
        suite.addTest(new NbTestSuite(XpaTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(XpaPowerManagerTest.class));
        suite.addTest(new NbTestSuite(XpaThrottleManagerTest.class));
        //suite.addTest(new NbTestSuite(XpaTrafficControllerTest.class));
        //suite.addTest(new NbTestSuite(XpaTrafficRouterTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
