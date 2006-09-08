// SendPacketAction.java

package jmri.jmrit.sendpacket;

import java.awt.event.*;

import javax.swing.*;

/**
 * Swing action to create and register a SendPacketFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.4 $
 */
public class SendPacketAction extends AbstractAction {

    public SendPacketAction(String s) { 
	super(s);

     // disable ourself if there is no command Station object available
        if (jmri.InstanceManager.commandStationInstance()==null) {
            setEnabled(false);
        }
    }

    public SendPacketAction() { this("Send DCC packet");}

    public void actionPerformed(ActionEvent e) {
        // create a SendPacketFrame
        SendPacketFrame f = new SendPacketFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SendPacketAction.class.getName());
}

/* @(#)LocoGenAction.java */
