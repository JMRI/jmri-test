// ConnectionConfig.java

package jmri.jmrix.cmri.serial.serialdriver;

import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an C/MRI SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.2 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

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

    public void loadDetails(JPanel details) {
        super.loadDetails(details);
        opt1Box.setEditable(true);
    }

    public String name() { return "C/MRI"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}

