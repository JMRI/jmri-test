// LoadXmlConfigAction.java

package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

/**
 * Load configuration information from an XML file.
 * <P>
 * The file context for this is the "config" file chooser.
 * <P>
 * This will load whatever information types are present in the file.
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2002
 * @version	    $Revision$
 * @see             jmri.jmrit.XmlFile
 */
public class LoadXmlConfigAction extends LoadStoreBaseAction {

    public LoadXmlConfigAction() {
        this("Open Panel File ...");
    }

    public LoadXmlConfigAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        loadFile(configFileChooser);
    }
    
    /**
     * 
     * @param fileChooser
     * @return true if successful
     */
    protected boolean loadFile(JFileChooser fileChooser) {
    	boolean results = false;
        java.io.File file = getFile(fileChooser);
        if (file!=null)
            try {
                results = InstanceManager.configureManagerInstance().load(file);
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile: "+e);
            }
        else
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        return results;
    }
   
    static public java.io.File getFile(JFileChooser fileChooser) {
	fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
	return getFileCustom(fileChooser);
    }
 
    static public java.io.File getFileCustom(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(null,null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
        if (log.isDebugEnabled()) log.debug("Open file: "+fileChooser.getSelectedFile().getPath());
        return fileChooser.getSelectedFile();
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(LoadXmlConfigAction.class.getName());

}
