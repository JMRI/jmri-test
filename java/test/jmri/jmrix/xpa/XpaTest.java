// XpaTest.java


package jmri.jmrix.xpa;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.xpa package
 * @author			Paul Bender
 * @version			$Revision$
 */
public class XpaTest extends TestCase {

    // from here down is testing infrastructure

    public XpaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XpaTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.xpa.XpaTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XpaMessageTest.class));
        //suite.addTest(new TestSuite(XpaTurnoutTest.class));
        //suite.addTest(new TestSuite(XpaPacketizerTest.class));
        //suite.addTest(new TestSuite(jmri.jmrix.xpa.packetgen.PacketGenFrameTest.class));
        //suite.addTest(new TestSuite(XpaTurnoutManagerTest.class));
        //suite.addTest(new TestSuite(XpaTrafficControllerTest.class));
        //suite.addTest(new TestSuite(XpaTrafficRouterTest.class));
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XpaTest.class.getName());

}
