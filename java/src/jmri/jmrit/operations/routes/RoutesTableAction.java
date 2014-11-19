// RoutesTableAction.java

package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a
 * RoutesTableFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @author 	Daniel Boudreau Copyright (C) 2008
 * @version         $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.routes.RoutesTableAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false,
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuRoutes",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4370)
public class RoutesTableAction extends AbstractAction {

    public RoutesTableAction(String s) {
    	super(s);
    }

    public RoutesTableAction() {
    	this(Bundle.getMessage("MenuRoutes"));	// NOI18N
    }

    static RoutesTableFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
    	if (f == null || !f.isVisible()){
    		f = new RoutesTableFrame();
     	}
    	f.setExtendedState(Frame.NORMAL);
       	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)RoutesTableAction.java */
