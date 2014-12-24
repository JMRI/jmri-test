// EnginesTableAction.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a EnginesTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.rollingstock.engines.EnginesTableAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false,
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuEngines",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4360)
public class EnginesTableAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 755025320493011103L;

	public EnginesTableAction(String s) {
		super(s);
	}

	public EnginesTableAction() {
		this(Bundle.getMessage("MenuEngines")); // NOI18N
	}

	public void actionPerformed(ActionEvent e) {
		// create a engine table frame
		new EnginesTableFrame();
	}
}

/* @(#)EnginesTableAction.java */
