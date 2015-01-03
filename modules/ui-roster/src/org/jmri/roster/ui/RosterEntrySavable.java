package org.jmri.roster.ui;

import java.io.IOException;
import jmri.jmrit.roster.Roster;
import org.netbeans.spi.actions.AbstractSavable;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntrySavable extends AbstractSavable {

    private final RosterEntryHandler object;

    public RosterEntrySavable(RosterEntryHandler object) {
        this.object = object;
        this.register();
    }

    @Override
    protected String findDisplayName() {
        return this.object.getRosterEntry().getDisplayName();
    }

    @Override
    protected void handleSave() throws IOException {
        this.object.save();
        this.object.getRosterEntry().updateFile();
        Roster.writeRosterFile();
        this.object.getInstanceContent().remove(this);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RosterEntrySavable) {
            if (this.object.getRosterEntry() != null) {
                return this.object.getRosterEntry().equals(((RosterEntrySavable) object).object.getRosterEntry());
            } else {
                return this.object.equals(((RosterEntrySavable) object).object);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.object.hashCode();
    }
}
