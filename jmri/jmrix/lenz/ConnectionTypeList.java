// ConnectionTypeList.java

package jmri.jmrix.lenz;


/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.1 $
 *
 */
public class ConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
          "jmri.jmrix.lenz.li100.ConnectionConfig",
          "jmri.jmrix.lenz.li100f.ConnectionConfig",
          "jmri.jmrix.lenz.li101.ConnectionConfig",
          "jmri.jmrix.lenz.liusb.ConnectionConfig",
          "jmri.jmrix.lenz.xntcp.ConnectionConfig",
          "jmri.jmrix.xpa.serialdriver.ConnectionConfig",
          "jmri.jmrix.lenz.xnetsimulator.ConnectionConfig", // experimental
          "jmri.jmrix.lenz.liusbserver.ConnectionConfig" // experimental
        };
    }

}

