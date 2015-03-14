// LIUSBServerTest.java
package jmri.jmrix.lenz.liusbserver;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver package
 *
 * @author Paul Bender
 * @version $Revision: 17977 $
 */
public class LIUSBServerTest extends NbTestCase {

    // from here down is testing infrastructure
    public LIUSBServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.liusbserver.LIUSBServerTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LIUSBServerAdapterTest.class));
        suite.addTest(new NbTestSuite(LIUSBServerXNetPacketizerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LIUSBServerTest.class.getName());

}
