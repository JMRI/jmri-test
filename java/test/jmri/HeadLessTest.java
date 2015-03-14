// HeadLessTest.java
package jmri;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Invoke complete set of tests for the jmri package
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 * @version $Revision$
 */
public class HeadLessTest extends NbTestCase {

    // from here down is testing infrastructure
    public HeadLessTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        // force headless operation
        System.setProperty("java.awt.headless", "true");
        System.setProperty("jmri.headlesstest", "true");

        // start tests
        String[] testCaseName = {"-noloading", HeadLessTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        apps.tests.AllTest.initLogging();
        NbTestSuite suite = new NbTestSuite("jmri.JmriTest");  // no tests in this class itself

        suite.addTest(jmri.PackageTest.suite());
        suite.addTest(apps.PackageTest.suite());

        return suite;
    }

}
