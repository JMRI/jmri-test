//SimpleOperationsServerTest.java
package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleOperationsServer class
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SimpleOperationsServerTest extends TestCase {

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
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public SimpleOperationsServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleOperationsServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.simpleserver.SimpleOperationsServerTest.class);

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SimpleOperationsServerTest.class.getName());

}
