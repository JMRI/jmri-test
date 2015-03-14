// AutomatTest.java
package jmri.jmrit.automat;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for classes in the jmri.jmrit.automat package
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class AutomatTest extends NbTestCase {

    boolean initDone;
    boolean handleDone;

    public void testCreate() {
        new AbstractAutomaton() {
        };
    }

    public void testRun() throws InterruptedException {
        initDone = false;
        handleDone = false;
        AbstractAutomaton a = new AbstractAutomaton() {
            public void init() {
                initDone = true;
            }

            public boolean handle() {
                handleDone = true;
                return false;
            }
        };
        Assert.assertTrue("!initDone at first", !initDone);
        Assert.assertTrue("!handleDone at first", !handleDone);

        // now run it
        a.start();

        // wait so thread can exec
        synchronized (this) {
            wait(100);
        }

        // and check
        Assert.assertTrue("initDone after run", initDone);
        Assert.assertTrue("handleDone after run", handleDone);
    }

    public void testRestart() throws InterruptedException {
        initDone = false;
        handleDone = false;
        AbstractAutomaton a = new AbstractAutomaton() {
            public void init() {
                initDone = true;
            }

            public boolean handle() {
                handleDone = true;
                return false;
            }
        };
        Assert.assertTrue("!initDone at first", !initDone);
        Assert.assertTrue("!handleDone at first", !handleDone);

        // now run it
        a.start();

        // wait so thread can exec
        synchronized (this) {
            wait(100);
        }

        // and check
        Assert.assertTrue("initDone after run", initDone);
        Assert.assertTrue("handleDone after run", handleDone);

        // restart
        initDone = false;
        handleDone = false;
        Assert.assertTrue("!initDone at second", !initDone);
        Assert.assertTrue("!handleDone at second", !handleDone);

        // now run it again
        a.start();

        // wait so thread can exec
        synchronized (this) {
            wait(100);
        }

        // and check
        Assert.assertTrue("initDone after 2nd run", initDone);
        Assert.assertTrue("handleDone after 2nd run", handleDone);
    }

    // from here down is testing infrastructure
    public AutomatTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AutomatTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(AutomatTest.class);
        // suite.addTest(RouteTableActionTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(AutomatTest.class.getName());

}
