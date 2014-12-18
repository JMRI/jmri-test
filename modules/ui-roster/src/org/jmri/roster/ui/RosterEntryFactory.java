package org.jmri.roster.ui;

import java.beans.IntrospectionException;
import java.util.List;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
class RosterEntryFactory extends ChildFactory<RosterEntry> {

    private final RosterGroup rosterGroup;
    private static final Logger log = LoggerFactory.getLogger(RosterEntryFactory.class);

    public RosterEntryFactory(RosterGroup rosterGroup) {
        this.rosterGroup = rosterGroup;
    }

    @Override
    protected boolean createKeys(List<RosterEntry> list) {
        list.addAll(this.rosterGroup.getEntries());
        return true;
    }

    @Override
    protected Node createNodeForKey(RosterEntry key) {
        RosterEntryNode node = null;
        try {
            node = new RosterEntryNode(key);
        } catch (IntrospectionException ex) {
            log.error("Unable to create node for RosterEntry \"{}\"", key.getId(), ex);
        }
        return node;
    }
}
