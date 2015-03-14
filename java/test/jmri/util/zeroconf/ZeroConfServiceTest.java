// ZeroConfServiceTest.java
package jmri.util.zeroconf;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class ZeroConfServiceTest extends NbTestCase {

    public void testCreate() {
        ZeroConfService zcs = ZeroConfService.create("_http._tcp.local.", 12345);
        Assert.assertNotNull(zcs);
    }

    // from here down is testing infrastructure
    public ZeroConfServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ZeroConfServiceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ZeroConfServiceTest.class);
        return suite;
    }

}
