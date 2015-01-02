package org.jmri.operations.ui;

import jmri.jmrit.operations.setup.OperationsSetupPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "Operations",
        displayName = "#AdvancedOption_DisplayName_General",
        keywords = "#AdvancedOption_Keywords_General",
        keywordsCategory = "Operations/General",
        position = 100
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_General=General",
    "AdvancedOption_Keywords_General=Scale"
})
public final class GeneralOptionsPanelController extends PreferencesPanelController {

    public GeneralOptionsPanelController() {
        super(new OperationsSetupPanel());
    }
}
