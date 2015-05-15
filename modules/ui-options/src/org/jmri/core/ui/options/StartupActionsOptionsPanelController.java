package org.jmri.core.ui.options;

import apps.PerformActionPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "StartupOptions",
        displayName = "#StartupOption_DisplayName_StartupActions",
        keywords = "#StartupOption_Keywords_StartupActions",
        keywordsCategory = "StartupOptions/Actions"
)
@NbBundle.Messages({
    "StartupOption_DisplayName_StartupActions=Actions",
    "StartupOption_Keywords_StartupActions=Actions, Startup"
})
public final class StartupActionsOptionsPanelController extends PreferencesPanelController {

    public StartupActionsOptionsPanelController() {
        super(new PerformActionPanel());
    }
}
