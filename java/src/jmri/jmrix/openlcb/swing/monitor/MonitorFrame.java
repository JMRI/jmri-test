// MonitorFrame.java

package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Frame displaying (and logging) OpenLCB (CAN) frames
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010
 * @version         $Revision: 1.3 $
 */

public class MonitorFrame extends jmri.jmrix.AbstractMonFrame implements CanListener {

    public MonitorFrame() {
        super();
    }

    protected String title() { return "OpenLCB Monitor"; }

    protected void init() {
        TrafficController.instance().addCanListener(this);
    }

    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }

    public synchronized void message(CanMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        StringBuilder formatted = new StringBuilder("M: ");
        formatted.append(l.isExtended() ? "[" : "(");
        formatted.append(Integer.toHexString(l.getHeader()));
        formatted.append((l.isExtended() ? "]" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++) {
            formatted.append(" ");
            formatted.append(jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        }
        formatted.append("\n");
        nextLine(new String(formatted), l.toString());
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        StringBuilder formatted = new StringBuilder("R: ");
        formatted.append(l.isExtended() ? "[" : "(");
        formatted.append(Integer.toHexString(l.getHeader()));
        formatted.append((l.isExtended() ? "]" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++) {
            formatted.append(" ");
            formatted.append(jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        }
        formatted.append("\n");
        nextLine(new String(formatted), l.toString());
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorFrame.class.getName());

}
