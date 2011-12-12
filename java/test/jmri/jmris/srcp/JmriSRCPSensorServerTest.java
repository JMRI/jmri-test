//JmriSRCPSensorServerTest.java

package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPSensorServer class
 * @author                      Paul Bender
 * @version                     $Revision$
 */
public class JmriSRCPSensorServerTest extends TestCase {

    public void testCtor() {
	java.io.DataOutputStream output=new java.io.DataOutputStream(System.out);
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        JmriSRCPSensorServer a = new JmriSRCPSensorServer(input,output);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure

    public JmriSRCPSensorServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.srcp.JmriSRCPSensorServerTest.class);

        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriSRCPSensorServerTest.class.getName());

}

