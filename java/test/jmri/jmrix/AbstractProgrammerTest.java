package jmri.jmrix;

import java.util.List;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the AbstractProgrammer class
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 * @version $Revision$
 */
public class AbstractProgrammerTest extends NbTestCase {

    public AbstractProgrammerTest(String s) {
        super(s);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testRegisterFromCV() {
        AbstractProgrammer abstractprogrammer = new AbstractProgrammer() {
            public void writeCV(int i, int j, ProgListener l) {
            }

            public void confirmCV(int i, int j, ProgListener l) {
            }

            public void readCV(int i, ProgListener l) {
            }

            public List<ProgrammingMode> getSupportedModes() {
                return null;
            }

            public void timeout() {
            }

            public boolean getCanRead() {
                return true;
            }
        };

        int cv1 = -1;

        try {
            Assert.assertEquals("test CV 1", 1,
                    abstractprogrammer.registerFromCV(cv1 = 1));
            Assert.assertEquals("test CV 2", 2,
                    abstractprogrammer.registerFromCV(cv1 = 2));
            Assert.assertEquals("test CV 3", 3,
                    abstractprogrammer.registerFromCV(cv1 = 3));
            Assert.assertEquals("test CV 4", 4,
                    abstractprogrammer.registerFromCV(cv1 = 4));
            Assert.assertEquals("test CV 29", 5,
                    abstractprogrammer.registerFromCV(cv1 = 29));
            Assert.assertEquals("test CV 7", 7,
                    abstractprogrammer.registerFromCV(cv1 = 7));
            Assert.assertEquals("test CV 8", 8,
                    abstractprogrammer.registerFromCV(cv1 = 8));
        } catch (Exception e) {
            Assert.fail("unexpected exception while cv = " + cv1);
        }

        // now try for some exceptions
        for (cv1 = 5; cv1 < 29; cv1++) {
            if (cv1 == 7 || cv1 == 8) {
                continue;
            }
            try {
                abstractprogrammer.registerFromCV(cv1); // should assert
                Assert.fail("did not throw as expected for cv = " + cv1);
            } catch (Exception e) {
            }
        }
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(AbstractProgrammerTest.class);
        return suite;
    }

}
