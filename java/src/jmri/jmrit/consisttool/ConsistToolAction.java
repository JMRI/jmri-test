// ConsistToolAction.java
package jmri.jmrit.consisttool;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a ConsistToolFrame object
 *
 * @author Paul Bender Copyright (C) 2003
 * @version $Revision$
 */
@ActionID(
        id = "jmri.jmrit.consisttool.ConsistToolAction",
        category = "JMRI"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemConsistTool",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools",
        position = 640
)
public class ConsistToolAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2206396348711181432L;

    public ConsistToolAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }
    }

    public ConsistToolAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }
    }

    public ConsistToolAction(String s) {
        super(s);

        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }

    }

    public ConsistToolAction() {
        this("Consist Tool");
    }

    public void actionPerformed(ActionEvent e) {

        ConsistToolFrame f = new ConsistToolFrame();
        f.setVisible(true);

    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    static Logger log = LoggerFactory.getLogger(ConsistToolAction.class.getName());
}


/* @(#)ConsistToolAction.java */
