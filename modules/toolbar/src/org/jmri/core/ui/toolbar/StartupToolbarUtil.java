package org.jmri.core.ui.toolbar;

import javax.swing.Action;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;

/**
 *
 * @author rhwood
 */
public final class StartupToolbarUtil {

    static public void addAction(String actionPath) {
        Toolbar toolbar = ToolbarPool.getDefault().findToolbar("AddEditDelete");
        Action action = null; // TODO: get from startup preferences XML.
        toolbar.add(action);
    }
}
