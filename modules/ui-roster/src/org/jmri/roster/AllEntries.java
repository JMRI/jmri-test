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
public class AllEntries extends RosterGroup {

    public AllEntries() {
        super(Roster.AllEntries(Locale.getDefault()));
    }

    @Override
    public List<RosterEntry> getEntries() {
        return Roster.instance().getEntriesInGroup(Roster.ALLENTRIES);
    }

    @Override
    public void setName(String name) {
        // Do nothing.
    }
}
