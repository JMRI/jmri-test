// NixieClockAction.java
package jmri.jmrit.nixieclock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a NixieClockFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 */
@ActionID(
        id = "jmri.jmrit.nixieclock.NixieClockAction",
        category = "JMRI"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemNixieClock",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Clocks",
        position = 610
)
public class NixieClockAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5653182277242573672L;

    public NixieClockAction() {
        this("Nixie Clock");
    }

    public NixieClockAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        NixieClockFrame f = new NixieClockFrame();
        f.setVisible(true);

    }

}

/* @(#)NixieClockAction.java */
