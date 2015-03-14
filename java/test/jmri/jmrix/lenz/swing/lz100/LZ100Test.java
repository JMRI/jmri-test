// LZ100Test.java
package jmri.jmrix.lenz.swing.lz100;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.lz100 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LZ100Test extends NbTestCase {

    // from here down is testing infrastructure
    public LZ100Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LZ100Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.swing.lz100.LZ100Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LZ100FrameTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LZ100Test.class.getName());

}
