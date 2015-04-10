// DefaultConditionalActionTest.java
package jmri.implementation;

import jmri.ConditionalAction;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the DefaultConditionalAction implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalActionTest extends TestCase {

    public void testCtor() {
        new DefaultConditionalAction();
    }

    public void testBasicBeanOperations() {
        ConditionalAction ix1 = new DefaultConditionalAction();

        ConditionalAction ix2 = new DefaultConditionalAction();

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());

    }

    // from here down is testing infrastructure
    public DefaultConditionalActionTest(String s) {
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
        String[] testCaseName = {DefaultConditionalActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultConditionalActionTest.class);
        return suite;
    }

}
