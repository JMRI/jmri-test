// ConnectionConfig.java

package jmri.jmrix.can.adapters.loopback;
import java.util.Vector;

import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jmri.jmrix.can.ConfigurationManager;

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
    
    @Override
    protected void checkInitDone(){
        if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        super.checkInitDone();

        opt1Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selection = (String)opt1Box.getSelectedItem();
                String newUserName = "MERG";
                if(ConfigurationManager.OPENLCB.equals(selection)){
                    newUserName = "OpenLCB";
                } else if (ConfigurationManager.RAWCAN.equals(selection)){
                    newUserName = "CANraw";
                } else if(ConfigurationManager.TEST.equals(selection)){
                    newUserName = "CANtest";
                }
                connectionNameField.setText(newUserName);
                
                if(!adapter.getSystemConnectionMemo().setUserName(newUserName)){
                    for (int x = 2; x<50; x++){
                        if(adapter.getSystemConnectionMemo().setUserName(newUserName+x)){
                            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                            break;
                        }
                    }
                }
            }
        });
    }

    public String name() { return "CAN Simulation"; }

    protected void setInstance() {
        if(adapter ==null){
            adapter = new Port();
        }
    }
    
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

