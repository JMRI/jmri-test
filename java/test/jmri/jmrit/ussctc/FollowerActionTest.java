// FollowerActionTest.java
package jmri.jmrit.ussctc;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for classes in the jmri.jmrit.ussctc.FollowerAction class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @version	$Revision$
 */
public class FollowerActionTest extends NbTestCase {

    public void testFrameCreate() {
        new FollowerAction("test");
    }

    public void testActionCreateAndFire() {
        new FollowerAction("test").actionPerformed(null);
    }

    // from here down is testing infrastructure
    public FollowerActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FollowerActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FollowerActionTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(FollowerActionTest.class.getName());

}
