// NceMacroEditFrame.java

package jmri.jmrix.nce.macro;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Frame for user edit of NCE macros
 * 
 * NCE macros are stored in Command Station (CS) memory starting at address
 * xC800. Each macro consists of 20 bytes. The last macro 255 is at address
 * xDBEC.
 * 
 * Macro addr
 * 0	xC800
 * 1	xC814
 * 2	xC828
 * 3	xC83C
 * .      .
 * .      .
 * 255	xDBEC
 * 
 * Each macro can close or throw up to ten accessories.  Macros can also be linked
 * together.  Two bytes (16 bit word) define an accessory address and command, or the
 * address of the next macro to be executed.  If the upper byte of the macro data word
 * is xFF, then the next byte contains the address of the next macro to be executed by
 * the NCE CS.  For example, xFF08 means link to macro 8.  NCE uses the NMRA DCC accessory
 * decoder packet format for the word defination of their macros.
 * 
 * Macro data byte:
 * 
 * bit	     15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
 *                                 _     _  _  _
 *  	      1  0  A  A  A  A  A  A  1  A  A  A  C  D  D  D
 * addr bit         7  6  5  4  3  2    10  9  8     1  0  
 * turnout												   T
 * 
 * By convention, MSB address bits 10 - 8 are one's complement.  NCE macros always set the C bit to 1.
 * The LSB "D" (0) determines if the accessory is to be thrown (0) or closed (1).  The next two bits
 * "D D" are the LSBs of the accessory address. Note that NCE display addresses are 1 greater than 
 * NMRA DCC. Note that address bit 2 isn't supposed to be inverted, but it is the way NCE implemented
 * their macros.
 * 
 * Examples:
 * 
 * 81F8 = accessory 1 thrown
 * 9FFC = accessory 123 thrown
 * B5FD = accessory 211 close
 * BF8F = accessory 2044 close
 * 
 * FF10 = link macro 16 
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.13 $
 */

public class NceMacroEditFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {

	private static final int CS_MACRO_MEM = 0xC800;	// start of NCE CS Macro memory 
	private static final int MAX_MACRO = 255;		// there are 256 possible macros
	private int macroNum = 0;						// macro being worked
	private static final int REPLY_1 = 1;			// reply length of 1 byte expected
	private static final int REPLY_16 = 16;			// reply length of 16 bytes expected
	private int replyLen = 0;						// expected byte length
	private int waiting = 0;						// to catch responses not intended for this module
	
	private static final String QUESTION = "  Add  ";// The three possible states for a turnout
	private static final String CLOSED = InstanceManager.turnoutManagerInstance().getClosedText();
	private static final String THROWN = InstanceManager.turnoutManagerInstance().getThrownText();	
	private static final String CLOSED_NCE = " Normal ";
	private static final String THROWN_NCE = "Reverse";	
	
	private static final String DELETE = "Delete";
	
	private static final String EMPTY = "empty";	// One of two accessory states
	private static final String ACCESSORY = "accessory";
	
	private static final String LINK = "Link macro";// Line 10 alternative to Delete

	private boolean macroSearchInc = false;		// next search
	private boolean macroSearchDec = false;		// previous search
	private int macroCount;						// search count not to exceed MAX_MACRO
	private boolean secondRead = false;			// when true, another 16 byte read expected
	private boolean macroValid = false;			// when true, NCE CS has responed to macro read
	private boolean macroModified = false;		// when true, macro has been modified by user
	
	// member declarations
    JLabel textMacro = new JLabel();
    JLabel textReply = new JLabel();
    JLabel macroReply = new JLabel();
    
    // major buttons
    JButton previousButton = new JButton();
    JButton nextButton = new JButton();
    JButton getButton = new JButton();
    JButton saveButton = new JButton();
    JButton backUpButton = new JButton();
    JButton restoreButton = new JButton();
    
    // check boxes
    JCheckBox checkBoxEmpty = new JCheckBox ();
    JCheckBox checkBoxNce = new JCheckBox ();
    
    // macro text field
    JTextField macroTextField = new JTextField(4);
    
    // for padding out panel
    JLabel space1 = new JLabel();
    JLabel space2 = new JLabel();
    
    // accessory row 1
    JLabel num1 = new JLabel();
    JLabel textAccy1 = new JLabel();
    JTextField accyTextField1 = new JTextField(4);
    JButton cmdButton1 = new JButton();
    JButton deleteButton1 = new JButton();
    
    //  accessory row 2
    JLabel num2 = new JLabel();
    JLabel textAccy2 = new JLabel();
    JTextField accyTextField2 = new JTextField(4);
    JButton cmdButton2 = new JButton();
    JButton deleteButton2 = new JButton();
    
