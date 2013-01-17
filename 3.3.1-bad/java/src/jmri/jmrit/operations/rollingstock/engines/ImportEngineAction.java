// ImportEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportEngines thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ImportEngineAction extends AbstractAction {
	
    public ImportEngineAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportEngines();
		mb.setName("Import Engines"); // NOI18N
		mb.start();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ImportEngineAction.class.getName());
}
