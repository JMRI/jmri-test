/**
 * SPROGMenu.java
 */

package jmri.jmrix.sprog;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SPROG-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.6 $
 */
public class SPROGMenu extends JMenu {
    public SPROGMenu(SprogSystemConnectionMemo memo) {
        this();
        setText(memo.getUserName());
    }

    public SPROGMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("SPROG");

        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.sprog.console.SprogConsoleAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.sprog.update.SprogVersionAction("Get SPROG Firmware Version"));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction("SPROG v3/v4 Firmware Update"));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction("SPROG II Firmware Update"));
        add(new jmri.jmrix.sprog.swing.PowerPanelAction("SPROG Power Control"));

    }

}


