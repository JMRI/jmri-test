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
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntryNode extends BeanNode<RosterEntry> implements Serializable {

    private static final long serialVersionUID = -6744524117316735354L;

    public RosterEntryNode(RosterEntry bean) throws IntrospectionException {
        this(bean, new InstanceContent());
    }

    public RosterEntryNode(RosterEntry bean, InstanceContent ic) throws IntrospectionException {
        super(bean, Children.LEAF, new ProxyLookup(Lookups.singleton(bean), new AbstractLookup(ic)));
        ic.add(ic);
        ic.add(bean);
        this.setIconBaseWithExtension("org/jmri/roster/ui/RosterEntry.png");
    }

    @Override
    public String getDisplayName() {
        return this.getBean().getDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>(Utilities.actionsForPath("Actions/Roster/Entry")); // NOI18N
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Action getPreferredAction() {
        return new AbstractAction("Edit") { // TODO: I18N

            private static final long serialVersionUID = 4377386270269629176L;

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
