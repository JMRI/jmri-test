/**
 * EasyDccPacketGenFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.packetgen.EasyDccPacketGenFrame
 * class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
package jmri.jmrix.easydcc.packetgen;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccPacketGenFrameTest extends NbTestCase {

    public void testFrameCreate() {
        new EasyDccPacketGenFrame();
    }

    // from here down is testing infrastructure
    public EasyDccPacketGenFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EasyDccPacketGenFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(EasyDccPacketGenFrameTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(EasyDccPacketGenFrameTest.class.getName());

}
