// MrcPacketGenPanel.java

package jmri.jmrix.mrc.swing.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.*;
import jmri.jmrix.mrc.*;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;


/**
 * Frame for user input of Mrc messages
 * @author	Ken Cameron		Copyright (C) 2010
 * derived from:
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * @version $Revision: 25018 $
 */
public class MrcPacketGenPanel extends jmri.jmrix.mrc.swing.MrcPanel{
	
    //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.mrc.packetgen.MrcPacketGenBundle");

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    
    private MrcTrafficController tc = null;

    public MrcPacketGenPanel() {
        super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof MrcSystemConnectionMemo ) {
            try {
				initComponents((MrcSystemConnectionMemo) context);
			} catch (Exception e) {
				//log.error("BoosterProg initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.mrc.packetgen.MrcPacketGenFrame"; }
    
    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("MRC_");
    	}
		x.append(": ");
    	x.append(Bundle.getMessage("Title"));
        return x.toString(); 
    }

    public void initComponents(MrcSystemConnectionMemo m) throws Exception {
    	this.memo = m;
    	this.tc = m.getMrcTrafficController();
    	
        // the following code sets the frame's initial state

        jLabel1.setText("Command: ");
        jLabel1.setVisible(true);
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
		packetTextField.setToolTipText("Enter command");
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        
        MrcMessage m = new MrcMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++)
            m.setElement(i, packetTextField.getText().charAt(i));

        tc.sendMrcMessage(m);
	}

    MrcMessage createPacket(String s) {
    	// gather bytes in result
    	byte b[];
    	try {
    		b = StringUtil.bytesFromHexString(s);
    	} catch (NumberFormatException e) {
    		return null;
    	}
    	if (b.length == 0)
    		return null; // no such thing as a zero-length message
    	MrcMessage m = new MrcMessage(b.length);
    	for (int i = 0; i < b.length; i++)
    		m.setElement(i, b[i]);
    	return m;
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.mrc.swing.MrcNamedPaneAction {


		public Default() {
            super("Open MRC Send Binary Command", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MrcPacketGenPanel.class.getName(), 
                jmri.InstanceManager.getDefault(MrcSystemConnectionMemo.class));
        }
    }

	static Logger log = LoggerFactory.getLogger(MrcPacketGenPanel.class.getName());
}

