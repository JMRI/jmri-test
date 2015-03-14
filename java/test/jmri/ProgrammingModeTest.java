// ProgrammingModeTest.java
package jmri;

import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the ProgrammingMode class
 *
 * @author Bob Jacobsen Copyright (C) 2014
 * @version $Revision$
 */
public class ProgrammingModeTest extends NbTestCase {

    public void testStateCtors() {
        // tests that statics exist, are not equal
        Assert.assertTrue(DefaultProgrammerManager.NONE.equals(DefaultProgrammerManager.NONE));
        Assert.assertTrue(!DefaultProgrammerManager.NONE.equals(DefaultProgrammerManager.PAGEMODE));
    }

    // from here down is testing infrastructure
    public ProgrammingModeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProgrammingModeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ProgrammingModeTest.class);
        return suite;
    }

}
