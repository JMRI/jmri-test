// SerialMonFrame.java

package jmri.jmrix.grapevine.serialmon;

import jmri.jmrix.grapevine.SerialListener;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2001, 2006, 2007, 2008
 * @version         $Revision: 1.3 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Grapevine Serial Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
        super.dispose();
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        nextLine("M: "+l.format()+"\n", l.toString());
    }

    public synchronized void reply(SerialReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        nextLine("R: "+l.format()+"\n", l.toString());
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}
