// ConnectionTypeList.java

package jmri.jmrix.openlcb;


/**
 * Returns a list of valid Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.2 $
 *
 */
public class ConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
    
        // set the connection types to have OPENLCB at the front
        jmri.jmrix.can.ConfigurationManager.setOpenLCB();
        
        // return the list of connector values for a CAN/MERG connection
        return new String[] {
              "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
              "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.loopback.ConnectionConfig"
        };
    }

}

