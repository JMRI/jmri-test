// SignalHeadTableActionTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.implementation.QuadOutputSignalHead;
import jmri.implementation.DoubleTurnoutSignalHead;
import jmri.implementation.SE8cSignalHead;

import jmri.util.JUnitUtil;
import jmri.util.NamedBeanHandle;
/**
 * Tests for the jmri.jmrit.beantable.SignalHeadTableAction class
 * @author	Bob Jacobsen  Copyright 2004, 2007, 2008, 2009
 * @version	$Revision: 1.3 $
 */
public class SignalHeadTableActionTest extends TestCase {

    public void testCreate() {
        new SignalHeadTableAction();
    }

    public void testInvoke() {
        // add a few signals and see if they exist
        InstanceManager.signalHeadManagerInstance().register(
            new DoubleTurnoutSignalHead("IH2", "double example 1-2", 
                new NamedBeanHandle<Turnout>("IT1",InstanceManager.turnoutManagerInstance().provideTurnout("IT1")),
                new NamedBeanHandle<Turnout>("IT2",InstanceManager.turnoutManagerInstance().provideTurnout("IT2"))
        ));
        InstanceManager.signalHeadManagerInstance().register(
            new QuadOutputSignalHead("IH4", "quad example 11-14", 
                new NamedBeanHandle<Turnout>("IT11",InstanceManager.turnoutManagerInstance().provideTurnout("IT11")),
                new NamedBeanHandle<Turnout>("IT12",InstanceManager.turnoutManagerInstance().provideTurnout("IT12")),
                new NamedBeanHandle<Turnout>("IT13",InstanceManager.turnoutManagerInstance().provideTurnout("IT13")),
                new NamedBeanHandle<Turnout>("IT14",InstanceManager.turnoutManagerInstance().provideTurnout("IT14"))
        ));

        InstanceManager.signalHeadManagerInstance().register(
            new SE8cSignalHead( 
                new NamedBeanHandle<Turnout>("IT1",InstanceManager.turnoutManagerInstance().provideTurnout("IT21")),
                new NamedBeanHandle<Turnout>("IT2",InstanceManager.turnoutManagerInstance().provideTurnout("IT22")),
                "SE8c from handles")
        );

        InstanceManager.signalHeadManagerInstance().register(
            new SE8cSignalHead(31, "SE8c from number")
        );

        new SignalHeadTableAction().actionPerformed(null);
        
    }


    // from here down is testing infrastructure

    public SignalHeadTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SignalHeadTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SignalHeadTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp(); 
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();
    }
    
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadTableActionTest.class.getName());
}
