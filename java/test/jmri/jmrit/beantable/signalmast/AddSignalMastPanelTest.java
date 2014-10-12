// AddSignalMastPanelTest.java

package jmri.jmrit.beantable.signalmast;

import jmri.implementation.SignalSystemTestUtil;

import javax.swing.JFrame;

import java.io.*;

import junit.framework.*;

/**
 * @author	Bob Jacobsen  Copyright 2014
 * @version	$Revision$
 */
public class AddSignalMastPanelTest extends TestCase {

    public void testDefaultSystems() {
        AddSignalMastPanel  a = new AddSignalMastPanel();
        
        jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");
        
        // check that "Basic Model Signals" (basic directory) system is present
        boolean found = false;
        for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
            if (a.sigSysBox.getItemAt(i).equals("Basic Model Signals")) {
                found = true;
            }
        }
        Assert.assertTrue("found Basic Model Signals", found);
    }


    public void testSearch() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();
        
            AddSignalMastPanel  a = new AddSignalMastPanel();
        
            jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");
        
            // check that mock (test) system is present
            boolean found = false;
            for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
                if (a.sigSysBox.getItemAt(i).equals(SignalSystemTestUtil.getMockUserName())) {
                    found = true;
                }
            }
            Assert.assertTrue("found JUnit Test Signals", found);
        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
    }
    
    // from here down is testing infrastructure

    public AddSignalMastPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AddSignalMastPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AddSignalMastPanelTest.class);

        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp(); 

        jmri.util.JUnitUtil.resetInstanceManager(); 
        jmri.util.JUnitUtil.initInternalTurnoutManager(); 
        jmri.util.JUnitUtil.initInternalLightManager(); 
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.managers.DefaultUserMessagePreferences.resetInstance();
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
    }
    protected void tearDown() throws Exception { 
        jmri.util.JUnitUtil.resetInstanceManager(); 

        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }
    
}
