@OptionsPanelController.ContainerRegistration(
        id = "Operations",
        categoryName = "#OperationsCategory_Name_Options",
        iconBase = "org/jmri/operations/ui/Gnome-text-x-generic.png",
        keywords = "#OperationsCategory_Keywords_Options",
        keywordsCategory = "Operations",
        position = 300
)
@NbBundle.Messages(value = {
    "OperationsCategory_Name_Options=Operations",
    "OperationsCategory_Keywords_Options=operations"
})
package org.jmri.operations.ui;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
