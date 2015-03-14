// PackageTest.java
package jmri.jmrit.beantable;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 *
 * @author	Bob Jacobsen Copyright 2004
 * @version	$Revision$
 */
public class PackageTest extends NbTestCase {

    public void testCreate() {
        new MemoryTableAction();
    }

    public void testExecute() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        new MemoryTableAction().actionPerformed(null);
    }

    public void testXCreation() {
        JFrame f = jmri.util.JmriJFrame.getFrame("Memory Table");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PackageTest.class);
        suite.addTest(BlockTableActionTest.suite());
        suite.addTest(LogixTableActionTest.suite());
        suite.addTest(LRouteTableActionTest.suite());
        suite.addTest(RouteTableActionTest.suite());
        suite.addTest(SensorTableWindowTest.suite());
        suite.addTest(SignalHeadTableActionTest.suite());
        suite.addTest(TurnoutTableWindowTest.suite());

        suite.addTest(jmri.jmrit.beantable.signalmast.PackageTest.suite());
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
