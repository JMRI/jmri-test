// SpeedoPortController.java

package jmri.jmrix.bachrus;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a Bachrus speedo communications port
 * @author			Bob Jacobsen        Copyright (C) 2001
 * @author			Andrew Crosland     Copyright (C) 2010
 * @version			$Revision: 1.3 $
 */public abstract class SpeedoPortController extends jmri.jmrix.AbstractSerialPortController {
	// base class. Implementations will provide InputStream and OutputStream
	// objects to SprogTrafficController classes, who in turn will deal in messages.

	// returns the InputStream from the port
	public abstract DataInputStream getInputStream();

	// returns the outputStream to the port
	public abstract DataOutputStream getOutputStream();

	// check that this object is ready to operate
	public abstract boolean status();
    
    protected SpeedoSystemConnectionMemo adaptermemo = null;
}


/* @(#)SpeedoPortController.java */
