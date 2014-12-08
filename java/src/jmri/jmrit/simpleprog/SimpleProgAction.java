// SimpleProgAction.java
package jmri.jmrit.simpleprog;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a SimpleProgAction object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
@ActionID(
        id = "jmri.jmrit.simpleprog.SimpleProgAction",
        category = "Programmers"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemSingleCVProgrammer",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Programmers",
        position = 600
)
public class SimpleProgAction extends JmriAbstractAction {

    public SimpleProgAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SimpleProgAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SimpleProgAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public SimpleProgAction() {
        this(Bundle.getMessage("MenuItemSingleCVProgrammer"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SimpleProgFrame f = new SimpleProgFrame();
        f.setVisible(true);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}

/* @(#)SimpleProgAction.java */
