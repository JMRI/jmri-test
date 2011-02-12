// SerialTurnoutManagerTest.java

package jmri.jmrix.powerline;

import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.*;
import jmri.jmrix.powerline.SerialInterfaceScaffold;
import jmri.jmrix.powerline.SerialTurnoutManager;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	    tests for the SerialTurnoutManager class
 * @author			Bob Jacobsen Copyright 2004, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version  $Revision: 1.9 $
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest  {

	private SerialInterfaceScaffold nis = null;
	
	public void setUp() {
		// prepare an interface, register
		nis = new SerialInterfaceScaffold();
		// create and register the manager object
		l = new SerialTurnoutManager(nis);
		jmri.InstanceManager.setTurnoutManager(l);
	}

	public String getSystemName(int n) {
		return "PT"+n;
	}

	public void testAsAbstractFactory () {
		// ask for a Turnout, and check type
		Turnout o = l.newTurnout("PT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assertTrue( null != (SerialTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+l.getBySystemName("PT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+l.getByUserName("my name"));

		assertTrue(null != l.getBySystemName("PT21"));
		assertTrue(null != l.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public SerialTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialTurnoutManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(SerialTurnoutManagerTest.class);
		return suite;
	}
    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManagerTest.class.getName());

}
