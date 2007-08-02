// PerformActionPanel.java

package apps;

import java.util.List;

/**
 * Provide a GUI for configuring PerformActionModel objects.
 * <P>
 * A PerformModel object invokes a Swing Action when
 * the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.6 $
 * @see apps.PerformActionModel
 */
public class PerformActionPanel extends AbstractActionPanel {

    public PerformActionPanel() {
        super("ButtonActionAdd","ButtonActionRemove");
    }
    List rememberedObjects() { return PerformActionModel.rememberedObjects(); }
    AbstractActionModel getNewModel(){ return new PerformActionModel(); }
}


