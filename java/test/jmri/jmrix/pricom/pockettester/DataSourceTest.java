// DataSourceTest.java
package jmri.jmrix.pricom.pockettester;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the DataSource class
 *
 * @author	Bob Jacobsen Copyright 2005
 * @version	$Revision$
 */
public class DataSourceTest extends NbTestCase {

    public void testCreate() {
        Assert.assertTrue("no instance before ctor", DataSource.instance() == null);
        DataSource d = new DataSource();
        Assert.assertTrue("no instance after ctor", DataSource.instance() == null);
        d.initComponents();
        Assert.assertTrue("valid instance after init", DataSource.instance() != null);
    }

    // test version handling
    public void testVersion() {
        DataSource f = new DataSource();
        String message;

        message = "nothing interesing";
        f.nextLine(message);
        Assert.assertTrue("pass misc ", !message.equals(f.version.getText()));

        message = PocketTesterTest.version;
        f.nextLine(message);
        Assert.assertTrue("show version ", message.equals(f.version.getText()));

    }

    // avoid spurious error messages
    void setup() {
        DataSource.existingInstance = null;
    }

    // from here down is testing infrastructure
    public DataSourceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DataSourceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(DataSourceTest.class);
        return suite;
    }

}