    //  accessory row 3
    JLabel num3 = new JLabel();
    JLabel textAccy3 = new JLabel();
    JTextField accyTextField3 = new JTextField(4);
    JButton cmdButton3 = new JButton();
    JButton deleteButton3 = new JButton();	
    
    //  accessory row 4
    JLabel num4 = new JLabel();
    JLabel textAccy4 = new JLabel();
    JTextField accyTextField4 = new JTextField(4);
    JButton cmdButton4 = new JButton();
    JButton deleteButton4 = new JButton();	
    
    //  accessory row 5
    JLabel num5 = new JLabel();
    JLabel textAccy5 = new JLabel();
    JTextField accyTextField5 = new JTextField(4);
    JButton cmdButton5 = new JButton();
    JButton deleteButton5 = new JButton();	
    
    //  accessory row 6
    JLabel num6 = new JLabel();
    JLabel textAccy6 = new JLabel();
    JTextField accyTextField6 = new JTextField(4);
    JButton cmdButton6 = new JButton();
    JButton deleteButton6 = new JButton();	
    
    //  accessory row 7
    JLabel num7 = new JLabel();
    JLabel textAccy7 = new JLabel();
    JTextField accyTextField7 = new JTextField(4);
    JButton cmdButton7 = new JButton();
    JButton deleteButton7 = new JButton();	
    
    //  accessory row 8
    JLabel num8 = new JLabel();
    JLabel textAccy8 = new JLabel();
    JTextField accyTextField8 = new JTextField(4);
    JButton cmdButton8 = new JButton();
    JButton deleteButton8 = new JButton();	
    
    //  accessory row 9
    JLabel num9 = new JLabel();
    JLabel textAccy9 = new JLabel();
    JTextField accyTextField9 = new JTextField(4);
    JButton cmdButton9 = new JButton();
    JButton deleteButton9 = new JButton();	
    
    //  accessory row 10
    JLabel num10 = new JLabel();
    JLabel textAccy10 = new JLabel();
    JTextField accyTextField10 = new JTextField(4);
    JButton cmdButton10 = new JButton();
    JButton deleteButton10 = new JButton();	
    
    public NceMacroEditFrame() {
        super();
    }
 

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
     	
    	textMacro.setText("Macro");
        textMacro.setVisible(true);
        
        textReply.setText("Reply:"); 
        textReply.setVisible(true);
  
        macroReply.setText("unknown"); 
        macroReply.setVisible(true);

        previousButton.setText("  Previous  ");
        previousButton.setVisible(true);
        previousButton.setToolTipText("Search for macro decrementing");
        
        nextButton.setText("      Next      "); //pad out for worse case of turnout state names
        nextButton.setVisible(true);
        nextButton.setToolTipText("Search for macro incrementing");
        
        getButton.setText("  Get  ");
        getButton.setVisible(true);
        getButton.setToolTipText("Read macro from NCE CS");
        
        macroTextField.setText("");
		macroTextField.setToolTipText("Enter macro 0 to 255");
		macroTextField.setMaximumSize(new Dimension(macroTextField
				.getMaximumSize().width, macroTextField.getPreferredSize().height));
        
        saveButton.setText("Save");
        saveButton.setVisible(true);
        saveButton.setEnabled (false);
        saveButton.setToolTipText("Update macro in NCE CS");
        
        backUpButton.setText("Backup");
        backUpButton.setVisible(true);
        backUpButton.setToolTipText("Save all macros to a file");
   	   
        restoreButton.setText("Restore");
        restoreButton.setVisible(true);
        restoreButton.setToolTipText("Restore all macros from a file");
   	   
		checkBoxEmpty.setText("Empty Macro");
        checkBoxEmpty.setVisible(true);
        checkBoxEmpty.setToolTipText("Check to search for empty macros");
        
        checkBoxNce.setText("NCE Turnout");
        checkBoxNce.setVisible(true);
        checkBoxNce.setToolTipText("Use NCE terminology for turnout states");
        
        space1.setText("            ");
        space1.setVisible(true);
        space2.setText(" ");
        space2.setVisible(true); 
        
        initAccyFields();
        
        setTitle("Edit NCE Macro");
        getContentPane().setLayout(new GridBagLayout());
        
        // Layout the panel by rows
        // row 0
        addItem(textMacro, 2,0);
        
        // row 1
        addItem(previousButton, 1,1);
        addItem(macroTextField, 2,1);
        addItem(nextButton, 3,1);
        addItem(checkBoxEmpty, 4,1);
        
        // row 2
        addItem(textReply, 0,2);
        addItem(macroReply, 1,2);
        addItem(getButton, 2,2);
        addItem(checkBoxNce, 4,2);
        
        // row 3 padding for looks
        addItem(space1, 1,3);
        
