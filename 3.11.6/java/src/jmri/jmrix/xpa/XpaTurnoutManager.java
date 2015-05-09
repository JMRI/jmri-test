// XpaTurnoutManager.java
package jmri.jmrix.xpa;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Xpa+Modem connections to XPressNet Based
 * systems.
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class XpaTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public XpaTurnoutManager() {

    }

    public String getSystemPrefix() {
        return "P";
    }

    // Xpa-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new XpaTurnout(addr);
        t.setUserName(userName);
        return t;
    }

    static public XpaTurnoutManager instance() {
        if (_instance == null) {
            _instance = new XpaTurnoutManager();
        }
        return _instance;
    }
    static XpaTurnoutManager _instance = null;

    static Logger log = LoggerFactory.getLogger(XpaTurnoutManager.class.getName());

}

/* @(#)XpaTurnoutManager.java */
