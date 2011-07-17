// DecoderDefnTest.java

package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.decoderdefn package
 * @author			Bob Jacobsen
 * @version			$Revision: 1.5 $
 */
public class DecoderDefnTest extends TestCase {


	// from here down is testing infrastructure

	public DecoderDefnTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DecoderDefnTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.decoderdefn");
		suite.addTest(IdentifyDecoderTest.suite());
		suite.addTest(DecoderIndexFileTest.suite());
		suite.addTest(DecoderFileTest.suite());
		suite.addTest(SchemaTest.suite());
		return suite;
	}

}
