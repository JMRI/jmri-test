// StoppingBlockTest.java
package jmri.jmrit.tracker;

import jmri.Block;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the StoppingBlock class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 */
public class StoppingBlockTest extends NbTestCase {

    public void testDirectCreate() {
        // check for exception in ctor
        new StoppingBlock(new Block("dummy"));
    }

    // from here down is testing infrastructure
    public StoppingBlockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StoppingBlockTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(StoppingBlockTest.class);
        return suite;
    }

}
