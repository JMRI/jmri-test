/** 
 * AbstractTurnoutManager.java
 *
 * Description:		Abstract base implementation of the TurnoutManager interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;

import java.util.Hashtable;


public abstract class AbstractTurnoutManager 
		implements TurnoutManager, java.beans.PropertyChangeListener {

	// abstract methods to be provided by subclasses
	public abstract Turnout newTurnout(String systemName, String userName);
	
	// abstract methods to be extended by subclasses
	// to free resources when no longer used
	public void dispose() throws JmriException {
		_tsys.clear();
		_tuser.clear();
	}

	// implemented methods
	protected Hashtable _tsys = new Hashtable();   // stores known Turnout instances by system name
	protected Hashtable _tuser = new Hashtable();   // stores known Turnout instances by user name

	public Turnout getBySystemName(String key) {
		return (Turnout)_tsys.get(key);
	}
	public Turnout getByAddress(TurnoutAddress key) {
		Turnout t = (Turnout)_tuser.get(key.getUserName());
		if (t != null) return t;
		return (Turnout)_tsys.get(key.getSystemName());
	}
	public Turnout getByUserName(String key) {
		return (Turnout)_tuser.get(key);
	}

	// keep track of Turnout user name changes
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("UserName")) {
			String old = (String) e.getOldValue();
			String now = (String) e.getNewValue();
			Turnout t = getByUserName(old);
			_tuser.remove(old);
			_tuser.put(now, t);
		}
	}
}


/* @(#)AbstractTurnoutManager.java */
