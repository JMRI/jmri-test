// AutoSave.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsXml;

/**
 * Auto Save.  When enabled will automatically save operation files.
 * 
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */

public class AutoSave {
	
	static Thread autoSave = null;
	
	public AutoSave(){
		if (Setup.isAutoSaveEnabled() && autoSave == null){
			autoSave = new Thread(new Runnable() {
				public void run() {
					saveFiles();
				}
			});
			autoSave.setName("Auto Save");
			autoSave.start();
		}
	}
	
	private synchronized void saveFiles(){
		while (true){			
			try {
				wait(60000);	// check every minute
			} catch (InterruptedException e) {}
			if (!Setup.isAutoSaveEnabled())
				break;
			if (OperationsXml.areFilesDirty()){
				log.debug("Detected dirty files");
				try {
					wait(60000);	// wait another minute before saving
				} catch (InterruptedException e) {}
				OperationsXml.save();
				log.info("Operations files automatically saved");
			}
		}
		autoSave = null;	// done
	}	
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoSave.class.getName());
}
