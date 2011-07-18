// PackageTest.java

package jmri.util.swing.mdi;

import junit.framework.*;
import java.io.File;

/**
 *
 * @author	    Bob Jacobsen  Copyright 2003, 2010
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

    public void testShow() {
        MdiMainFrame f = new MdiMainFrame("Test of MDI Frame", 
                new File("java/test/jmri/util/swing/xml/Gui3LeftTree.xml"), 
    	        new File("java/test/jmri/util/swing/xml/Gui3Menus.xml"), 
    	        new File("java/test/jmri/util/swing/xml/Gui3MainToolBar.xml")
        );
        f.setSize(new java.awt.Dimension(400,400));
        f.setVisible(true);
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

        //suite.addTest(MultiJfcUnitTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
        jmri.InstanceManager.setShutDownManager(
                new jmri.managers.DefaultShutDownManager());
        jmri.InstanceManager.store(
                jmri.managers.DefaultUserMessagePreferences.getInstance(),
                jmri.UserPreferencesManager.class);
    }
    
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
