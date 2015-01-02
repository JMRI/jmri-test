package org.jmri.options.profiles;

import jmri.profile.ProfilePreferencesPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_Profiles",
        iconBase = "org/jmri/options/profiles/Gnome-applications-office.png",
        keywords = "#OptionsCategory_Keywords_Profiles",
        keywordsCategory = "Profiles",
        position = 150
)
@org.openide.util.NbBundle.Messages({
    "OptionsCategory_Name_Profiles=Config Profiles",
    "OptionsCategory_Keywords_Profiles=profile"
})
public final class ProfilesOptionsPanelController extends PreferencesPanelController {

    public ProfilesOptionsPanelController() {
        super(new ProfilePreferencesPanel());
    }

}
