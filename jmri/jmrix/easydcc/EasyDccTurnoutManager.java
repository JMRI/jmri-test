/**
 * EasyDccTurnoutManager.java
 *
 * Description:		Implement turnout manager for EasyDcc systems
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: EasyDccTurnoutManager.java,v 1.5 2003-04-08 22:08:06 jacobsen Exp $
 */

// System names are "ETnnn", where nnn is the turnout number without padding.

package jmri.jmrix.easydcc;

import jmri.JmriException;
import jmri.Turnout;

public class EasyDccTurnoutManager extends jmri.AbstractTurnoutManager {

    final String prefix = "ET";

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter() { return prefix.charAt(0); }

	// to free resources when no longer used
	public void dispose() throws JmriException {
		super.dispose();
	}

	// EasyDcc-specific methods

	public void putBySystemName(EasyDccTurnout t) {
		String system = prefix+t.getNumber();
		_tsys.put(system, t);
	}

	public Turnout newTurnout(String systemName, String userName) {
		// if system name is null, supply one from the number in userName
		if (systemName == null) systemName = prefix+userName;

		// return existing if there is one
		Turnout t;
		if ( (userName != null) && ((t = getByUserName(userName)) != null)) return t;
		if ( (t = getBySystemName(systemName)) != null) return t;

		// get number from name
		if (!systemName.startsWith(prefix)) {
			log.error("Invalid system name for EasyDcc turnout: "+systemName);
			return null;
		}
		int addr = Integer.valueOf(systemName.substring(2)).intValue();
		t = new EasyDccTurnout(addr);
		t.setUserName(userName);

		_tsys.put(systemName, t);
		if (userName!=null) _tuser.put(userName, t);
		t.addPropertyChangeListener(this);

		return t;
	}

	public EasyDccTurnoutManager() {
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccTurnoutManager.class.getName());

}


/* @(#)EasyDccTurnoutManager.java */
