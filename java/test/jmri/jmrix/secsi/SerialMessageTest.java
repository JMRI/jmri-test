// SerialMessageTest.java
package jmri.jmrix.secsi;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @version	$Revision$
 */
public class SerialMessageTest extends NbTestCase {

    public void testCreate() {
        SerialMessage m = new SerialMessage(4);
        Assert.assertNotNull("exists", m);
    }

    public void testBytesToString() {
        SerialMessage m = new SerialMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    // from here down is testing infrastructure
    public SerialMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SerialMessageTest.class);
        return suite;
    }

}
