// QsiPacketGenAction.java

package jmri.jmrix.qsi.packetgen;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			PacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2007, 2008
 * @version			$Revision: 1.2 $
 */
public class PacketGenAction extends AbstractAction {

	public PacketGenAction(String s) { super(s);}

	public PacketGenAction() {
	    this(java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle")
	            .getString("MenuItemSendCommand"));
	}

    public void actionPerformed(ActionEvent e) {
		PacketGenFrame f = new PacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PacketGenAction.class.getName());
}


/* @(#)PacketGenAction.java */
