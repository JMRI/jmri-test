/**
 * PacketGenFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.qsi.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
package jmri.jmrix.qsi.packetgen;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGenFrameTest extends NbTestCase {

    public void testFrameCreate() {
        new PacketGenFrame();
    }

    // from here down is testing infrastructure
    public PacketGenFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PacketGenFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PacketGenFrameTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PacketGenFrameTest.class.getName());

}
