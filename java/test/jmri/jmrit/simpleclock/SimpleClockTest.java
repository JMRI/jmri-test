// SimpleClockTest.java
package jmri.jmrit.simpleclock;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmrit.simpleclock package
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SimpleClockTest extends NbTestCase {

    // from here down is testing infrastructure
    public SimpleClockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleClockTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.simpleclock.SimpleClockTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.simpleclock.SimpleTimebaseTest.suite());
        return suite;
    }

}
