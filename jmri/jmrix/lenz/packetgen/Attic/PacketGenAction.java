/**
 * PacketGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			XpressNet PacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2002
 * @version			$Revision: 2.2 $
 */

package jmri.jmrix.lenz.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.lenz.XNetTrafficController;

public class PacketGenAction 			extends AbstractAction {

    public PacketGenAction(String s) { super(s);}

    public PacketGenAction() {
        this("Generate XPressNet message");
    }

    public void actionPerformed(ActionEvent e) {
		// create a PacketGenFrame
		PacketGenFrame f = new PacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);

		// connect to the TrafficController
		f.connect(XNetTrafficController.instance());
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PacketGenAction.class.getName());
}


/* @(#)LocoGenAction.java */
