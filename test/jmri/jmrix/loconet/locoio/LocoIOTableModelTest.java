// LocoIOTableModelTest.java

package jmri.jmrix.loconet.locoio;

import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

/**
 * LocoIOTableModelTest.java
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version
 */
public class LocoIOTableModelTest extends TestCase {

	public void testObjectCreate() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);
	}

	// LnSensor test for incoming status message
	public void testLnSensorStatusMsg() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LnSensor t = new LnSensor("LS043");

		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb2);         // OPC_INPUT_REP
		m.setElement(1, 0x15);     // all but lowest bit of address
		m.setElement(2, 0x60);     // Aux (low addr bit high), sensor high
		m.setElement(3, 0x38);
		lnis.sendTestMessage(m);
		assert(t.getKnownState() == jmri.Sensor.ACTIVE);

		m = new LocoNetMessage(4);
		m.setOpCode(0xb2);         // OPC_INPUT_REP
		m.setElement(1, 0x15);     // all but lowest bit of address
		m.setElement(2, 0x40);     // Aux (low addr bit high), sensor low
		m.setElement(3, 0x18);
		lnis.sendTestMessage(m);
		assert(t.getKnownState() == jmri.Sensor.INACTIVE);

	}

    // test mapping from cv mode values to strings
    public void testModeFromValues() {
		LocoIOTableModel m = new LocoIOTableModel(0x1081);

        assertEquals("0x0F toggle switch", "Toggle switch", m.modeFromValues(0x0F, 0x101C));
        assertEquals("0x2F push low", "Pushbutton active low", m.modeFromValues(0x2F, 0x101C));
        assertEquals("0x6F push high", "Pushbutton active high", m.modeFromValues(0x6F, 0x101C));
        assertEquals("0x80 throw", "Turnout thrown command", m.modeFromValues(0x80, 0x101C));
        assertEquals("0x80 close", "Turnout closed command", m.modeFromValues(0x80, 0x301C));
        assertEquals("0xC0 push high", "Status message", m.modeFromValues(0xC0, 0x20CC));
    }

	// test read from toggle
	public void testReadOperationToggle() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

        int channel = 2;
		m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN);

        read3Sequence(channel, 0x0F, 0x1C, 0x10,lnis );

        Assert.assertEquals("mode", "Toggle switch",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));
	}

	// test read from pushbutton low
	public void testReadOperationPushLow() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

        int channel = 5;
		m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN);

        read3Sequence(channel, 0x2F, 0x1C, 0x10,lnis );

        Assert.assertEquals("mode", "Pushbutton active low",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));
	}

	// test read from pushbutton high
	public void testReadOperationPushHigh() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

        int channel = 5;
		m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN);

        read3Sequence(channel, 0x6F, 0x1C, 0x10,lnis );

        Assert.assertEquals("mode", "Pushbutton active high",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));
	}

    /**
     * Service routine, runs through the sequence for a read operation,
     * returning each of the three bytes given as arguments.
     * @param channel The channel being read
     * @param cv value returned for the configuration CV read
     * @param addrlow value returned for the low address read
     * @param addrhigh value returned for the high address read
     * @param lnis Test interface for loconet i/o
     */
    void read3Sequence(int channel, int cv, int addrlow, int addrhigh,
                        LocoNetInterfaceScaffold lnis ) {
        int src;
        int dst;
        // check transmitted message
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        // read low addr
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 2 "
                            +Integer.toHexString(channel*3+4)+" 0 0 10 0 0 0 0 0 ", msg.toString());

        // turn that message around as the echo
 		lnis.sendTestMessage(msg);
        Assert.assertEquals("listener present", 1, lnis.numListeners());
        Assert.assertEquals("echo ignored", 1, lnis.outbound.size());

        // turn around as the reply to the read low
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,addrlow); // low addr
 		lnis.sendTestMessage(msg);

        // 2nd read
        Assert.assertEquals("reply does 2nd read", 2, lnis.outbound.size());
        msg = (LocoNetMessage)lnis.outbound.get(1);
        // CV11 for read high address
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 2 "
                            +Integer.toHexString(channel*3+5)+" 0 0 10 0 0 0 0 0 ", msg.toString());

        // turn around as the reply to the read high
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,addrhigh);
 		lnis.sendTestMessage(msg);

        // 3rd read
        Assert.assertEquals("reply does 3rd read", 3, lnis.outbound.size());
        msg = (LocoNetMessage)lnis.outbound.get(2);
        // channel 2 (above) is CV 9 for read mode
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 2 "
                            +Integer.toHexString(channel*3+3)+" 0 0 10 0 0 0 0 0 ", msg.toString());

        // turnaround as the reply to the mode read
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,cv); // Toggle switch
 		lnis.sendTestMessage(msg);

        Assert.assertEquals("reply does no more messages", 3, lnis.outbound.size());
    }

    // test setting of mode, and its effect on address
    public void testSetOnMode() {
		LocoIOTableModel m = new LocoIOTableModel(0x1081);
        int channel = 12;
        m.setValueAt("101c", channel, LocoIOTableModel.ADDRCOLUMN);
        m.setValueAt("Turnout closed command", channel, LocoIOTableModel.ONMODECOLUMN);
        Assert.assertEquals("mode", "Turnout closed command",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "301c", m.getValueAt(channel, m.ADDRCOLUMN));
        m.setValueAt("Turnout thrown command", channel, LocoIOTableModel.ONMODECOLUMN);
        Assert.assertEquals("mode", "Turnout thrown command",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));
    }

    // test setting of address, and its effect on mode
    public void testSetAddr() {
		LocoIOTableModel m = new LocoIOTableModel(0x1081);
        int channel = 12;
        m.setValueAt("Turnout closed command", channel, LocoIOTableModel.ONMODECOLUMN);
        m.setValueAt("101c", channel, LocoIOTableModel.ADDRCOLUMN);
        Assert.assertEquals("mode", "Turnout thrown command",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));

        m.setValueAt("301c", channel, LocoIOTableModel.ADDRCOLUMN);
        Assert.assertEquals("mode", "Turnout closed command",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "301c", m.getValueAt(channel, m.ADDRCOLUMN));
    }

	// test write from pushbutton high
	public void testWriteOperationPushHigh() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

        int channel = 5;
        m.setValueAt("101c", channel, LocoIOTableModel.ADDRCOLUMN);
        m.setValueAt("Pushbutton active high", channel, LocoIOTableModel.ONMODECOLUMN);
		m.setValueAt(null, channel, LocoIOTableModel.WRITECOLUMN);

        write3Sequence(channel, 0x6F, 0x1C, 0x10,lnis );

        Assert.assertEquals("mode", "Pushbutton active high",
                            m.getValueAt(channel, m.ONMODECOLUMN));
        Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN));
	}

    /**
     * Service routine, runs through the sequence for a write operation,
     * checking each of the three bytes given as arguments.
     * @param channel The channel being read
     * @param cv value value expected for configuration CV read
     * @param addrlow value expected for the low address read
     * @param addrhigh value expected for the high address read
     * @param lnis Test interface for loconet i/o
     */
    void write3Sequence(int channel, int cv, int addrlow, int addrhigh,
                        LocoNetInterfaceScaffold lnis ) {
        int src;
        int dst;
        // check transmitted message
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        // write low addr
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 1 "
                            +Integer.toHexString(channel*3+4)+" 0 "
                            +Integer.toHexString(addrlow)+" 10 0 0 0 0 0 ", msg.toString());

        // turn that message around as the echo
 		lnis.sendTestMessage(msg);
        Assert.assertEquals("listener present", 1, lnis.numListeners());
        Assert.assertEquals("echo ignored", 1, lnis.outbound.size());

        // turn around as the reply to the read low
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,addrlow); // low addr
 		lnis.sendTestMessage(msg);

        // 2nd read
        Assert.assertEquals("reply does 2nd read", 2, lnis.outbound.size());
        msg = (LocoNetMessage)lnis.outbound.get(1);
        // CV11 for read high address
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 1 "
                            +Integer.toHexString(channel*3+5)+" 0 "
                            +Integer.toHexString(addrhigh)+" 10 0 0 0 0 0 ", msg.toString());

        // turn around as the reply to the read high
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,addrhigh);
 		lnis.sendTestMessage(msg);

        // 3rd read
        Assert.assertEquals("reply does 3rd read", 3, lnis.outbound.size());
        msg = (LocoNetMessage)lnis.outbound.get(2);
        // channel 2 (above) is CV 9 for read mode
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 1 "
                            +Integer.toHexString(channel*3+3)+" 0 "
                            +Integer.toHexString(cv)+" 10 0 0 0 0 0 ", msg.toString());

        // turnaround as the reply to the mode read
        src = msg.getElement(2);
        dst = msg.getElement(3);
        msg.setElement(2, dst);
        msg.setElement(3, src);
        msg.setElement(4, 0x01);  // seems to be fixed PC address high
        msg.setElement(14,cv); // Toggle switch
 		lnis.sendTestMessage(msg);

        Assert.assertEquals("reply does no more messages", 3, lnis.outbound.size());
    }

	// test for outgoing read request
	public void testSendReadCommand() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

		m.sendReadCommand(1);

        // check transmitted message
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 2 1 0 0 10 0 0 0 0 0 ", msg.toString());
	}

	// test for outgoing write request
	public void testSendWriteCommand() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LocoIOTableModel m = new LocoIOTableModel(0x1081);

		m.sendWriteCommand(1, 0x31);

        // check transmitted message
        Assert.assertEquals("One message sent", 1, lnis.outbound.size());
        LocoNetMessage msg = (LocoNetMessage)lnis.outbound.get(0);
        Assert.assertEquals("message length", 16, msg.getNumDataElements());
        Assert.assertEquals("message opCode", 0xE5, msg.getOpCode());
        Assert.assertEquals("message bytes", "e5 10 50 81 10 0 1 1 0 31 10 0 0 0 0 0 ", msg.toString());
	}

	// from here down is testing infrastructure

	public LocoIOTableModelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoIOTableModelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoIOTableModelTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOTableModelTest.class.getName());

}
