// DeleteRosterItemAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import jmri.beans.Beans;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.FileUtil;

/**
 * Remove a locomotive from the roster.
 *
 * <P>In case of error, this
 * moves the definition file to a backup.  This action posts
 * a dialog box to select the loco to be deleted, and then posts
 * an "are you sure" dialog box before acting.
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
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 */
public class DeleteRosterItemAction extends JmriAbstractAction {

    public DeleteRosterItemAction(String s, WindowInterface wi) {
    	super(s, wi);
    }

 	public DeleteRosterItemAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public DeleteRosterItemAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();
        String rosterGroup = Roster.instance().getDefaultRosterGroup();
        RosterEntry[] entries;
        // rosterGroup may legitimately be null
        // but getProperty returns null if the property cannot be found, so
        // we test that the property exists before attempting to get its value
        if (Beans.hasProperty(wi, "selectedRosterGroup")) {
            rosterGroup = (String) Beans.getProperty(wi, "selectedRosterGroup");
        }
        if (Beans.hasProperty(wi, "selectedRosterEntries")) {
            entries = (RosterEntry[]) Beans.getProperty(wi, "selectedRosterEntries");
        } else {
            entries = selectRosterEntry(rosterGroup);
        }
        if (entries == null) {
            return;
        }
        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // find the file for the selected entry
        for (RosterEntry re : entries) {
            String filename = roster.fileFromTitle(re.titleString());
            String fullFilename = LocoFile.getFileLocation() + filename;
            if (log.isDebugEnabled()) {
                log.debug("resolves to \"" + filename + "\", \"" + fullFilename + "\"");
            }

            // prompt for one last chance
            if (rosterGroup == null) {
                if (!userOK(re.titleString(), filename, fullFilename)) {
                    return;
                }
                // delete it from roster
                roster.removeEntry(re);
            } else {
                String group = Roster.getRosterGroupProperty(rosterGroup);
                re.deleteAttribute(group);
                re.updateFile();
            }
            Roster.writeRosterFile();

            // backup the file & delete it
            if (rosterGroup == null) {
                try {
                    // ensure preferences will be found
                    FileUtil.createDirectory(LocoFile.getFileLocation());

                    // do backup
                    LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
                    df.makeBackupFile(LocoFile.getFileLocation() + filename);

                    // locate the file and delete
                    File f = new File(fullFilename);
                    if (!f.delete()) { // delete file and check success
                        log.error("failed to delete file");
                    }

                } catch (Exception ex) {
                    log.error("error during locomotive file output: " + ex);
                }
            }
        }

    }

    protected RosterEntry[] selectRosterEntry(String rosterGroup){
        // create a dialog to select the roster entry
        JComboBox selections = new RosterEntryComboBox(rosterGroup);
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Select one roster entry", "Delete roster entry",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":"
                  +selections.getSelectedItem());
        if (retval != 1) return null;
        RosterEntry[] entries = new RosterEntry[1];
        entries[0] = (RosterEntry) selections.getSelectedItem();
        return entries;
    }

    /**
     * Can provide some mechanism to prompt for user for one
     * last chance to change his/her mind
     * @return true if user says to continue
     */
    boolean userOK(String entry, String filename, String fullFileName) {
        return ( JOptionPane.YES_OPTION ==
                 JOptionPane.showConfirmDialog(_who,
                        java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("DeletePrompt"),
                                        entry,fullFileName),
                        java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("DeleteTitle"),
                                        entry),
                        JOptionPane.YES_NO_OPTION));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeleteRosterItemAction.class.getName());

    /**
     * Main entry point to run as standalone tool. This doesn't work
     * so well yet:  It should take an optional command line argument,
     * and should terminate when done, or at least let you delete
     * another file.
     */
    public static void main(String s[]) {

        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        // log.info("DeleteRosterItemAction starts");

        // fire the action
        Action a = new DeleteRosterItemAction("Delete Roster Item", new javax.swing.JFrame());
        a.actionPerformed(new ActionEvent(a, 0, "dummy"));
    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
