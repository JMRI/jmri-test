package org.jmri.roster.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Icon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.swing.RosterEntryNetBeansGlue;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntrySavable extends AbstractSavable implements Icon {

    private final RosterEntryNetBeansGlue object;
    private static final Icon icon = ImageUtilities.loadImageIcon("org/jmri/roster/ui/RosterEntry.png", true);

    public RosterEntrySavable(RosterEntryNetBeansGlue object) {
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
