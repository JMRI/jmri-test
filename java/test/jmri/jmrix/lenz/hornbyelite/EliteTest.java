// EliteTest.java
package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class EliteTest extends NbTestCase {

    // from here down is testing infrastructure
    public EliteTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EliteTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.hornbyelite.EliteTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(HornbyEliteCommandStationTest.class));
        suite.addTest(new NbTestSuite(EliteAdapterTest.class));
        suite.addTest(new NbTestSuite(EliteConnectionTypeListTest.class));
        suite.addTest(new NbTestSuite(EliteXNetInitializationManagerTest.class));
        suite.addTest(new NbTestSuite(EliteXNetThrottleManagerTest.class));
        suite.addTest(new NbTestSuite(EliteXNetThrottleTest.class));
        suite.addTest(new NbTestSuite(EliteXNetTurnoutTest.class));
        suite.addTest(new NbTestSuite(EliteXNetTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(EliteXNetProgrammerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(EliteTest.class.getName());

}
