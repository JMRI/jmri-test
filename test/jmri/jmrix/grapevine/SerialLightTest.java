// SerialLightTest.java

package jmri.jmrix.grapevine;

import jmri.*;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.grapevine.SerialLight class,
 * low address.
 * @author			Bob Jacobsen
 * @version			$Revision: 1.5 $
 */
public class SerialLightTest extends AbstractLightTest {

	private SerialTrafficControlScaffold tcis = null;
        private SerialNode n = new SerialNode();

	public void setUp() {
		// prepare an interface
		tcis = new SerialTrafficControlScaffold();
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6));
        
		t = new SerialLight("GL1104","t4");
	}

	public int numListeners() { return tcis.numListeners(); }

	public void checkOffMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "81 1C 81 04", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
	}

	public void checkOnMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "81 18 81 0C", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
	}

	// from here down is testing infrastructure

	public SerialLightTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialLightTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialLightTest.class);
		return suite;
	}

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialLightTest.class.getName());

}
