// ActiveSystemFlagTest.java
package jmri.jmrix;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the XmlFile class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class ActiveSystemFlagTest extends NbTestCase {

    public void testInactive() throws Exception {
        Assert.assertTrue(!ActiveSystemFlag.isActive("jmri.jmrix.direct"));
    }

    public void testActive() throws Exception {
        jmri.jmrix.loconet.ActiveFlag.setActive();
        Assert.assertTrue(ActiveSystemFlag.isActive("jmri.jmrix.loconet"));
    }

    public void testNoSystem() throws Exception {
        try {
            Assert.assertTrue(ActiveSystemFlag.isActive("jmri.foo"));
            Assert.fail("Didn't throw exception");
        } catch (Exception e) {
        }
    }

    // from here down is testing infrastructure
    public ActiveSystemFlagTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ActiveSystemFlagTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ActiveSystemFlagTest.class);
        return suite;
    }

    // protected access for subclass
    static protected Logger log = LoggerFactory.getLogger(ActiveSystemFlagTest.class.getName());

}
