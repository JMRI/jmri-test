// PackageTest.java
package jmri.jmrix.loconet.soundloader;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.soundloader package
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 */
public class PackageTest extends NbTestCase {

    public void testCreate() {
        return;
    }

    public void testRead() {
        return;
    }

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
        NbTestSuite suite = new NbTestSuite(PackageTest.class);
        suite.addTest(LoaderEngineTest.suite());
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());

}
