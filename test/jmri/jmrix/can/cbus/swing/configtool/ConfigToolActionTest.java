// ConfigToolActionTest.java

package jmri.jmrix.can.cbus.swing.configtool;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.configtool package.
 * @author      Bob Jacobsen  Copyright 2008
 * @version   $Revision: 1.2 $
 */
public class ConfigToolActionTest extends TestCase {

    // from here down is testing infrastructure

    public ConfigToolActionTest(String s) {
        super(s);
    }

    public void testAction() {
        new ConfigToolAction().actionPerformed(null);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConfigToolActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(ConfigToolActionTest.class);
        return suite;
    }

}
