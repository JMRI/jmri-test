package org.jmri.roster.ui;

import java.beans.IntrospectionException;
import jmri.jmrit.roster.RosterEntry;
import org.openide.nodes.BeanNode;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterEntryNode extends BeanNode {

    public RosterEntryNode(RosterEntry bean) throws IntrospectionException {
        super(bean);
        this.setDisplayName(bean.getDisplayName());
    }

}
