/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@OptionsPanelController.ContainerRegistration(
        id = "Roster",
        categoryName = "#OptionsCategory_Name_Roster",
        iconBase = "org/jmri/roster/ui/RosterGroup.png",
        keywords = "#OptionsCategory_Keywords_Roster", 
        keywordsCategory = "Roster")
@NbBundle.Messages({
    "OptionsCategory_Name_Roster=Roster",
    "OptionsCategory_Keywords_Roster=Roster"
})
package org.jmri.roster.ui.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
