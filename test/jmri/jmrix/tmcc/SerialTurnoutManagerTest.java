// SerialTurnoutManagerTest.java

package jmri.jmrix.tmcc;

import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.*;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	    tests for the SerialTurnoutManager class
 * @author			Bob Jacobsen
 * @version  $Revision: 1.2 $
 */
public class SerialTurnoutManagerTest extends jmri.AbstractTurnoutMgrTest  {

	public void setUp() {
		// create and register the manager object
		l = new SerialTurnoutManager();
		jmri.InstanceManager.setTurnoutManager(l);
	}

	public String getSystemName(int n) {
		return "TT"+n;
	}

	public void testAsAbstractFactory () {
		// ask for a Turnout, and check type
		Turnout o = l.newTurnout("TT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assertTrue( null != (SerialTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+l.getBySystemName("TT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+l.getByUserName("my name"));

		assertTrue(null != l.getBySystemName("TT21"));
		assertTrue(null != l.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public SerialTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialTurnoutManager.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(SerialTurnoutManagerTest.class);
		return suite;
	}

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManagerTest.class.getName());

}
