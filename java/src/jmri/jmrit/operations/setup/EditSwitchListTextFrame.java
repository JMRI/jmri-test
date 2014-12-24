// ManifestTextFrame.java
package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of switch list text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 21846 $
 */
public class EditSwitchListTextFrame extends OperationsFrame {

    public EditSwitchListTextFrame() {
        super(Bundle.getMessage("TitleSwitchListText"), new EditSwitchListTextPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N
    }

    private static final Logger log = LoggerFactory.getLogger(EditSwitchListTextFrame.class);
}
