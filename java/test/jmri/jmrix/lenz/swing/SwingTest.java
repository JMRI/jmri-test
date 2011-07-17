// SwingTest.java

package jmri.jmrix.lenz.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing package
 * @author			Bob Jacobsen
 * @version			$Revision: 1.3 $
 */
public class SwingTest extends TestCase {

    // from here down is testing infrastructure

    public SwingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.SwingTest");  // no tests in this class itself
        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.lenz.swing.liusb.LIUSBTest.suite());
            suite.addTest(jmri.jmrix.lenz.swing.li101.LI101Test.suite());
            suite.addTest(jmri.jmrix.lenz.swing.mon.MonTest.suite());
            suite.addTest(jmri.jmrix.lenz.swing.stackmon.StackMonTest.suite());
            suite.addTest(jmri.jmrix.lenz.swing.systeminfo.SystemInfoTest.suite());
            suite.addTest(jmri.jmrix.lenz.swing.packetgen.PacketGenTest.suite());
        }
        
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SwingTest.class.getName());

}
