package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionID(
        id = "jmri.jmrit.beantable.LightTableAction",
        category = "Elements"
)
@ActionRegistration(
        iconBase = "org/jmri/core/ui/toolbar/generic.gif",
        displayName = "jmri.jmrit.Bundle#MenuItemLightTable",
        iconInMenu = true
)
@ActionReference(
        path = "Menu/Tools/Tables",
        position = 620
)
public class LightTableTabAction extends AbstractTableTabAction {

    /**
     *
     */
    private static final long serialVersionUID = 2814960247992024318L;

    public LightTableTabAction(String s) {
        super(s);
    }

    public LightTableTabAction() {
        this("Multiple Tabbed");
    }

    protected Manager getManager() {
        return InstanceManager.lightManagerInstance();
    }

    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    protected AbstractTableAction getNewTableAction(String choice) {
        return new LightTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    static Logger log = LoggerFactory.getLogger(LightTableTabAction.class.getName());
}
