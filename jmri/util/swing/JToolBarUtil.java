// JToolBarUtil.java

package jmri.util.swing;

import javax.swing.*;
import java.io.File;
import org.jdom.*;

/**
 * Common utility methods for working with JToolBars.
 * <P>
 * Chief among these is the loadToolBar method, for
 * creating a JToolBar from an XML definition
 * <p>
 * Only parses top level of XML file, since ToolBars have only level.
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.4 $
 */

public class JToolBarUtil extends GuiUtilBase {

    static public JToolBar loadToolBar(File file) {
        return loadToolBar(file, null, null);  // tool bar without window or context
    }

    static public JToolBar loadToolBar(File file, WindowInterface wi, Object context) {
        Element root = rootFromFile(file);
                
        JToolBar retval = new JToolBar(root.getChild("name").getText());
        
        for (Object item : root.getChildren("node")) {
            Action act = actionFromNode((Element)item, wi, context);
            if (act == null) continue;
            if (act.getValue(javax.swing.Action.SMALL_ICON) != null) {
                // icon present, add explicitly
                JButton b = new JButton((javax.swing.Icon)act.getValue(javax.swing.Action.SMALL_ICON));
                b.setAction(act);
                retval.add(b);
            } else {
                retval.add(new JButton(act));
            }
        }
        return retval;
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JToolBarUtil.class.getName());
}