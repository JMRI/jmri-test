// XNetSimulatorTest.java
package jmri.jmrix.lenz.xnetsimulator;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.xnetsimulator package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class XNetSimulatorTest extends NbTestCase {

    // from here down is testing infrastructure
    public XNetSimulatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XNetSimulatorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(XNetSimulatorAdapterTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(XNetSimulatorTest.class.getName());

}
