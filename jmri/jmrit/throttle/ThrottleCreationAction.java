package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version     $Revision: 1.11 $
 */
public class ThrottleCreationAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
    // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }

    public ThrottleCreationAction() {
        this("New Throttle...");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
    	// load throttle preference 
    	LoadXmlThrottleAction lxta = new LoadXmlThrottleAction();
    	if (lxta.loadThrottles(new File(SaveThrottlePreferencesAction.getDefaultThrottleFilename())))
    		return;
		// need to create a new one
    	ThrottleFrame tf =
			ThrottleFrameManager.instance().createThrottleFrame();
		tf.setVisible(true);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleCreationAction.class.getName());

}
