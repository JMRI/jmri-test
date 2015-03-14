// NceSensorManagerTest.java
package jmri.jmrix.nce;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the NceAIU class.
 *
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision$
 */
public class NceSensorManagerTest extends NbTestCase {

    public void testNceSensorCreate() {
        // prepare an interface
        NceInterfaceScaffold lnis = new NceInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        // create and register the manager object
        NceSensorManager n = new NceSensorManager(lnis, "N");
        jmri.InstanceManager.setSensorManager(n);
    }

    // from here down is testing infrastructure
    public NceSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(NceSensorManagerTest.class);
        return suite;
    }

}
