// StoreXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Store the JMRI configuration information as XML.
 * <P>
 * Note that this does not store preferences, tools or user information
 * in the file.  This is not a complete store!
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.10 $
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlConfigAction extends LoadStoreBaseAction {

    public StoreXmlConfigAction() {
        this("Store configuration ...");
    }

    public StoreXmlConfigAction(String s) {
        super(s);
    }

    /**
     * Do the filename handling:
     *<OL>
     *<LI>rescan directory to see any new files
     *<LI>Prompt user to select a file
     *<LI>if that file exists, check with user
     *</OL>
     * Returns null if selection failed for any reason
     */
    protected File getFileName(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
        
        File file = fileChooser.getSelectedFile();
        if (log.isDebugEnabled()) log.debug("Save file: "+file.getPath());
        // check for possible overwrite
        if (file.exists()) {
            int selectedValue = JOptionPane.showConfirmDialog(null,
                                                                 "File "+file.getName()+" already exists, overwrite it?",
                                                                 "Overwrite file?",
                                                                 JOptionPane.OK_CANCEL_OPTION);
            if (selectedValue != JOptionPane.OK_OPTION) return null;
        }
        return file;
    }
    
    public void actionPerformed(ActionEvent e) {
        File file = getFileName(configFileChooser);
        if (file==null) return;
        
        // and finally store
        InstanceManager.configureManagerInstance().storeConfig(file);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlConfigAction.class.getName());
}
