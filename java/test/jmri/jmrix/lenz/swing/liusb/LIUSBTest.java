// LIUSBTest.java
package jmri.jmrix.lenz.swing.liusb;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.liusb package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LIUSBTest extends NbTestCase {

    // from here down is testing infrastructure
    public LIUSBTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.liusb.swing.LIUSBTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LIUSBConfigFrameTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LIUSBTest.class.getName());

}
