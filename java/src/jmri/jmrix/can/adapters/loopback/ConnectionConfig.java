// ConnectionConfig.java

package jmri.jmrix.can.adapters.loopback;
import java.util.Vector;

import java.util.ResourceBundle;

/**
 * Definition of objects to handle configuring a layout connection
 * via a LocoNet hexfile emulator
 *
 * @author      Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "CAN Simulation"; }

    protected void setInstance() {
        adapter = Port.instance();
    }
    
    @SuppressWarnings("unchecked")
	protected Vector<String> getPortNames() {
        Vector<String> portNameVector = new Vector<String>();
        portNameVector.addElement("(None)");
        return portNameVector;
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
}

