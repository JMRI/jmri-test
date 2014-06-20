// MrcMenu.java

package jmri.jmrix.mrc.swing;

import javax.swing.*;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the JMRI MRC-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2010
 * Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 * @version     $Revision: 23001 $
 */

public class MrcMenu extends JMenu {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7065069034402845776L;

	/**
     * Create a MRC menu.
     * And loads the MrcSystemConnectionMemo to the various actions.
     * Actions will open new windows.
     */
    // Need to Sort out the MRC server menu items;
    public MrcMenu(MrcSystemConnectionMemo memo) {
        super();

        // memo can not be null!
        if (memo == null){
        	new Exception().printStackTrace();
        	return;
        }        	
            
        setText(memo.getUserName());
            
        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
            	MrcNamedPaneAction a = new MrcNamedPaneAction( Bundle.getMessage(item.name), wi, item.load, memo);
                add(a);
            }
        }
        
        // do we have a MrcTrafficController?
        setEnabled(memo.getMrcTrafficController() != null);	// disable menu, no connection, no tools!
        
        add(new javax.swing.JSeparator());
    }

    private Item[] panelItems = new Item[] {
        new Item("MenuItemCommandMonitor", "jmri.jmrix.mrc.swing.mrcmon.MrcMonPanel"),
        new Item("MenuItemSendCommand", "jmri.jmrix.mrc.swing.packetgen.MrcPacketGenPanel"),
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

/* @(#)MrcMenu.java */