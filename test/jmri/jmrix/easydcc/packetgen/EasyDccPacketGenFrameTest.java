/**
 * EasyDccPacketGenFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.packetgen.EasyDccPacketGenFrame class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.2 $
 */

package jmri.jmrix.easydcc.packetgen;

import java.io.*;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.easydcc.*;

public class EasyDccPacketGenFrameTest extends TestCase {

	public void testFrameCreate() {
		new EasyDccPacketGenFrame();
	}

	// from here down is testing infrastructure

	public EasyDccPacketGenFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccPacketGenFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EasyDccPacketGenFrameTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccPacketGenFrameTest.class.getName());

}
