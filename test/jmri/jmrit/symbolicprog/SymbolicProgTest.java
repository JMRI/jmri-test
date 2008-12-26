// SymbolicProgTest.java

package jmri.jmrit.symbolicprog;

import jmri.DefaultProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;

/**
 * Test the jmri.jmrix.symbolicprog package.
 * @author			Bob Jacobsen Copyright 2006
 * @version         $Revision: 1.15 $
 */
public class SymbolicProgTest extends TestCase {

    // check configuring the programmer
    public void testConfigProgrammer() {
        // initialize the system
        Programmer p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        assertTrue(InstanceManager.programmerManagerInstance().getGlobalProgrammer() == p);
    }

    // from here down is testing infrastructure

    public SymbolicProgTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SymbolicProgTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite(SymbolicProgTest.class);
        suite.addTest(jmri.jmrit.symbolicprog.CompositeVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.Pr1ImporterTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ComboCheckBoxTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ComboRadioButtonsTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.DecVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.EnumVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.HexVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.LongAddrVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.SplitVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CvValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CvTableModelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.VariableTableModelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CombinedLocoSelListPaneTest.suite());

        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.CheckProgrammerNames.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
