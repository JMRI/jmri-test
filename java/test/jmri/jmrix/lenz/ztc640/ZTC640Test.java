// ZTC640Test.java
package jmri.jmrix.lenz.ztc640;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.ztc640 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class ZTC640Test extends NbTestCase {

    // from here down is testing infrastructure
    public ZTC640Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ZTC640Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.ztc640.ZTC640Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(ZTC640AdapterTest.class));
        suite.addTest(new NbTestSuite(ZTC640XNetPacketizerTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(ZTC640Test.class.getName());

}
