// LocoNetInterfaceScaffold.java

package jmri.jmrix.loconet;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

import com.sun.java.util.collections.Vector;

/**
 *
 * Description:	 	Test scaffold implementation of LocoNetInterface
 * @author			Bob Jacobsen Copyright (C) 2001, 2006
 * @version
 *
 * Use an object of this type as a LnTrafficController in tests
 */
public class LocoNetInterfaceScaffold extends LnTrafficController {

	public LocoNetInterfaceScaffold() {
		self = this;
	}

	// override some LnTrafficController methods for test purposes

	public boolean status() { return true;
	}

	/**
	 * record LocoNet messages sent, provide access for making sure they are OK
	 */
	public Vector outbound = new Vector();  // public OK here, so long as this is a test class
	public void sendLocoNetMessage(LocoNetMessage m) {
		if (log.isDebugEnabled()) log.debug("sendLocoNetMessage ["+m+"]");
		// save a copy
		outbound.addElement(m);
		// we don't return an echo so that the processing before the echo can be
		// separately tested
	}

    public boolean isXmtBusy() { return false; }
    
	// test control member functions

    /**
     * Forward a message that came from unit under test
     */
    void forwardMessage(int i) {
        sendTestMessage((LocoNetMessage)outbound.elementAt(i));
    }

	/**
	 * forward a message to the listeners, e.g. test receipt
	 */
	public void sendTestMessage (LocoNetMessage m) {
		// forward a test message to LocoNetListeners
		if (log.isDebugEnabled()) log.debug("sendTestMessage    ["+m+"]");
		notify(m);
		return;
	}

	/*
	* Check number of listeners, used for testing dispose()
	*/

	public int numListeners() {
		return listeners.size();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetInterfaceScaffold.class.getName());

}


/* @(#)LocoNetInterfaceScaffold.java */
