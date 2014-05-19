// MrcPacketGenPanel.java

package jmri.jmrix.mrc.packetgen;

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
public class MrcPacketGenPanel extends jmri.jmrix.mrc.swing.MrcPanel implements jmri.jmrix.mrc.MrcListener {
	
    //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.mrc.packetgen.MrcPacketGenBundle");

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    /*javax.swing.JCheckBox checkBoxBinCmd = new javax.swing.JCheckBox ();
    javax.swing.JTextField replyLenTextField = new javax.swing.JTextField(2);*/
    
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

        /*checkBoxBinCmd.setText("Binary");
        checkBoxBinCmd.setVisible(true);
        checkBoxBinCmd.setToolTipText("Check to enable binary commands");
        checkBoxBinCmd.setSelected(true);*/
        
        /*replyLenTextField.setVisible(true);
        replyLenTextField.setMaximumSize(new Dimension(50,replyLenTextField.getPreferredSize().height));
        replyLenTextField.setToolTipText("Enter number of expected bytes, will override internal defaults");*/
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(sendButton);
        //add(checkBoxBinCmd);
        //add(replyLenTextField);


        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
		/*if (checkBoxBinCmd.isSelected()) {
			// Binary selected, convert ASCII to hex

			MrcMessage m = createPacket(packetTextField.getText());
			if (m == null) {
				JOptionPane.showMessageDialog(MrcPacketGenPanel.this,
						"Enter hexadecimal numbers only", "MRC Binary Command",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			m.setBinary(true);
			int replyLen = getReplyLen (replyLenTextField.getText());
			if (replyLen > 0)
				m.setReplyLen(replyLen);
			else
				m.setReplyLen(getMessageLength(m.getOpCode()));
			tc.sendMrcMessage(m, this);
		} else {*/
			// ASCII Mode selected

			MrcMessage m = new MrcMessage(packetTextField.getText().length());
			for (int i = 0; i < packetTextField.getText().length(); i++)
				m.setElement(i, packetTextField.getText().charAt(i));

			tc.sendMrcMessage(m);

		//}
	}

    public void  message(MrcMessage m) {}  // ignore replies
    //public void  reply(MrcReply r) {} // ignore replies

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
    
    private int getReplyLen (String s) {
    	// gather bytes in result
    	int b;
    	try {
    		b = Integer.parseInt(s);
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    	return b;
     }
    
    // gets the expected number of bytes returned
    private int getMessageLength(int opcode) {
		int replyLen = 1;
		switch (opcode & 0xFF) {
		
		case 0xAB:
		case 0xAC:
			replyLen = 0;
			break;
			
		case 0x82:
		case 0x9B:
		case 0xA1:
		case 0xA7:
		case 0xA9:
			replyLen = 2;
			break;
			
		case 0x8C:
		case 0xAA:
			replyLen = 3;
			break;
			
		case 0x8A:
			replyLen = 4;
			break;
			
		case 0x8F:
			replyLen = 16;
			break;
		default:
			replyLen = 1;
		}
		return replyLen;
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

