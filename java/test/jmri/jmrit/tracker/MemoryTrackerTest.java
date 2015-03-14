// MemoryTrackerTest.java
package jmri.jmrit.tracker;

import jmri.Block;
import jmri.InstanceManager;
import jmri.MemoryManager;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests for the MemoryTracker class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 */
public class MemoryTrackerTest extends NbTestCase {

    public void testDirectCreate() {
        MemoryManager m = InstanceManager.memoryManagerInstance();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        m.provideMemory("dummy");
        // check for exception in ctor
        new MemoryTracker(new Block("dummy"), "");
    }

    // from here down is testing infrastructure
    public MemoryTrackerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MemoryTrackerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MemoryTrackerTest.class);
        return suite;
    }

}
