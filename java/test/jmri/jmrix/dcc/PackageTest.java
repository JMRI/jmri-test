package jmri.jmrix.dcc;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.dcc package
 *
 * @author	Bob Jacobsen
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.dcc.PackageTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(DccTurnoutTest.class));
        suite.addTest(new NbTestSuite(DccTurnoutManagerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());
}
