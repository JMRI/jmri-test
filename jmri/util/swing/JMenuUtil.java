// JMenuUtil.java

package jmri.util.swing;

import javax.swing.*;
import java.io.File;
import org.jdom.*;
import jmri.util.swing.*;

/**
 * Common utility methods for working with JMenus.
 * <P>
 * Chief among these is the loadMenu method, for
 * creating a set of menus from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.1 $
 */

public class JMenuUtil extends GuiUtilBase {

    static public JMenu[] loadMenu(String filename, WindowInterface wi) {
        Element root;
        
        try {
            root = new jmri.jmrit.XmlFile(){}.rootFromName(filename);
        } catch (Exception e) {
            log.error("Could not parse JMenu file \""+filename+"\" due to: "+e);
            return null;
        }
        int n = root.getChildren("node").size();
        JMenu[] retval = new JMenu[n];
        
        int i = 0;
        for (Object child : root.getChildren("node")) {
            retval[i++] = jMenuFromElement((Element)child, wi);
        }
        return retval;
    }
    
    static JMenu jMenuFromElement(Element main, WindowInterface wi) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        
        for (Object item : main.getChildren("node")) {
            Element child = (Element) item;
            if (child.getChildren("node").size() == 0) {  // leaf
                Action act = actionFromNode(child, wi);
                menu.add(new JMenuItem(act));
            } else {
                menu.add(jMenuFromElement(child, wi)); // not leaf
            }
        }
        return menu;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMenuUtil.class.getName());
}