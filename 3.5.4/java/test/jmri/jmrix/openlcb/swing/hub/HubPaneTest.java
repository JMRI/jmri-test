// HubPaneTest.java

package jmri.jmrix.openlcb.swing.hub;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.can.*;
/**
 *
 * @author      Bob Jacobsen  Copyright 2013
 * @version   $Revision$
 */
public class HubPaneTest extends TestCase {
    HubPane hub;
    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;
    
    public void testCtor() {
        hub = new HubPane();
        Assert.assertNotNull("Connection memo object non-null", memo);
        hub.initContext(memo);
    }
    
    // from here down is testing infrastructure

    public HubPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", HubPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(HubPaneTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { 
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp(); 
        
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        Assert.assertNotNull("Connection memo object non-null", memo);
        tc = new jmri.jmrix.can.adapters.loopback.LoopbackTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        
    }
    protected void tearDown() { 
        hub.stopHubThread();
        apps.tests.Log4JFixture.tearDown(); 
    }
}
