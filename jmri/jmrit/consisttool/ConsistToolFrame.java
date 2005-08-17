// ConsistToolFrame.java

package jmri.jmrit.consisttool;

import jmri.InstanceManager;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.jmrit.roster.*;
import jmri.DccLocoAddress;

import jmri.jmrit.throttle.ThrottleFrameManager;

import java.awt.*;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;

/**
 * Frame object for manipulating consists.
 *
 * @author               Paul Bender Copyright (C) 2003
 * @version              $Revision: 1.15 $
 */
public class ConsistToolFrame extends javax.swing.JFrame implements jmri.ConsistListener{

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    jmri.jmrit.DccLocoAddressSelector adrSelector = new jmri.jmrit.DccLocoAddressSelector();
    javax.swing.JComboBox consistAdrBox = new javax.swing.JComboBox();

    javax.swing.JRadioButton isAdvancedConsist = new javax.swing.JRadioButton("Advanced Consist");
    javax.swing.JRadioButton isCSConsist = new javax.swing.JRadioButton("Command Station Consist");

    javax.swing.JButton deleteButton = new javax.swing.JButton();
    javax.swing.JButton throttleButton = new javax.swing.JButton();

    javax.swing.JLabel textLocoLabel = new javax.swing.JLabel();
    jmri.jmrit.DccLocoAddressSelector locoSelector = new jmri.jmrit.DccLocoAddressSelector();
    javax.swing.JComboBox locoRosterBox;

    javax.swing.JButton addLocoButton = new javax.swing.JButton();
    javax.swing.JButton resetLocoButton = new javax.swing.JButton();

    javax.swing.JCheckBox locoDirectionNormal = new javax.swing.JCheckBox("Direction Normal");

    ConsistDataModel consistModel = new ConsistDataModel(1,4);
    javax.swing.JTable consistTable = new javax.swing.JTable(consistModel);

    ConsistManager ConsistMan = null;

    javax.swing.JLabel _status = new javax.swing.JLabel("Ready");

    private int _Consist_Type = Consist.ADVANCED_CONSIST;

