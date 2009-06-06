// EditorPaneTest.java
package jmri.jmrix.loconet.soundloader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests for the jmri.jmrix.loconet.soundloader.EditorPane class.
 *
 * @author			Bob Jacobsen  Copyright 2001, 2002, 2006
 * @version         $Revision: 1.6 $
 */
public class EditorPaneTest extends TestCase {

    public void testShowPane() {
        new EditorFrame().setVisible(true);
    }

    // from here down is testing infrastructure

    public EditorPaneTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EditorPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EditorPaneTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorPaneTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
