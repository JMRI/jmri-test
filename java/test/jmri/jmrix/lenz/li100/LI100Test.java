// LI100Test.java
package jmri.jmrix.lenz.li100;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.li100 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LI100Test extends NbTestCase {

    // from here down is testing infrastructure
    public LI100Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI100Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.li100.LI100Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LI100AdapterTest.class));
        suite.addTest(new NbTestSuite(LI100XNetInitializationManagerTest.class));
        suite.addTest(new NbTestSuite(LI100XNetProgrammerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LI100Test.class.getName());

}
