/** 
 * LnSensorManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.LnSensorManagerTurnout class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.loconet;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;
import jmri.*;

public class LnSensorManagerTest extends TestCase  {

	public void testLnTurnoutCreate() {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();
		jmri.InstanceManager.setTurnoutManager(l);
		assert(l == jmri.InstanceManager.turnoutManagerInstance());
				
	}

	public void testLnTurnoutPutGet() {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();

		// sample turnout object
		LnTurnout t = new LnTurnout(22);
		
		// store and get
		l.putByUserName("mine", t);
		assert(t == l.getByUserName("mine"));
		assert(t == l.getBySystemName("LT22"));				
	}

	public void testByAddress() {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();

		// sample turnout object
		LnTurnout t = new LnTurnout(22);
		
		// sample address object
		TurnoutAddress a = new TurnoutAddress("LT22", "user");
		
		// store and get
		l.putByUserName("user", t);
		assert(t == l.getByAddress(a));				
	}

	public void testMisses() {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();
		
		// sample address object
		TurnoutAddress a = new TurnoutAddress("LT22", "user");
		
		// try to get nonexistant turnouts
		assert(null == l.getByAddress(a));				
		assert(null == l.getByUserName("foo"));				
		assert(null == l.getBySystemName("bar"));				
	}

	public void testLocoNetMessages() {
		// prepare an interface, register
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();
		
		// send messages for 21, 22
		// notify the Ln that somebody else changed it...
		LocoNetMessage m1 = new LocoNetMessage(4);
		m1.setOpCode(0xb1);
		m1.setElement(1, 0x14);     // set CLOSED
		m1.setElement(2, 0x20);
		m1.setElement(3, 0x7b);
		lnis.sendTestMessage(m1);

		// notify the Ln that somebody else changed it...
		LocoNetMessage m2 = new LocoNetMessage(4);
		m2.setOpCode(0xb0);
		m2.setElement(1, 0x15);     // set CLOSED
		m2.setElement(2, 0x20);
		m2.setElement(3, 0x7a);
		lnis.sendTestMessage(m2);

				
		// try to get turnouts to see if they exist
		assert(null != l.getBySystemName("LT21"));				
		assert(null != l.getBySystemName("LT22"));				
	}

	public void testAsAbstractFactory () {
		// create and register the manager object
		LnTurnoutManager l = new LnTurnoutManager();
		jmri.InstanceManager.setTurnoutManager(l);
		
		// ask for a Turnout, and check type
		TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();
		
		Turnout o = t.newTurnout("LT21", "my name");
		
		
		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assert( null != (LnTurnout)o);
		
		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+t.getBySystemName("LT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+t.getByUserName("my name"));
		
		assert(null != t.getBySystemName("LT21"));				
		assert(null != t.getByUserName("my name"));				
		
	}
	
	
	// from here down is testing infrastructure
	
	public LnSensorManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnSensorManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnSensorManagerTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManagerTest.class.getName());

}
