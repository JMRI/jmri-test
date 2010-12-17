// NceMacroGenPane.java


package jmri.jmrix.nce.macro;

import jmri.jmrix.nce.*;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Pane for user input of Nce macros
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * Cloned into a Pane by
 * @author kcameron
 * @version $Revision: 1.1.2.1 $
 **/

public class NceMacroGenPane extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {
	
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.macro.NceMacroBundle");

	private static final int REPLY_LEN = 1;
	
	// member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel macroText = new javax.swing.JLabel();
    javax.swing.JLabel macroReply = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(4);
    
    private NceTrafficController tc = null;

    public NceMacroGenPane() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				//log.error("NceClockMon initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.macro.NceMacroGenFrame"; }
    public String getTitle() { 
        return rb.getString("TitleNceMacroGen"); 
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
        this.tc = m.getNceTrafficController();
        // the following code sets the frame's initial state

        jLabel1.setText("  Macro: ");
        jLabel1.setVisible(true);
        
        macroText.setText("  Reply: "); 
        macroText.setVisible(true);
        
        macroReply.setText("unknown"); 
        macroReply.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Execute NCE macro");

        packetTextField.setText("");
		packetTextField.setToolTipText("Enter macro 0 to 255");
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));

        
        setLayout(new GridLayout(4,2));

        add(jLabel1);
        add(packetTextField);
        add(macroText);
        add(macroReply);
        add(sendButton);
 
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
  
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

		// Send Macro

		NceMessage m = createMacroCmd(packetTextField.getText());
		if (m == null) {
			macroReply.setText("error");
			JOptionPane.showMessageDialog(this,
					"Enter 0 to 255", "NCE Macro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		macroReply.setText("waiting");
		tc.sendNceMessage(m, this);
		
		// Unfortunately, the new command doesn't tell us if the macro is empty
		// so we send old command for status
		NceMessage m2 = createOldMacroCmd(packetTextField.getText());
		tc.sendNceMessage(m2, this);
	}

    public void  message(NceMessage m) {}  // ignore replies
    public void reply(NceReply r) {
		if (r.getNumDataElements() == REPLY_LEN) {

			int recChar = r.getElement(0);
			if (recChar == '!')
				macroReply.setText("okay");
			if (recChar == '0')
				macroReply.setText("empty");

		} else {
			macroReply.setText("error");
		}
    } 

    
    NceMessage createMacroCmd(String s) {

		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 0 | macroNum > 255)
			return null;
		
		if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {

			// NCE always responds with okay (!) if macro number is in range.
			// We need to send this version of macro command to cause turnout
			// state to change in NCE CS
			NceMessage m = new NceMessage(5);
			m.setElement(0, 0xAD); 		// Macro cmd
			m.setElement(1, 0x00); 		// addr_h
			m.setElement(2, 0x01); 		// addr_l
			m.setElement(3, 0x01); 		// Macro cmd
			m.setElement(4, macroNum); 	// Macro #
			m.setBinary(true);
			m.setReplyLen(REPLY_LEN);
			return m;

		} else {
			
			// NCE responds with okay (!) if macro exist, (0) if not
			NceMessage m = new NceMessage(2);
			m.setElement(0, 0x9C); 		// Macro cmd
			m.setElement(1, macroNum); 	// Macro #
			m.setBinary(true);
			m.setReplyLen(REPLY_LEN);
			return m;
		}
	}

	NceMessage createOldMacroCmd(String s) {

		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 0 | macroNum > 255)
			return null;

		// NCE responds with okay (!) if macro exist, (0) if not
		NceMessage m = new NceMessage(2);
		m.setElement(0, 0x9C); // Macro cmd
		m.setElement(1, macroNum); // Macro #
		m.setBinary(true);
		m.setReplyLen(REPLY_LEN);
		return m;
	}
}


