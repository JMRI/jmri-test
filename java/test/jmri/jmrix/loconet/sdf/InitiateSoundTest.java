// InitiateSoundTest.java
package jmri.jmrix.loconet.sdf;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.sdf.InitiateSound class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class InitiateSoundTest extends NbTestCase {

    public void testCtor() {
        new InitiateSound((byte) 0, (byte) 0);
    }

    // from here down is testing infrastructure
    public InitiateSoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {InitiateSoundTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(InitiateSoundTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(InitiateSoundTest.class.getName());

}
