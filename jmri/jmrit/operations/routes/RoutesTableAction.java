// RoutesTableAction.java

package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * RoutesTableFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @author 	Daniel Boudreau Copyright (C) 2008
 * @version         $Revision: 1.3 $
 */
public class RoutesTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

    public RoutesTableAction(String s) {
    	super(s);
    }

    RoutesTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
    	if (f == null || !f.isVisible()){
    		f = new RoutesTableFrame();
     	}
    	f.setExtendedState(f.NORMAL);
    	f.setVisible(true);
    }
}

/* @(#)RoutesTableAction.java */
