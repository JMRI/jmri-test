package org.jmri.core.ui.toolbar;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.awt.Actions.ButtonActionConnector;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author rhwood
 */
@ServiceProvider(service = ButtonActionConnector.class, position = 100)
public class ToolbarButtonConnector implements ButtonActionConnector {

    @Override
    public boolean connect(AbstractButton button, Action action) {
        String text = (String) action.getValue(Action.NAME);
        if (text != null) {
            button.setAction(action);
            button.setText(Actions.cutAmpersand(text));
            String desc = (String) action.getValue(Action.SHORT_DESCRIPTION);
            if (desc != null) {
                button.setToolTipText(desc);
            } else {
                button.setToolTipText((String) action.getValue(Action.NAME));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean connect(JMenuItem jmi, Action action, boolean bln) {
        return false; // use default implementation
    }

}
