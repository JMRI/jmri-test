package org.jmri.core.ui.options;

import apps.PerformScriptPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "StartupOptions",
        displayName = "#StartupOption_DisplayName_StartupScripts",
        keywords = "#StartupOption_Keywords_StartupScripts",
        keywordsCategory = "StartupOptions/Scripts"
)
@NbBundle.Messages({
    "StartupOption_DisplayName_StartupScripts=Scripts",
    "StartupOption_Keywords_StartupScripts=Scripts, Startup"
})
public final class StartupScriptsOptionsPanelController extends PreferencesPanelController {

    public StartupScriptsOptionsPanelController() {
        super(new PerformScriptPanel());
    }
}
