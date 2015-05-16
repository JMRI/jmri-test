package org.jmri.roster.ui.options;

import jmri.jmrit.symbolicprog.ProgrammerConfigPane;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

@OptionsPanelController.SubRegistration(
        location = "Roster",
        displayName = "#AdvancedOption_DisplayName_Programmer",
        keywords = "#AdvancedOption_Keywords_Programmer",
        keywordsCategory = "Roster/Programmer",
        position = 10
)
@NbBundle.Messages({
    "AdvancedOption_DisplayName_Programmer=Programmer",
    "AdvancedOption_Keywords_Programmer=Programmer, Format"
})
public final class ProgrammerOptionsPanelController extends PreferencesPanelController {

    public ProgrammerOptionsPanelController() {
        super(new ProgrammerConfigPane());
    }
}
