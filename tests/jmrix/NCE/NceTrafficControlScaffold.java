/** 
 * NceInterfaceScaffold.java
 *
 * Description:	    Stands in for the NceTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NcePortController;

import java.util.Vector;

public class NceTrafficControlScaffold extends NceTrafficController {
	public NceTrafficControlScaffold() {
	}

	// override some NceTrafficController methods for test purposes
	
	public boolean status() { return true; 
	}

	/**
	 * record messages sent, provide access for making sure they are OK
	 */
	public Vector outbound = new Vector();  // public OK here, so long as this is a test class
	public void sendNceMessage(NceMessage m) {
		if (log.isDebugEnabled()) log.debug("sendNceMessage ["+m+"]");
		// save a copy
		outbound.addElement(m);
		// we don't return an echo so that the processing before the echo can be
		// separately tested
	}

	// test control member functions
	
	/** 
	 * forward a message to the listeners, e.g. test receipt
	 */
	protected void sendTestMessage (NceMessage m, NceListener l) {
		// forward a test message to NceListeners
		if (log.isDebugEnabled()) log.debug("sendTestMessage    ["+m+"]");
		notifyMessage(m, l);
		return;
	}
	
	/*
	* Check number of listeners, used for testing dispose()
	*/
	
	public int numListeners() {
		return cmdListeners.size();
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTrafficControlScaffold.class.getName());

}
