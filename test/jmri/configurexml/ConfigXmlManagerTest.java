// ConfigXmlManagerTest.java

package jmri.configurexml;

import junit.framework.*;
import java.io.*;
import jmri.jmrit.XmlFile;

/**
 * Tests for ConfigXmlManager.
 * <P>
 * Uses the local preferences for test files.
 * @author Bob Jacobsen Copyright 2003
 * @version $Revision: 1.3 $
 */
public class ConfigXmlManagerTest extends TestCase {

    public ConfigXmlManagerTest(String s) {
        super(s);
    }

    boolean innerFlag;

    public void testRegister() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        Object o1=  "";
        innerFlag=false;
        configxmlmanager.register(o1);
        Assert.assertTrue("register didn't find class", innerFlag);
    }

    public void testAdapterName() {
        ConfigXmlManager c = new ConfigXmlManager();
        Assert.assertEquals("String class adapter", "java.lang.configurexml.StringXml",
                            c.adapterName(""));
    }

    public void testFindFile() throws FileNotFoundException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager(){
                void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
                    innerFlag=true;
                }
            };
        File result;
        result = configxmlmanager.find("foo.biff");
        Assert.assertTrue("dont find foo.biff", result==null);
        result = configxmlmanager.find("roster.xml");
        Assert.assertTrue("should find roster.xml", result!=null);

        // make sure no test file exists in "layout"
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"layout");
        File f = new File(XmlFile.prefsDir()+"layout"+File.separator+"testConfigXmlManagerTest.xml");
        f.delete();  // remove it if its there

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        Assert.assertTrue("should not find testConfigXmlManagerTest.xml", result==null);

        // put file back and find
        PrintStream p = new PrintStream (new FileOutputStream(f));
        p.println("stuff"); // load a new one
        p.close();

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        Assert.assertTrue("should find testConfigXmlManagerTest.xml", result!=null);

    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigXmlManagerTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConfigXmlManagerTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
}
