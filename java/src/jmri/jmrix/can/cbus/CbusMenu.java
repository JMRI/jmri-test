// CbusMenu.java

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author	    Bob Jacobsen   Copyright 2003, 2008, 2009
 * @author      Andrew Crosland 2008
 * @version     $Revision$
 * @deprecated 2.99.2
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class CbusMenu extends jmri.jmrix.can.cbus.swing.CbusMenu {
    public CbusMenu(CanSystemConnectionMemo memo) {
        super(memo);
    }
}


