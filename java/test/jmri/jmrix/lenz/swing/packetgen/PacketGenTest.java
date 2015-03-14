// PacketGenTest.java
package jmri.jmrix.lenz.swing.packetgen;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.swing.packetgen.package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class PacketGenTest extends NbTestCase {

    // from here down is testing infrastructure
    public PacketGenTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PacketGenTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("jmri.jmrix.lenz.swing.packetgen.PacketGenTest");  // no tests in this class itself
        suite.addTest(new NbTestSuite(PacketGenFrameTest.class));
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PacketGenTest.class.getName());

}
