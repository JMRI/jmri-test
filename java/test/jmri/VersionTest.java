/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author zoo
 */
public class VersionTest extends NbTestCase {

    public VersionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    public void testIsCanonicalVersion() {

        assertTrue(Version.isCanonicalVersion("1.2.3"));
        assertFalse(Version.isCanonicalVersion("1.2"));

    }

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    public void testCompareCanonicalVersions() {

        assertTrue(Version.compareCanonicalVersions("1.2.3", "1.2.3") == 0);
        assertTrue(Version.compareCanonicalVersions("1.2.1", "1.2.3") < 0);
        assertTrue(Version.compareCanonicalVersions("1.2.4", "1.2.3") > 0);

        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.1") == 0);
        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.10") < 0);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(VersionTest.class);
        return suite;
    }
}
