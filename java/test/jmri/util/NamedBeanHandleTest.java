// NamedBeanHandleTest.java
package jmri.util;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.NamedBeanUtil class.
 *
 * @author	Bob Jacobsen Copyright 2009
 * @version	$Revision$
 */
public class NamedBeanHandleTest extends NbTestCase {

    public void testCtor() {
        new NamedBeanHandle<Turnout>("", null);
    }

    public void testHoldsTurnout() {
        Turnout t = new AbstractTurnout("name") {
            /**
             *
             */
            private static final long serialVersionUID = -449495740652645244L;

            protected void forwardCommandChangeToLayout(int s) {
            }

            protected void turnoutPushbuttonLockout(boolean b) {
            }
        };
        NamedBeanHandle<Turnout> n = new NamedBeanHandle<Turnout>("name", t);

        Assert.assertEquals("same TO", t, n.getBean());
    }

    // from here down is testing infrastructure
    public NamedBeanHandleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NamedBeanHandleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(NamedBeanHandleTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(NamedBeanHandleTest.class.getName());

}
