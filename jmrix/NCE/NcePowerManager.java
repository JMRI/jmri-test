/** 
 * NcePowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

public class NcePowerManager implements PowerManager, NceListener {

	public NcePowerManager() {
		// connect to the TrafficManager
		tc = NceTrafficController.instance();
		tc.addNceListener(~0, this);
	}

	int power = UNKNOWN;

	boolean waiting = false;
	int onReply = UNKNOWN;
		
	public void setPower(int v) {
		power = v;
		if (v==ON) {
			// send "Enable main track"
			NceMessage l = new NceMessage(1);
			l.setOpCode('E');
			tc.sendNceMessage(l);
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.ON;
			firePropertyChange("Power", null, null);
		} else if (v==OFF) {
			// send "Kill main track"
			NceMessage l = new NceMessage(1);
			l.setOpCode('K');
			tc.sendNceMessage(l);
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.ON;
			firePropertyChange("Power", null, null);
		}
	}
	
	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeNceListener(~0, this);
		tc = null;
	}

	// to hear of changes
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) { 
		pcs.addPropertyChangeListener(l);
		}
	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) { 
		pcs.removePropertyChangeListener(l);
		}
	
	NceTrafficController tc = null;

	// to listen for status changes from NCE system
	public void message(NceMessage m) {
		if (waiting) {  
			power = onReply;
			firePropertyChange("Power", null, null);
		}
		waiting = false;
	}
	
}


/* @(#)NcePowerManager.java */
