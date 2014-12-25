// ImportEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportEngines thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ImportEngineAction extends AbstractAction {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -2708260245204450029L;

	public ImportEngineAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportEngines();
		mb.setName("Import Engines"); // NOI18N
		mb.start();
	}

	static Logger log = LoggerFactory
	.getLogger(ImportEngineAction.class.getName());
}
