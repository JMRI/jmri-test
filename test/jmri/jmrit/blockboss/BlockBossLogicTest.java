// BlockBossLogicTest.java

package jmri.jmrit.blockboss;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;

/**
 * Tests for the BlockBossLogic class
 * @author	Bob Jacobsen
 * @version $Revision: 1.13 $
 */
public class BlockBossLogicTest extends TestCase {
	
	// test creation
	public void testCreate() {
		BlockBossLogic p = new BlockBossLogic("IH2");
		Assert.assertEquals("driven signal name", "IH2", p.getDrivenSignal());
	}

	// test simplest block, just signal following
	public void testSimpleBlock() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.start();
		Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets green", SignalHead.GREEN, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets green", SignalHead.GREEN, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in distant simple block
	public void testSimpleBlockDistant() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setDistantSignal(true);
		p.start();
		Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets red", SignalHead.RED, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets green", SignalHead.GREEN, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in limited simple block
	// (not particularly interesting, as next signal can't set red)
	public void testSimpleBlockLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setLimitSpeed1(true);
		p.start();
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in distant, limited simple block
	public void testSimpleBlockDistantLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setDistantSignal(true);
		p.setLimitSpeed1(true);
		p.start();
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets red", SignalHead.RED, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		p.stop();
	}

	// if no next signal, it's considered green
	public void testSimpleBlockNoNext() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.start();
		
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("missing signal is green", SignalHead.GREEN, h1.getAppearance());
		p.stop();
	}

	// if no next signal, it's considered green
	public void testSimpleBlockNoNextLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setLimitSpeed1(true);
		p.start();
		
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("missing signal is green, show yellow", SignalHead.YELLOW, h1.getAppearance());
		p.stop();
	}

	// from here down is testing infrastructure

    // Ensure minimal setup for log4J

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5;
    SignalHead h1, h2, h3, h4;
    
    /**
    * Test-by test initialization.
    * Does log4j for standalone use, and then
    * creates a set of turnouts, sensors and signals
    * as common background for testing
    */
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
        
       	Assert.assertNotNull("Instance exists", i );
        
        // reset InstanceManager
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT1", "1");
        t2 = InstanceManager.turnoutManagerInstance().newTurnout("IT2", "2");
        t3 = InstanceManager.turnoutManagerInstance().newTurnout("IT3", "3");
        s1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "1");
        s2 = InstanceManager.sensorManagerInstance().newSensor("IS2", "2");
        s3 = InstanceManager.sensorManagerInstance().newSensor("IS3", "3");
        s4 = InstanceManager.sensorManagerInstance().newSensor("IS4", "4");
        s5 = InstanceManager.sensorManagerInstance().newSensor("IS5", "5");

        h1 = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.signalHeadManagerInstance().register(h1);
        h2 = new jmri.implementation.VirtualSignalHead("IH2");
        InstanceManager.signalHeadManagerInstance().register(h2);
        h3 = new jmri.implementation.VirtualSignalHead("IH3");
        InstanceManager.signalHeadManagerInstance().register(h3);
        h4 = new jmri.implementation.VirtualSignalHead("IH4");
        InstanceManager.signalHeadManagerInstance().register(h4);
    }

	public BlockBossLogicTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", BlockBossLogicTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockBossLogicTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
