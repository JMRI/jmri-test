// BlockTableActionTest.java
package jmri.jmrit.beantable;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrit.beantable.BlockTableAction class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008
 * @version	$Revision$
 */
public class BlockTableActionTest extends jmri.util.SwingTestCase {

    public void testCreate() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        BlockTableAction ba = new BlockTableAction();
        assertNotNull("BlockTableAction is null!", ba);
        TestHelper.disposeWindow(ba.f, this);
    }

    public void testInvoke() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        BlockTableAction ba = new BlockTableAction();
        ba.actionPerformed(null);

        // create a couple blocks, and see if they show
        InstanceManager.blockManagerInstance().createNewBlock("IB1", "block 1");

        Block b2 = InstanceManager.blockManagerInstance().createNewBlock("IB2", "block 2");
        b2.setDirection(jmri.Path.EAST);
        TestHelper.disposeWindow(ba.f, this);
    }

    // from here down is testing infrastructure
    public BlockTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", BlockTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(BlockTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();

        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(BlockTableActionTest.class.getName());
}
