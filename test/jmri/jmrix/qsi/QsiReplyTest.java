// QsiReplyTest.java

package jmri.jmrix.qsi;

import apps.tests.Log4JFixture;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitAppender;

import jmri.jmrix.qsi.QsiReply;

/**
 * JUnit tests for the QsiReplyclass
 * @author			Bob Jacobsen Copyright 2006, 2007
 *
 * @version         $Revision: 1.3 $
 */
public class QsiReplyTest extends TestCase {

	public void testCreate() {
		QsiReply m = new QsiReply();
		m.setElement(0, 'A');
		Assert.assertEquals("expected length ", 1, m.getNumDataElements());
		m.setElement(1, 'B');
		m.setElement(2, 'C');
		Assert.assertEquals("expected length ", 3, m.getNumDataElements());
		m.setElement(0, '1');
		Assert.assertEquals("expected length ", 3, m.getNumDataElements());
		m.setElement(3, 'D');
		Assert.assertEquals("expected length ", 4, m.getNumDataElements());
		m.setElement(5, 'A');
		Assert.assertEquals("expected length ", 6, m.getNumDataElements());

	}

	public void testAsciiToString() {
		QsiReply m = new QsiReply();
		m.setOpCode('C');
		m.setElement(1, 'o');
		m.setElement(2, 'm');
		m.setElement(3, ':');
		Assert.assertEquals("string compare ", "Com:", m.toString());
	}

	public void testSkipWhiteSpace() {
		QsiReply m = new QsiReply();
		m.setElement(0, '0');
		m.setElement(1, ' ');
		m.setElement(2, ' ');
		m.setElement(3, 'A');
		m.setElement(4, 0x0A);
		m.setElement(5, 'A');
		Assert.assertEquals(" skip two blanks", 3, m.skipWhiteSpace(1));
		Assert.assertEquals(" skip no blanks", 3, m.skipWhiteSpace(3));
		Assert.assertEquals(" handle start", 0, m.skipWhiteSpace(0));
		Assert.assertEquals(" skip LF", 5, m.skipWhiteSpace(4));
	}

	public void testValue1() {
		// value when just the string comes back
		QsiReply m = new QsiReply();
                int i= 0;
		m.setElement(i++, 'C');
		m.setElement(i++, 'V');
		m.setElement(i++, '0');
		m.setElement(i++, '0');
		m.setElement(i++, '1');
		m.setElement(i++, '2');
		m.setElement(i++, '7');
		Assert.assertEquals("value ", -1, m.value());
		JUnitAppender.assertErrorMessage("Unable to get number from reply: \"CV\" index: 0 message: \"CV00127\"");
	}

	public void testValue2() {
		// value when a hex string comes back
		QsiReply m = new QsiReply();
        int i= 0;
		m.setElement(i++, 'C');
		m.setElement(i++, 'V');
		m.setElement(i++, '0');
		m.setElement(i++, '0');
		m.setElement(i++, '1');
		m.setElement(i++, 'A');
		m.setElement(i++, 'B');
		Assert.assertEquals("value ", -1, m.value());
		JUnitAppender.assertErrorMessage("Unable to get number from reply: \"CV\" index: 0 message: \"CV001AB\"");
	}

	public void testMatch() {
		QsiReply m = new QsiReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		Assert.assertEquals("find ", 5, m.match("PROGRAMMING"));
		Assert.assertEquals("not find ", -1, m.match("foo"));
	}

	// from here down is testing infrastructure

	public QsiReplyTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", QsiReplyTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(QsiReplyTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
