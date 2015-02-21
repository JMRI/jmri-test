package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save throttles to XML
 *
 * @author	Lionel Jeanson Copyright 2009
 * @version $Revision$
 */
@ActionID(
        id = "jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction",
        category = "Throttles"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemSaveAsDefaultThrottleLayout",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Throttles",
        position = 640
)
public class StoreDefaultXmlThrottlesLayoutAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 2009494965932977394L;

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public StoreDefaultXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable this ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    /**
     * The action is performed. Let the user choose the file to save to. Write
     * XML for each ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        StoreXmlThrottlesLayoutAction sxta = new StoreXmlThrottlesLayoutAction();
        sxta.saveThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename()));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(StoreXmlThrottlesLayoutAction.class.getName());

}
