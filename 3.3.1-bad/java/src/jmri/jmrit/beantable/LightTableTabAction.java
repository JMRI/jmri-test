package jmri.jmrit.beantable;

import jmri.*;

public class LightTableTabAction extends AbstractTableTabAction {

    public LightTableTabAction(String s){
        super(s);
    }
    
    public LightTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.lightManagerInstance();
    }
    
    protected String getClassName() {
        return LightTableAction.class.getName();
    }
    
    protected AbstractTableAction getNewTableAction (String choice){
        return new LightTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LightTableTabAction.class.getName());
}