        // row 4 RFU
        
        // row 5 accessory 1
        addAccyRow (num1, textAccy1, accyTextField1, cmdButton1, deleteButton1, 5);
          
        // row 6 accessory 2
        addAccyRow (num2, textAccy2, accyTextField2, cmdButton2, deleteButton2, 6);
        
        // row 7 accessory 3
        addAccyRow (num3, textAccy3, accyTextField3, cmdButton3, deleteButton3, 7);
        
        // row 8 accessory 4
        addAccyRow (num4, textAccy4, accyTextField4, cmdButton4, deleteButton4, 8);
        
        // row 9 accessory 5
        addAccyRow (num5, textAccy5, accyTextField5, cmdButton5, deleteButton5, 9);
        
        // row 10 accessory 6
        addAccyRow (num6, textAccy6, accyTextField6, cmdButton6, deleteButton6, 10);
        
        // row 11 accessory 7
        addAccyRow (num7, textAccy7, accyTextField7, cmdButton7, deleteButton7, 11);
        
        // row 12 accessory 8
        addAccyRow (num8, textAccy8, accyTextField8, cmdButton8, deleteButton8, 12);
        
        // row 13 accessory 9
        addAccyRow (num9, textAccy9, accyTextField9, cmdButton9, deleteButton9, 13);
        
        // row 14 accessory 10
        addAccyRow (num10, textAccy10, accyTextField10, cmdButton10, deleteButton10, 14);
        
        // row 15 padding for looks
        addItem(space2, 2,15);
        
        // row 16
        addItem(saveButton, 2,16);
        addItem(backUpButton, 3,16);
        addItem(restoreButton, 4,16);
        
        // setup buttons
        addButtonAction(previousButton);
        addButtonAction(nextButton);
        addButtonAction(getButton);
        addButtonAction(saveButton);
        addButtonAction(backUpButton);
        addButtonAction(restoreButton);
        
        // accessory command buttons
        addButtonCmdAction(cmdButton1);
        addButtonCmdAction(cmdButton2);
        addButtonCmdAction(cmdButton3);
        addButtonCmdAction(cmdButton4);
        addButtonCmdAction(cmdButton5);
        addButtonCmdAction(cmdButton6);
        addButtonCmdAction(cmdButton7);
        addButtonCmdAction(cmdButton8);
        addButtonCmdAction(cmdButton9);
        addButtonCmdAction(cmdButton10);
        
        // accessory delete buttons
        addButtonDelAction(deleteButton1);
        addButtonDelAction(deleteButton2);
        addButtonDelAction(deleteButton3);
        addButtonDelAction(deleteButton4);
        addButtonDelAction(deleteButton5);
        addButtonDelAction(deleteButton6);
        addButtonDelAction(deleteButton7);
        addButtonDelAction(deleteButton8);
        addButtonDelAction(deleteButton9);
        addButtonDelAction(deleteButton10);
        
        // NCE checkbox
        addCheckBoxAction(checkBoxNce);

