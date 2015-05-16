package org.jmri.roster.ui.options;

import jmri.jmrit.roster.RosterConfigPane;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "Roster",
        displayName = "#AdvancedOption_DisplayName_General",
        keywords = "#AdvancedOption_Keywords_General",
        keywordsCategory = "Roster/Roster",
        position = 20
)
@NbBundle.Messages({
    "AdvancedOption_DisplayName_General=General",
    "AdvancedOption_Keywords_General=Roster, Owner"
})
public final class RosterOptionsPanelController extends PreferencesPanelController {

    public RosterOptionsPanelController() {
        super(new RosterConfigPane());
    }
}
