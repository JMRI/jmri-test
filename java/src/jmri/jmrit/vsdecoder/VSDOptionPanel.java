package jmri.jmrit.vsdecoder;

/*
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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import javax.swing.*;
import java.awt.*;
import jmri.util.swing.*;

@SuppressWarnings("serial")
public class VSDOptionPanel extends JmriPanel {

    private javax.swing.JComboBox hornOptionComboBox;

    String decoder_id;
    VSDecoderPane main_frame;

    public VSDOptionPanel() {
	this(null, null);
    }

    public VSDOptionPanel(String dec, VSDecoderPane dad) {
	super();
	decoder_id = dec;
	main_frame = dad;
	initComponents();
    }

    public void init() {}

    public void initContext(Object context) {
	initComponents();
    }

    public void initComponents() {

	// Below is mostly just "filler" stuff until we implement the real thing

	this.setLayout(new GridLayout(0,2));

	hornOptionComboBox = new javax.swing.JComboBox();
	hornOptionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3-Chime Leslie", "5-Chime Leslie", "4-Chime Nathan" }));
	JLabel x = new JLabel();
	x.setText("Horn Option: ");
	this.add(x);
	this.add(hornOptionComboBox);
	x = new JLabel();
	x.setText("Engine Option: ");
	this.add(x);
	JComboBox y = new javax.swing.JComboBox();
	y.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Non-Turbo", "Turbo" }));
	this.add(y);
    }

    // Unused as yet.  Commented out to hide the compiler warning.
    //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDOptionPanel.class.getName());

    
}