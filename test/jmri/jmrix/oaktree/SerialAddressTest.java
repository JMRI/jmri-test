// SerialAddressTest.java

package jmri.jmrix.oaktree;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialAddress utility class.
 * @author	Dave Duchamp Copyright 2004
 * @version	$Revision: 1.1 $
 */
public class SerialAddressTest extends TestCase {

	public void testValidateSystemNameFormat() {
            Assert.assertTrue("valid format - OL2", SerialAddress.validSystemNameFormat("OL2",'L') );
            Assert.assertTrue("valid format - OL0B2", SerialAddress.validSystemNameFormat("OL0B2",'L') );
            Assert.assertTrue("invalid format - OL", !SerialAddress.validSystemNameFormat("OL",'L') );
            Assert.assertTrue("invalid format - OLB2", !SerialAddress.validSystemNameFormat("OLB2",'L') );
            Assert.assertTrue("valid format - OL2005", SerialAddress.validSystemNameFormat("OL2005",'L') );
            Assert.assertTrue("valid format - OL2B5", SerialAddress.validSystemNameFormat("OL2B5",'L') );
            Assert.assertTrue("valid format - OT2005", SerialAddress.validSystemNameFormat("OT2005",'T') );
            Assert.assertTrue("valid format - OT2B5", SerialAddress.validSystemNameFormat("OT2B5",'T') );
            Assert.assertTrue("valid format - OS2005", SerialAddress.validSystemNameFormat("OS2005",'S') );
            Assert.assertTrue("valid format - OS2B5", SerialAddress.validSystemNameFormat("OS2B5",'S') );
            Assert.assertTrue("invalid format - OY2005", !SerialAddress.validSystemNameFormat("OY2005",'L') );
            Assert.assertTrue("invalid format - OY2B5", !SerialAddress.validSystemNameFormat("OY2B5",'L') );
            Assert.assertTrue("valid format - OL22001", SerialAddress.validSystemNameFormat("OL22001",'L') );
            Assert.assertTrue("valid format - OL22B1", SerialAddress.validSystemNameFormat("OL22B1",'L') );
            Assert.assertTrue("invalid format - OL22000", !SerialAddress.validSystemNameFormat("OL22000",'L') );
            Assert.assertTrue("invalid format - OL22B0", !SerialAddress.validSystemNameFormat("OL22B0",'L') );
            Assert.assertTrue("valid format - OL2999", SerialAddress.validSystemNameFormat("OL2999",'L') );
            Assert.assertTrue("valid format - OL2B2048", SerialAddress.validSystemNameFormat("OL2B2048",'L') );
            Assert.assertTrue("invalid format - OL2B2049", !SerialAddress.validSystemNameFormat("OL2B2049",'L') );
            Assert.assertTrue("valid format - OL127999", SerialAddress.validSystemNameFormat("OL127999",'L') );
            Assert.assertTrue("invalid format - OL128000", !SerialAddress.validSystemNameFormat("OL128000",'L') );
            Assert.assertTrue("valid format - OL127B7", SerialAddress.validSystemNameFormat("OL127B7",'L') );
            Assert.assertTrue("invalid format - OL128B7", !SerialAddress.validSystemNameFormat("OL128B7",'L') );
            Assert.assertTrue("invalid format - OL2oo5", !SerialAddress.validSystemNameFormat("OL2oo5",'L') );
            Assert.assertTrue("invalid format - OL2aB5", !SerialAddress.validSystemNameFormat("OL2aB5",'L') );
            Assert.assertTrue("invalid format - OL2B5x", !SerialAddress.validSystemNameFormat("OL2B5x",'L') );
	}

