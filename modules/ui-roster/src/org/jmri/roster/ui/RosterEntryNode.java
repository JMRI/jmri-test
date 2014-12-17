package org.jmri.roster.ui;

import java.beans.IntrospectionException;
import jmri.jmrit.roster.RosterEntry;
import org.openide.nodes.BeanNode;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntryNode extends BeanNode {

    @Messages({
        "# {0} - Road name",
        "# {1} - Road number",
        "# {2} - DCC Address",
        "ShortDescription={0} {1} ({2})",
        "# {0} - Roster ID",
        "# {1} - Road name",
        "# {2} - Road number",
        "DisplayName={0} ({1} {2})"
    })

    public RosterEntryNode(RosterEntry bean) throws IntrospectionException {
        super(bean);
        if (bean.getRoadName() == null || bean.getRoadName().isEmpty()) {
            this.setDisplayName(bean.getId());
        } else {
            this.setDisplayName(Bundle.DisplayName(bean.getId(), bean.getRoadName(), bean.getRoadNumber()));
        }
        this.setShortDescription(Bundle.ShortDescription(bean.getRoadName(), bean.getRoadNumber(), bean.getDccLocoAddress()));
    }

}
