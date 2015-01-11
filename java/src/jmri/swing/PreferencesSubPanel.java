package jmri.swing;

/**
 * Additional methods that a PreferencesPanel that implements a GUI for another,
 * different PreferencesPanel (called the parent) needs to implement for the
 * {@link apps.gui3.TabbedPreferences} Preferences Window to correctly save
 * preferential changes. These methods make it possible for TabbedPreferences to
 * ensure the parent PreferencesPanel is created and correctly associated with
 * this panel.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public interface PreferencesSubPanel extends PreferencesPanel {

    public abstract String getParentClassName();

    public abstract void setParent(PreferencesPanel parent);

    public abstract PreferencesPanel getParent();
}
