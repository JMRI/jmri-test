// StoppingBlockTest.java

package jmri.jmrit.tracker;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.Block;

/**
 * Tests for the StoppingBlock class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision: 1.2 $
 */
public class StoppingBlockTest extends TestCase {

	public void testDirectCreate() {
	    // check for exception in ctor
        new StoppingBlock(new Block("dummy"));
	}

    
	// from here down is testing infrastructure

	public StoppingBlockTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {StoppingBlockTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(StoppingBlockTest.class);
		return suite;
	}

}
