// SelectRosterGroupAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Selects a Roster group to work with
 *
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
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version	$Revision: 1.2 $
 */
public class SelectRosterGroupAction extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public SelectRosterGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();

        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // create a dialog to select the roster entry
        JComboBox selections = roster.rosterGroupBox();
        selections.addItem("Global");
        String currentgroup = roster.getRosterGroup();
        if (currentgroup==null) currentgroup = "Global";
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Select one roster group to work with\nCurrent Active Group is " + currentgroup, "Select roster group",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":"
                  +selections.getSelectedItem());
        if (retval != 1) return;
        String entry = (String) selections.getSelectedItem();

        roster.setRosterGroup(entry);
    }


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SelectRosterGroupAction.class.getName());

}
