// XNTCPTest.java
package jmri.jmrix.lenz.xntcp;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.xntcp package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class XnTcpTest extends NbTestCase {

    // from here down is testing infrastructure
    public XnTcpTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XnTcpTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.xntcp.XnTcpTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(XnTcpAdapterTest.class));
        suite.addTest(new NbTestSuite(XnTcpXNetPacketizerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(XnTcpTest.class.getName());

}
