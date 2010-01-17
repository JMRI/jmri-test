// FileHistoryTest.java

package jmri.jmrit.revhistory;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.revhistory package & jmrit.revhistory.FileHistory class.
 * @author	Bob Jacobsen     Copyright (C) 2010
 * @version     $Revision: 1.1 $
 */
public class FileHistoryTest extends TestCase {

    public void testCtor() {
        new FileHistory();
    }

    public void testAdd2() {
        FileHistory r = new FileHistory();
        r.addOperation("load", "foo.xml", null);
        r.addOperation("load", "bar.xml", null);
        
        Assert.assertEquals(2, r.list.size());

        Assert.assertEquals("foo.xml", r.list.get(0).filename);
        Assert.assertEquals("bar.xml", r.list.get(1).filename);

        Assert.assertEquals("load", r.list.get(0).type);
        Assert.assertEquals("load", r.list.get(1).type);
    }

    public void testNest() {
        FileHistory r1 = new FileHistory();
        r1.addOperation("load", "date 1", "file 1", null);

        Assert.assertEquals(1, r1.list.size());
        
        FileHistory r2 = new FileHistory();
        r2.addOperation("load", "date 2", "file 2", r1);
        
        Assert.assertEquals(1, r2.list.size());

        Assert.assertEquals("file 1",r2.list.get(0).history.list.get(0).filename);
        Assert.assertEquals("date 1", r2.list.get(0).history.list.get(0).date);
    }

    public void testToString() {
        FileHistory r1 = new FileHistory();
        r1.addOperation("load", "date 1", "file 1", null);

        
        FileHistory r2 = new FileHistory();
        r2.addOperation("load", "date 2", "file 2", r1);
        r2.addOperation("load", "date 3", "file 3", null);

        String result = r2.toString();
        String expected = "date 2: load file 2\n"+
                          "    date 1: load file 1\n"+
                          "date 3: load file 3\n";

        Assert.assertEquals(expected, result);
    }
    

    // from here down is testing infrastructure

    public FileHistoryTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", FileHistoryTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FileHistoryTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
