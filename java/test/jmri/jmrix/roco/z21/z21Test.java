// z21Test.java
package jmri.jmrix.roco.z21;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.roco.z21 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class z21Test extends NbTestCase {

    // from here down is testing infrastructure
    public z21Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {z21Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.roco.z21.z21Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(z21AdapterTest.class));
        suite.addTest(new NbTestSuite(z21MessageTest.class));
        suite.addTest(new NbTestSuite(z21ReplyTest.class));
        suite.addTest(new NbTestSuite(z21TrafficControllerTest.class));
        suite.addTest(new NbTestSuite(z21SystemConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(z21XPressNetTunnelTest.class));
        suite.addTest(new NbTestSuite(z21XNetProgrammerTest.class));
        suite.addTest(new TestSuite(z21XNetThrottleManagerTest.class));
        suite.addTest(new TestSuite(z21XNetThrottleTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(z21Test.class.getName());

}
