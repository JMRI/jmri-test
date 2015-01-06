// ProgServiceModeComboBox.java
package jmri.jmrit.progsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.beans.*;
import javax.swing.*;
import jmri.*;

/**
 * Provide a JPanel with a JComboBox to configure the service mode programmer.
 * <P>
 * The using code should get a configured programmer with getProgrammer. Since
 * there's only one service mode programmer, maybe this isn't critical, but
 * it's a good idea for the future.
 * <P>
 * A ProgModePane may "share" between one of these and a ProgOpsModePane,
 * which means that there might be _none_ of these buttons selected.  When
 * that happens, the mode of the underlying programmer is left unchanged
 * and no message is propagated.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 *
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
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
 */
public class ProgServiceModeComboBox extends ProgModeSelector implements PropertyChangeListener, ActionListener {

	private static final long serialVersionUID = -337689867042266871L;
	// GUI member declarations
    JComboBox<GlobalProgrammerManager>  progBox;
    JComboBox<ProgrammingMode>          modeBox;
    ArrayList<Integer> modes = new ArrayList<Integer>();

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        return ((GlobalProgrammerManager)progBox.getSelectedItem()).getGlobalProgrammer();
    }

    /**
     * Are any of the modes selected?
     * @return true
     */
    public boolean isSelected() {
        return true;
    }

    public ProgServiceModeComboBox() {
        this(BoxLayout.X_AXIS);
    }
    
    protected List<GlobalProgrammerManager> getMgrList() {
        return InstanceManager.getList(jmri.GlobalProgrammerManager.class);
    }

    
    public ProgServiceModeComboBox(int direction) {
        modeBox = new JComboBox<ProgrammingMode>();
        modeBox.addActionListener(this);
        
        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // create the programmer display combo box
        progBox = new JComboBox<GlobalProgrammerManager>();
        java.util.Vector<GlobalProgrammerManager> v = new java.util.Vector<GlobalProgrammerManager>();
        for (GlobalProgrammerManager pm : getMgrList()) {
            v.add(pm);
            // listen for changes
            pm.getGlobalProgrammer().addPropertyChangeListener(this);
        }
        add(progBox = new JComboBox<GlobalProgrammerManager>(v));
        // if only one, don't show
        if (progBox.getItemCount()<2) {
            progBox.setVisible(false);
        } else {
            progBox.setSelectedItem(InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)); // set default
            progBox.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // new programmer selection
                    programmerSelected();
                }
            });
        }
        
        // install items in GUI
        add(new JLabel(Bundle.getMessage("ProgrammingMode")));

        add(modeBox);
        
        // and execute the setup for 1st time
        programmerSelected();

    }

    /**
     * reload the interface with the new programmers
     */
    void programmerSelected() {
        DefaultComboBoxModel<ProgrammingMode> model = new DefaultComboBoxModel<ProgrammingMode>();
        for (ProgrammingMode mode : getProgrammer().getSupportedModes()) {
            model.addElement(mode);
        }
        modeBox.setModel(model);
    }
    
    /**
     * Listen to box for mode changes
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // convey change to programmer
        log.debug("Selected mode: {}", modeBox.getSelectedItem());
        getProgrammer().setMode((ProgrammingMode)modeBox.getSelectedItem());
    }
    
    /**
     * Listen to programmer for mode changes
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("Mode".equals(e.getPropertyName()) && getProgrammer().equals(e.getSource())) {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // if we're not holding a current mode, don't update
                modeBox.setSelectedItem((ProgrammingMode)e.getNewValue());
            }
        }
    }
    
    // no longer needed, disconnect if still connected
    public void dispose() {
    }
    static Logger log = LoggerFactory.getLogger(ProgServiceModeComboBox.class.getName());
}
