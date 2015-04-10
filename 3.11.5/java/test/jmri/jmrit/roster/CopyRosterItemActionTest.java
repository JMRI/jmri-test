package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.RosterEntryPane class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class CopyRosterItemActionTest extends TestCase {

    /**
     * Really just checks that the thing can init; doesn't really copy the file,
     * etc. Should do that some day!
     *
     * @throws IOException
     */
    public void testCopy() { //throws java.io.IOException, java.io.FileNotFoundException
        // create a special roster
        //Roster r = RosterTest.createTestRoster();
        // make that the default; not that test roster uses special name
        Roster.resetInstance();
        Roster.instance();

        // copy the item
        CopyRosterItemAction a = new CopyRosterItemAction("copy", new javax.swing.JFrame()) {
            /**
             *
             */
            private static final long serialVersionUID = -3247630279571447763L;

            protected boolean selectFrom() {
                return false;  // aborts operation
            }
        };
        a.actionPerformed(null);
    }

    // from here down is testing infrastructure
    public CopyRosterItemActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CopyRosterItemActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CopyRosterItemActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
