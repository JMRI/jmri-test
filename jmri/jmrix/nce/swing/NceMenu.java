// NceMenu.java

package jmri.jmrix.nce.swing;

import java.util.ResourceBundle;
import javax.swing.*;
import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri Nce-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2010
 * converted to multiple connection
 * @author	kcameron	Copyright 2010
 * @version     $Revision: 1.1.2.2 $
 */

public class NceMenu extends JMenu {

    /**
     * Create a Nce menu.
     * Preloads the TrafficController to certain actions.
     * Actions will open new windows.
     */
    // Need to Sort out the Nce server menu items;
    public NceMenu(NceSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        if (memo != null)
            setText(memo.getUserName());
        else
            setText(rb.getString("MenuNce"));
            
        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new NceNamedPaneAction( rb.getString(item.name), wi, item.load, memo));
            }
        }
        add(new javax.swing.JSeparator());
    }
        
    Item[] panelItems = new Item[] {
        new Item("MenuItemCommandMonitor", "jmri.jmrix.nce.ncemon.NceMonPane"),
        new Item("MenuItemSendCommand", "jmri.jmrix.nce.packetgen.NcePacketGenAction"),
        new Item("MenuItemMacroCommand", "jmri.jmrix.nce.macro.NceMacroGenAction"),
        new Item("MenuItemMacroEdit", "jmri.jmrix.nce.macro.NceMacroEditAction"),
        new Item("MenuItemConsistEdit", "jmri.jmrix.nce.consist.NceConsistEditAction"),
        new Item("MenuItemTrackPacketMonitor", "jmri.jmrix.ncemonitor.NcePacketMonitorAction"),
        new Item("MenuItemClockMon", "jmri.jmrix.nce.clockmon.ClockMonAction"),
        new Item("MenuItemShowCabs", "jmri.jmrix.nce.cab.NceShowCabAction"),
        new Item("MenuItemBoosterProg", "jmri.jmrix.nce.boosterprog.BoosterProgAction")
    };
    
    static class Item {
        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }
        String name;
        String load;
    }
}


