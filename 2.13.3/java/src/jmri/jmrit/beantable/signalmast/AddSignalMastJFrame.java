// AddSignalMastJFrame.java

package jmri.jmrit.beantable.signalmast;

import jmri.util.JmriJFrame;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JFrame to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision$
 */

public class AddSignalMastJFrame extends JmriJFrame {

    public AddSignalMastJFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle")
                .getString("TitleAddSignalMast"),false, true);
        
        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSignalMastPanel());
        pack();
    }
    
}


/* @(#)AddSignalMastJFrame.java */
