// LnTcpDriverAdapter.java

package jmri.jmrix.loconet.loconetovertcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.*;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network connection.
 * <P>This connects
 * a Loconet via a telnet connection.
 * Normally controlled by the LnTcpDriverFrame class.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @author      Alex Shepherd Copyright (C) 2003, 2006
 * @version     $Revision$
 */

public class LnTcpDriverAdapter extends LnNetworkPortController {

    public LnTcpDriverAdapter() {
        super();
        option2Name = "CommandStation";
        option3Name = "TurnoutHandle";
        options.put(option2Name, new Option("Command station type:", commandStationNames, false));
        options.put(option3Name, new Option("Turnout command handling:", new String[]{"Normal", "Spread", "One Only", "Both"}));
        adaptermemo = new LocoNetSystemConnectionMemo();
    }
    /**
     * set up all of the other objects to operate with a LocoNet
     * connected via this class.
     */
    public void configure() {
    
        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer();
        packets.connectPort(this);

        // create memo
        adaptermemo.setLnTrafficController(packets);
        // do the common manager config
        adaptermemo.configureCommandStation(commandStationType,
                                            mTurnoutNoRetry, mTurnoutExtraSpace);
        adaptermemo.configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }


    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;
    
    public void configureOption1(String value) {
        super.configureOption1(value);
    	log.debug("configureOption1: "+value);
        setCommandStationType(value);
    }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static Logger log = LoggerFactory.getLogger(LnTcpDriverAdapter.class.getName());

}
