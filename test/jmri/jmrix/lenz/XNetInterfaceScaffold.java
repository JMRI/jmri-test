/**
 * XNetInterfaceScaffold.java
 *
 * Description:	 	Test scaffold implementation of XNetInterface
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 2.0 $
 *
 * Use an object of this type as a XNetTrafficController in tests
 */

package jmri.jmrix.lenz;

import java.util.Vector;

public class XNetInterfaceScaffold extends XNetTrafficController {

	public XNetInterfaceScaffold(LenzCommandStation pCommandStation) {
        super(pCommandStation);
		self = this;
	}

	// override some XNetTrafficController methods for test purposes

	public boolean status() { return true;
	}

	/**
	 * record XNet messages sent, provide access for making sure they are OK
	 */
	public Vector outbound = new Vector();  // public OK here, so long as this is a test class
	public void sendXNetMessage(XNetMessage m, XNetListener replyTo) {
		if (log.isDebugEnabled()) log.debug("sendXNetMessage ["+m+"]");
		// save a copy
		outbound.addElement(m);
	}

	// test control member functions

	/**
	 * forward a message to the listeners, e.g. test receipt
	 */
	public void sendTestMessage (XNetReply m) {
		// forward a test message to XNetListeners
		if (log.isDebugEnabled()) log.debug("sendTestMessage    ["+m+"]");
		notify(m, null);
		return;
	}

	/*
	* Check number of listeners, used for testing dispose()
	*/

	public int numListeners() {
		return listeners.size();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetInterfaceScaffold.class.getName());

}


/* @(#)LocoNetInterfaceScaffold.java */
