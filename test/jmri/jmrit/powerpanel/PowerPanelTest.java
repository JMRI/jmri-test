/** 
 * PowerPanelTest.java
 *
 * Description:	    tests for the jmrit.PowerPanel package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.powerpanel;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PowerPanelTest extends TestCase {

	// from here down is testing infrastructure
	
	public PowerPanelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PowerPanelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.powerpanel.PowerPanelTest"); // no tests in class itself
		suite.addTest(jmri.jmrit.powerpanel.PowerPaneTest.suite());
		return suite;
	}
	
}
