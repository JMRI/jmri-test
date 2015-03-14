/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.acela;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela package
 *
 * @author	Bob Coleman
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.acela.AcelaTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(AcelaNodeTest.class));
//        suite.addTest(new NbTestSuite(AcelaLightManagerTest.class));
//        suite.addTest(new NbTestSuite(AcelaLightTest.class));
        suite.addTest(new NbTestSuite(AcelaTurnoutManagerTest.class));
        suite.addTest(new NbTestSuite(AcelaTurnoutTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PackageTest.class.getName());
}
