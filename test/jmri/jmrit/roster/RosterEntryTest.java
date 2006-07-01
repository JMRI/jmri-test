package jmri.jmrit.roster;

import jmri.jmrit.*;
import java.io.*;

import junit.framework.*;

/**
 * Tests for the jmrit.roster.RosterEntry class
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002
 * @version	$Revision: 1.4 $
 */
public class RosterEntryTest extends TestCase {

    public void testCreate() {
        RosterEntry r = new RosterEntry("file here");
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "", r.getDccAddress());
        Assert.assertEquals("road name ", "", r.getRoadName());
        Assert.assertEquals("road number ", "", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
    }

    public void testPartialLoad() {
        // create Element
        org.jdom.Element e = new org.jdom.Element("locomotive")
            .addAttribute("id","our id")
            .addAttribute("fileName","file here")
            .addAttribute("roadNumber","431")
            .addAttribute("roadName","SP")
            .addAttribute("mfg","Athearn")
            .addAttribute("dccAddress","1234")
            ; // end create element

        RosterEntry r = new RosterEntry(e);
        // check
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
        Assert.assertEquals("road name ", "SP", r.getRoadName());
        Assert.assertEquals("road number ", "431", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
    }

    public void testEmptyLoad() {
        // create Element
        org.jdom.Element e = new org.jdom.Element("locomotive")
            .addAttribute("id","our id")
            .addAttribute("fileName","file here")
            ; // end create element

        RosterEntry r = new RosterEntry(e);
        // check
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "", r.getDccAddress());
        Assert.assertEquals("road name ", "", r.getRoadName());
        Assert.assertEquals("road number ", "", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "", r.getMfg());
        Assert.assertEquals("model ", "", r.getDecoderModel());
        Assert.assertEquals("family ", "", r.getDecoderFamily());
    }

    public void testFullLoad() {
        // create Element
        org.jdom.Element e = new org.jdom.Element("locomotive")
            .addAttribute("id","our id")
            .addAttribute("fileName","file here")
            .addAttribute("roadNumber","431")
            .addAttribute("roadName","SP")
            .addAttribute("mfg","Athearn")
            .addAttribute("dccAddress","1234")
            .addContent(new org.jdom.Element("decoder")
                        .addAttribute("family","91")
                        .addAttribute("model","33")
                        )
            ; // end create element

        RosterEntry r = new RosterEntry(e);
        // check
        Assert.assertEquals("file name ", "file here", r.getFileName());
        Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
        Assert.assertEquals("road name ", "SP", r.getRoadName());
        Assert.assertEquals("road number ", "431", r.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
        Assert.assertEquals("model ", "33", r.getDecoderModel());
        Assert.assertEquals("family ", "91", r.getDecoderFamily());
    }

    public void testStore() {
        // create Element
        org.jdom.Element e = new org.jdom.Element("locomotive")
            .addAttribute("id","our id")
            .addAttribute("fileName","file here")
            .addAttribute("roadNumber","431")
            .addAttribute("roadName","SP")
            .addAttribute("mfg","Athearn")
            .addAttribute("dccAddress","1234")
            .addContent(new org.jdom.Element("decoder")
                        .addAttribute("family","91")
                        .addAttribute("model","33")
                        )
            ; // end create element

        RosterEntry r = new RosterEntry(e);
        org.jdom.Element o = r.store();
        // check
        Assert.assertEquals("XML Element ", e.toString(), o.toString());
        Assert.assertEquals("family ","91", o.getChild("decoder").getAttribute("family").getValue());
        Assert.assertEquals("model ","33", o.getChild("decoder").getAttribute("model").getValue());
    }

    public void testEnsureFilenameExistsNew() {
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f = new File(XmlFile.prefsDir()+LocoFile.fileLocation+File.separator+"test_Roster_Entry_123456789ABC.xml");
        if (f.exists()) f.delete();
        r.ensureFilenameExists();
        Assert.assertEquals("final filename ", "test_Roster_Entry_123456789ABC.xml", r.getFileName());
    }

    public void testEnsureFilenameExistsOld() throws IOException {
        RosterEntry r = new RosterEntry();
        Assert.assertEquals("initial filename ", null, r.getFileName());
        r.setId("test Roster Entry 123456789ABC");
        Assert.assertEquals("initial ID ", "test Roster Entry 123456789ABC", r.getId());
        File f1 = new File(XmlFile.prefsDir()+LocoFile.fileLocation+File.separator+"test_Roster_Entry_123456789ABC.xml");
        if (!f1.exists()) f1.createNewFile();
        File f2 = new File(XmlFile.prefsDir()+LocoFile.fileLocation+File.separator+"test_Roster_Entry_123456789ABC0.xml");
        if (!f2.exists()) f2.createNewFile();
        r.ensureFilenameExists();
        Assert.assertEquals("final filename ", "test_Roster_Entry_123456789ABC1.xml", r.getFileName());
    }

    // from here down is testing infrastructure

    public RosterEntryTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RosterEntry.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterEntryTest.class);
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
