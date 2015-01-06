package org.jmri.roster.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Icon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntrySavable extends AbstractSavable implements Icon {

    private final RosterEntry rosterEntry;
    private final InstanceContent instanceContent;
    private static final Icon icon = ImageUtilities.loadImageIcon("org/jmri/roster/ui/RosterEntry.png", true);

    public RosterEntrySavable(RosterEntry re, InstanceContent ic) {
        this.rosterEntry = re;
        this.instanceContent = ic;
        this.register();
    }

    @Override
    protected String findDisplayName() {
        return this.rosterEntry.getDisplayName();
    }

    @Override
    protected void handleSave() throws IOException {
        this.rosterEntry.updateFile();
        Roster.writeRosterFile();
        this.instanceContent.remove(this);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RosterEntrySavable) {
            return this.rosterEntry.equals(((RosterEntrySavable) object).rosterEntry);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.rosterEntry.hashCode();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
