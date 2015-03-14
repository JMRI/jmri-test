// X10SequenceTest.java
package jmri.jmrix.powerline;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the X10Sequence class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @author	Dave Duchamp multi-node extensions 2003
 * @version	$Revision$
 */
public class X10SequenceTest extends NbTestCase {

    public void testCtors() {
        new X10Sequence();
    }

    public void testSequence() {
        X10Sequence s = new X10Sequence();
        s.addAddress(1, 2);
        s.addFunction(1, 3, 0);

        // 
        s.reset();
        X10Sequence.Command a1 = s.getCommand();
        Assert.assertTrue("1 is address", a1.isAddress());
        Assert.assertTrue("1 is not function", !a1.isFunction());

        X10Sequence.Command a2 = s.getCommand();
        Assert.assertTrue("2 is not address", !a2.isAddress());
        Assert.assertTrue("2 is function", a2.isFunction());

        X10Sequence.Command a3 = s.getCommand();
        Assert.assertTrue("3 is null", a3 == null);

    }

    // from here down is testing infrastructure
    public X10SequenceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {X10SequenceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(X10SequenceTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
