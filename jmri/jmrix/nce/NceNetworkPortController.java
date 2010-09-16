// NceNetworkPortController.java

package jmri.jmrix.nce;

/*
 * Identifying class representing a NCE communications port
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version $Revision: 1.1 $
 */

public abstract class NceNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to NceTrafficController classes, who in turn will deal in messages.
    protected NceSystemConnectionMemo adaptermemo = null;
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)NceNetworkPortController.java */
