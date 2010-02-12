// ConnectionTypeList.java

package jmri.jmrix.cmri;


/**
 * Returns a list of valid CMRI Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.1 $
 *
 */
public class ConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig",
              "jmri.jmrix.cmri.serial.sim.ConnectionConfig"
        };
    }

}

