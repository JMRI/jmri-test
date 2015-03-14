// DccConsistTest.java
package jmri;

import jmri.implementation.DccConsist;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of DccConsist
 *
 * @author	Paul Copyright (C) 2011
 * @version	$Revision$
 */
public class DccConsistTest extends NbTestCase {

    public void testCtor() {
        // DccLocoAddress constructor test.
        DccConsist c = new DccConsist(new DccLocoAddress(12, true));
        Assert.assertNotNull(c);
    }

    public void testCtor2() {
        // integer constructor test.
        DccConsist c = new DccConsist(new DccLocoAddress(12, true));
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public DccConsistTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DccConsistTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite(DccConsistTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(DccConsistTest.class.getName());

}
