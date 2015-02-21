package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.beans.Beans;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Create a new throttle.
 *
 * @author	Glen Oberhauser
 * @version $Revision$
 */
@ActionID(
        id = "jmri.jmrit.throttle.ThrottleCreationAction",
        category = "Throttles"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemNewThrottle",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Throttles",
        position = 600
)
public class ThrottleCreationAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3268542525652376730L;

    public ThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public ThrottleCreationAction() {
        this(Bundle.getMessage("MenuItemNewThrottle"));
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        String group = null;
        if (Beans.hasProperty(wi, "selectedRosterGroup")) {
            group = (String) Beans.getProperty(wi, "selectedRosterGroup");
        }
        ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
        tf.getAddressPanel().getRosterEntrySelector().setSelectedRosterGroup(group);
        tf.toFront();
    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