	public void testGetBitFromSystemName() {
            Assert.assertEquals("OL2", 2, SerialAddress.getBitFromSystemName("OL2") );
            Assert.assertEquals("OL2002", 2, SerialAddress.getBitFromSystemName("OL2002") );
            Assert.assertEquals("OL1", 1, SerialAddress.getBitFromSystemName("OL1") );
            Assert.assertEquals("OL2001", 1, SerialAddress.getBitFromSystemName("OL2001") );
            Assert.assertEquals("OL999", 999, SerialAddress.getBitFromSystemName("OL999") );
            Assert.assertEquals("OL2999", 999, SerialAddress.getBitFromSystemName("OL2999") );
            Assert.assertEquals("OL29O9", 0, SerialAddress.getBitFromSystemName("OL29O9") );
            Assert.assertEquals("OL0B7", 7, SerialAddress.getBitFromSystemName("OL0B7") );
            Assert.assertEquals("OL2B7", 7, SerialAddress.getBitFromSystemName("OL2B7") );
            Assert.assertEquals("OL0B1", 1, SerialAddress.getBitFromSystemName("OL0B1") );
            Assert.assertEquals("OL2B1", 1, SerialAddress.getBitFromSystemName("OL2B1") );
            Assert.assertEquals("OL0B2048", 2048, SerialAddress.getBitFromSystemName("OL0B2048") );
            Assert.assertEquals("OL11B2048", 2048, SerialAddress.getBitFromSystemName("OL11B2048") );
        }

        SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
        SerialNode c = new SerialNode(10,SerialNode.SMINI);
        SerialNode b = new SerialNode(127,SerialNode.SMINI);
        
	public void testGetNodeFromSystemName() {
            SerialNode d = new SerialNode(14,SerialNode.USIC_SUSIC);
            SerialNode c = new SerialNode(17,SerialNode.SMINI);
            SerialNode b = new SerialNode(127,SerialNode.SMINI);
            Assert.assertEquals("node of OL14007", d, SerialAddress.getNodeFromSystemName("OL14007") );
            Assert.assertEquals("node of OL14B7", d, SerialAddress.getNodeFromSystemName("OL14B7") );
            Assert.assertEquals("node of OL127007", b, SerialAddress.getNodeFromSystemName("OL127007") );
            Assert.assertEquals("node of OL127B7", b, SerialAddress.getNodeFromSystemName("OL127B7") );
            Assert.assertEquals("node of OL17007", c, SerialAddress.getNodeFromSystemName("OL17007") );
            Assert.assertEquals("node of OL17B7", c, SerialAddress.getNodeFromSystemName("OL17B7") );
            Assert.assertEquals("node of OL11007", null, SerialAddress.getNodeFromSystemName("OL11007") );
            Assert.assertEquals("node of OL11B7", null, SerialAddress.getNodeFromSystemName("OL11B7") );
        }

