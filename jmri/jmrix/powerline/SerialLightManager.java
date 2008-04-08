// SerialLightManager.java

package jmri.jmrix.powerline;

import jmri.AbstractLightManager;
import jmri.Light;

/**
 * Implement light manager for powerline serial systems
 * <P>
 * System names are "PLnnn", where nnn is the bit number without padding.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version	$Revision: 1.4 $
 */
abstract public class SerialLightManager extends AbstractLightManager {

    public SerialLightManager() {
        _instance = this;
    }

    /**
     *  Returns the system letter
     */
    public char systemLetter() { return 'P'; }
    
    /**
     * Method to create a new Light based on the system name
     * Returns null if the system name is not in a valid format
     * Assumes calling method has checked that a Light with this
     *    system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if ( SerialAddress.validSystemNameFormat(systemName,'L') ) {
            lgt = createNewSpecificLight(systemName,userName); 
            if (!SerialAddress.validSystemNameConfig(systemName,'L')) {
                log.warn("Light system Name does not refer to configured hardware: "
                                                            +systemName);
            }
        }
        else {
            log.error("Invalid Light system Name format: "+systemName);
        }
        return lgt;
    }    

    /** 
     * Create light of a specific type for the interface
     */
    abstract protected Light createNewSpecificLight(String systemName, String userName);
    
    /**
     * Public method to validate system name format
     *   returns 'true' if system name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName,'L'));
        }

    /**
     * Public method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName,'L'));
    }
    
    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, 
     *      else returns "".
     */
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }
    
    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a 
     *      valid alternate representation, else return "".
     */
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName));
    }
    
    /** 
     * Allow access to SerialLightManager
     */
    static public SerialLightManager instance() {
        if (_instance == null) {
            log.error("instance called without manager loaded");
            _instance = new SerialLightManager(){
                protected Light createNewSpecificLight(String systemName, String userName){return null;}
            };
        }
        return _instance;
    }
    static SerialLightManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLightManager.class.getName());

}

/* @(#)SerialLighttManager.java */
