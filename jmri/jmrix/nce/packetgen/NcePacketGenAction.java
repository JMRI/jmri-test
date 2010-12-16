// NcePacketGenAction.java

package jmri.jmrix.nce.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a 
 *       			NcePacketGenFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @version		$Revision: 1.6.14.1 $	
 */

@Deprecated
public class NcePacketGenAction extends AbstractAction {

	public NcePacketGenAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		NcePacketGenFrame f = new NcePacketGenFrame();
		f.addHelpMenu("package.jmri.jmrix.nce.packetgen.NcePacketGenFrame", true);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NcePacketGenAction.class.getName());
}


/* @(#)NcePacketGenAction.java */
