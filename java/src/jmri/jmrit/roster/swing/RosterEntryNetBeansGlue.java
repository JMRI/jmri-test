package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.RosterEntry;
import org.openide.util.lookup.InstanceContent;

/**
 * Provide an interface for objects (mostly TopComponents) accepted by
 * RosterEntrySavable to provide methods called by RosterEntrySavable without
 * using introspection to test for the presence of the needed methods.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public interface RosterEntryNetBeansGlue {

    public RosterEntry getRosterEntry();

    public InstanceContent getInstanceContent();

    /**
     * Mark the handler savable by creating a Savable and attaching it to the
     * instance content.
     */
    public void savable();

    /**
     * Take any required actions to save the handled
     * {@link jmri.jmrit.roster.RosterEntry}.
     *
     * @return true if RosterEntry needs to be updated
     */
    public boolean save();
}
