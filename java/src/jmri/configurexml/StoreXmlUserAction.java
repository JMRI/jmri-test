// StoreXmlUserAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

/**
 * Store the JMRI user-level information as XML.
 * <P>
 * Note that this does not store preferences, configuration, or tool information
 * in the file.  This is not a complete store!
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlUserAction extends StoreXmlConfigAction {

    public StoreXmlUserAction() {
        this(
            java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle")
                .getString("MenuItemStore"));
    }

    public StoreXmlUserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        String oldButtonText=userFileChooser.getApproveButtonText();
        String oldDialogTitle=userFileChooser.getDialogTitle();
        int oldDialogType=userFileChooser.getDialogType();
	userFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        userFileChooser.setApproveButtonText(java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle").getString("MenuItemStore"));
        userFileChooser.setDialogTitle(java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle").getString("MenuItemStore"));
        java.io.File file = getFileCustom(userFileChooser);
 
        if (file==null) return;
        
        // make a backup file
        InstanceManager.configureManagerInstance().makeBackup(file);
        // and finally store
        InstanceManager.configureManagerInstance().storeUser(file);

        // The last thing we do is restore the Approve button text.
	userFileChooser.setDialogType(oldDialogType);
        userFileChooser.setApproveButtonText(oldButtonText);
	userFileChooser.setDialogTitle(oldDialogTitle);
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StoreXmlUserAction.class.getName());
}
