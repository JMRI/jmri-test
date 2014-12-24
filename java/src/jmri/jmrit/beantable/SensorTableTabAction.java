package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionID(
        id = "jmri.jmrit.beantable.SensorTableAction",
        category = "Elements"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemSensorTable",
        iconInMenu = true
)
@ActionReference(
        path = "Menu/Tools/Tables",
        position = 610
)
public class SensorTableTabAction extends AbstractTableTabAction {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8373953953343271566L;

	public SensorTableTabAction(String s){
        super(s);
    }
    
    public SensorTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.sensorManagerInstance();
    }
       
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }
    
    protected AbstractTableAction getNewTableAction (String choice){
        return new SensorTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
    
    static Logger log = LoggerFactory.getLogger(SensorTableTabAction.class.getName());
}
