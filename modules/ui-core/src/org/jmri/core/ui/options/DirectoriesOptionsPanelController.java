package org.jmri.core.ui.options;

import apps.FileLocationPane;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "Advanced",
        displayName = "#AdvancedOption_DisplayName_Directories",
        keywords = "#AdvancedOption_Keywords_Directories",
        keywordsCategory = "General/Directories"
)
@NbBundle.Messages({
    "AdvancedOption_DisplayName_Directories=Directories",
    "AdvancedOption_Keywords_Directories=Jython, File, Location, User Files, User, Script"
})
public final class DirectoriesOptionsPanelController extends PreferencesPanelController {

    public DirectoriesOptionsPanelController() {
        super(new FileLocationPane());
    }
}
