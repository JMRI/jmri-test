package org.jmri.core.ui.options;

import jmri.jmrix.swing.ConnectionsPreferencesPanel;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_Connections",
        iconBase = "org/jmri/core/ui/options/Gnome-application-x-executable.png",
        keywords = "#OptionsCategory_Keywords_Connections",
        keywordsCategory = "Connections"
)
@org.openide.util.NbBundle.Messages({
    "OptionsCategory_Name_Connections=Connections",
    "OptionsCategory_Keywords_Connections=Connection, System, Loconet"
})
public final class ConnectionsOptionsPanelController extends PreferencesPanelController {

    public ConnectionsOptionsPanelController() {
        super(new ConnectionsPreferencesPanel());
    }

    @Override
    public void applyChanges() {
        this.applyChanges(true);
    }
}
