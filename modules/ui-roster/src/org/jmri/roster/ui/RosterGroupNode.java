package org.jmri.roster.ui;

import apps.gui3.dp3.PaneProgDp3Action;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import org.openide.actions.NewAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.lookup.Lookups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterGroupNode extends AbstractNode {

    private final RosterGroup group;
    private static final Logger log = LoggerFactory.getLogger(RosterGroupNode.class);

    public RosterGroupNode(RosterGroup group, Children children) throws IntrospectionException {
        super(children, Lookups.singleton(group));
        this.group = group;
        this.setDisplayName(group.getDisplayName());
        this.setIconBaseWithExtension("org/jmri/roster/ui/RosterGroup.png");
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        if (group.canChangeContents()) {
            actions.add(SystemAction.get(NewAction.class));
        }
        actions.addAll(Utilities.actionsForPath("Actions/Roster/Group"));
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public NewType[] getNewTypes() {
        List<NewType> types = new ArrayList<>();
        types.add(new NewType() {

            @Override
            public String getName() {
                return "Roster Entry"; // TODO: I18N
            }

            @Override
            public void create() throws IOException {
                (new PaneProgDp3Action()).actionPerformed(null);
            }

        });
        // Need this to prevent attempts to nest groups until that's supported elsewhere
        if (group.getName().equals(Roster.ALLENTRIES)) {
            types.add(new NewType() {

                @Override
                public String getName() {
                    return "Roster Group"; // TODO: I18N
                }

                @Override
                public void create() throws IOException {
                    (new jmri.jmrit.roster.swing.CreateRosterGroupAction("", null, null)).actionPerformed(null);
                }
            });
        }
        return types.toArray(new NewType[types.size()]);
    }
}
