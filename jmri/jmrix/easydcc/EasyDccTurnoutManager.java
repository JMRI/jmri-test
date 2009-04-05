// EasyDccTurnoutManager.java

package jmri.jmrix.easydcc;

import jmri.Turnout;

/**
 * Implement turnout manager for EasyDcc systems
 * <P>
 * System names are "ETnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.11 $
 */

public class EasyDccTurnoutManager extends jmri.AbstractTurnoutManager {

    public EasyDccTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'E'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new EasyDccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public EasyDccTurnoutManager instance() {
        if (_instance == null) _instance = new EasyDccTurnoutManager();
        return _instance;
    }
    static EasyDccTurnoutManager _instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EasyDccTurnoutManager.class.getName());

}

/* @(#)EasyDccTurnoutManager.java */
