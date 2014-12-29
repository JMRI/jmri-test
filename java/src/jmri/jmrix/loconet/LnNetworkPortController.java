// LnNetworkPortController.java

package jmri.jmrix.loconet;

/**
 * Base for classes representing a LocoNet communications port
 * @author		Kevin Dickerson    Copyright (C) 2011
 * @version             $Revision: 1.24 $
 */
public abstract class LnNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    public LnNetworkPortController(){
        super();
        setManufacturer(jmri.jmrix.DCCManufacturerList.DIGITRAX);
    }

    protected LocoNetSystemConnectionMemo adaptermemo = null;

    protected LnCommandStationType commandStationType = null;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    protected LnCommandStationType[] commandStationTypes = {
                                    LnCommandStationType.COMMAND_STATION_DCS100, 
                                    LnCommandStationType.COMMAND_STATION_DCS200,
                                    LnCommandStationType.COMMAND_STATION_DCS050,
                                    LnCommandStationType.COMMAND_STATION_DCS051 ,
                                    LnCommandStationType.COMMAND_STATION_DB150,
                                    LnCommandStationType.COMMAND_STATION_IBX_TYPE_1,
                                    LnCommandStationType.COMMAND_STATION_IBX_TYPE_2,
                                    LnCommandStationType.COMMAND_STATION_LBPS,
                                    LnCommandStationType.COMMAND_STATION_MM };
    
    protected String[] commandStationNames;
    { 
        commandStationNames = new String[commandStationTypes.length];
        int i = 0;
        for (LnCommandStationType type : commandStationTypes) {
            commandStationNames[i++] = type.getName();
        }
    }

    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
                                    
    /**
     * Set config info from a name, which needs to be one of the valid
     * ones.
     */
    public void setCommandStationType(String name) {
        setCommandStationType(LnCommandStationType.getByName(name));
    }
    
    /**
     * Set config info from the command station type enum.
     */
    public void setCommandStationType(LnCommandStationType value) {
		if (value == null) return;  // can happen while switching protocols
    	log.debug("setCommandStationType: "+value);
        commandStationType = value;
    }

    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals("Both")) mTurnoutNoRetry = true;
        if (value.equals("Spread") || value.equals("Both")) mTurnoutExtraSpace = true;
        log.debug("turnout no retry: "+mTurnoutNoRetry);
        log.debug("turnout extra space: "+mTurnoutExtraSpace);
    }

    /**
     * Set the third port option.  Only to be used after construction, but
     * before the openPort call
     */
    public void configureOption3(String value) {
        super.configureOption3(value);
    	log.debug("configureOption3: "+value);
        setTurnoutHandling(value);
    }
}


/* @(#)LnNetworkPortController.java */