package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(
        id = "jmri.jmrit.throttle.ThrottlesListAction",
        category = "Throttles"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemThrottlesList",
        iconInMenu = false
)
@ActionReference(
        path = "Menu/Tools/Throttles",
        position = 610
)
public class ThrottlesListAction extends AbstractAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6318756102854541505L;

	/**
     * Constructor
     * @param s Name for the action.
     */
    public ThrottlesListAction(String s) {
        super(s);
    // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }
    
    public ThrottlesListAction() {
	  this("Throttles list");         
    }
    
	public void actionPerformed(ActionEvent e) {
		jmri.jmrit.throttle.ThrottleFrameManager.instance().showThrottlesList();
	}
}
