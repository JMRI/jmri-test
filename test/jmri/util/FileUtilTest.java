// FileUtilTest.java

package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.io.*;

import jmri.jmrit.XmlFile;

/**
 * Tests for the jmri.util.FileUtil class.
 * @author	Bob Jacobsen  Copyright 2003, 2009
 * @version	$Revision: 1.11 $
 */
public class FileUtilTest extends TestCase {


    // tests of internal to external mapping
    
    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFRel() {
        String name = FileUtil.getExternalFilename("resources/icons");
        Assert.assertEquals("resources"+File.separator+"icons", name);
    }

    // relative file with no prefix: Leave relative in system-specific form
    public void testGEFAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename(f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // resource: prefix with relative path, convert to relative in system-specific form
    public void testGEFResourceRel() {
        String name = FileUtil.getExternalFilename("resource:resources/icons");
        Assert.assertEquals("resources"+File.separator+"icons", name);
    }

    // resource: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFResourceAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("resource:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // program: prefix with relative path, convert to relative in system-specific form
    public void testGEFProgramRel() {
        String name = FileUtil.getExternalFilename("program:jython");
        Assert.assertEquals("jython", name);
    }

    // program: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFProgramAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("program:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // preference: prefix with relative path, convert to absolute in system-specific form
    public void testGEFPrefRel() {
        String name = FileUtil.getExternalFilename("preference:foo");
        Assert.assertEquals(XmlFile.userFileLocationDefault()+"foo", name);
    }

    // preference: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFPrefAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("preference:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // file: prefix with relative path, convert to absolute in system-specific form
    public void testGEFFileRel() {
        String name = FileUtil.getExternalFilename("file:foo");
        Assert.assertEquals(XmlFile.userFileLocationDefault()+"resources"+File.separator+"foo", name);
    }

    // file: prefix with absolute path, convert to absolute in system-specific form
    public void testGEFFileAbs() {
        File f = new File("resources/icons");
        String name = FileUtil.getExternalFilename("file:"+f.getAbsolutePath());
        Assert.assertEquals(f.getAbsolutePath(), name);
    }

    // tests of external to internal mapping

    @SuppressWarnings("unused")
	public void testGetpfPreferenceF() throws IOException {
        File f = new File(XmlFile.prefsDir()+"foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:foo", name);
    }

    public void testGetpfPreferenceS() {
        String name = FileUtil.getPortableFilename("preference:foo");
        Assert.assertEquals("preference:foo", name);
    }

    @SuppressWarnings("unused")
	public void testGetpfResourceF() throws IOException {
        File f = new File(XmlFile.prefsDir()+"resources"+File.separator+"foo");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/foo", name);
    }

    public void testGetpfResourceS() {
        String name = FileUtil.getPortableFilename("preference:resources/foo");
        Assert.assertEquals("preference:resources/foo", name);
    }

    @SuppressWarnings("unused")
	public void testGetpfPrefF() throws IOException {
        File f = new File(XmlFile.prefsDir()+"resources"+File.separator+"icons");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("preference:resources/icons", name);
    }

    @SuppressWarnings("unused")
	public void testGetpfProgramF() throws IOException {
        File f = new File("resources"+File.separator+"icons");
        String name = FileUtil.getPortableFilename(f);
        Assert.assertEquals("program:resources/icons", name);
    }

    public void testGetpfProgramS() {
        String name = FileUtil.getPortableFilename("program:resources/icons");
        Assert.assertEquals("program:resources/icons", name);
    }

    public void testGetpfFileS() {
        String name = FileUtil.getPortableFilename("file:icons");
        Assert.assertEquals("preference:resources/icons", name);
    }

    public void testGetpfFileS2() {
        String name = FileUtil.getPortableFilename("resource:resources/icons");
        Assert.assertEquals("program:resources/icons", name);
    }

	// from here down is testing infrastructure

	public FileUtilTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {FileUtilTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(FileUtilTest.class);
		return suite;
	}

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileUtilTest.class.getName());

}
