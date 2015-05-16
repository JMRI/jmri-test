@OptionsPanelController.ContainerRegistration(
        id = "NetworkServices",
        categoryName = "#OptionsCategory_Name_NetworkServices",
        iconBase = "org/jmri/server/ui/options/Gnome-network-server.png",
        keywords = "#OptionsCategory_Keywords_NetworkServices",
        keywordsCategory = "NetworkServices")
@NbBundle.Messages(value = {
    "OptionsCategory_Name_NetworkServices=Servers",
    "OptionsCategory_Keywords_NetworkServices=Network, Servers"})
package org.jmri.server.ui.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
