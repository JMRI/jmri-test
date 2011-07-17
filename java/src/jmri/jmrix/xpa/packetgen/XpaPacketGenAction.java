// XpaPacketGenAction.java

package jmri.jmrix.xpa.packetgen;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			XpaPacketGenFrame object
 *
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */
public class XpaPacketGenAction  extends AbstractAction {

	public XpaPacketGenAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		XpaPacketGenFrame f = new XpaPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XpaPacketGenAction.class.getName());
}


/* @(#)XpaPacketGenAction.java */
