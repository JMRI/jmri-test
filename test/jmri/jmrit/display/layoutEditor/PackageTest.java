// PackageTest.java

package jmri.jmrit.display.layoutEditor;

import junit.framework.*;

/**
 * Tests for the jmrit.display.layoutEditor package
 * @author			Bob Jacobsen  Copyright 2008, 2009, 2010
 * @version         $Revision: 1.2 $
 */
public class PackageTest extends TestCase {

	// from here down is testing infrastructure
	public PackageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PackageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PackageTest.class.getName());  
		suite.addTest(LayoutEditorWindowTest.suite());
		suite.addTest(LEConnectivityTest.suite());
		return suite;
	}

}