        // set frame size for display
		pack();
		if ((getWidth()<400) && (getHeight()<400)) setSize(400, 400);
    }
 
    // Previous, Next, Get, Save, Restore & Backup buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;

		if (ae.getSource() == saveButton) {
			boolean status = saveMacro();
			if (status) // was save successful?
				setSaveButton(false); // yes, clear save button
			return;
		}

		if (macroModified) {
			// warn user that macro has been modified
			JOptionPane.showMessageDialog(NceMacroEditFrame.this,
					"Macro has been modified, use Save to update NCE CS memory", "NCE Macro",
					JOptionPane.WARNING_MESSAGE);
			macroModified = false;		// only one warning!!!

		} else {
			
			setSaveButton(false);		// Turn off save button
		
			if (ae.getSource() == previousButton) {
				macroCount = 0; // used to determine if all 256 macros have been
				// read
				macroSearchDec = true;
				macroNum = getMacro();	// check for valid and kick off read process
				if (macroNum == -1) 	// Error user input incorrect
					macroSearchDec = false;
			}
			if (ae.getSource() == nextButton) {
				macroCount = 0; // used to determine if all 256 macros have been
				// read
				macroSearchInc = true;
				macroNum = getMacro();	// check for valid and kick off read process
				if (macroNum == -1) 	// Error user input incorrect
					macroSearchInc = false;
			}

			if (ae.getSource() == getButton) {
				// Get Macro
				macroNum = getMacro();
			}
			
	    	if (ae.getSource() == backUpButton){
	    		
	            Thread mb = new NceMacroBackup();
	            mb.setName("Macro Backup");
	            mb.start ();
	    	}
	   		
	    	if (ae.getSource() == restoreButton){
	            Thread mr = new NceMacroRestore();
	            mr.setName("Macro Restore");
	            mr.start ();
	    	}
		}
	}

	// One of the ten accessory command buttons pressed
	public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;
		
		if (ae.getSource() == cmdButton1) {
			updateAccyCmdPerformed(accyTextField1, cmdButton1, textAccy1,
					deleteButton1);
		}
		if (ae.getSource() == cmdButton2) {
			updateAccyCmdPerformed(accyTextField2, cmdButton2, textAccy2,
					deleteButton2);
		}
		if (ae.getSource() == cmdButton3) {
			updateAccyCmdPerformed(accyTextField3, cmdButton3, textAccy3,
					deleteButton3);
		}
		if (ae.getSource() == cmdButton4) {
			updateAccyCmdPerformed(accyTextField4, cmdButton4, textAccy4,
					deleteButton4);
		}
		if (ae.getSource() == cmdButton5) {
			updateAccyCmdPerformed(accyTextField5, cmdButton5, textAccy5,
					deleteButton5);
		}
		if (ae.getSource() == cmdButton6) {
			updateAccyCmdPerformed(accyTextField6, cmdButton6, textAccy6,
					deleteButton6);
		}
		if (ae.getSource() == cmdButton7) {
			updateAccyCmdPerformed(accyTextField7, cmdButton7, textAccy7,
					deleteButton7);
		}
		if (ae.getSource() == cmdButton8) {
			updateAccyCmdPerformed(accyTextField8, cmdButton8, textAccy8,
					deleteButton8);
		}
		if (ae.getSource() == cmdButton9) {
			updateAccyCmdPerformed(accyTextField9, cmdButton9, textAccy9,
					deleteButton9);
		}
		if (ae.getSource() == cmdButton10) {
			updateAccyCmdPerformed(accyTextField10, cmdButton10, textAccy10,
					deleteButton10);
		}
	}

	// One of ten Delete buttons pressed
	public void buttonActionDeletePerformed(java.awt.event.ActionEvent ae) {
		
		// if we're searching ignore user 
		if (macroSearchInc || macroSearchDec)
			return;
		
		if (ae.getSource() == deleteButton1) {
			updateAccyDelPerformed(accyTextField1, cmdButton1, textAccy1,
					deleteButton1);
		}
		if (ae.getSource() == deleteButton2) {
			updateAccyDelPerformed(accyTextField2, cmdButton2, textAccy2,
					deleteButton2);
		}
		if (ae.getSource() == deleteButton3) {
			updateAccyDelPerformed(accyTextField3, cmdButton3, textAccy3,
					deleteButton3);
		}
		if (ae.getSource() == deleteButton4) {
			updateAccyDelPerformed(accyTextField4, cmdButton4, textAccy4,
					deleteButton4);
		}
		if (ae.getSource() == deleteButton5) {
			updateAccyDelPerformed(accyTextField5, cmdButton5, textAccy5,
					deleteButton5);
		}
		if (ae.getSource() == deleteButton6) {
			updateAccyDelPerformed(accyTextField6, cmdButton6, textAccy6,
					deleteButton6);
		}
		if (ae.getSource() == deleteButton7) {
			updateAccyDelPerformed(accyTextField7, cmdButton7, textAccy7,
					deleteButton7);
		}
		if (ae.getSource() == deleteButton8) {
			updateAccyDelPerformed(accyTextField8, cmdButton8, textAccy8,
					deleteButton8);
		}
		if (ae.getSource() == deleteButton9) {
			updateAccyDelPerformed(accyTextField9, cmdButton9, textAccy9,
					deleteButton9);
		}
		// row ten delete button behaves differently
		// could be link button
		if (ae.getSource() == deleteButton10) {
			
			// is the user trying to link a macro?
			if (deleteButton10.getText() == LINK){
				if (macroValid == false) { // Error user input incorrect
					JOptionPane.showMessageDialog(NceMacroEditFrame.this,
							"Get macro number 1 to 255", "NCE Macro",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int linkMacro = validMacro (accyTextField10.getText());
				if (linkMacro == -1) {
					JOptionPane.showMessageDialog(NceMacroEditFrame.this,
							"Enter macro number 1 to 255 in line 10", "NCE Macro",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// success, link a macro
				setSaveButton(true);
				textAccy10.setText(LINK); 
				cmdButton10.setVisible(false);
				deleteButton10.setText(DELETE);
				deleteButton10.setToolTipText("Remove macro link");
				
			// user wants to delete a accessory address or a link	
			}else{
			updateAccyDelPerformed(accyTextField10, cmdButton10, textAccy10,
					deleteButton10);
			initAccyRow10 ();
			}
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		getMacro();
	}
    
    // gets the user supplied macro number and then reads NCE CS memory
    private int getMacro (){
    	int mN = validMacro (macroTextField.getText());
		if (mN == -1) {
			macroReply.setText("error");
			JOptionPane.showMessageDialog(NceMacroEditFrame.this,
					"Enter macro number 0 to 255", "NCE Macro",
					JOptionPane.ERROR_MESSAGE);
			macroValid = false;
			return mN;
		}
		if (macroSearchInc || macroSearchDec) {
			macroReply.setText("searching");
		}else{
			macroReply.setText("waiting");
		}
		
		NceMessage m = readMacroMemory (mN, false);
		NceTrafficController.instance().sendNceMessage(m, this);
		return mN;
    }
  
 
    
    // Updates the accessory line when the user hits the command button
    private void updateAccyCmdPerformed (JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton){
    	if (macroValid == false) { // Error user input incorrect
			JOptionPane.showMessageDialog(NceMacroEditFrame.this,
					"Get macro number 0 to 255", "NCE Macro",
					JOptionPane.ERROR_MESSAGE);
		} else {
			String accyText = accyTextField.getText();
			int accyNum = 0;
			try {
				accyNum = Integer.parseInt(accyText);
			} catch (NumberFormatException e) {
				accyNum = -1;
			}

			if (accyNum < 1 | accyNum > 2044){
				JOptionPane.showMessageDialog(NceMacroEditFrame.this,
						"Enter accessory number 1 to 2044", "NCE macro address",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String accyCmd = cmdButton.getText();
			
			// Use JMRI or NCE turnout terminology
			if (checkBoxNce.isSelected()) {

				if (accyCmd != THROWN_NCE)
					cmdButton.setText(THROWN_NCE);
				if (accyCmd != CLOSED_NCE)
					cmdButton.setText(CLOSED_NCE);

			} else {

				if (accyCmd != THROWN)
					cmdButton.setText(THROWN);
				if (accyCmd != CLOSED)
					cmdButton.setText(CLOSED);
			}
			
			setSaveButton(true);
			textAccy.setText(ACCESSORY);
			deleteButton.setText(DELETE);
			deleteButton.setToolTipText("Remove this accessory from the macro");
			deleteButton.setEnabled(true);
		}
    }
    
    // Delete an accessory from the macro
    private void updateAccyDelPerformed (JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton){
    	setSaveButton(true);
		textAccy.setText(EMPTY);
		accyTextField.setText("");
		cmdButton.setText(QUESTION);
		deleteButton.setEnabled(false);
	}
    
    // Updates all 20 bytes in NCE CS memory as long as there are no user input errors
    private boolean saveMacro(){
        byte [] macroAccy = new byte [20];			// NCE Macro data
        int index = 0;
        int accyNum = 0;
        accyNum = getAccyRow (macroAccy, index, textAccy1, accyTextField1, cmdButton1);
        if (accyNum < 0)	//error
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy2, accyTextField2, cmdButton2);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy3, accyTextField3, cmdButton3);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy4, accyTextField4, cmdButton4);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy5, accyTextField5, cmdButton5);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy6, accyTextField6, cmdButton6);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy7, accyTextField7, cmdButton7);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy8, accyTextField8, cmdButton8);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy9, accyTextField9, cmdButton9);
        if (accyNum < 0)
        	return false;
        if (accyNum > 0)
        	index+=2;
        accyNum = getAccyRow (macroAccy, index, textAccy10, accyTextField10, cmdButton10);
        if (accyNum < 0){
			JOptionPane.showMessageDialog(NceMacroEditFrame.this,
					"Enter macro number 1 to 255 in line 10", "NCE Macro",
					JOptionPane.ERROR_MESSAGE);
			return false;
        }
          
        NceMessage m = writeMacroMemory(macroNum, macroAccy,  false);
        NceTrafficController.instance().sendNceMessage(m, this);
        NceMessage m2 = writeMacroMemory(macroNum, macroAccy,  true);
        NceTrafficController.instance().sendNceMessage(m2, this);
        return true;
     }
    
    private int getAccyRow (byte[] b, int i, JLabel textAccy, JTextField accyTextField, JButton cmdButton){
        int accyNum = 0;
    	if (textAccy.getText() == ACCESSORY){
        	accyNum = getAccyNum(accyTextField.getText());
        	if (accyNum < 0)
        		return accyNum;
        	accyNum = accyNum + 3;							// adjust for NCE's way of encoding
        	int upperByte = (accyNum&0xFF);
        	upperByte = (upperByte >>2)+ 0x80;
        	b[i] = (byte)upperByte;
        	int lowerByteH = (((accyNum ^ 0x0700) & 0x0700)>>4);// 3 MSB 1s complement
        	int lowerByteL = ((accyNum & 0x3)<<1);       	// 2 LSB
        	int lowerByte = (lowerByteH + lowerByteL + 0x88);
        	if (cmdButton.getText() == CLOSED)				// adjust for turnout command	
        		lowerByte++;
        	if (cmdButton.getText() == CLOSED_NCE)			// adjust for turnout command	
        		lowerByte++;
         	b[i+1] = (byte)(lowerByte);
        }
    	if (textAccy.getText() == LINK){
         	int macroLink = validMacro (accyTextField.getText());
         	if (macroLink < 0)
         		return macroLink;
         	b[i] = (byte) 0xFF;								// NCE macro link command
         	b[i+1] = (byte) macroLink;						// link macro number
    	}
        return accyNum;
    }
    
    private int getAccyNum(String accyText){
       	int accyNum = 0;
		try {
			accyNum = Integer.parseInt(accyText);
		} catch (NumberFormatException e) {
			accyNum = -1;
		}
		if (accyNum < 1 | accyNum > 2044){
			JOptionPane.showMessageDialog(NceMacroEditFrame.this,
					"Enter accessory number 1 to 2044", "NCE macro address",
					JOptionPane.ERROR_MESSAGE);
			accyNum = -1;
		}
		return accyNum;
    }
    
    // display save button
    private void setSaveButton(boolean display) {
		macroModified = display;
		saveButton.setEnabled(display);
		backUpButton.setEnabled(!display);
		restoreButton.setEnabled(!display);
	}
    
    public void  message(NceMessage m) {}  // ignore replies
    
    // response from save, get, next or previous
	public void reply(NceReply r) {
		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		waiting--;
		if (r.getNumDataElements() != replyLen) {
			macroReply.setText("error");
			return;
		}
		// Macro command
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar == '!')
				macroReply.setText("okay");
			if (recChar == '0')
				macroReply.setText("empty");
		}
		// Macro memory read
		if (replyLen == REPLY_16) {
			// NCE macros consists of 20 bytes
			// 1st 16 bytes followed by another 16 but we only use the
			// first 4
			if (secondRead) {
				// Second memory read for accessories 9 and 10
				secondRead = false;
				loadAccy9and10(r);

			} else {
				int recChar = r.getElement(0);
				recChar = recChar << 8;
				recChar = recChar + r.getElement(1);
				if (recChar == 0) {
					if (checkBoxEmpty.isSelected()) {
						if (macroCount > 0) {
							macroSearchInc = false;
							macroSearchDec = false;
						}
					}
					// Macro is empty so init the accessory fields
					macroReply.setText("macro empty");
					initAccyFields();
					macroValid = true;
				} else {
					if (checkBoxEmpty.isSelected() == false) {
						if (macroCount > 0) {
							macroSearchInc = false;
							macroSearchDec = false;
						}
					}
					macroReply.setText("macro found");
					secondRead = loadAccy1to8(r);
					macroValid = true;
				}
				// if we're searching, don't bother with second read
				if (macroSearchInc || macroSearchDec)
					secondRead = false;
				// Do we need to read more CS memory?
				if (secondRead)
					// force second read of CS memory
					getMacro2ndHalf(macroNum);
				// when searching, have we read all of the possible
				// macros?
				macroCount++;
				if (macroCount > MAX_MACRO) {
					macroSearchInc = false;
					macroSearchDec = false;
				}
				if (macroSearchInc) {
					macroNum++;
					if (macroNum == MAX_MACRO + 1)
						macroNum = 0;
				}
				if (macroSearchDec) {
					macroNum--;
					if (macroNum == -1)
						macroNum = MAX_MACRO;
				}
				if (macroSearchInc || macroSearchDec) {
					macroTextField.setText(Integer.toString(macroNum));
					macroNum = getMacro();
				}
			}
		}
	}

  
	// Convert NCE macro hex data to accessory address
	// returns 0 if macro address is empty
	// returns a negative address if link address
	// & loads accessory 10 with link macro
    private int getNextMacroAccyAdr(int i, NceReply r) {
		int b = (i - 1) << 1;
		int accyAddrL = r.getElement(b);
		int accyAddr = 0;
		// check for null
		if ((accyAddrL == 0) && (r.getElement(b + 1) == 0)) {
			return accyAddr;
		}
		// Check to see if link address
		if ((accyAddrL & 0xFF) == 0xFF) {
			// Link address
			accyAddr = r.getElement(b + 1);
			linkAccessory10(accyAddr & 0xFF);
			accyAddr = -accyAddr;
			
		// must be an accessory address	
		} else {
			accyAddrL = (accyAddrL << 2) & 0xFC;			// accessory address bits 7 - 2
			int accyLSB = r.getElement(b + 1);
			accyLSB = (accyLSB & 0x06) >> 1;				// accessory address bits 1 - 0
			int accyAddrH = r.getElement(b + 1);
			accyAddrH = (0x70 - (accyAddrH & 0x70)) << 4; 	// One's completent of MSB of address 10 - 8
															// & multiply by 16
			accyAddr = accyAddrH + accyAddrL + accyLSB - 3; // adjust for the way NCE displays addresses
		}
		return accyAddr;
	}
    
    // whenever link macro is found, put it in the last location
    // this makes it easier for the user to edit the macro
    private void linkAccessory10(int accyAddr){
    	textAccy10.setText(LINK); 
		accyTextField10.setText(Integer.toString(accyAddr));
		cmdButton10.setVisible(false);
		deleteButton10.setText(DELETE);
		deleteButton10.setToolTipText("Remove macro link");
    }
    
    // update the panel first 8 accessories
	// returns true if 2nd read is needed
	private boolean loadAccy1to8(NceReply r) {
		// flag second read only necessary for accessories 9 and 10
		boolean req2ndRead = false;

		// Set all fields to default and build from there
		initAccyFields();

		// As soon as a macro link is found, stop reading the rest
		// of the macro
		// update the first accessory in the table
		// we know it exist or we wouldn't be here!

		int row = 1;
		//	1st word of macro
		int accyAddr = getNextMacroAccyAdr(row, r);
		// neg address = link address
		// null = empty 
		if (accyAddr <= 0)
			return req2ndRead;
		// enter accessory
		setAccy(row++, accyAddr, r, textAccy1, accyTextField1, cmdButton1,
				deleteButton1);

		// 2nd word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy2, accyTextField2, cmdButton2,
				deleteButton2);

		// 3rd word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy3, accyTextField3, cmdButton3,
				deleteButton3);

		// 4th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy4, accyTextField4, cmdButton4,
				deleteButton4);

		// 5th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy5, accyTextField5, cmdButton5,
				deleteButton5);

		// 6th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy6, accyTextField6, cmdButton6,
				deleteButton6);

		// 7th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy7, accyTextField7, cmdButton7,
				deleteButton7);

		// 8th word of macro
		accyAddr = getNextMacroAccyAdr(row, r);
		if (accyAddr <= 0)
			return req2ndRead;
		setAccy(row++, accyAddr, r, textAccy8, accyTextField8, cmdButton8,
				deleteButton8);
		req2ndRead = true; // Need to read NCE CS memory for 9 & 10
		return req2ndRead;
	}
    
    // update the panel 9 & 10 accessories
    private void loadAccy9and10(NceReply r){
		// 9th word of macro arrives in second read block
    	int pass2ndRow= 1;
		int accyAddr = getNextMacroAccyAdr(pass2ndRow, r);
		if (accyAddr <= 0) {
			return;
		}
		if (accyAddr > 0) {
			setAccy(pass2ndRow++, accyAddr, r, textAccy9, accyTextField9, cmdButton9,
					deleteButton9);
		}
		// 10th word of macro
		accyAddr = getNextMacroAccyAdr(pass2ndRow, r);
		if (accyAddr <= 0) {
			return;
		}
		if (accyAddr > 0) {
			setAccy(pass2ndRow++, accyAddr, r, textAccy10, accyTextField10, cmdButton10,
					deleteButton10);
			deleteButton10.setText(DELETE);
			deleteButton10.setToolTipText("Remove this accessory from the macro");
		}
		return;	// done with reading macro
      }
    
    // loads one row with a macro's accessory address and command
    private void setAccy(int row, int accyAddr, NceReply r, JLabel textAccy,
			JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
		textAccy.setText(ACCESSORY);
		accyTextField.setText(Integer.toString(accyAddr));
		deleteButton.setEnabled(true);
		cmdButton.setText(getAccyCmd(row, r));
	}
    
    // returns the accessory command
    private String getAccyCmd (int row, NceReply r){
		int b = (row - 1) << 1;
		int accyCmd = r.getElement(b+1);
		String s = THROWN;
		if (checkBoxNce.isSelected())
			s = THROWN_NCE;
		accyCmd = accyCmd & 0x01;
		if (accyCmd == 0){
			return s;
		}else{
			s = CLOSED;
			if (checkBoxNce.isSelected())
				s = CLOSED_NCE;
		}
		return s;
    }
    
    // Check for valid macro, return number if valid, -1 if not.
    private int validMacro (String s){
    	int mN;
		try {
			mN = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
		if (mN < 0 | mN > MAX_MACRO)
			return -1;
		else
			return mN;
    }
    
    // gets the last 4 bytes of the macro by reading 16 bytes of data 
    private void getMacro2ndHalf (int mN){
    	NceMessage m = readMacroMemory (mN, true);
		NceTrafficController.instance().sendNceMessage(m, this);
    }
 
    // Reads 16 bytes of NCE macro memory, and adjusts for second read if needed 
    private NceMessage readMacroMemory(int macroNum, boolean second) {
       	int nceMacroAddr = (macroNum * 20) + CS_MACRO_MEM;
    	if(second){
    		nceMacroAddr = nceMacroAddr + 16;	//adjust for second memory read
    	}
    	replyLen = REPLY_16;			// Expect 16 byte response
    	waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_16);
		return m;
    }
    
    // writes 20 bytes of NCE macro memory, and adjusts for second write 
	private NceMessage writeMacroMemory(int macroNum, byte[] b, boolean second) {
		int nceMacroAddr = (macroNum * 20) + CS_MACRO_MEM;
		replyLen = REPLY_1; // Expect 1 byte response
		waiting++;
		byte[] bl;

		// write 4 bytes
		if (second) {
			nceMacroAddr += 16; 	// adjust for second memory
			// write
			bl = NceBinaryCommand.accMemoryWriteN(nceMacroAddr, 4);
			int j = bl.length-16;
			for (int i = 0; i < 4; i++, j++)
				bl[j] = b[i+16];
			
		// write 16 bytes	
		} else {
			bl = NceBinaryCommand.accMemoryWriteN(nceMacroAddr, 16);
			int j = bl.length-16;
			for (int i = 0; i < 16; i++, j++)
				bl[j] = b[i];
		}
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_1);
		return m;
	}
    
    private void addAccyRow (JComponent col1, JComponent col2, JComponent col3, JComponent col4, JComponent col5, int row){
        addItem(col1,0,row); 
        addItem(col2,1,row);
        addItem(col3,2,row);
        addItem(col4,3,row);
        addItem(col5,4,row);	
    }
    
    private void addItem(JComponent c, int x, int y ){
    	GridBagConstraints gc = new GridBagConstraints ();
    	gc.gridx = x;
    	gc.gridy = y;
    	gc.weightx = 100.0;
    	gc.weighty = 100.0;
    	getContentPane().add(c, gc);
    }
    
    private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}

	private void addButtonCmdAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionCmdPerformed(e);
			}
		});
	}  
    
    private void addButtonDelAction (JButton b){
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionDeletePerformed(e);
			}
		});
    } 
    
    private void addCheckBoxAction (JCheckBox cb){
		cb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
    } 
    
    //  initialize accessories 1 to 10
    private void initAccyFields() {
    	initAccyRow(1, num1, textAccy1, accyTextField1, cmdButton1, deleteButton1);
    	initAccyRow(2, num2, textAccy2, accyTextField2, cmdButton2, deleteButton2);
    	initAccyRow(3, num3, textAccy3, accyTextField3, cmdButton3, deleteButton3);
    	initAccyRow(4, num4, textAccy4, accyTextField4, cmdButton4, deleteButton4);
    	initAccyRow(5, num5, textAccy5, accyTextField5, cmdButton5, deleteButton5);
    	initAccyRow(6, num6, textAccy6, accyTextField6, cmdButton6, deleteButton6);
    	initAccyRow(7, num7, textAccy7, accyTextField7, cmdButton7, deleteButton7);
    	initAccyRow(8, num8, textAccy8, accyTextField8, cmdButton8, deleteButton8);
    	initAccyRow(9, num9, textAccy9, accyTextField9, cmdButton9, deleteButton9);
    	initAccyRow(10, num10, textAccy10, accyTextField10, cmdButton10, deleteButton10);
	}
    
    private void initAccyRow(int row, JLabel num, JLabel textAccy, JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
		num.setText(Integer.toString(row));
		num.setVisible(true);
		textAccy.setText(EMPTY);
		textAccy.setVisible(true);
		cmdButton.setText(QUESTION);
		cmdButton.setVisible(true);
		cmdButton.setToolTipText("Set accessory command to closed or thrown");
		deleteButton.setText(DELETE);
		deleteButton.setVisible(true);
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText("Remove this accessory from the macro");
		accyTextField.setText("");
		accyTextField.setToolTipText("Enter accessory address 1 to 2044");
		accyTextField.setMaximumSize(new Dimension(accyTextField
				.getMaximumSize().width,
				accyTextField.getPreferredSize().height));
		if (row == 10)
			initAccyRow10 ();
    }
    
    private void initAccyRow10 (){
		cmdButton10.setVisible(true);
		deleteButton10.setText(LINK);
		deleteButton10.setEnabled(true);
		deleteButton10.setToolTipText("Link another macro to this one");
		accyTextField10.setToolTipText("Enter accessory address 1 to 2044 or link macro address 1 to 255");
    }
    
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMacroEditFrame.class.getName());	
}

