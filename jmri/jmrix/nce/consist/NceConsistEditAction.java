/** 
 * NceConsistEditAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceConsistEditFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.consist;
 

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.nce.NceTrafficController;

public class NceConsistEditAction  extends AbstractAction {

    private NceTrafficController tc = null;
    
	public NceConsistEditAction(NceTrafficController tc, String s) { 
		super(s);
		this.tc = tc;
		
		// disable if NCE USB selected
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
			setEnabled(false);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		NceConsistEditFrame f = new NceConsistEditFrame(tc);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceConsistEditAction.class.getName());
}


/* @(#)NceConsistEditAction.java */

