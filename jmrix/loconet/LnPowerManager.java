/** 
 * LnPowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

public class LnPowerManager implements PowerManager, LocoNetListener {

	public LnPowerManager() {
		// connect to the TrafficManager
		tc = LnTrafficController.instance();
		tc.addLocoNetListener(~0, this);
	}

	int power = UNKNOWN;
	
	public void setPower(int v) {
		power = v;
		if (v==ON) {
			// send GPON
			LocoNetMessage l = new LocoNetMessage(2);
			l.setOpCode(LnConstants.OPC_GPON);
			tc.sendLocoNetMessage(l);
		} else if (v==OFF) {
			// send GPOFF
			LocoNetMessage l = new LocoNetMessage(2);
			l.setOpCode(LnConstants.OPC_GPOFF);
			tc.sendLocoNetMessage(l);
		}
	}
	
	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeLocoNetListener(~0, this);
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
	
	LnTrafficController tc = null;

	// to listen for status changes from LocoNet
	public void message(LocoNetMessage m) {
		if (m.getOpCode() == LnConstants.OPC_GPON) {
			power = ON;
			firePropertyChange("Power", null, null);
		}
		else if (m.getOpCode() == LnConstants.OPC_GPOFF) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
	}
	
}


/* @(#)LnPowerManager.java */
