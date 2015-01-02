/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jmri.operations.ui;

import jmri.jmrit.operations.setup.PrintOptionPanel;
import org.jmri.core.ui.options.PreferencesPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "Operations",
        displayName = "#AdvancedOption_DisplayName_ManifestPrint",
        keywords = "#AdvancedOption_Keywords_ManifestPrint",
        keywordsCategory = "Operations/ManifestPrint",
        position = 120
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_ManifestPrint=Manifest",
    "AdvancedOption_Keywords_ManifestPrint=manifest, print, format, font"
})
public final class ManifestPrintOptionsPanelController extends PreferencesPanelController {

    public ManifestPrintOptionsPanelController() {
        super(new PrintOptionPanel());
    }
}
