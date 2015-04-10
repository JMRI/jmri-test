/**
 * SimpleTurnoutCtrlAction.java
 *
 * Description:	Swing action to create and register a SimpleTurnoutCtrlFrame
 * object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version
 */
package jmri.jmrit.simpleturnoutctrl;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTurnoutCtrlAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6750002493680281206L;

    public SimpleTurnoutCtrlAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SimpleTurnoutCtrlAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SimpleTurnoutCtrlAction(String s) {
        super(s);

        // disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.turnoutManagerInstance() == null) {
            setEnabled(false);
        }

    }

    public SimpleTurnoutCtrlAction() {
        this(java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle").getString("Turnouts"));
    }

    public void actionPerformed(ActionEvent e) {

        SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
        f.setVisible(true);

    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    static Logger log = LoggerFactory.getLogger(SimpleTurnoutCtrlAction.class.getName());
}


/* @(#)SimpleTurnoutCtrlAction.java */
