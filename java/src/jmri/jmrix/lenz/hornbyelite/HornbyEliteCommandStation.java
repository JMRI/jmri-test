/*
 * HornbyEliteCommandStation.java
 */

package jmri.jmrix.lenz.hornbyelite;

import org.apache.log4j.Logger;


/**
 * Defines the routines that differentiate a Hornby Elite Command Station
 * from a Lenz command station.
 *
 * @author			Paul Bender Copyright (C) 2008
 * @version			$Revision$
 */
public class HornbyEliteCommandStation extends jmri.jmrix.lenz.LenzCommandStation implements jmri.jmrix.DccCommandStation,jmri.CommandStation {

    /**
     * The Hornby Elite does NOT use a service mode
     */
    public boolean getHasServiceMode() {return false;}

    /**
     * The Hornby Elite does support Ops Mode programming
     */
    public boolean isOpsModePossible() {return true;}

    /*
     * We need to register for logging
     */
    static Logger log = Logger.getLogger(HornbyEliteCommandStation.class.getName());
    
}


/* @(#)HornbyEliteCommandStation.java */
