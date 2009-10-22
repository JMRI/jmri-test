// AddRosterEntryToEcos.java

package jmri.jmrix.ecos.utilities;

import jmri.jmrit.XmlFile;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Add a Roster Entry to the Ecos
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
public class AddRosterEntryToEcos extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     */
    public AddRosterEntryToEcos(String s) {
        super(s);
    }


    JComboBox rosterEntry = new JComboBox();
    JComboBox selections;
    Roster roster;
    
    public void actionPerformed(ActionEvent event) {
        
        roster = Roster.instance();

        rosterEntryUpdate();

        int retval = JOptionPane.showOptionDialog(null,
                                                  "Select the roster entry to add to the Ecos\nThe Drop down list only shows locos that have not already been added. " , "Add to Ecos",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", rosterEntry}, null );
        log.debug("Dialog value "+retval+" selected, "
                  + rosterEntry.getSelectedIndex()+":" + rosterEntry.getSelectedItem());
        if (retval != 1) {
            return;
        }
        
        String selEntry = (String) rosterEntry.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        //System.out.println("Add " + re.getId() + " to Ecos");
        RosterToEcos rosterToEcos = new RosterToEcos();
        rosterToEcos.createEcosLoco(re);
        actionPerformed(event);
    }

    void rosterEntryUpdate(){
        if (rosterEntry!=null)
            rosterEntry.removeAllItems();
        for(int i=0; i<roster.numEntries(); i++){
            RosterEntry r = roster.getEntry(i);
            if(r.getAttribute("EcosObject")==null)
                rosterEntry.addItem(r.titleString());
        }
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddRosterEntryToEcos.class.getName());
}
