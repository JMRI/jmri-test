package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/** 
 * IdentifyLocoTest.java
 *
 * Description:	    tests for the jmrit.roster.IdentifyLoco class
 * @author			Bob Jacobsen
 * @version			
 */
public class IdentifyLocoTest extends TestCase {

	static int cvRead = -1;
	
	public void testShort() {
		// initialize the system
		jmri.progdebugger.ProgDebugger p = new jmri.progdebugger.ProgDebugger() {
			public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
				cvRead = CV;
			}
		};
		jmri.InstanceManager.setProgrammer(p);

		// create our test object
		IdentifyLoco i = new IdentifyLoco() {
			public void message(String m) {}
			public void done(int i) {}
		};
		
		i.start();
		Assert.assertEquals("step 1 reads CV ", 29, cvRead);
		Assert.assertEquals("running after 1 ", true, i.isRunning());
		
		// simulate CV read complete, with long read bit off
		i.programmingOpReply(0x00, 0);
		Assert.assertEquals("step 2 reads CV ", 1, cvRead);
		Assert.assertEquals("running after 2 ", true, i.isRunning());

		// simulate CV read of short address complete
		i.programmingOpReply(123, 0);
		Assert.assertEquals("running after 2 ", false, i.isRunning());
		Assert.assertEquals("found address ", 123, i.address);
		
	}

	public void testLong() {
		// initialize the system
		jmri.progdebugger.ProgDebugger p = new jmri.progdebugger.ProgDebugger() {
			public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
				cvRead = CV;
			}
		};
		jmri.InstanceManager.setProgrammer(p);

		// create our test object
		IdentifyLoco i = new IdentifyLoco() {
			public void message(String m) {}
			public void done(int i) {}
		};
		
		i.start();
		Assert.assertEquals("step 1 reads CV ", 29, cvRead);
		Assert.assertEquals("running after 1 ", true, i.isRunning());
		
		// simulate CV read complete, with long read bit on
		i.programmingOpReply(0x20, 0);
		Assert.assertEquals("step 2 reads CV ", 17, cvRead);
		Assert.assertEquals("running after 2 ", true, i.isRunning());

		// simulate read of CV17 complete
		i.programmingOpReply(210, 0);
		Assert.assertEquals("step 3 reads CV ", 18, cvRead);
		Assert.assertEquals("running after 3 ", true, i.isRunning());

		// simulate read of CV18 complete
		i.programmingOpReply(189, 0);
		Assert.assertEquals("running after 4 ", false, i.isRunning());
		Assert.assertEquals("found address ", 4797, i.address);
		
	}
	// from here down is testing infrastructure
	
	public IdentifyLocoTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {IdentifyLocoTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(IdentifyLocoTest.class);
		return suite;
	}
	
}
