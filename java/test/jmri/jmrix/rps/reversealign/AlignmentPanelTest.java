// AlignmentPanelTest.java
package jmri.jmrix.rps.reversealign;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * JUnit tests for the rps.AlignmentPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class AlignmentPanelTest extends NbTestCase {

    public void testShow() {
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        AlignmentPanel panel = new AlignmentPanel();
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
    }

    public void testXFrameCreation() {
        JFrame f = jmri.util.JmriJFrame.getFrame("RPS Alignment");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public AlignmentPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AlignmentPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(AlignmentPanelTest.class);
        return suite;
    }

}
