package org.jmri.core.ui.options;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.swing.PreferencesPanel;
import org.jmri.managers.PersistentPreferencesManager;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Abstract OptionsPanelController that maps the standard OptionsPanelController
 * to a JMRI PreferencesPanel.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public abstract class PreferencesPanelController extends OptionsPanelController {

    @Messages({
        "# {0} - Application name",
        "restartConfirmation.message=Click here to restart {0} and apply your preferences.",
        "# {0} - Application name",
        "restartConfirmation.title=Restart {0}."
    })

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PreferencesPanel preferencesPanel;

    public PreferencesPanelController(PreferencesPanel preferencesPanel) {
        if (!preferencesPanel.isPersistant()) {
            this.preferencesPanel = preferencesPanel;
        } else {
            PersistentPreferencesManager manager = Lookup.getDefault().lookup(PersistentPreferencesManager.class);
            PreferencesPanel aPanel = manager.getPanel(preferencesPanel.getClass().getName());
            if (aPanel != null) {
                this.preferencesPanel = aPanel;
            } else {
                manager.setPanel(preferencesPanel);
                this.preferencesPanel = preferencesPanel;
            }
        }
    }

    public PreferencesPanel getPreferencesPanel() {
        return this.preferencesPanel;
    }

    @Override
    public void update() {
        // do nothing
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
            boolean isRestartRequired = false;
            if (isPersistent && this.preferencesPanel.isDirty()) {
                // this may result in multiple writes to the profile configuration
                // if a persistant PreferencesPanel sets itself to dirty after
                // another PreferencesPanel has already written the configuration
                PersistentPreferencesManager manager = Lookup.getDefault().lookup(PersistentPreferencesManager.class);
                for (PreferencesPanel panel : manager.getPreferencesPanels().values()) {
                    if (panel.isRestartRequired()) {
                        isRestartRequired = true;
                        break;
                    }
                }
                manager.savePreferences(isRestartRequired);
            }
            if (isRestartRequired || this.preferencesPanel.isRestartRequired()) {
                this.promptToRestart();
            }
        });
    }

    /**
     * If any action needs to be taken when the Options dialog is dismissed
     * without pressing OK, this method should be overridden.
     */
    @Override
    public void cancel() {
        // do nothing by default
    }

    /**
     * Return true if preferences can be saved or applied.
     *
     * @return true if preferences can be saved or applied.
     */
    @Override
    public boolean isValid() {
        return this.preferencesPanel.isPreferencesValid();
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

    public void promptToRestart() {
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                        Bundle.restartConfirmation_message(Application.getApplicationName()),
                        Bundle.restartConfirmation_title(Application.getApplicationName()),
                        NotifyDescriptor.YES_NO_OPTION)))) {
            LifecycleManager.getDefault().markForRestart();
            LifecycleManager.getDefault().exit();
        } else {
            this.notifyToRestart();
        }
    }

    public void notifyToRestart() {
        NotificationDisplayer.getDefault().notify(Bundle.restartConfirmation_title(Application.getApplicationName()),
                new ImageIcon("org/jmri/core/ui/options/Gnome-view-refresh.png"),
                Bundle.restartConfirmation_title(Application.getApplicationName()),
                (ActionEvent e) -> {
                    LifecycleManager.getDefault().markForRestart();
                    LifecycleManager.getDefault().exit();
                },
                NotificationDisplayer.Priority.HIGH,
                NotificationDisplayer.Category.INFO);
    }
}
