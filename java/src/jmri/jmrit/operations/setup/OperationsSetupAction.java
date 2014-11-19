// OperationsSetupAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a OperationsSetupFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.setup.OperationsSetupAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false,
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuSetup",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4390,
        separatorBefore = 4385)
public class OperationsSetupAction extends AbstractAction {

    
    public OperationsSetupAction(String s) {
    	super(s);
    }

    public OperationsSetupAction() {
    	this(Bundle.getMessage("MenuSetup"));	// NOI18N
    }

    static OperationsSetupFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new OperationsSetupFrame();
    		f.initComponents();
    	}
        f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)OperationsSetupAction.java */
