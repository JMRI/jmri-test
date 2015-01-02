// SerialPortController.java

package jmri.jmrix.tmcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Abstract base for classes representing a TMCC communications port
 * @author	Bob Jacobsen    Copyright (C) 2001, 2006
 * @version	$Revision$
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();

    @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return null; // No SystemConnectionMemo
    }
}


/* @(#)SerialPortController.java */
