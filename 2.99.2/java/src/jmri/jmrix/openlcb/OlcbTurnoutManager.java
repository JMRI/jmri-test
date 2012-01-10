// OlcbTurnoutManager.java

package jmri.jmrix.openlcb;

import jmri.*;
import jmri.managers.AbstractTurnoutManager;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * OpenLCB implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author			Bob Jacobsen Copyright (C) 2008, 2010
 * @version			$Revision$
 * @since 2.3.1
 */
public class OlcbTurnoutManager extends AbstractTurnoutManager {
	/*
    public OlcbTurnoutManager(){
        super();
    }*/
    
    public OlcbTurnoutManager(CanSystemConnectionMemo memo){
        this.memo=memo;
        prefix = memo.getSystemPrefix();
    }
    
    CanSystemConnectionMemo memo;
    
    String prefix = "M";
    
    public String getSystemPrefix() { return prefix; }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    protected Turnout createNewTurnout(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length()+1);
        Turnout t = new OlcbTurnout(getSystemPrefix(), addr, memo.getTrafficController());
        t.setUserName(userName);
        return t;
    }
    
    public boolean allowMultipleAdditions() { return false;  }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        // don't check for integer; should check for validity here
        return prefix+typeLetter()+curAddress;
    }

    public String getNextValidAddress(String curAddress, String prefix) throws JmriException{
        // always return this (the current) name without change
        return curAddress;
    }

   /**
    * A method that creates an array of systems names to allow bulk
    * creation of turnouts.
    */
    //further work needs to be done on how to format a number of turnouts, therefore this method will only return one entry.
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix){
        numberToAdd = 1;
        String range[] = new String[numberToAdd];
        for (int x = 0; x < numberToAdd; x++){
            range[x] = prefix+"T"+start;
        }
        return range;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbTurnoutManager.class.getName());
}

/* @(#)OlcbTurnoutManager.java */
