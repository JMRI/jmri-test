/**
 * LocoIOFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.locoio.LocoIOFrame class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.loconet.locoio;

import java.io.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

public class LocoIOFrameTest extends TestCase {

	public void testFrameCreate() {
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		new LocoIOFrame();
	}

	public void testDispose() {
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		LocoIOFrame f = new LocoIOFrame();
        f.dispose();
	}

    public void testReadAll() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LocoIOFrame f = new LocoIOFrame();

        // click button
        f.readAllButton.doClick();

        // check first message of ReadAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 51 1 0 2 4 0 0 10 0 0 0 0 0 ", msg.toString());

        // dispose and end operation
        f.dispose();
    }

    public void testAddrField() {
    // make sure that the address field does a notify
    // and new address is used
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

        LocoIOFrame f = new LocoIOFrame();

        f.addrField.setText("1234");
        f.addrField.postActionEvent();

        // click button
        f.readAllButton.doClick();

        // check first message of readAll
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 34 12 0 2 4 0 0 10 0 0 0 0 0 ", msg.toString());

        // dispose and end operation
        f.dispose();
    }

	// from here down is testing infrastructure

	public LocoIOFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoIOFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoIOFrameTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOFrameTest.class.getName());

}
