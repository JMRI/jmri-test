package jmri.util;

import junit.framework.Assert;

import jmri.InstanceManager;
import jmri.managers.InternalLightManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;

/**
 * Common utility methods for working with JUnit.
 *<p>
 * To release the current thread and allow other listeners to execute:
<code><pre>
    JUnitUtil.releaseThread(this);
    super.tearDown();
</pre></code>
 * Note that this is not appropriate for Swing objects;
 * you need to use JFCUnit for that.
 *<p>
 * If you're using the InstanceManager, setUp() implementation should start with:
<code><pre>
    super.setUp();
    JUnitUtil.resetInstanceManager();
    JUnitUtil.initInternalTurnoutManager();
    JUnitUtil.initInternalLightManager();
    JUnitUtil.initInternalSensorManager();
</pre></code>
 *<p>
 * Your tearDown() should end with:
<code><pre>
    JUnitUtil.resetInstanceManager();
    super.tearDown();
</pre></code>

 * @author Bob Jacobsen  Copyright 2009
 * @version $Revision: 1.1 $
 * @since 2.5.3
 */

public class JUnitUtil {

    static int DEFAULTDELAY = 200;
    
    /** 
     * Release the current thread, allowing other 
     * threads to process
     */
	public static void releaseThread(Object self, int delay) {
	    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
	        System.err.println("ERROR: Cannot use releaseThread on Swing thread");
	        new Exception().printStackTrace();
	        return;
	    }
	    synchronized (self) {
            try {
                int priority = Thread.currentThread().getPriority(); 
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                Thread.yield();
                Thread.sleep(delay);
                Thread.currentThread().setPriority(priority);
                self.wait(delay);
            }
            catch (InterruptedException e) {
                Assert.fail("failed due to InterruptedException");
            }
        }
	}
    
	public static void releaseThread(Object self) {
	    releaseThread(self, DEFAULTDELAY);
    }

    public static void resetInstanceManager() {    
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
    }

    public static void initInternalTurnoutManager() {
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
    }

    public static void initInternalLightManager() {
        InstanceManager.setLightManager(new InternalLightManager());
    }

    public static void initInternalSensorManager() {
        InstanceManager.setSensorManager(new InternalSensorManager());
    }

}