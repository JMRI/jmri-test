package org.jmri.roster.ui;

import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import jmri.jmrit.roster.RosterEntry;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntryNode extends BeanNode<RosterEntry> implements Serializable {

    public RosterEntryNode(RosterEntry bean) throws IntrospectionException {
        super(bean, Children.LEAF, Lookups.singleton(bean));
        this.setDisplayName(bean.getDisplayName());
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>(Utilities.actionsForPath("Actions/Roster/Entry")); // NOI18N
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Action getPreferredAction() {
        return new AbstractAction("Edit") { // TODO: I18N
            @Override
            public void actionPerformed(ActionEvent e) {
                RosterEntry rosterEntry = getLookup().lookup(RosterEntry.class);
                TopComponent tc = findTopComponent(rosterEntry);
                if (tc == null) {
                    tc = MultiViews.createMultiView("application/x-jmri-rosterentry-node", RosterEntryNode.this); // NOI18N
                    tc.open();
                }
                tc.requestActive();
            }
        };
    }

    private TopComponent findTopComponent(RosterEntry rosterEntry) {
        Set<TopComponent> openTopComponents = WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : openTopComponents) {
            try {
                if (!tc.getName().equals("RosterExplorerTopComponent")) { // NOI18N
                    if (tc.getLookup().lookup(RosterEntry.class).equals(rosterEntry)) {
                        return tc;
                    }
                }
            } catch (NullPointerException ex) {
                // do nothing - this single try/catch avoids saving every method
                // result and checking it for null within this loop
            }
        }
        return null;
    }
}
