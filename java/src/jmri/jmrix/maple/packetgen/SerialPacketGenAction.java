// SerialPacketGenAction.java

package jmri.jmrix.maple.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SerialPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version   	$Revision: 1.2 $
 */
public class SerialPacketGenAction  extends AbstractAction {

	public SerialPacketGenAction(String s) { super(s);}

    public SerialPacketGenAction() {
        this("Send C/MRI message");
    }

    public void actionPerformed(ActionEvent e) {
		SerialPacketGenFrame f = new SerialPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialPacketGenAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */
