// DefaultLogixManagerTest.java
package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmri.managers.DefaultLogixManager class.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixManagerTest extends NbTestCase {

    public void testCtor() {
        new DefaultLogixManager();
    }

    // from here down is testing infrastructure
    public DefaultLogixManagerTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultLogixManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(DefaultLogixManagerTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(DefaultLogixManagerTest.class.getName());

}
