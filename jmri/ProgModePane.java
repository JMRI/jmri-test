/** 
 * ProgModePane.java
 *
 * Description:		Pane to select programming mode
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

/*
 *  Note that you should call the done() method when you're really done, so that
 *  a ProgModePane object can disconnect its listeners.
 */
public class ProgModePane extends javax.swing.JPanel implements java.beans.PropertyChangeListener {

	// GUI member declarations
	
	javax.swing.ButtonGroup modeGroup 			= new javax.swing.ButtonGroup();
	javax.swing.JRadioButton addressButton  	= new javax.swing.JRadioButton();
	javax.swing.JRadioButton pagedButton    	= new javax.swing.JRadioButton();
	javax.swing.JRadioButton directBitButton   	= new javax.swing.JRadioButton();
	javax.swing.JRadioButton directByteButton   = new javax.swing.JRadioButton();
	javax.swing.JRadioButton registerButton 	= new javax.swing.JRadioButton();
			
	public ProgModePane() {

		// configure items for GUI				
		pagedButton.setText("Paged Mode");
		directBitButton.setText("Direct Bit Mode");
		directByteButton.setText("Direct Byte Mode");
		registerButton.setText("Register Mode");
		addressButton.setText("Address Mode");
		modeGroup.add(pagedButton);
		modeGroup.add(directByteButton);
		modeGroup.add(directBitButton);
		modeGroup.add(registerButton);
		modeGroup.add(addressButton);

		// add listeners to buttons
		pagedButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
				connect();
				if (connected) setProgrammerMode(getMode());
			}
		});
		directBitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
				connect();
				if (connected) setProgrammerMode(getMode());
			}
		});
		directByteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
				connect();
				if (connected) setProgrammerMode(getMode());
			}
		});
		registerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
				connect();
				if (connected) setProgrammerMode(getMode());
			}
		});
		addressButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
				connect();
				if (connected) setProgrammerMode(getMode());
			}
		});
		
		// load the state if a programmer exists
		connect();
		if (connected) InstanceManager.programmerInstance().setMode(InstanceManager.programmerInstance().getMode());
		
		// general GUI config
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// install items in GUI
		add(pagedButton);
		add(directBitButton);
		add(directByteButton);
		add(registerButton);
		add(addressButton);
	}
  		
  	public int getMode() {
  		if (pagedButton.isSelected())
	  		return jmri.Programmer.PAGEMODE;
	  	else if (directBitButton.isSelected())
	  		return jmri.Programmer.DIRECTBITMODE;
	  	else if (directByteButton.isSelected())
	  		return jmri.Programmer.DIRECTBYTEMODE;
	  	else if (registerButton.isSelected())
	  		return jmri.Programmer.REGISTERMODE;
	  	else if (addressButton.isSelected())
	  		return jmri.Programmer.ADDRESSMODE;
	  	else
	  		return 0;
  	}
  	  		

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// mode changed in programmer, change GUI here if needed
		if (e.getPropertyName() == "Mode") {
			int mode = ((Integer)e.getNewValue()).intValue();
			switch (mode) {
				case jmri.Programmer.REGISTERMODE:
					registerButton.setSelected(true);
					break;
				case jmri.Programmer.PAGEMODE:
					pagedButton.setSelected(true);
					break;
				case jmri.Programmer.DIRECTBYTEMODE:
					directByteButton.setSelected(true);
					break;
				case jmri.Programmer.DIRECTBITMODE:
					directBitButton.setSelected(true);
					break;
				case jmri.Programmer.ADDRESSMODE:
					addressButton.setSelected(true);
					break;
				default:
					log.warn("propertyChange without valid mode value");
					break;
			}
		} else log.warn("propertyChange with unexpected propertyName: "+e.getPropertyName());
	}

	// connect to the Programmer interface
	boolean connected = false;
	
	private void connect() {
		if (!connected) {
			if (InstanceManager.programmerInstance() != null) {
				InstanceManager.programmerInstance().addPropertyChangeListener(this);
				connected = true;
				log.info("Connecting to programmer");
			} else {
				log.info("No programmer present to connect");
			}
		}
	}
	
	// set the programmer to the current mode
	private void setProgrammerMode(int mode) {
			log.info("Setting programmer to mode "+mode);
			if (InstanceManager.programmerInstance() != null) InstanceManager.programmerInstance().setMode(mode);
	}
	
	// no longer needed, disconnect if still connected
	public void done() {
		if (connected) {
			if (InstanceManager.programmerInstance() != null) 
				InstanceManager.programmerInstance().removePropertyChangeListener(this);
			connected = false;
		}
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgModePane.class.getName());

}
