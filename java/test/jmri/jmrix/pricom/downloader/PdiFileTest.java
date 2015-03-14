// PdiFileTest.java
package jmri.jmrix.pricom.downloader;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the PdiFile class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class PdiFileTest extends NbTestCase {

    public void testCreate() {
        new PdiFile(null);
    }

    // create and show, with some data present
    public void testOpen() {
        PdiFile f = new PdiFile(null);
        Assert.assertNotNull("exists", f);
    }

    // from here down is testing infrastructure
    public PdiFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PdiFileTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PdiFileTest.class);
        return suite;
    }

}
