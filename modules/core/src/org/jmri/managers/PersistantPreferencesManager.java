package org.jmri.managers;

import apps.AppConfigBase;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author rhwood
 */
@ServiceProvider(service = PersistantPreferencesManager.class)
public class PersistantPreferencesManager extends AppConfigBase {

    private static final long serialVersionUID = -9062945353256849209L;

    public PreferencesPanel getPanel(String clazz) {
        return this.preferencesPanels.get(clazz);
    }

    public void setPanel(PreferencesPanel panel) {
        this.getPreferencesPanels().put(panel.getClass().getName(), panel);
        if (panel.isPersistant()) {
            items.add(panel);
        }
    }
    
    public void savePreferences() {
        this.savePreferences(this.isRestartRequired());
    }

    public void savePreferences(boolean isRestartRequired) {
        this.savePressed(isRestartRequired);
    }
    
    public boolean isRestartRequired() {
        return this.preferencesPanels.values().stream().anyMatch((panel) -> (panel.isRestartRequired()));
    }

    public boolean isDirty() {
        return this.preferencesPanels.values().stream().anyMatch((panel) -> (panel.isDirty()));
    }
}
