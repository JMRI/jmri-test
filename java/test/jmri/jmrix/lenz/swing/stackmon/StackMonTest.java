// StackMonTest.java
package jmri.jmrix.lenz.swing.stackmon;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class StackMonTest extends NbTestCase {

    // from here down is testing infrastructure
    public StackMonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StackMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.swing.stackmon.StackMonTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(StackMonFrameTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(StackMonTest.class.getName());

}
