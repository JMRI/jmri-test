// AbstractPortControllerTest.java
package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ResourceBundle;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class AbstractPortControllerTest extends NbTestCase {

    public void testisDirtyNotNPE() {
        apc.isDirty();
    }

    // from here down is testing infrastructure
    AbstractPortController apc;

    public AbstractPortControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractPortControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(AbstractPortControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() {
        apc = new AbstractPortControllerScaffold();
    }

    public static class AbstractPortControllerScaffold extends AbstractPortController {

        public AbstractPortControllerScaffold() {
            super(new SystemConnectionMemo("", "") {

                @Override
                protected ResourceBundle getActionModelResourceBundle() {
                    return null;
                }
            });
        }

        @Override
        public DataInputStream getInputStream() {
            return null;
        }

        @Override
        public DataOutputStream getOutputStream() {
            return null;
        }

        @Override
        public String getCurrentPortName() {
            return "";
        }

        @Override
        public void dispose() {
            super.dispose();
        }

        @Override
        public void recover() {
        }

        @Override
        public void connect() {
        }

        @Override
        public void configure() {
        }
    }
}
