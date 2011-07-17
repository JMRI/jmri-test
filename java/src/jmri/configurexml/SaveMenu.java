// SaveMenu.java

package jmri.configurexml;

import javax.swing.JMenu;
import java.util.*;

/**
 * Create a "Save" menu item containing actions for storing 
 * various data (subsets).
 *
 * @author	Bob Jacobsen   Copyright 2005
 * @version     $Revision: 1.2 $
 */
public class SaveMenu extends JMenu {
    public SaveMenu(String name) {
        this();
        setText(name);
    }

    public SaveMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.configurexml.SaveMenuBundle");

        setText(rb.getString("MenuItemSave"));

        add(new jmri.configurexml.StoreXmlConfigAction(rb.getString("MenuItemStoreConfig")));
        add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStoreUser")));

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SaveMenu.class.getName());
}


