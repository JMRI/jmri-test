package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionID(
        id = "jmri.jmrit.beantable.TurnoutTableAction",
        category = "Elements"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemTurnoutTable",
        iconInMenu = true
)
@ActionReference(
        path = "Menu/Tools/Tables",
        position = 600
)
public class TurnoutTableTabAction extends AbstractTableTabAction {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5514320062139920106L;

	public TurnoutTableTabAction(String s){
        super(s);
    }
    
    public TurnoutTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.turnoutManagerInstance();
    }
    
    protected String getClassName() {
        return TurnoutTableAction.class.getName();
    }
       
    protected AbstractTableAction getNewTableAction (String choice){
        return new TurnoutTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }
    
    static Logger log = LoggerFactory.getLogger(TurnoutTableTabAction.class.getName());
}
