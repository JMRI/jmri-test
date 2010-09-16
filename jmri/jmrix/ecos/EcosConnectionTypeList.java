// EcosConnectionTypeList.java

package jmri.jmrix.ecos;


/**
 * Returns a list of valid ESU Ecos Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.1 $
 *
 */
public class EcosConnectionTypeList implements jmri.jmrix.ConnectionTypeList{
    
    public String[] getAvailableProtocolClasses() { 
        return new String[] {
          "jmri.jmrix.ecos.networkdriver.ConnectionConfig",
        };
    }

}

