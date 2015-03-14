// PackageTest.java
package jmri.jmrix.ieee802154;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.ieee802154 package
 *
 * @author	Paul Bender
 * @version	$Revision$
 */
public class PackageTest extends NbTestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.ieee802154.IEEE802154Test");  // no tests in this class itself
        suite.addTest(new NbTestSuite(IEEE802154MessageTest.class));
        suite.addTest(new NbTestSuite(IEEE802154SystemConnectionMemoTest.class));
        suite.addTest(jmri.jmrix.ieee802154.xbee.PackageTest.suite());
        suite.addTest(jmri.jmrix.ieee802154.serialdriver.PackageTest.suite());
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
