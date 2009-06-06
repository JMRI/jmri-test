// TrafficController.java

package jmri.jmrix.can;

/**
 * Traffic controller for CAN access.
 *
 * @author          Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
abstract public class TrafficController extends AbstractCanTrafficController {
    
    public TrafficController() {
        super();
    }
   
    /**
     * static function returning the CanTrafficController instance to use.
     * @return The registered TrafficController instance for general use,
     *         which must have been initialized previously
     */
    static public TrafficController instance() {
        return self;
    }
    
    static protected TrafficController self = null;
    protected void setInstance() { self = this; }

}


/* @(#)GcTrafficController.java */

