// LI101Test.java
package jmri.jmrix.lenz.li101;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.li101 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LI101Test extends NbTestCase {

    // from here down is testing infrastructure
    public LI101Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI101Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.li101.LI101Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LI101AdapterTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LI101Test.class.getName());

}
