package jmri.jmrix.lenz.swing.mon;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNetMonPaneTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.mon.XNetMonPane class
 *
 * @author	Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class XNetMonPaneTest extends NbTestCase {

    public void testCtor() {
        XNetMonPane f = new XNetMonPane();
        Assert.assertNotNull(f);
    }

    public void testDefault() {
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);

        jmri.util.swing.JmriNamedPaneAction f = new XNetMonPane.Default();
        Assert.assertNotNull(f);
        jmri.InstanceManager.deregister(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
    }

    // from here down is testing infrastructure
    public XNetMonPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetMonPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XNetMonPaneTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(XNetMonPaneTest.class.getName());

}
