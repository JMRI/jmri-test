// LnTrafficController.java

package jmri.jmrix.loconet;

import java.util.Vector;

/**
 * Abstract base class for implementations of LocoNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation and some
 * statistics support.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 1.9 $
 *
 */
public abstract class LnTrafficController implements LocoNetInterface {

    /**
     * static function returning the LnTrafficController instance to use.
     * @return The registered LnTrafficController instance for general use,
     *         if need be creating one.
     */
    static public LnTrafficController instance() {
        return self;
    }

    static protected LnTrafficController self = null;

    // Abstract methods for the LocoNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *<P>
     * Implementations should update the transmit count statistic.
     *
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendLocoNetMessage(LocoNetMessage m);

    // The methods to implement adding and removing listeners
    protected Vector listeners = new Vector();

    public synchronized void addLocoNetListener(int mask, LocoNetListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    public synchronized void removeLocoNetListener(int mask, LocoNetListener l) {
    	if (listeners.contains(l)) {
            listeners.removeElement(l);
    	}
    }

    /**
     * Forward a LocoNetMessage to all registered listeners.
     * <P>
     * this needs to have public access, as 
     * {@link jmri.jmrix.loconet.loconetovertcp.LnOverTcpPacketizer}
     * and
     * {@link jmri.jmrix.loconet.Intellibox.IBLnPacketizer} invoke it,
     * but don't inherit from it
     * @param m Message to forward. Listeners should not modify it!
     */
    public void notify(LocoNetMessage m) {
        // record statistics
        receivedMsgCount++;
        receivedByteCount += m.getNumDataElements();
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this) {
            v = (Vector) listeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of incoming LocoNet packet: "+m.toString());
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            LocoNetListener client = (LocoNetListener) listeners.elementAt(i);
            client.message(m);
        }
    }

    /**
     * Reset statistics (received message count, transmitted message count,
     * received byte count)
     */
    public void resetStatistics() {
        receivedMsgCount = 0;
        transmittedMsgCount = 0;
        receivedByteCount = 0;
    }
    
    /**
     * Monitor the number of LocoNet messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedMsgCount() {
        return receivedMsgCount;
    }
    protected int receivedMsgCount = 0;
    
    /**
     * Monitor the number of bytes in LocoNet messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedByteCount() {
        return receivedByteCount;
    }
    protected int receivedByteCount = 0;
    
    /**
     * Monitor the number of LocoNet messaages transmitted across the interface.
     */
    public int getTransmittedMsgCount() {
        return transmittedMsgCount;
    }
    protected int transmittedMsgCount = 0;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTrafficController.class.getName());
}


/* @(#)LnTrafficController.java */

