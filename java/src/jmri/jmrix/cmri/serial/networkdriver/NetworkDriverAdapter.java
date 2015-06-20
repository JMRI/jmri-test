// NetworkDriverAdapter.java
package jmri.jmrix.cmri.serial.networkdriver;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNetworkPortController;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialSensorManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the NCE system network connection.
 * <P>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 * @version	$Revision: 28746 $
 */
public class NetworkDriverAdapter extends SerialNetworkPortController {

    public NetworkDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        setManufacturer(jmri.jmrix.DCCManufacturerList.CMRI);
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.cmri.serial.SerialTurnoutManager.instance());
        jmri.InstanceManager.setLightManager(jmri.jmrix.cmri.serial.SerialLightManager.instance());

        SerialSensorManager s;
        jmri.InstanceManager.setSensorManager(s = jmri.jmrix.cmri.serial.SerialSensorManager.instance());
        SerialTrafficController.instance().setSensorManager(s);
        jmri.jmrix.cmri.serial.ActiveFlag.setActive();
    }


    static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}
