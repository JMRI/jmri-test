/** 
 * AllTest.java
 *
 * Description:	    Driver for JMRI project test classes
 * @author			Bob Jacobsen
 * @version			
 */

package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTest extends TestCase  {
	public AllTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) { 
		String[] testCaseName = {AllTest.class.getName()};
		// initialize junit
		junit.swingui.TestRunner.main(testCaseName);
	}
		
	// test suite
	public static Test suite() {
		initLogging();
		// all tests from here down in heirarchy
		TestSuite suite = new TestSuite("AllTest");  // no tests in this class itself
		// all tests from other classes
		suite.addTest(jmri.jmrit.JmritTest.suite());
		suite.addTest(jmri.jmrix.JmrixTest.suite());
		suite.addTest(JmriTest.suite());
		
		return suite;
	}
	
	static boolean log4jinit = true;
	public static void initLogging() {
		if (log4jinit) {
			log4jinit = false;
    		// initialize log4j - from logging control file (lcf) if you can find it
     		String logFile = "default.lcf";
    		if (new java.io.File(logFile).canRead()) {
    			System.out.println(logFile+" configures logging");
    			org.apache.log4j.PropertyConfigurator.configure("default.lcf");
   		} else {
    			System.out.println(logFile+" not found, using default logging");
	    		org.apache.log4j.BasicConfigurator.configure();
	   			// only log warnings and above
	   			org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.INFO);
     		}
		}
	}
		
  	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance("jmri");

}
