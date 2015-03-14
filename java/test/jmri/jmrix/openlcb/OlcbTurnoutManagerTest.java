// OlcbTurnoutManagerTest.java
package jmri.jmrix.openlcb;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 * @version $Revision$
 */
public class OlcbTurnoutManagerTest extends NbTestCase {

    public void testDummy() {
    }

    // from here down is testing infrastructure
    public OlcbTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OlcbTurnoutManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(OlcbTurnoutManagerTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(OlcbTurnoutManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
