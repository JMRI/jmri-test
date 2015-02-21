// AddSignalMastJFrame.java
package jmri.jmrit.beantable.signalmast;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * JFrame to create a new SignalMast
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 */
public class AddSignalMastJFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -6193330521542648003L;

    public AddSignalMastJFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle")
                .getString("TitleAddSignalMast"), false, true);

        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(sigMastPanel = new AddSignalMastPanel());
        pack();
    }

    public AddSignalMastJFrame(jmri.SignalMast mast) {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle")
                .getString("TitleAddSignalMast"), false, true);

        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSignalMastPanel(mast));
        pack();
    }

    AddSignalMastPanel sigMastPanel = null;

    public void refresh() {
        if (sigMastPanel != null) {
            sigMastPanel.updateSelectedDriver();
            sigMastPanel.refreshHeadComboBox();

        }
    }

}


/* @(#)AddSignalMastJFrame.java */
