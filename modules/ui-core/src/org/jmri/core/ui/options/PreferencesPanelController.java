package org.jmri.core.ui.options;

import apps.gui3.TabbedPreferences;
import java.beans.PropertyChangeEvent;
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
 * Abstract OptionsPanelController that maps the standard OptionsPanelController
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

    public PreferencesPanel getPreferencesPanel() {
        return this.preferencesPanel;
    }

    @Override
    public void update() {
        TabbedPreferences tabbedPreferences = InstanceManager.getDefault(TabbedPreferences.class);
        if (!tabbedPreferences.isInitialised()) {
            tabbedPreferences.addPropertyChangeListener(TabbedPreferences.INITIALIZATION, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ((int) evt.getNewValue() == TabbedPreferences.INITIALISED) {
                        tabbedPreferences.removePropertyChangeListener(this);
                        update();
                    }
                }
            });
            new Thread(tabbedPreferences::init).start();
        } else {
            tabbedPreferences.addPreferencesPanel(preferencesPanel);
        }
    }

    @Override
    public void applyChanges() {
        this.applyChanges(this.preferencesPanel.isPersistant());
    }

    /**
     * Called by {@link #applyChanges() } with a boolean value indicating the
     * changes should be saved as if the preferences are handled by the
     * persistent configuration manager or not.
     *
     * The default applyChanges passes the result of
     * <code>this.preferencesPanel.isPersistant()</code> for the isPersistent
     * parameter.
     *
     * @param isPersistent true if persistent configuration manager should be
     * invoked
     */
    protected void applyChanges(boolean isPersistent) {
        SwingUtilities.invokeLater(() -> {
            this.preferencesPanel.savePreferences();
            if (isPersistent && this.preferencesPanel.isDirty()) {
                // this may result in multiple writes to the profile configuration
                // if a persistant PreferencesPanel sets itself to dirty after
                // another PreferencesPanel has already written the configuration
                InstanceManager.getDefault(TabbedPreferences.class).savePressed(this.preferencesPanel.isRestartRequired());
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
        // setting is outside the valid range. If your PreferencesPanel uses
        // persistant perferences, ensure you return true only is super.isValid
        // is true when overriding this method
        return (InstanceManager.getDefault(TabbedPreferences.class).init() == TabbedPreferences.INITIALISED);
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
