package jmri.managers;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.managers.DefaultUserMessagePreferencesTest class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class DefaultUserMessagePreferencesTest extends NbTestCase {

    public void testSetGet() {
        DefaultUserMessagePreferences d = new DefaultUserMessagePreferences() {
            public void displayRememberMsg() {
            }
        };
        jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");

        Assert.assertTrue(!d.getSimplePreferenceState("one"));

        d.setSimplePreferenceState("one", true);
        Assert.assertTrue(d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

        d.setSimplePreferenceState("one", false);
        Assert.assertTrue(!d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

    }

    // from here down is testing infrastructure
    public DefaultUserMessagePreferencesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultUserMessagePreferencesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(DefaultUserMessagePreferencesTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        System.setProperty("org.jmri.Apps.configFilename", "jmriconfig2.xml");
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(DefaultUserMessagePreferencesTest.class.getName());

}
