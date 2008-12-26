// CarsTableAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a OperationsSetupFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */
public class OperationsSetupAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    public OperationsSetupAction(String s) {
    	super(s);
    }

    OperationsSetupFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new OperationsSetupFrame();
    		f.initComponents();
    	}
        f.setExtendedState(f.NORMAL);
        f.setVisible(true);
    }
}

/* @(#)CarsTableAction.java */
