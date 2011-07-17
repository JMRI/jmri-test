package jmri.jmrix.loconet.locostats;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locostats package.
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 * @version     $Revision: 1.2 $
 */
public class LocoStatsTest extends TestCase {

    // from here down is testing infrastructure

    public LocoStatsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoStatsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.locostats.LocoStatsTest");  // no tests in this class itself
        suite.addTest(LocoStatsFrameTest.suite());
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoStatsTest.class.getName());

}
