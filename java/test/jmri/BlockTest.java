// BlockTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Block class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision$
 */
public class BlockTest extends TestCase {

    /**
     * Normally, users create Block objects via a manager, 
     * but we test the direct create here.  If it works, we can 
     * use it for testing.
     */
	public void testDirectCreate() {
	    new Block("SystemName");
	}

	public void testSensorAdd() {
	    Block b = new Block("SystemName");
	    b.setSensor("IS12");
	}

    static int count; 
    
	public void testSensorInvoke() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    count = 0;
	    Block b = new Block("SystemName"){
            void handleSensorChange(java.beans.PropertyChangeEvent e) {
                count++;
            }
	    };
        Sensor s = sm.provideSensor("IS12");
	    b.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS12", s));
	    sm.provideSensor("IS12").setState(jmri.Sensor.ACTIVE);
	    Assert.assertEquals("count of detected changes", 1, count);
	}


	public void testValueField() {
	    Block b = new Block("SystemName");
	    b.setValue("string");
	    Assert.assertEquals("Returned Object matches", "string", b.getValue());
	}

	public void testSensorSequence() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    count = 0;
	    Block b = new Block("SystemName");
	    Sensor s = sm.provideSensor("IS12");
	    Assert.assertEquals("Initial state", Block.UNKNOWN, s.getState());
	    b.setSensor("IS12");
	    s.setState(jmri.Sensor.ACTIVE);
	    Assert.assertEquals("State with sensor active", Block.OCCUPIED, s.getState());
	    s.setState(jmri.Sensor.INACTIVE);
	    Assert.assertEquals("State with sensor inactive", Block.UNOCCUPIED, s.getState());
	}


    // test going active with only one neighbor
	public void testFirstGoActive() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();

	    Block b1 = new Block("SystemName1");

	    Block b2 = new Block("SystemName2");
        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");
        
        Path p = new Path();
        p.setBlock(b2);
        
        b1.addPath(p);
        
        // actual test
        b1.goingActive();
	    Assert.assertEquals("Value transferred", "b2 contents", b1.getValue());
	}
	
    // Test going active with two neighbors, one active.
    // b2 is between b1 and b3. b1 contains a train
	public void testOneOfTwoGoesActive() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();

	    Block b1 = new Block("SystemName1");
	    Block b2 = new Block("SystemName2");
	    Block b3 = new Block("SystemName3");

        Sensor s1 = sm.provideSensor("IS1");
        b1.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        
        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);
        
        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
        s3.setState(Sensor.INACTIVE);
        
        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        b2.addPath(p21);
        
        Path p23 = new Path();
        p23.setBlock(b3);
        p23.setFromBlockDirection(Path.LEFT);
        p23.setToBlockDirection(Path.RIGHT);
        b2.addPath(p23);
        
        // actual test
        b2.goingActive();
        Assert.assertEquals("State", Block.OCCUPIED, b2.getState());
  	    Assert.assertEquals("Value transferred", "b1 contents", b2.getValue());
        Assert.assertEquals("Direction", Path.RIGHT, b2.getDirection());
  	    
	}
	
    // Test going active with two neighbors, both active.
    // b2 is between b1 and b3. 
	public void testTwoOfTwoGoesActive() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();

	    Block b1 = new Block("SystemName1");
	    Block b2 = new Block("SystemName2");
	    Block b3 = new Block("SystemName3");

        Sensor s1 = sm.provideSensor("IS1");
        b1.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        b1.setDirection(Path.RIGHT);
        
        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);
        
        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
        s3.setState(Sensor.ACTIVE);
        b3.setValue("b3 contents");
        b3.setDirection(Path.RIGHT);
        
        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        b2.addPath(p21);
        
        Path p23 = new Path();
        p23.setBlock(b3);
        p23.setFromBlockDirection(Path.LEFT);
        p23.setToBlockDirection(Path.RIGHT);
        b2.addPath(p23);
        
        // actual test
        b2.goingActive();
        Assert.assertEquals("State", Block.OCCUPIED, b2.getState());
  	    Assert.assertEquals("Value transferred", "b1 contents", b2.getValue());
        Assert.assertEquals("Direction", Path.RIGHT, b2.getDirection());
  	    
	}
	
	public void testReporterAdd() {
	    ReporterManager rm = new jmri.managers.InternalReporterManager();
	    Block b = new Block("SystemName");
            b.setReporter(rm.provideReporter("IR22"));
	}

        public void testReporterInvokeAll() {
            ReporterManager rm = new jmri.managers.InternalReporterManager();
	    count = 0;
	    Block b = new Block("SystemName"){
                @Override
                void handleReporterChange(java.beans.PropertyChangeEvent e) {
                    count++;
                }
	    };
	    b.setReporter(rm.provideReporter("IR22"));
	    rm.provideReporter("IR22").setReport("report");
            // For each report, there are two PropertyChangeEvents -
            // "currentReport" and "lastReport"
	    Assert.assertEquals("count of detected changes", 2, count);
        }

        public void testReporterInvokeCurrent() {
            ReporterManager rm = new jmri.managers.InternalReporterManager();
            count = 0;
            Block b = new Block("SystemName"){
                @Override
                void handleReporterChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("currentReport")) {
                        count++;
                    }
                }
            };
            b.setReporter(rm.provideReporter("IR22"));
            rm.provideReporter("IR22").setReport("report");
            // Only detecting "currentReport" PropertyChangeEvent
            Assert.assertEquals("count of detected changes", 1, count);

            rm.provideReporter("IR22").setReport(null);
            // Current report should change
            Assert.assertEquals("count of detected changes", 2, count);
        }
    
        public void testReporterInvokeLast() {
            ReporterManager rm = new jmri.managers.InternalReporterManager();
            count = 0;
            Block b = new Block("SystemName"){
                @Override
                void handleReporterChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("lastReport")) {
                        count++;
                    }
                }
            };
            b.setReporter(rm.provideReporter("IR22"));
            rm.provideReporter("IR22").setReport("report");
            // Only detecting "lastReport" PropertyChangeEvent
            Assert.assertEquals("count of detected changes", 1, count);

            rm.provideReporter("IR22").setReport(null);
            // Last report should not change
            Assert.assertEquals("count of detected changes", 1, count);
        }

	// from here down is testing infrastructure

	public BlockTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {BlockTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
        InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
