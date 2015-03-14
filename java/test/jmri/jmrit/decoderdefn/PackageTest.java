// PackageTest.java
package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the jmrit.decoderdefn package
 *
 * @author	Bob Jacobsen
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
        NbTestSuite suite = new NbTestSuite("jmri.jmrit.decoderdefn");
        suite.addTest(IdentifyDecoderTest.suite());
        suite.addTest(DecoderIndexFileTest.suite());
        suite.addTest(DecoderFileTest.suite());
        suite.addTest(SchemaTest.suite());
        return suite;
    }

}
