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

    protected boolean mCanRead = true;
    protected boolean mProgPowersOff = false;
    protected String commandStationName = "<unknown>";
    
    protected String[] commandStationNames = {
                                    "DCS100 (Chief)", 
                                    "DCS200",
                                    "DCS50 (Zephyr)",
                                    "DCS51 (Zephyr Xtra)",
                                    "DB150 (Empire Builder)",
                                    "Intellibox",
                                    "LocoBuffer (PS)",
                                    "Mix-Master"};
    
    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet"
    // in pr3/PR3Adapter
                                    
    /**
     * Set config info from the command station type name.
     */
    public void setCommandStationType(String value) {
		if (value == null) return;  // can happen while switching protocols
    	log.debug("setCommandStationType: "+value);
        if (value.equals("DB150 (Empire Builder)") || value.equals("Mix-Master")) {
            mCanRead = false;
            mProgPowersOff = true;
        }
        else {
            mCanRead = true;
            mProgPowersOff = false;
        }
        commandStationName = value;
    }
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
}


/* @(#)LnNetworkPortController.java */