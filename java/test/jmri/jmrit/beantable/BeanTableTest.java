// BeanTableTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 * @author	Bob Jacobsen  Copyright 2004
 * @version	$Revision: 1.11 $
 */
public class BeanTableTest extends TestCase {

    public void testCreate() {
        new MemoryTableAction();
    }

    public void testExecute() {
        new MemoryTableAction().actionPerformed(null);
    }

    // from here down is testing infrastructure

    public BeanTableTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", BeanTableTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BeanTableTest.class);
        suite.addTest(BlockTableActionTest.suite());
		suite.addTest(LogixTableActionTest.suite());
		suite.addTest(LRouteTableActionTest.suite());
        suite.addTest(RouteTableActionTest.suite());
		suite.addTest(SensorTableWindowTest.suite());
        suite.addTest(SignalHeadTableActionTest.suite());
		suite.addTest(TurnoutTableWindowTest.suite());
        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
}
