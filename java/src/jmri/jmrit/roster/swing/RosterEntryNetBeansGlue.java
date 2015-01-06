package jmri.jmrit.roster.swing;

/**
 * Provide an interface for NetBeans UI objects (mostly TopComponents) that wrap
 * panes used by the SymbolicProgrammer.
 *
 * This interface is in the JMRI Library module so that NetBeans UI objects that
 * extend it can be in other modules that depend on the JMRI Library module.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public interface RosterEntryNetBeansGlue {

    /**
     * Mark the handler savable by creating a Savable and attaching it to the
     * instance content.
     */
    public void savable();

}
