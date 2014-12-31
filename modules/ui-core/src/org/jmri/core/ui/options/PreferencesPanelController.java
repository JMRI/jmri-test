package org.jmri.core.ui.options;

import apps.gui3.TabbedPreferences;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Generic OptionsPanelController that maps the standard OptionsPanelController
 * to a JMRI PreferencesPanel.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public abstract class PreferencesPanelController extends OptionsPanelController {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PreferencesPanel preferencesPanel;

    public PreferencesPanelController(PreferencesPanel preferencesPanel) {
        this.preferencesPanel = preferencesPanel;
    }

    @Override
    public void update() {
        // ensure that TabbedPreferences are initialized
        new Thread(InstanceManager.getDefault(TabbedPreferences.class)::init).start();
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            this.preferencesPanel.savePreferences();
            if (this.preferencesPanel.isPersistant()
                    && this.preferencesPanel.isDirty()) {
                // this may result in multiple writes to the profile configuration
                // if a persistant PreferencesPanel sets itself to dirty after
                // another PreferencesPanel has already written the configuration
                InstanceManager.getDefault(TabbedPreferences.class).savePressed(true);
            }
        });
    }

    @Override
    public void cancel() {
        // if any action needs to be taken when the Options dialog is dismissed
        // without pressing OK, this method should be overridden
    }

    @Override
    public boolean isValid() {
        // override this method to indicate that a preferences setting is not
        // valid, for example, there are no connections defined, or a numeric
        // setting is outside the valid range
        // return (InstanceManager.getDefault(TabbedPreferences.class).init() == TabbedPreferences.INITIALIZED);
        return true;
    }

    @Override
    public boolean isChanged() {
        return this.preferencesPanel.isDirty();
    }

    @Override
    public JComponent getComponent(Lookup lkp) {
        return this.preferencesPanel.getPreferencesComponent();
    }

    @Override
    public HelpCtx getHelpCtx() {
        // there is no analogous mechanism in a PreferencesPanel
        // subclasses should implement this if they have specific help
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        this.pcs.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        this.pcs.removePropertyChangeListener(pl);
    }

    public void changed() {
        if (this.isChanged()) {
            this.pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        if (this.isValid()) {
            this.pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }
    }

}
