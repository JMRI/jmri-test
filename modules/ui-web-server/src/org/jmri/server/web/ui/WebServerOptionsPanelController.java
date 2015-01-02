package org.jmri.server.web.ui;

import jmri.web.server.WebServerPreferencesPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "NetworkServices",
        displayName = "#AdvancedOption_DisplayName_WebServer",
        keywords = "#AdvancedOption_Keywords_WebServer",
        keywordsCategory = "NetworkServices/WebServer"
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_WebServer=Web",
    "AdvancedOption_Keywords_WebServer=Web, Server"
})
public final class WebServerOptionsPanelController extends PreferencesPanelController {

    public WebServerOptionsPanelController() {
        super(new WebServerPreferencesPanel());
    }
}
