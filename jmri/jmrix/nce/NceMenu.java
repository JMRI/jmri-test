// NceMenu.java

package jmri.jmrix.nce;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Some of the tools used here are also used directly by the Wangrow
 * support in {@link jmri.jmrix.wangrow}.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.8 $
 */
public class NceMenu extends JMenu {
    public NceMenu(String name) {
        this();
        setText(name);
    }

    public NceMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemNCE"));

        
        add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.nce.macro.NceMacroGenAction(rb.getString("MenuItemMacroCommand")));
        add(new jmri.jmrix.nce.macro.NceMacroEditAction(rb.getString("MenuItemMacroEdit")));
        add(new jmri.jmrix.nce.consist.NceConsistEditAction(rb.getString("MenuItemConsistEdit")));
        add(new jmri.jmrix.ncemonitor.NcePacketMonitorAction(rb.getString("MenuItemTrackPacketMonitor")));
        add(new jmri.jmrix.nce.clockmon.ClockMonAction(rb.getString("MenuItemClockMon")));
        add(new jmri.jmrix.nce.cab.NceShowCabAction(rb.getString("MenuItemShowCabs")));

        add(new jmri.jmrix.nce.boosterprog.BoosterProgAction());
    }

}
