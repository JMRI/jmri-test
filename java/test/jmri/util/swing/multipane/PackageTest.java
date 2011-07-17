// PackageTest.java

package jmri.util.swing.multipane;

import junit.framework.*;
import java.io.File;
import javax.swing.*;

import jmri.util.swing.*;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	    Bob Jacobsen  Copyright 2003
 * @version         $Revision: 1.2 $
 */
public class PackageTest extends TestCase {

    public void testShow() {
        new MultiPaneWindow("Test of empty Multi Pane Window", 
                new File("xml/config/apps/panelpro/Gui3LeftTree.xml"), 
    	        new File("xml/config/apps/panelpro/Gui3Menus.xml"), 
    	        new File("xml/config/apps/panelpro/Gui3MainToolBar.xml")
        ).setVisible(true);
    }
    
    public void testAction() {
        MultiPaneWindow m =         new MultiPaneWindow("Test of Multi Pane Window function", 
                new File("xml/config/apps/panelpro/Gui3LeftTree.xml"), 
    	        new File("xml/config/apps/panelpro/Gui3Menus.xml"), 
    	        new File("xml/config/apps/panelpro/Gui3MainToolBar.xml")
        );
        {
            JButton b = new JButton(new ButtonTestAction(
                                      "Open new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
            m.getUpperRight().add(b);
        }
        {
            JButton b = new JButton(new ButtonTestAction(
                                      "Open in upper panel", new PanedInterface(m)));
            m.getLowerRight().add(b);

            b = new JButton(new jmri.jmrit.powerpanel.PowerPanelAction(
                                      "power", new PanedInterface(m)));
            m.getLowerRight().add(b);            
        }
        m.setVisible(true);
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
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class); 

        suite.addTest(MultiJfcUnitTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
