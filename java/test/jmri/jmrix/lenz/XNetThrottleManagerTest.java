package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNetThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetThrottleManager class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XNetThrottleManagerTest extends NbTestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetThrottleManager c = new XNetThrottleManager(new XNetSystemConnectionMemo(tc));

        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public XNetThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetThrottleManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XNetThrottleManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(XNetThrottleManagerTest.class.getName());

}
