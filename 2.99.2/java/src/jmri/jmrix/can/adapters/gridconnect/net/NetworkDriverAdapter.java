// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.SystemConnectionMemo;

import java.util.Vector;

/**
 * Implements SerialPortAdapter for the OpenLCB system network connection.
 * <P>This connects via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends jmri.jmrix.AbstractNetworkPortController {

    //This should all probably be updated to use the AbstractNetworkPortContoller
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
    
    public NetworkDriverAdapter() {
        super();
        adaptermemo = new jmri.jmrix.can.CanSystemConnectionMemo();
        setManufacturer(jmri.jmrix.DCCManufacturerList.OPENLCB);
    }
    
    /**
     * set up all of the other objects to operate with an NCE command
     * station connected to this port
     */
    public void configure() {
    	// set the command options, Note that the NetworkDriver uses
    	// the second option for EPROM revision
        if (getCurrentOption1Setting().equals(validOption1()[0])) {
        	//
        } else {
            // setting binary mode
            //
        }

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new GcTrafficController();
        adaptermemo.setTrafficController(tc);
        
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        adaptermemo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);

        // do central protocol-specific configuration    
        adaptermemo.configureManagers();

    }

    public boolean status() {return opened;}

    /**
     * Option 2 is various filters
     */
    public String[] validOption1() { return new String[]{"Pass All", "Filtering"}; }

    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return "Gateway: "; }

    /**
     * Set the binary vs ASCII command set option.
     */
    public void configureOption1(String value) { mOpt1 = value; }

    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[1];
        return mOpt1;
    }

    // private control members
    private boolean opened = false;

    //Socket socket;
    
    public Vector<String> getPortNames() {
        log.error("Unexpected call to getPortNames");
        return null;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}
