// SerialTurnoutManager.java

package jmri.jmrix.tmcc;

import jmri.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for TMCC serial systems
 * <P>
 * System names are "TTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 * @version	$Revision: 1.2 $
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'T'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName=="") {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t!=null) {
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
        if (t!=null) {
            return null;
        }
        // create the turnout
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SerialTurnout(addr);
        t.setUserName(userName);
        
        // does system name correspond to configured hardware
        if ( !SerialAddress.validSystemNameConfig(sName,'T') ) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '"+sName+"' refers to an undefined Serial Node.");
        }
        return t;
    }

    static public SerialTurnoutManager instance() {
        if (_instance == null) _instance = new SerialTurnoutManager();
        return _instance;
    }
    static SerialTurnoutManager _instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */
