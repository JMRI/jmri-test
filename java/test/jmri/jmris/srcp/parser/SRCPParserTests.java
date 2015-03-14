//SRCPParserTests.java
package jmri.jmris.srcp.parser;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.srcp.parser package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SRCPParserTests extends NbTestCase {

    // from here down is testing infrastructure
    public SRCPParserTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPParserTests.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmris.srcp.SRCPParserTests");  // no tests in this class itself
        suite.addTest(new NbTestSuite(SRCPTokenizerTest.class));
        suite.addTest(new NbTestSuite(SRCPParserTest.class));
        suite.addTest(new NbTestSuite(SRCPVisitorTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SRCPParserTests.class.getName());

}
