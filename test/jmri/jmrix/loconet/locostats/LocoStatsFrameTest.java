package jmri.jmrix.loconet.locostats;

import junit.framework.*;

import jmri.jmrix.loconet.swing.*;
import jmri.jmrix.loconet.*;

import jmri.util.JmriJFrame;

/**
 * Tests for the LocoStatsFrame class
 * @author	Bob Jacobsen Copyright (C) 2006, 2008, 2010
 * @version     $Revision: 1.4 $
 */
public class LocoStatsFrameTest extends TestCase {

    LocoStatsPanel getFrame(String title, int offset) throws Exception {
        JmriJFrame f = new JmriJFrame();
        LocoStatsPanel p = new LocoStatsPanel() {
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        p.initComponents();
        f.getContentPane().add(p);
        f.setTitle(title);
        f.setLocation(0, offset);
        f.pack();
        f.setVisible(true);
        return p;
    }

    public void testDefaultFormat() throws Exception {
        LocoStatsPanel p = getFrame("Default LocoStats Window",0);
    }
    
    public void testLocoBufferFormat()throws Exception {
        LocoStatsPanel p = getFrame("LocoBuffer Stats Window", 150);
        p.requestUpdate();
        p.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
    }
    
    public void testPR2Format() throws Exception {
        LocoStatsPanel p = getFrame("PR2 Stats Window", 300);
        p.requestUpdate();
        p.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01, 
                        0x00, 1, 2, 0, 4, 
                        0x00, 5, 6, 0, 0, 
                      0}
        ));
    }
    
    public void testMS100Format() throws Exception {
        LocoStatsPanel p = getFrame("MS100 Stats Window", 450);
        p.requestUpdate();
        p.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01, 
                        0x00, 1, 2, 0x20, 4, 
                        0x00, 5, 6, 0, 0, 
                      0}
        ));
    }
    
    // from here down is testing infrastructure

    public LocoStatsFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoStatsFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoStatsFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoStatsFrameTest.class.getName());
}
