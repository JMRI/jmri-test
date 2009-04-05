// SRCPMonFrame.java

package jmri.jmrix.srcp.srcpmon;

import jmri.jmrix.srcp.SRCPListener;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPReply;
import jmri.jmrix.srcp.SRCPTrafficController;

/**
 * Frame displaying (and logging) SRCP command messages
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class SRCPMonFrame extends jmri.jmrix.AbstractMonFrame implements SRCPListener {

	public SRCPMonFrame() {
		super();
	}

	protected String title() { return "SRCP Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		SRCPTrafficController.instance().addSRCPListener(this);
	}

	public void dispose() {
		SRCPTrafficController.instance().removeSRCPListener(this);
		super.dispose();
	}

	public synchronized void message(SRCPMessage l) {  // receive a message and log it
	    
		nextLine("cmd: "+l.toString(), "");
	}
	public synchronized void reply(SRCPReply l) {  // receive a reply message and log it
		nextLine("rep: "+l.toString(), "");
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPMonFrame.class.getName());

}
