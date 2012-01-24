// RosterGroupComboBox.java
package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

/**
 * A JComboBox of Roster Groups.
 *
 * @author  Randall Wood Copyright (C) 2011
 * @version	$Revision: $
 * @see         jmri.jmrit.roster.Roster
 */
public class RosterGroupComboBox extends JComboBox implements RosterGroupSelector {

    private Roster _roster;

    /**
     * Create a RosterGroupComboBox with an arbitrary Roster instead of the
     * default Roster instance.
     *
     * @param roster
     */
    // needed for unit tests
    public RosterGroupComboBox(Roster roster) {
        this(roster, roster.getDefaultRosterGroup());
    }

    /**
     * Create a RosterGroupComboBox with an arbitrary selection.
     *
     * @param selection
     */
    public RosterGroupComboBox(String selection) {
        this(Roster.instance(), selection);
    }

    /**
     * Create a RosterGroupComboBox with arbitrary selection and Roster.
     * 
     * @param roster
     * @param selection 
     */
    public RosterGroupComboBox(Roster roster, String selection) {
        super();
        _roster = roster;
        update(selection);
        Roster.instance().addPropertyChangeListener(new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals("RosterGroupAdded")) {
                    update();
                } else if (pce.getPropertyName().equals("RosterGroupRemoved")
                    || pce.getPropertyName().equals("RosterGroupRenamed")) {
                    if (getSelectedItem().equals(pce.getOldValue())) {
                        update((String)pce.getNewValue());
                    } else {
                        update();
                    }
                }
            }
        });
    }

    /**
     * Create a RosterGroupComboBox with the default Roster instance and the
     * default roster group.
     */
    public RosterGroupComboBox() {
        this(Roster.instance(), Roster.instance().getDefaultRosterGroup());
    }

    /**
     * Update the combo box and reselect the current selection.
     */
    public final void update() {
        update((String) this.getSelectedItem());
    }

    /**
     * Update the combo box and select given String.
     *
     * @param selection
     */
    public final void update(String selection) {
        if (selection == null) {
            selection = Roster.ALLENTRIES;
        }
        removeAllItems();
        ArrayList<String> l = _roster.getRosterGroupList();
        Collections.sort(l);
        for (String g : l) {
            addItem(g);
        }
        insertItemAt(Roster.ALLENTRIES, 0);
        setSelectedItem(selection);
        if (this.getItemCount() == 1) {
            this.setSelectedIndex(0);
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    }

    public String getSelectedRosterGroup() {
        if (getSelectedItem().equals(Roster.ALLENTRIES)) {
            return null;
        } else {
            return getSelectedItem().toString();
        }
    }
}
