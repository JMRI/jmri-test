// XNTCPTest.java
package jmri.jmrix.lenz.liusbethernet;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.liusbethernet package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LIUSBEthernetTest extends NbTestCase {

    // from here down is testing infrastructure
    public LIUSBEthernetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBEthernetTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.liusbethernet.LIUSBEthernetTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LIUSBEthernetAdapterTest.class));
        suite.addTest(new NbTestSuite(LIUSBEthernetXNetPacketizerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LIUSBEthernetTest.class.getName());

}
