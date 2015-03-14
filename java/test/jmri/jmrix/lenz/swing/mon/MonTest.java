// MonTest.java
package jmri.jmrix.lenz.swing.mon;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.mon package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class MonTest extends NbTestCase {

    // from here down is testing infrastructure
    public MonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.swing.mon.MonTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(XNetMonPaneTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(MonTest.class.getName());

}
