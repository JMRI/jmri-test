// WangrowMenu.java

package jmri.jmrix.wangrow;

import java.util.ResourceBundle;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Note that this is still using specific tools from the
 * {@link jmri.jmrix.nce} package.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2.16.1 $
 */
public class WangrowMenu extends JMenu {
	
	NceSystemConnectionMemo memo = null;
	
    public WangrowMenu(NceSystemConnectionMemo m) {

        super();
        this.memo = m;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        
        if (memo != null)
            setText(memo.getUserName());
        else
            setText(rb.getString("MenuWangrow"));


        setText(rb.getString("MenuItemWangrow"));

        add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
    }
}


