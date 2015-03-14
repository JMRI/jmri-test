package jmri.jmrix.modbus.slave;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.modbus.slave package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2014
 * @version $Revision$
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.modbus.slave.PackageTest");  // no tests in this class itself

        // suite.addTest(jmri.jmrix.modbus.slave.FooTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            //suite.addTest(jmri.jmrix.modbus.slave.FooTest.suite());
        }

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
