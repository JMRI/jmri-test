/**
 * PacketGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			XpressNet PacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2002
 * @version			$Revision: 2.4 $
 */

package jmri.jmrix.lenz.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PacketGenAction 			extends AbstractAction {

    jmri.jmrix.lenz.XNetSystemConnectionMemo _memo=null;

    public PacketGenAction(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) { 
       super(s);
       _memo=memo;
    }

    public PacketGenAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("Generate XPressNet message",memo);
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
		f.connect(_memo.getXNetTrafficController());
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PacketGenAction.class.getName());
}


/* @(#)LocoGenAction.java */
