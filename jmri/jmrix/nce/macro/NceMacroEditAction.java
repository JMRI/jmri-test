/** 
 * NceMacroEditAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceMacroEditFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.macro;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.nce.NceTrafficController;

@Deprecated
public class NceMacroEditAction  extends AbstractAction {

    private NceTrafficController tc = null;
    
	public NceMacroEditAction(NceTrafficController tc, String s) {
		super(s);
		this.tc = tc;

		// disable if NCE USB detected
		if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
			setEnabled(false);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		NceMacroEditFrame f = new NceMacroEditFrame(tc);
		f.addHelpMenu("package.jmri.jmrix.nce.macro.NceMacroEditFrame", true);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceMacroEditAction.class.getName());
}


/* @(#)NceMacroEditAction.java */

