// EliteTest.java


package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite package
 * @author                      Paul Bender  
 * @version                     $Revision: 1.2 $
 */
public class EliteTest extends TestCase {

    // from here down is testing infrastructure

    public EliteTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EliteTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.hornbyelite.EliteTest");  // no tests in this class itself
        suite.addTest(new TestSuite(EliteAdapterTest.class));
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteTest.class.getName());

}

