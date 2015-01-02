package org.jmri.operations.ui;

import jmri.jmrit.operations.setup.OptionPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "Operations",
        displayName = "#AdvancedOption_DisplayName_Option",
        keywords = "#AdvancedOption_Keywords_Option",
        keywordsCategory = "Operations/Option",
        position = 110
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_Option=Options",
    "AdvancedOption_Keywords_Option=option, options, switcher, staging, routing, car routing, logging"
})
public final class OptionOptionsPanelController extends PreferencesPanelController {

    public OptionOptionsPanelController() {
        super(new OptionPanel());
    }
}
