// DualDecoderToolAction.java
package jmri.jmrit.dualdecoder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a DualDecoderTool
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision$
 */
@ActionID(
        id = "jmri.jmrit.dualdecoder.DualDecoderToolAction",
        category = "Programmers"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemMultiDecoderControl",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Programmers",
        position = 630
)
public class DualDecoderToolAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6247002513212825213L;

    public DualDecoderToolAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance() == null) {
            setEnabled(false);
        }

    }

    public DualDecoderToolAction() {

        this(Bundle.getMessage("MenuItemMultiDecoderControl"));
    }

    public void actionPerformed(ActionEvent e) {

        new DualDecoderSelectFrame().setVisible(true);

    }

}

/* @(#)DualDecoderToolAction.java */
