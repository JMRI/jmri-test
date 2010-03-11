// LocoNetMenu.java

package jmri.jmrix.loconet.swing;

import java.util.ResourceBundle;

import javax.swing.JMenu;

import jmri.jmrix.loconet.LocoNetBundle;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2010
 * @version     $Revision: 1.1 $
 */
public class LocoNetMenu extends JMenu {

    /**
     * Create a LocoNet menu.
     * Preloads the TrafficController to certain actions
     */
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = LocoNetBundle.bundle();

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new LnNamedPaneAction( rb.getString(item.name), wi, item.load, memo));
            }
        }
    }
    
    Item[] panelItems = new Item[] {
        new Item("MenuItemLocoNetMonitor",      "jmri.jmrix.loconet.locomon.LocoMonPane"),
        new Item("MenuItemSlotMonitor",         "jmri.jmrix.loconet.slotmon.SlotMonPane"),
        new Item("MenuItemClockMon",            "jmri.jmrix.loconet.clockmon.ClockMonPane"),
        new Item("MenuItemLocoStats",           "jmri.jmrix.loconet.locostats.LocoStatsPanel"),
        null,
        new Item("MenuItemBDL16Programmer",     "jmri.jmrix.loconet.bdl16.BDL16Panel"),
        new Item("MenuItemLocoIOProgrammer",    "jmri.jmrix.loconet.locoio.LocoIOPanel"),
        new Item("MenuItemPM4Programmer",       "jmri.jmrix.loconet.pm4.PM4Panel"),
        new Item("MenuItemSE8cProgrammer",      "jmri.jmrix.loconet.se8.SE8Panel"),
        new Item("MenuItemDS64Programmer",      "jmri.jmrix.loconet.ds64.DS64Panel"),
        new Item("MenuItemCmdStnConfig",        "jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane"),
        new Item("MenuItemSetID",               "jmri.jmrix.loconet.locoid.LocoIdPanel"),
        null,
        new Item("MenuItemStartLocoNetServer",  "jmri.jmrix.loconet.locormi.LnMessageServerPanel"),
        new Item("MenuItemLocoNetOverTCPServer","jmri.jmrix.loconet.loconetovertcp.ServerPanel"),
        null,
        new Item("MenuItemThrottleMessages",    "jmri.jmrix.loconet.swing.throttlemsg.MessagePanel"),
        new Item("MenuItemSendPacket",          "jmri.jmrix.loconet.locogen.LocoGenPanel"),
        new Item("MenuItemPr3ModeSelect",       "jmri.jmrix.loconet.pr3.swing.Pr3SelectPane"),
        null,
        new Item("MenuItemDownload",            "jmri.jmrix.loconet.downloader.LoaderPane"),
        new Item("MenuItemSoundload",           "jmri.jmrix.loconet.soundloader.LoaderPane"),
        new Item("MenuItemSoundEditor",         "jmri.jmrix.loconet.soundloader.EditorPane")
    };
    
    class Item {
        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }
        String name;
        String load;
    }
}


