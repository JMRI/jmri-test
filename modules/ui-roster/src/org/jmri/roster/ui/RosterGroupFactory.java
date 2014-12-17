package org.jmri.roster.ui;

import java.util.List;
import java.util.Locale;
import jmri.jmrit.roster.Roster;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
class RosterGroupFactory extends ChildFactory<String> {

    private final String group;

    public RosterGroupFactory(String group) {
        this.group = group;
    }

    @Override
    protected boolean createKeys(List<String> list) {
        list.add(Roster.AllEntries(Locale.getDefault()));
        list.addAll(Roster.instance().getRosterGroupList());
        return true;
    }

    @Override
    protected Node createNodeForKey(String key) {
        AbstractNode node = null;
        Children children = Children.create(new RosterEntryFactory(key), true);
        node = new AbstractNode(children);
        node.setDisplayName(key);
        return node;
    }
}
