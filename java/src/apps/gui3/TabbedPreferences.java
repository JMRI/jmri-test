// TabbedPreferences.java
package apps.gui3;

import apps.AppConfigBase;
import java.util.ArrayList;
import java.util.List;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to preferences via a tabbed pane.
 *
 * Preferences panels listed in the PreferencesPanel property of the
 * apps.AppsStructureBundle ResourceBundle will be automatically loaded if they
 * implement the {@link jmri.swing.PreferencesPanel} interface.
 *
 * Other Preferences Panels will need to be manually added to this file in a
 * manner similar to the WiThrottlePrefsPanel.
 *
 * @author Bob Jacobsen Copyright 2010
 * @author Randall Wood 2012
 * @version $Revision$
 */
public class TabbedPreferences extends AppConfigBase {

    @Override
    public boolean isMultipleInstances() {
        return false;
    } // only one of these!

    private static final long serialVersionUID = -6266891995866315885L;

    public static final int UNINITIALISED = 0x00;
    public static final int INITIALISING = 0x01;
    public static final int INITIALISED = 0x02;
    public static final String INITIALIZATION = "PROP_INITIALIZATION";

    public TabbedPreferences() {
    }

    public synchronized int init() {
        return INITIALISED;
    }

    public int getInitialisationState() {
        return INITIALISED;
    }

    public boolean isInitialised() {
        return (this.getInitialisationState() == INITIALISED);
    }

    // package only - for TabbedPreferencesFrame
    boolean isDirty() {
        for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            if (panel.isDirty()) {
                return true;
            }
        }
        return false;
    }

    // package only - for TabbedPreferencesFrame
    boolean invokeSaveOptions() {
        boolean restartRequired = false;
        for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            panel.savePreferences();
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) restart is {}required.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isRestartRequired()) ? "" : "not ");
            }
            if (!restartRequired) {
                restartRequired = panel.isRestartRequired();
            }
        }
        return restartRequired;
    }

    void selection(String View) {
    }

    public void addPreferencesPanel(PreferencesPanel panel) {
        this.getPreferencesPanels().put(panel.getClass().getName(), panel);
    }

    /* Method allows for the preference to goto a specific list item */
    public void gotoPreferenceItem(String selection, String subCategory) {
        // do nothing
    }

    /*
     * Returns a List of existing Preference Categories.
     */
    public List<String> getPreferenceMenuList() {
        return new ArrayList<>();
    }

    /*
     * Returns a list of Sub Category Items for a give category
     */
    public List<String> getPreferenceSubCategory(String category) {
        return new ArrayList<>();
    }

    int getCategoryIndexFromString(String category) {
        return -1;
    }

    public void disablePreferenceItem(String selection, String subCategory) {
        // do nothing
    }

    protected ArrayList<String> getChoices() {
        return new ArrayList<>();
    }

    void updateJList() {
    }

    static Logger log = LoggerFactory.getLogger(TabbedPreferences.class.getName());

}
