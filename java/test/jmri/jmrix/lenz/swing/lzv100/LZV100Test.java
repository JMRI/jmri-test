// LZV100Test.java
package jmri.jmrix.lenz.swing.lzv100;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.lzv100 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LZV100Test extends NbTestCase {

    // from here down is testing infrastructure
    public LZV100Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LZV100Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.swing.lzv100.LZV100Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(LZV100FrameTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(LZV100Test.class.getName());

}
