package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XpaThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaThrottleManager class
 *
 * @author	Paul Bender
 * @version $Revision: 17977 $
 */
public class XpaThrottleManagerTest extends NbTestCase {

    public void testCtor() {
        XpaThrottleManager t = new XpaThrottleManager();
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaThrottleManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XpaThrottleManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(XpaThrottleManagerTest.class.getName());

}
