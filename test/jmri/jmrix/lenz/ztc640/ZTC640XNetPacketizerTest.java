package jmri.jmrix.lenz.ztc640;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>Title: ZTC640XNetPacketizerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2009</p>
 * @author Paul Bender 
 * @version $Revision: 1.2 $
 */
public class ZTC640XNetPacketizerTest extends TestCase {

        public void testCtor() {
          ZTC640Frame f = new ZTC640Frame();
          Assert.assertTrue(f != null);
        }

        // from here down is testing infrastructure

    public ZTC640XNetPacketizerTest(String s) {
        super(s);
    }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", ZTC640XNetPacketizerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZTC640XNetPacketizerTest.class.getName());

}
