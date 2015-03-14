// PreferNumericComparatorTest.java
package jmri.util;

import java.util.Comparator;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.StringUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version	$Revision: 24569 $
 */
public class PreferNumericComparatorTest extends NbTestCase {

    public void testCompareNumbersEquals() {
        Comparator<Object> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 == 1", 0, c.compare("1", "1"));
        Assert.assertEquals(" 10 == 10", 0, c.compare("10", "10"));
        Assert.assertEquals(" 100 == 100", 0, c.compare("100", "100"));
    }

    public void testCompareNumbersGreater() {
        Comparator<Object> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 > 0", 1, c.compare("1", "0"));
        Assert.assertEquals(" 10 > 2", 1, c.compare("10", "2"));
        Assert.assertEquals(" 2 > 1", 1, c.compare("2", "1"));
    }

    public void testCompareNumbersLesser() {
        Comparator<Object> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 < 2", -1, c.compare("1", "2"));
        Assert.assertEquals(" 1 < 10", -1, c.compare("1", "10"));
        Assert.assertEquals(" 2 < 10 ", -1, c.compare("2", "10"));
    }

    public void testCompareNestedNumeric() {
        Comparator<Object> c = new PreferNumericComparator();
        Assert.assertEquals(" 1.1.0 < 2.1.0", -1, c.compare("1.1.0", "2.1.0"));
        Assert.assertEquals(" 1.1.1 == 1.1.1", 0, c.compare("1.1.1", "1.1.1"));
        Assert.assertEquals(" 2.1.0 > 1.1.0", 1, c.compare("2.1.0", "1.1.0"));
    }

    // from here down is testing infrastructure
    public PreferNumericComparatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PreferNumericComparatorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PreferNumericComparatorTest.class);
        return suite;
    }

    static Logger log = LoggerFactory.getLogger(PreferNumericComparatorTest.class.getName());

}
