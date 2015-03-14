// PackageTest.java
package jmri.jmrix.ieee802154.xbee;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.ieee802154.xbee package
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.ieee802154.xbee.XBeeTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(XBeeMessageTest.class));
        suite.addTest(new NbTestSuite(XBeeReplyTest.class));
        suite.addTest(new NbTestSuite(XBeeConnectionMemoTest.class));
        suite.addTest(new NbTestSuite(XBeeTrafficControllerTest.class));
        suite.addTest(new NbTestSuite(XBeeNodeTest.class));
        suite.addTest(new NbTestSuite(XBeeSensorManagerTest.class));
        suite.addTest(new NbTestSuite(XBeeSensorTest.class));
        suite.addTest(new NbTestSuite(XBeeLightManagerTest.class));
        suite.addTest(new NbTestSuite(XBeeLightTest.class));
        suite.addTest(new NbTestSuite(XBeeTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(XBeeTurnoutTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
