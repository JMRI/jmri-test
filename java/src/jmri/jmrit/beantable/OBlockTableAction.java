package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.beantable.oblock.TableFrames;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * GUI to define OBlocks, OPaths and Portals
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2009, 2010
 * @version $Revision$
 */
@ActionID(
        id = "jmri.jmrit.beantable.OBlockTableAction",
        category = "Elements"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemOBlockTable",
        iconInMenu = true
)
@ActionReference(
        path = "Menu/Tools/Tables",
        position = 690
)
public class OBlockTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6331453045183182013L;

    public OBlockTableAction() {
        this("OBlock Table");
    }

    public OBlockTableAction(String actionName) {
        super(actionName);
    }

    public void actionPerformed(ActionEvent e) {
        TableFrames f = new TableFrames();
        f.initComponents();
    }
}
