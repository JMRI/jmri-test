// ClockMonAction.java

package jmri.jmrix.nce.clockmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceUSB;


/**
 * Create and register a NceClockMonitorFrame object.
 *
 * @author			Ken Cameron    Copyright (C) 2007
 * @version			$Revision: 1.3.14.2 $
 *
 * based on LocoNet.ClockMonAction by Bob Jacobsen Copyright (C) 2003
 */
public class ClockMonAction extends AbstractAction {

	public ClockMonAction(String s) {
		super(s);
		
		// disable if NCE USB detected
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
			setEnabled(false);
		}
	}
    
	private NceTrafficController tc = null;
	
    public ClockMonAction(NceTrafficController t) {
        this("NCE clock monitor");
        tc = t;
    }

    ClockMonFrame f = null;
    
    public void actionPerformed(ActionEvent e) {
        if (f == null) {
            f = new ClockMonFrame(tc);
            try {
                f.initComponents();
                }
            catch (Exception ex) {
                log.error("Exception: "+ex.toString());
                }
        }
        
        // in any case, make it visible
        f.setVisible(true);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClockMonAction.class.getName());

}

/* @(#)ClockMonAction.java */
