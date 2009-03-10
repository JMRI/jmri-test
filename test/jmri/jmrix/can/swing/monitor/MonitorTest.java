// MonitorTest.java

package jmri.jmrix.can.swing.monitor;

import jmri.jmrix.can.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.monitor package.
 * @author      Bob Jacobsen  Copyright 2003, 2007, 2008, 2009
 * @version   $Revision: 1.1 $
 */
public class MonitorTest extends TestCase {

    // from here down is testing infrastructure

    public MonitorTest(String s) {
        super(s);
    }

    public void testDisplay() throws Exception {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();

        MonitorFrame f = new MonitorFrame(){
          { rawCheckBox.setSelected(true);}
        };
        f.initComponents();
        f.setVisible(true);
        
        // show std message
        CanMessage m = new CanMessage();
        m.setHeader(0x123);
        m.setNumDataElements(3);
        m.setElement(0, (byte)0x02);
        m.setElement(1, (byte)0xA2);
        m.setElement(2, (byte)0x31);
        
        f.message(m);
        
        // show ext message
        m = new CanMessage();
        m.setExtended(true);
        m.setHeader(0x654321);
        m.setNumDataElements(3);
        m.setElement(0, (byte)0x02);
        m.setElement(1, (byte)0xA2);
        m.setElement(2, (byte)0x31);
        
        f.message(m);
        
        // show reply
        CanReply r = new CanReply();
        r.setNumDataElements(3);
        r.setElement(0, (byte)0x11);
        r.setElement(1, (byte)0x82);
        r.setElement(2, (byte)0x33);
        
        f.reply(r);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonitorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MonitorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