    public ConsistToolFrame() {

  	ConsistMan = InstanceManager.consistManagerInstance();

        // configure items for GUI


        textAdrLabel.setText("Consist:");
        textAdrLabel.setVisible(true);

	adrSelector.setVisible(true);
        adrSelector.setToolTipText("consist being created or deleted");

	initializeConsistBox();

	consistAdrBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			consistSelected();
		}
	});

	consistAdrBox.setToolTipText("Select an existing Consist");

        isAdvancedConsist.setSelected(true);
	isAdvancedConsist.setVisible(true);
	isAdvancedConsist.setEnabled(false);
        isAdvancedConsist.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    isAdvancedConsist.setSelected(true);
		    isCSConsist.setSelected(false);
		    _Consist_Type = Consist.ADVANCED_CONSIST;
		    adrSelector.setEnabled(true);
                }
            });
        isCSConsist.setSelected(false);
	isCSConsist.setVisible(true);
	isCSConsist.setEnabled(false);
        isCSConsist.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    isAdvancedConsist.setSelected(false);
		    isCSConsist.setSelected(true);
		    _Consist_Type = Consist.CS_CONSIST;
	     	    if(ConsistMan.csConsistNeedsSeperateAddress()) {
		    adrSelector.setEnabled(false);
		    }
		    else adrSelector.setEnabled(true);
                }
            });

	if(ConsistMan.isCommandStationConsistPossible())
	{
		isAdvancedConsist.setEnabled(true);
		isCSConsist.setEnabled(true);
	}

        deleteButton.setText("Delete");
        deleteButton.setVisible(true);
        deleteButton.setToolTipText("Delete the consist/remove all locomotives");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    deleteButtonActionPerformed(e);
                }
            });

        throttleButton.setText("Throttle");
        throttleButton.setVisible(true);
        throttleButton.setToolTipText("Create the consist AND Start a Throttle for it");
        throttleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    throttleButtonActionPerformed(e);
                }
            });

        // Set up the controlls for the First Locomotive in the consist.

        textLocoLabel.setText("New Locomotive");
        textLocoLabel.setVisible(true);

        locoSelector.setToolTipText("Address of A New Locomotive to add to the consist");
	locoSelector.setVisible(true);
        
        locoRosterBox = Roster.instance().fullRosterComboBox();
        locoRosterBox.insertItemAt("",0);
        locoRosterBox.setSelectedIndex(0);

        locoRosterBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                        locoSelected();
                }
        });

        locoRosterBox.setVisible(true);

        locoDirectionNormal.setToolTipText("Consist Forward is Forward for this locomotive if checked");

        locoDirectionNormal.setSelected(true);
        locoDirectionNormal.setVisible(true);
        locoDirectionNormal.setEnabled(false);

        addLocoButton.setText("add");
        addLocoButton.setVisible(true);
        addLocoButton.setToolTipText("Add to the Consist");
        addLocoButton.addActionListener(new java.awt.event.ActionListener()
        {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addLocoButtonActionPerformed(e);
                }
            });

        resetLocoButton.setText("reset");
        resetLocoButton.setVisible(true);
        resetLocoButton.setToolTipText("Reset locomotive information");
        resetLocoButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    resetLocoButtonActionPerformed(e);
                }
            });

        // general GUI config
        setTitle("Consist Control");
        //getContentPane().setLayout(new GridLayout(4,1));
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

        // install items in GUI

        // The address and related buttons are installed in a single pane
        JPanel addressPanel = new JPanel();
        addressPanel.setLayout(new FlowLayout());

        addressPanel.add(textAdrLabel);
        addressPanel.add(adrSelector.getCombinedJPanel());
        addressPanel.add(consistAdrBox);
        addressPanel.add(isAdvancedConsist);
        addressPanel.add(isCSConsist);

        getContentPane().add(addressPanel);

        // The address and related buttons for each Locomotive
        // are installed in a single pane

        // New Locomotive
        JPanel locoPanel = new JPanel();
        locoPanel.setLayout(new FlowLayout());

        locoPanel.add(textLocoLabel);

        locoPanel.add(locoSelector.getCombinedJPanel());
        
        locoPanel.add(locoRosterBox);
        locoPanel.add(locoDirectionNormal);

        locoPanel.add(addLocoButton);
        locoPanel.add(resetLocoButton);

        getContentPane().add(locoPanel);

	// Set up the jtable in a Scroll Pane..
	JScrollPane consistPane = new JScrollPane(consistTable);
	consistPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	consistModel.initTable(consistTable);
	getContentPane().add(consistPane);

	// Set up the Control Button panel
	JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new FlowLayout());

        controlPanel.add(deleteButton);
        controlPanel.add(throttleButton);

	getContentPane().add(controlPanel);

	// add the status line directly to the bottom of the ContentPane.
	JPanel statusPanel = new  JPanel();
	statusPanel.setLayout(new FlowLayout());
	statusPanel.add(_status);
	getContentPane().add(statusPanel);

        pack();

    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    private void initializeConsistBox() {
        ArrayList existingConsists = ConsistMan.getConsistList();
        if(!existingConsists.isEmpty()) {
           consistAdrBox.removeAllItems();
           for(int i=0; i<existingConsists.size(); i++) {
                consistAdrBox.insertItemAt(existingConsists.get(i),i);
           }
           consistAdrBox.setEnabled(true);
           consistAdrBox.insertItemAt("",0);
           consistAdrBox.setSelectedItem(adrSelector.getAddress());
           if(adrSelector.getAddress()!=null) {
		if(consistModel.getConsist()!=null){
			consistModel.getConsist().removeConsistListener(this);
			_status.setText("Ready");
		}
 		consistModel.setConsist(adrSelector.getAddress());
		consistModel.getConsist().addConsistListener(this);
		adrSelector.setEnabled(false);
	   } else {
		if(consistModel.getConsist()!=null){
			consistModel.getConsist().removeConsistListener(this);
			_status.setText("Ready");
		}
		consistModel.setConsist((Consist)null);
		adrSelector.setEnabled(true);
	   }
        } else {
	   consistAdrBox.setEnabled(false);
           consistAdrBox.removeAllItems();
           consistAdrBox.insertItemAt("",0);
           consistAdrBox.setSelectedIndex(0);
	   if(consistModel.getConsist()!=null){
		consistModel.getConsist().removeConsistListener(this);
		_status.setText("Ready");
	   }
	   consistModel.setConsist((Consist)null);
	   adrSelector.setEnabled(true);
        }
     }


    public void deleteButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(adrSelector.getAddress()==null) {
           	javax.swing.JOptionPane.showMessageDialog(this,
						"No Consist Address Selected");
		return;
	}
	DccLocoAddress address=adrSelector.getAddress();
	Consist tempConsist = ConsistMan.getConsist(address);
	/* get the list of locomotives to delete */
	ArrayList addressList = tempConsist.getConsistList();

	for(int i=(addressList.size()-1);i>=0;i--) {
		if(log.isDebugEnabled()) log.debug("Deleting Locomotive: " + (String)addressList.get(i));
		DccLocoAddress locoaddress=(DccLocoAddress)addressList.get(i);
		try {
			tempConsist.remove(locoaddress);
	    	}  catch (Exception ex) {
					log.error("Error removing address "
						    + locoaddress.toString()
						    + " from consist "
						    + address.toString());
		}
		//addressList = tempConsist.getConsistList();
	}
	try {
		ConsistMan.delConsist(address);
	    }  catch (Exception ex) {
					log.error("Error delting consist "
						    + address);
	    }
	adrSelector.reset();
	adrSelector.setEnabled(true);
        initializeConsistBox();
        resetLocoButtonActionPerformed(e);
	canAdd();
    	}

    public void throttleButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(adrSelector.getAddress()==null) {
           	javax.swing.JOptionPane.showMessageDialog(this,
						"No Consist Address Selected");
		return;
		}
	  // make sure any new locomotives are added to the consist.
	  addLocoButtonActionPerformed(e);
	  // Create a throttle object with the selected consist address
	  jmri.jmrit.throttle.ThrottleFrame tf=
		jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
		DccLocoAddress address=adrSelector.getAddress();
		tf.notifyAddressChosen(address.getNumber(), address.isLongAddress());
		tf.setVisible(true);
    	}

    public void consistSelected() {
	if(log.isDebugEnabled()) log.debug("Consist Selected");
	if(consistAdrBox.getSelectedIndex()==-1 && !(adrSelector.getAddress()==null)) {
	   if(log.isDebugEnabled()) log.debug("No Consist Selected");
	   adrSelector.setEnabled(false);
	   recallConsist();
	}
	else if(consistAdrBox.getSelectedIndex()==-1 || 
		consistAdrBox.getSelectedItem().equals("")) {
	   if(log.isDebugEnabled()) log.debug("Null Consist Selected");
	   adrSelector.reset();
	   adrSelector.setEnabled(true);
	   recallConsist();
	} else 
if(((DccLocoAddress)consistAdrBox.getSelectedItem())!=adrSelector.getAddress()) {
    	   if(log.isDebugEnabled()) log.debug("Consist " + consistAdrBox.getSelectedItem().toString() +" Selected");
	   adrSelector.setEnabled(false);
	   adrSelector.setAddress((DccLocoAddress)consistAdrBox.getSelectedItem());
	   recallConsist();
	}
    }

    // Recall the consist
    private void recallConsist() {
	if(adrSelector.getAddress()==null) {
	   // Clear any consist information that was present
	   locoSelector.reset();
	   locoRosterBox.setSelectedIndex(0);
	   if(consistModel.getConsist()!=null){
		consistModel.getConsist().removeConsistListener(this);
		_status.setText("Ready");
	   }
	   consistModel.setConsist((Consist)null);
	
           canAdd();

	   return;
	}
	DccLocoAddress address = adrSelector.getAddress();
	if(consistModel.getConsist()!=null){
		consistModel.getConsist().removeConsistListener(this);
		_status.setText("Ready");
	}
	Consist selectedConsist = ConsistMan.getConsist(address);
	consistModel.setConsist(selectedConsist);
	selectedConsist.addConsistListener(this);

	// reset the editable locomotive information.
	locoSelector.reset();
	locoRosterBox.setSelectedIndex(0);
        locoDirectionNormal.setSelected(true);

	// if there aren't any locomotives in the consist, don't let
	// the user change the direction
	if(consistModel.getRowCount()==0)
		locoDirectionNormal.setEnabled(false);
	else
		locoDirectionNormal.setEnabled(true);

	if(log.isDebugEnabled()) log.debug("Recall Consist " + address);

	// What type of consist is this?
	if(selectedConsist.getConsistType()==Consist.ADVANCED_CONSIST) {
	        log.debug("Consist type is Advanced Consist ");
	        isAdvancedConsist.setSelected(true);
        	isCSConsist.setSelected(false);
		_Consist_Type=Consist.ADVANCED_CONSIST;
	} else {
	  // This must be a CS Consist.
	        log.debug("Consist type is Command Station Consist ");
	        isAdvancedConsist.setSelected(false);
        	isCSConsist.setSelected(true);
		_Consist_Type=Consist.CS_CONSIST;
	}

	canAdd();
    }

    public void resetLocoButtonActionPerformed(java.awt.event.ActionEvent e) {
	   locoSelector.reset();
	   locoRosterBox.setSelectedIndex(0);
           locoDirectionNormal.setSelected(true);
	   // if there aren't any locomotives in the consist, don't let
	   // the user change the direction
	   if(consistModel.getRowCount()==0)
	        locoDirectionNormal.setEnabled(false);
	   else
		locoDirectionNormal.setEnabled(true);
	}

     // Check to see if a consist address is selected, and if it
     //	is, dissable the "add button" if the maximum consist size is reached
     public void canAdd(){
	   // If a consist address is selected, dissable the "add button"
	   // if the maximum size is reached
	   if(adrSelector.getAddress()!=null) {
		DccLocoAddress address = adrSelector.getAddress();
		if(consistModel.getRowCount()==	ConsistMan.getConsist(address)
							   .sizeLimit()){
			locoSelector.setEnabled(false);
    			locoRosterBox.setEnabled(false);
			addLocoButton.setEnabled(false);
			resetLocoButton.setEnabled(false);
			locoDirectionNormal.setEnabled(false);
		} else {
			locoSelector.setEnabled(true);
    			locoRosterBox.setEnabled(true);
			addLocoButton.setEnabled(true);
			resetLocoButton.setEnabled(true);
			locoDirectionNormal.setEnabled(false);
	   		// if there aren't any locomotives in the consist,
			// don't let the user change the direction
	   		if(consistModel.getRowCount()==0)
	   		     locoDirectionNormal.setEnabled(false);
	   		else
			     locoDirectionNormal.setEnabled(true);
		 }
	   } else {
		locoSelector.setEnabled(true);
    		locoRosterBox.setEnabled(true);
		addLocoButton.setEnabled(true);
		resetLocoButton.setEnabled(true);
		locoDirectionNormal.setEnabled(false);
	   	// if there aren't any locomotives in the consist, don't let
	   	// the user change the direction
	   	if(consistModel.getRowCount()==0)
	        	locoDirectionNormal.setEnabled(false);
	   	else
			locoDirectionNormal.setEnabled(true);
	   }
	}

    public void addLocoButtonActionPerformed(java.awt.event.ActionEvent e) {
	if(locoSelector.getAddress()==null) { return; }
	if(_Consist_Type==Consist.ADVANCED_CONSIST && adrSelector.getAddress()==null) {
           	javax.swing.JOptionPane.showMessageDialog(this,
						"No Consist Address Selected");
		return;
	}
	else if(_Consist_Type==Consist.ADVANCED_CONSIST && 
	   adrSelector.getAddress().isLongAddress()) {
           	javax.swing.JOptionPane.showMessageDialog(this,
						"Advanced Consist Requires Short Consist Address");
		return;
	}
	else if(_Consist_Type==Consist.CS_CONSIST && adrSelector.getAddress()==null) {
	     if(ConsistMan.csConsistNeedsSeperateAddress()) {
           	javax.swing.JOptionPane.showMessageDialog(this,
						"No Consist Address Selected");
		return;
	     }
	     else {
		// We need to set an identifier so we can recall the
	        // consist.  We're going to use the lead locomotive number 
		// for this.
		adrSelector.setAddress(locoSelector.getAddress());
	     }
	}
	DccLocoAddress address = adrSelector.getAddress();
	/* Make sure the marked consist type matches the consist type
	   stored for this consist */
	if(_Consist_Type != ConsistMan.getConsist(address).getConsistType()){
		if (log.isDebugEnabled()) {
			if(_Consist_Type==Consist.ADVANCED_CONSIST)
				log.debug("Setting Consist Type to Advanced Consist");
			else if(_Consist_Type==Consist.CS_CONSIST)
				log.debug("Setting Consist Type to Command Station Assisted Consist");
		}
		ConsistMan.getConsist(address).setConsistType(_Consist_Type);
	}
	
	DccLocoAddress locoaddress=locoSelector.getAddress();
	
	// Make sure the Address in question is allowed for this type of
	// consist, and add it to the consist if it is
	if(!ConsistMan.getConsist(address).isAddressAllowed(locoaddress)){
           	javax.swing.JOptionPane.showMessageDialog(this,
						"Selected Address not allowed in this consist");
		return;
	} else {
	    ConsistMan.getConsist(address).add(locoaddress,
					       locoDirectionNormal.isSelected());	    
   	    if(consistAdrBox.getSelectedItem()!=adrSelector.getAddress())
		initializeConsistBox();
	    consistModel.fireTableDataChanged();
            resetLocoButtonActionPerformed(e);
	}
    }

    public void locoSelected() {
	if (!(locoRosterBox.getSelectedItem().equals(""))){
           String rosterEntryTitle = locoRosterBox.getSelectedItem().toString();
           RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		locoSelector.setAddress(entry.getDccLocoAddress());
        }
    }

    /*
     * we're registering as a listener for Consist events, so we need to
     * implement the interface
     */
    public void consistReply(DccLocoAddress locoaddress,int status){
	if(log.isDebugEnabled()) log.debug("Consist Reply recieved for Locomotive "  +locoaddress.toString() + " with status " + status);
	_status.setText(ConsistMan.decodeErrorCode(status));
	// For some status codes, we want to trigger specific actions
	if((status & jmri.ConsistListener.CONSIST_FULL)!=0) {
		canAdd();
	} else {
		canAdd();
	}
	consistModel.fireTableDataChanged();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistToolFrame.class.getName());

}
