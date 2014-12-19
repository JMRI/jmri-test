package org.jmri.roster.ui;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterGroupNode extends BeanNode {

    public RosterGroupNode(RosterGroup bean, Children children) throws IntrospectionException {
        super(bean, children);
        this.setDisplayName(bean.getDisplayName());
        this.setIconBaseWithExtension("org/jmri/roster/ui/RosterGroup.png");
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>(Utilities.actionsForPath("Actions/Roster/Group"));
        return actions.toArray(new Action[actions.size()]);
    }

}
