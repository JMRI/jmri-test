package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jmri.DccLocoAddress;

/**
 * Frame for defining and launching an entry/exit warrant.  An NX warrant is a warrant that
 * can be defined on the run without a pre-recorded learn mode session using a set script for
 * ramping startup and stop throttle settings.
 * <P>
 * The route can be defined in a form or by mouse clicking on the OBlock IndicatorTrack icons.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class NXFrame extends WarrantRoute {
	WarrantTableFrame 	_parent;
    JTextField  _dccNumBox = new JTextField();
    JTextField  _trainNameBox = new JTextField();
    JTextField  _nameBox = new JTextField();
    JTextField  _speedBox = new JTextField();
    JRadioButton _forward = new JRadioButton(Bundle.getMessage("forward"));
    JRadioButton _reverse = new JRadioButton(Bundle.getMessage("reverse"));
    JCheckBox	_stageEStop = new JCheckBox();    
    JCheckBox	_haltStart = new JCheckBox();
    JCheckBox	_addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField();
    JTextField _searchDepth = new JTextField();
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel		_autoRunPanel;
    JPanel		_manualPanel;

    private static NXFrame _instance;
    
    static NXFrame getInstance() {
    	if (_instance==null) {
    		_instance = new NXFrame();
    	}
    	_instance.setVisible(true);
    	_instance._dccNumBox.setText(null);
    	_instance._trainNameBox.setText(null);
    	_instance._nameBox.setText(null);
    	_instance.clearRoute();
    	return _instance;
    }

    private NXFrame() {
		super();
		_parent = WarrantTableFrame.getInstance();
		setTitle(Bundle.getMessage("AutoWarrant"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10,10));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        panel.add(makeBlockPanels());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup bg = new ButtonGroup();
        bg.add(_runAuto);
        bg.add(_runManual);
        _runAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(true);
            }
        });
        _runManual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(false);
            }
        });
        _runAuto.setSelected(true);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runAuto);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runManual);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        p1.add(WarrantFrame.makeTextBoxPanel(false, _speedBox, "Speed", true));        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(WarrantFrame.makeTextBoxPanel(false, _trainNameBox, "TrainName", true));
        bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        ppp.add(_forward);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        ppp.add(_reverse);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        p2.add(ppp);
        
        _autoRunPanel = new JPanel();
        _autoRunPanel.setLayout(new BoxLayout(_autoRunPanel, BoxLayout.Y_AXIS));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p2);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        _autoRunPanel.add(pp);
        _autoRunPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeBoxPanel(false, _stageEStop, "StageEStop"));
        ppp.add(WarrantFrame.makeBoxPanel(false, _haltStart, "HaltAtStart"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _rampInterval, "rampInterval", true));
        ppp.add(WarrantFrame.makeBoxPanel(false, _addTracker, "AddTracker"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        _autoRunPanel.add(pp);
       
        _manualPanel = new JPanel();
        _manualPanel.setLayout(new BoxLayout(_manualPanel, BoxLayout.X_AXIS));
        _manualPanel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        _manualPanel.add(WarrantFrame.makeTextBoxPanel(false, _nameBox, "TrainName", true));
        _manualPanel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        
        panel.add(_autoRunPanel);
        panel.add(_manualPanel);
		_manualPanel.setVisible(false);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        
        pp = new JPanel();
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(WarrantFrame.makeTextBoxPanel(false, _searchDepth, "SearchDepth", true));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(pp);
        _forward.setSelected(true);
        _stageEStop.setSelected(WarrantTableFrame._defaultEStop);
        _haltStart.setSelected(WarrantTableFrame._defaultHaltStart);
        _addTracker.setSelected(WarrantTableFrame._defaultAddTracker);
        _speedBox.setText(WarrantTableFrame._defaultSpeed);
        _rampInterval.setText(WarrantTableFrame._defaultIntervalTime);
        _searchDepth.setText(WarrantTableFrame._defaultSearchdepth);
        JPanel p = new JPanel();
        JButton button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    	makeAndRunWarrant();
                    }
                });
        p.add(button);
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    	dispose();
                    	_parent.closeNXFrame();
                    }
                });
        p.add(button);
        panel.add(p);
        mainPanel.add(panel);
        getContentPane().add(mainPanel);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                _parent.closeNXFrame();
            }
        });
        setLocation(_parent.getLocation().x+200, _parent.getLocation().y+100);
        setAlwaysOnTop(true);
        pack();
        setVisible(true);      		
	}
    
    private void enableAuto(boolean enable) {
    	if (enable) {
    		_manualPanel.setVisible(false);
    		_autoRunPanel.setVisible(true);
    	} else {
    		_manualPanel.setVisible(true);
    		_autoRunPanel.setVisible(false);    		
    	}
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
//        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
        	doAction(e.getSource());
        }
    }

    /**
     * Callback from RouteFinder.findRoute()
     */
    public void selectedRoute(ArrayList<BlockOrder> orders) {
    	String msg =null;
    	Warrant warrant = null;
    	if (_runManual.isSelected()) {
    		runManual();
    		return;
    	} else if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
            msg = Bundle.getMessage("NoLoco");
        }
		if (msg==null) {
        	String name =_trainNameBox.getText();
        	if (name==null || name.trim().length()==0) {
        		name = _addr;
        	}
        	String s = (""+Math.random()).substring(2);
        	warrant = new Warrant("IW"+s, "NX("+_addr+")");
        	warrant.setDccAddress( new DccLocoAddress(_dccNum, _isLong));
        	warrant.setTrainName(name);
        	
        	msg = makeCommands(warrant);           	
            if (msg==null) {
                warrant.setBlockOrders(getOrders());
            }
		}
        if (msg==null) {
        	_parent.getModel().addNXWarrant(warrant);	//need to catch propertyChange at start
        	msg = _parent.runTrain(warrant);
        	if (msg!=null) {
        		_parent.getModel().removeNXWarrant(warrant);
        	}
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            warrant = null;
        } else {
        	if (_haltStart.isSelected()) {
        		WarrantTableFrame._defaultHaltStart = true;
            	class Halter implements Runnable {
            		Warrant war;
            		Halter (Warrant w) {
            			war = w;
            		}
            		public void run() {
                    	int limit = 0;  
                    	try {
                        	while (!war.controlRunTrain(Warrant.HALT) && limit<3000) {
                        		Thread.sleep(200);
                        		limit += 200;
                        	}            		
                    	} catch (InterruptedException e) {
                    		war.controlRunTrain(Warrant.HALT);
                    	}           			
            		}
            	}
            	Halter h = new Halter(warrant);
            	new Thread(h).start();
         	} else {
        		WarrantTableFrame._defaultHaltStart = false;         		
         	}
        	_parent.scrollTable();
        	dispose();
        	_parent.closeNXFrame();           	
        }
    }
    private void runManual() {
    	String name =_nameBox.getText();
    	if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noTrainName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
    	}
    	String s = (""+Math.random()).substring(2);
    	Warrant warrant = new Warrant("IW"+s, "NX("+name+")");
    	warrant.setTrainName(name);            			
        warrant.setRoute(0, getOrders());
    	_parent.getModel().addNXWarrant(warrant);
    	warrant.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
    	_parent.scrollTable();
    	dispose();
    	_parent.closeNXFrame();           	
    }
    
    private String makeCommands(Warrant w) {
        String speed = _speedBox.getText();
        WarrantTableFrame._defaultSpeed = speed;
        String interval = _rampInterval.getText();
    	float f = 0; 
    	int time = 4000;
        try {
        	f = Float.parseFloat(speed);
        	if (f>1.0 || f<0) {
                return Bundle.getMessage("badSpeed");            	            		
        	}
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("badSpeed");            	
        }
        try {
        	float t = Float.parseFloat(interval)*1000;
        	if (t>60000 || t<0) {
                return Bundle.getMessage("invalidNumber");            	            		
        	}
        	time = Math.round(t);
        } catch (NumberFormatException nfe) {
        	time = 4000;            	
        }
        WarrantTableFrame._defaultIntervalTime = interval;
    	List<BlockOrder> orders = getOrders();
    	String blockName = orders.get(0).getBlock().getDisplayName();
    	w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(2000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(500, "Forward", 
    										(_forward.isSelected()?"true":"false"), blockName));
    	w.addThrottleCommand(new ThrottleSetting(0, "Speed", Float.toString(f/4), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/8), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/2), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/4), blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(7*f/8), blockName));        		
    	if (orders.size() > 2) {
        	blockName = orders.get(1).getBlock().getDisplayName();
    		w.addThrottleCommand(new ThrottleSetting(5000, "NoOp", "Enter Block", blockName));
        	if (orders.size() > 3) {
            	w.addThrottleCommand(new ThrottleSetting(time, "Speed", speed, blockName));            		
            	for (int i=2; i<orders.size()-2; i++) {
            		w.addThrottleCommand(new ThrottleSetting(20000, "NoOp", "Enter Block", 
            									orders.get(i).getBlock().getDisplayName()));        		
            	}
            	blockName = orders.get(orders.size()-2).getBlock().getDisplayName();
        		w.addThrottleCommand(new ThrottleSetting(10000, "NoOp", "Enter Block", blockName));        		            		
        	} else {
            	blockName = orders.get(orders.size()-2).getBlock().getDisplayName();
        	}
        	// next to last block
        	OBlock block = orders.get(orders.size()-2).getBlock();
        	int delay = (int)Math.max(block.getLengthIn()-48,0)*100;
        	w.addThrottleCommand(new ThrottleSetting(delay, "Speed", Float.toString(7*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/4), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(5*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/2), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(3*f/8), blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(f/3), blockName));        		
     	}
    	// Last block
    	OBlock block = orders.get(orders.size()-1).getBlock();
    	blockName = block.getDisplayName();
		w.addThrottleCommand(new ThrottleSetting(time, "NoOp", "Enter Block", blockName));        		
    	if (_stageEStop.isSelected()) {
    		WarrantTableFrame._defaultEStop = true;
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
        	time = 0;
    	} else {
    		WarrantTableFrame._defaultEStop = false;
    		int delay = (int)Math.max(block.getLengthIn()-6, 0)*500;
        	w.addThrottleCommand(new ThrottleSetting(delay, "Speed", Float.toString(f/4), blockName));
     	}
    	w.addThrottleCommand(new ThrottleSetting(time, "Speed", "0.0", blockName));        		
    	w.addThrottleCommand(new ThrottleSetting(500, "F2", "true", blockName));
    	w.addThrottleCommand(new ThrottleSetting(3000, "F2", "false", blockName));
    	w.addThrottleCommand(new ThrottleSetting(500, "F0", "false", blockName));
    	if (_addTracker.isSelected()) {
    		WarrantTableFrame._defaultAddTracker = true;
    	   	w.addThrottleCommand(new ThrottleSetting(10, "START TRACKER", "", blockName));
    	} else {
    		WarrantTableFrame._defaultAddTracker = false;
    	}
       	return null;    		
   }
    
    private String _addr;
    private int _dccNum;
    private boolean  _isLong;
 
	boolean makeAndRunWarrant() {
        int depth = 10;
        String msg = null;
    	_addr = _dccNumBox.getText();
        if (_addr!= null && _addr.length() != 0) {
        	_addr = _addr.toUpperCase().trim();
        	_isLong = false;
    		Character ch = _addr.charAt(_addr.length()-1);
    		try {
        		if (!Character.isDigit(ch)) {
        			if (ch!='S' && ch!='L' && ch!=')') {
        				msg = Bundle.getMessage("BadDccAddress", _addr);
        			}
        			if (ch==')') {
                    	_dccNum = Integer.parseInt(_addr.substring(0, _addr.length()-3));
                    	ch = _addr.charAt(_addr.length()-2);
                    	_isLong = (ch=='L');
        			} else {
                    	_dccNum = Integer.parseInt(_addr.substring(0, _addr.length()-1));        				
                    	_isLong = (ch=='L');
        			}
        		} else {
            		_dccNum = Integer.parseInt(_addr);
            		ch = _addr.charAt(0);
            		_isLong = (ch=='0' || _dccNum>127);  // leading zero means long
                    _addr = _addr + (_isLong?"L":"S");
        		}
            } catch (NumberFormatException nfe) {
                msg = Bundle.getMessage("BadDccAddress", _addr);
            }
        } else {
        	msg = Bundle.getMessage("BadDccAddress", _addr);
        }
        if (msg==null) {
        	try {
            	WarrantTableFrame._defaultSearchdepth = _searchDepth.getText();
                depth = Integer.parseInt(_searchDepth.getText());
            } catch (NumberFormatException nfe) {
            	depth = 10;
            }
            msg = findRoute(depth);
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
	}
	
    static Logger log = LoggerFactory.getLogger(NXFrame.class.getName());
}
