//SimpleTurnoutServerTest.java
package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleTurnoutServer class
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SimpleTurnoutServerTest extends NbTestCase {

    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleTurnoutServer a = new SimpleTurnoutServer(input, output);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public SimpleTurnoutServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleTurnoutServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(jmri.jmris.simpleserver.SimpleTurnoutServerTest.class);

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SimpleTurnoutServerTest.class.getName());

}
