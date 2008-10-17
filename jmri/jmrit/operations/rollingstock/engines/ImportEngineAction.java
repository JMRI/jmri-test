// ImportEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrix.nce.consist.NceConsistBackup;

import javax.swing.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

import jmri.util.StringUtil;


/**
 * This routine will import engines into the operation database.
 * 
 * Each field is space delimited.  Field order:
 * Number Road Type Length Owner Year Location
 * Note that all fields must be single words except for Location.
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */


public class ImportEngineAction extends AbstractAction {
	
    public ImportEngineAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportEngines();
		mb.setName("ImportEngines");
		mb.start();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(ImportEngineAction.class.getName());
}
