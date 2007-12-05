/**
 * SPROGCSMenu.java
 */

package jmri.jmrix.sprog;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SPROG-specific tools
 *
 * @author	Andrew Crosland   Copyright 2006
 * @version     $Revision: 1.1 $
 */
public class SPROGCSMenu extends JMenu {
    public SPROGCSMenu(String name) {
        this();
        setText(name);
    }

    public SPROGCSMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("SPROG");

        add(new jmri.jmrix.sprog.sprogslotmon.SprogSlotMonAction(rb.getString("MenuItemSlotMonitor")));
        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction("SPROG v3/v4 Firmware Update"));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction("SPROG II Firmware Update"));

    }

}

  /* @(#)SprogCSMenu.java */

