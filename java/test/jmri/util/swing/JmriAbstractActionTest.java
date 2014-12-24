// JmriAbstractActionTest.java

package jmri.util.swing;

import junit.framework.*;

/**
 *
 * @author	    Bob Jacobsen  Copyright 2010
 * @version         $Revision$
 */
public class JmriAbstractActionTest extends TestCase {
    
    public void testAccess() {
        JmriAbstractAction a = new JmriAbstractAction("foo", new jmri.util.swing.sdi.JmriJFrameInterface()){
            /**
			 * 
			 */
			private static final long serialVersionUID = 2665653970807828869L;

			public jmri.util.swing.JmriPanel makePanel(){ return null; }
        };
        
        Assert.assertEquals("foo",a.getValue(javax.swing.Action.NAME));

        javax.swing.Icon i = new javax.swing.ImageIcon("resources/icons/throttles/PowerRed24.png");
        a = new JmriAbstractAction("foo", i, null){
            /**
			 * 
			 */
			private static final long serialVersionUID = 3253789860317664884L;

			public jmri.util.swing.JmriPanel makePanel(){ return null; }
        };
        
        Assert.assertEquals("foo",a.getValue(javax.swing.Action.NAME));
        Assert.assertEquals(i,a.getValue(javax.swing.Action.SMALL_ICON));
    }
    
    // from here down is testing infrastructure
    public JmriAbstractActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmriAbstractActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriAbstractActionTest.class); 
       
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
