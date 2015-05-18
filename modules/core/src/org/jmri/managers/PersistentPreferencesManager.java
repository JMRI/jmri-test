package org.jmri.managers;

import apps.gui3.TabbedPreferences;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * An eventual replacement for TabbedPreferences and AppConfigBase.
 *
 * The TabbedPreferences window is too tightly integrated into specific
 * preferences to be easily replaced.
 *
 * @author Randall Wood
 */
@ServiceProvider(service = PersistentPreferencesManager.class)
public class PersistentPreferencesManager {

    private final HashMap<String, PreferencesPanel> preferencesPanels = new HashMap<>();
    private static final long serialVersionUID = -9062945353256849209L;

    public PreferencesPanel getPanel(String clazz) {
        return this.getTabbedPreferences().getPreferencesPanels().get(clazz);
    }

    public void putPanel(PreferencesPanel panel) {
        this.getTabbedPreferences().getPreferencesPanels().put(panel.getClass().getName(), panel);
    }

    public void savePreferences() {
        this.savePreferences(this.isRestartRequired());
    }

    /*
     * This needs to replicate AppConfigBase.savePressed(boolean) to function 
     * independently of TabbedPreferences. Note that for JMRI applications to 
     * function correctly once that has been done, AppConfigBase will need to
     * call Lookup.getDefault().getLookup(PersistentPreferencesManager.class).getPreferencesPanels()
     * to function correctly. This has not been done as of now since the JMRI
     * Core module depends on the JMRI Library module.
     */
    public void savePreferences(boolean isRestartRequired) {
        this.getTabbedPreferences().savePressed(isRestartRequired);
    }

    public boolean isRestartRequired() {
        return this.getTabbedPreferences().getPreferencesPanels().values().stream().anyMatch((panel) -> (panel.isRestartRequired()));
    }

    public boolean isDirty() {
        return this.getTabbedPreferences().getPreferencesPanels().values().stream().anyMatch((panel) -> (panel.isDirty()));
    }

    public boolean isPreferencesValid() {
        return this.getTabbedPreferences().getPreferencesPanels().values().stream().anyMatch((panel) -> (panel.isPreferencesValid()));
    }

    protected HashMap<String, PreferencesPanel> getPreferencesPanels() {
        return this.preferencesPanels;
    }

    /*
     * Remove this method and all references to it to break the dependency on 
     * TabbedPreferences. All TabbedPreferences methods called by this class
     * have one-to-one analogs with identical names already.
     */
    private TabbedPreferences getTabbedPreferences() {
        return InstanceManager.getDefault(TabbedPreferences.class);
    }
}
