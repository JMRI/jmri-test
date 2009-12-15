// OutputBitsTest.java

package jmri.jmrix.maple;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.AbstractMRMessage;

/**
 * JUnit tests for the OutputBits class
 * @author		Dave Duchamp  2009
 * @version		$Revision: 1.1 $
 */
public class OutputBitsTest extends TestCase {
		
    private OutputBits obit = new OutputBits();
       
    public void testConstructor1() {
        Assert.assertNotNull("check instance", obit.instance());
    }

    public void testAccessors() {
        obit.setNumOutputBits(75);
		obit.setSendDelay(250);
        Assert.assertEquals("check numOutputBits", 75, obit.getNumOutputBits());
        Assert.assertEquals("check sendDelay", 250, obit.getSendDelay());
    }
    
    public void testWriteOutputBits1() {
		obit.setNumOutputBits(48);
        obit.setOutputBit(2,false);
        obit.setOutputBit(1,false);
        obit.setOutputBit(23,false);
        obit.setOutputBit(41,false);
        obit.setOutputBit(31,false);
        obit.setOutputBit(2,true);
        obit.setOutputBit(19,false);
        obit.setOutputBit(5,false);
        obit.setOutputBit(26,false);
        obit.setOutputBit(48,false);

        AbstractMRMessage m = obit.createOutPacket(1,48);

        Assert.assertEquals("packet size", 62, m.getNumDataElements() );
        Assert.assertEquals("node address 1", '0', m.getElement(1) );
        Assert.assertEquals("node address 2", '0', m.getElement(2) );
        Assert.assertEquals("packet type 1", 'W', m.getElement(3) );        
        Assert.assertEquals("packet type 2", 'C', m.getElement(4) );        
        Assert.assertEquals("TO val 1 f", '1', (m.getElement(10+1) & 0xff));      
        Assert.assertEquals("TO val 2 t", '0', (m.getElement(10+2) & 0xff));      
        Assert.assertEquals("TO val 3 unknown", '0', (m.getElement(10+3) & 0xff));      
    }
	
    // from here down is testing infrastructure
    public OutputBitsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OutputBitsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OutputBitsTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
