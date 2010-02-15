// LoadStoreBaseAction.java

package jmri.configurexml;

import jmri.InstanceManager;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Base implementation for the load and store actions.
 * <P>
 * Primarily provides file checking services to the 
 * specific subclasses that load/store particular types of data.
 * <P>
 * Also used to
 * hold common information, specifically common instances of
 * the JFileChooser. These bring the user back to the same
 * place in the file system each time an action is invoked.
 *
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @version	$Revision: 1.6 $
 * @see         jmri.jmrit.XmlFile
 */
abstract public class LoadStoreBaseAction extends AbstractAction {

    public LoadStoreBaseAction(String s) {
        super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());
    }

    static JFileChooser allFileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
    static JFileChooser configFileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
    public static JFileChooser userFileChooser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

    static {  // static class initialization
        jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("XML files");
        filt.addExtension("xml");
        allFileChooser.setFileFilter(filt);
        configFileChooser.setFileFilter(filt);
        userFileChooser.setFileFilter(filt);
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadStoreBaseAction.class.getName());

}
