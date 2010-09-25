// SpecificReplyTest.java

package jmri.jmrix.powerline.insteon2412s;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.powerline.SerialReply;

/**
 * JUnit tests for the cm11.SpecficReply class.
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2009, 2010
 * @version	$Revision: 1.1 $
 */
public class SpecificReplyTest extends TestCase {

	public void testCreate() {
		SerialReply m = new SpecificReply();
		Assert.assertNotNull("exists", m );
	}

	public void testBytesToString() {
		SerialReply m = new SpecificReply();
		m.setOpCode(0x81);
		m.setElement(1, (byte)0x02);
		m.setElement(2, (byte)0xA2);
		m.setElement(3, (byte)0x00);
		Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
	}

	// from here down is testing infrastructure

	public SpecificReplyTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SpecificReplyTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SpecificReplyTest.class);
		return suite;
	}

}
