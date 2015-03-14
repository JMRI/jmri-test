// CbusProgrammerManagerTest.java
package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TestTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusProgrammerManager class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class CbusProgrammerManagerTest extends NbTestCase {

    public void testGlobal() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("no global mode", !m.isGlobalModePossible());
    }

    public void testAddressed() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("addressed mode ok", m.isAddressedModePossible());
    }

    // from here down is testing infrastructure
    public CbusProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CbusProgrammerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CbusProgrammerManagerTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(CbusProgrammerManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
