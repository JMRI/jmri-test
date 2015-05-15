@OptionsPanelController.ContainerRegistration(id = "StartupOptions",
        categoryName = "#OptionsCategory_Name_StartupOptions",
        iconBase = "org/jmri/core/ui/options/Gnome-system-run.png",
        keywords = "#OptionsCategory_Keywords_StartupOptions",
        keywordsCategory = "StartupOptions")
@NbBundle.Messages(value = {"OptionsCategory_Name_StartupOptions=Startup",
    "OptionsCategory_Keywords_StartupOptions=Startup"})
package org.jmri.core.ui.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
