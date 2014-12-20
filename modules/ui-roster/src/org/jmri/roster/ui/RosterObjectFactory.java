package org.jmri.roster.ui;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterObject;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
class RosterObjectFactory extends ChildFactory.Detachable<RosterObject> implements LookupListener {

    private final RosterGroup group;
    private Result<RosterObject> result;
    private static final Logger log = LoggerFactory.getLogger(RosterObjectFactory.class);

    public RosterObjectFactory(RosterGroup group) {
        this.group = group;
    }

    @Override
    protected boolean createKeys(List<RosterObject> list) {
        if (group.getName().equals(Roster.ALLENTRIES)) {
            list.addAll(Roster.instance().getRosterGroups().values());
        }
        list.addAll(group.getEntries());
        return true;
    }

    @Override
    protected Node createNodeForKey(RosterObject key) {
        AbstractNode node = null;
        if (key instanceof RosterGroup) {
            try {
                Children children = Children.create(new RosterObjectFactory((RosterGroup) key), true);
                node = new RosterGroupNode((RosterGroup) key, children);
            } catch (IntrospectionException ex) {
                log.error("Unable to create node for RosterGroup {}.", ((RosterGroup) key).getName(), ex);
                return null;
            }
        } else if (key instanceof RosterEntry) {
            try {
                node = new RosterEntryNode((RosterEntry) key);
            } catch (IntrospectionException ex) {
                log.error("Unable to create node for RosterEntry {}.", ((RosterEntry) key).getId(), ex);
                return null;
            }
        }
        return node;
    }

    @Override
    protected void addNotify() {
        Roster.instance().addPropertyChangeListener((PropertyChangeEvent evt) -> {
            refresh(true);
        });
        result = Lookups.forPath("Roster").lookupResult(RosterObject.class);
        result.addLookupListener(this);
    }

    @Override
    protected void removeNotify() {
        result.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent le) {
        refresh(true);
    }
}