	public void testValidSystemNameConfig() {
            SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
            d.setNumBitsPerCard (32);
            d.setCardTypeByAddress (0,SerialNode.INPUT_CARD);
            d.setCardTypeByAddress (1,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (2,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (3,SerialNode.OUTPUT_CARD);
            d.setCardTypeByAddress (4,SerialNode.INPUT_CARD);
            d.setCardTypeByAddress (5,SerialNode.OUTPUT_CARD);
            SerialNode c = new SerialNode(10,SerialNode.SMINI);
            Assert.assertTrue("valid config OL4007", SerialAddress.validSystemNameConfig("OL4007",'L') );
            Assert.assertTrue("valid config OL4B7", SerialAddress.validSystemNameConfig("OL4B7",'L') );
            Assert.assertTrue("valid config OS10007", SerialAddress.validSystemNameConfig("OS10007",'S') );
            Assert.assertTrue("valid config OS10B7", SerialAddress.validSystemNameConfig("OS10B7",'S') );
            Assert.assertTrue("valid config OL10048", SerialAddress.validSystemNameConfig("OL10048",'L') );
            Assert.assertTrue("valid config OL10B48", SerialAddress.validSystemNameConfig("OL10B48",'L') );
            Assert.assertTrue("invalid config OL10049", !SerialAddress.validSystemNameConfig("OL10049",'L') );
            Assert.assertTrue("invalid config OL10B49", !SerialAddress.validSystemNameConfig("OL10B49",'L') );
            Assert.assertTrue("valid config OS10024", SerialAddress.validSystemNameConfig("OS10024",'S') );
            Assert.assertTrue("valid config OS10B24", SerialAddress.validSystemNameConfig("OS10B24",'S') );
            Assert.assertTrue("invalid config OS10025", !SerialAddress.validSystemNameConfig("OS10025",'S') );
            Assert.assertTrue("invalid config OS10B25", !SerialAddress.validSystemNameConfig("OS10B25",'S') );
            Assert.assertTrue("valid config OT4128", SerialAddress.validSystemNameConfig("OT4128",'T') );
            Assert.assertTrue("valid config OT4B128", SerialAddress.validSystemNameConfig("OT4B128",'T') );
            Assert.assertTrue("invalid config OT4129", !SerialAddress.validSystemNameConfig("OT4129",'T') );
            Assert.assertTrue("invalid config OT4129", !SerialAddress.validSystemNameConfig("OT4B129",'T') );
            Assert.assertTrue("valid config OS4064", SerialAddress.validSystemNameConfig("OS4064",'S') );
            Assert.assertTrue("valid config OS4B64", SerialAddress.validSystemNameConfig("OS4B64",'S') );
            Assert.assertTrue("invalid config OS4065", !SerialAddress.validSystemNameConfig("OS4065",'S') );
            Assert.assertTrue("invalid config OS4B65", !SerialAddress.validSystemNameConfig("OS4B65",'S') );
            Assert.assertTrue("invalid config OL11007", !SerialAddress.validSystemNameConfig("OL11007",'L') );
            Assert.assertTrue("invalid config OL11B7", !SerialAddress.validSystemNameConfig("OL11B7",'L') );
        }        
        
	public void testConvertSystemNameFormat() {
            Assert.assertEquals("convert OL14007", "OL14B7", SerialAddress.convertSystemNameToAlternate("OL14007") );
            Assert.assertEquals("convert OS7", "OS0B7", SerialAddress.convertSystemNameToAlternate("OS7") );
            Assert.assertEquals("convert OT4007", "OT4B7", SerialAddress.convertSystemNameToAlternate("OT4007") );
            Assert.assertEquals("convert OL14B7", "OL14007", SerialAddress.convertSystemNameToAlternate("OL14B7") );
            Assert.assertEquals("convert OL0B7", "OL7", SerialAddress.convertSystemNameToAlternate("OL0B7") );
            Assert.assertEquals("convert OS4B7", "OS4007", SerialAddress.convertSystemNameToAlternate("OS4B7") );
            Assert.assertEquals("convert OL14B8", "OL14008", SerialAddress.convertSystemNameToAlternate("OL14B8") );
            Assert.assertEquals("convert OL128B7", "", SerialAddress.convertSystemNameToAlternate("OL128B7") );
        }
        
	public void testNormalizeSystemName() {
            Assert.assertEquals("normalize OL14007", "OL14007", SerialAddress.normalizeSystemName("OL14007") );
            Assert.assertEquals("normalize OL007", "OL7", SerialAddress.normalizeSystemName("OL007") );
            Assert.assertEquals("normalize OL004007", "OL4007", SerialAddress.normalizeSystemName("OL004007") );
            Assert.assertEquals("normalize OL14B7", "OL14B7", SerialAddress.normalizeSystemName("OL14B7") );
            Assert.assertEquals("normalize OL0B7", "OL0B7", SerialAddress.normalizeSystemName("OL0B7") );
            Assert.assertEquals("normalize OL004B7", "OL4B7", SerialAddress.normalizeSystemName("OL004B7") );
            Assert.assertEquals("normalize OL014B0008", "OL14B8", SerialAddress.normalizeSystemName("OL014B0008") );
            Assert.assertEquals("normalize OL128B7", "", SerialAddress.normalizeSystemName("OL128B7") );
        }
        
	// from here down is testing infrastructure

	public SerialAddressTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {SerialAddressTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(SerialAddressTest.class);
            return suite;
	}

}
