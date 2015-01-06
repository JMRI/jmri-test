package org.jmri.roster;

import java.util.List;
import java.util.Locale;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.rostergroup.RosterGroup;

/**
 * This is a special default {@link jmri.jmrit.roster.rostergroup.RosterGroup}
 * that includes all entries in the {@link jmri.jmrit.roster.Roster}.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class AllRosterEntries extends RosterGroup {

    public AllRosterEntries() {
        super(Roster.ALLENTRIES);
    }

    @Override
    public List<RosterEntry> getEntries() {
        return Roster.instance().getEntriesInGroup(Roster.ALLENTRIES);
    }

    @Override
    public String getDisplayName() {
        return Roster.AllEntries(Locale.getDefault());
    }

    @Override
    public void setName(String name) {
        // Do nothing.
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canDelete() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }
}
