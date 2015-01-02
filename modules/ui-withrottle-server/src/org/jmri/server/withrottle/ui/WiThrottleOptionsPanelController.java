package org.jmri.server.withrottle.ui;

import jmri.jmrit.withrottle.WiThrottlePrefsPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "NetworkServices",
        displayName = "#AdvancedOption_DisplayName_WiThrottle",
        keywords = "#AdvancedOption_Keywords_WiThrottle",
        keywordsCategory = "NetworkServices/WiThrottle"
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_WiThrottle=WiThrottle",
    "AdvancedOption_Keywords_WiThrottle=WiThrottle"
})
public final class WiThrottleOptionsPanelController extends PreferencesPanelController {

    public WiThrottleOptionsPanelController() {
        super(new WiThrottlePrefsPanel());
    }
}
