// MrcTrafficController.java

package jmri.jmrix.mrc;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from MRC messages.  The "MrcInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a MrcPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class MrcTrafficController extends AbstractMRTrafficController
	implements MrcInterface {

    public MrcTrafficController() {
        super();
    }

    // The methods to implement the MrcInterface

    public synchronized void addMrcListener(MrcListener l) {
        this.addListener(l);
    }

    public synchronized void removeMrcListener(MrcListener l) {
        this.removeListener(l);
    }


    /**
     * Forward a MrcMessage to all registered MrcInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((MrcListener)client).message((MrcMessage)m);
    }

    /**
     * Forward a MrcReply to all registered MrcInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((MrcListener)client).reply((MrcReply)m);
    }

    public void setSensorManager(jmri.SensorManager m) { }
    protected AbstractMRMessage pollMessage() {
		return null;
    }
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendMrcMessage(MrcMessage m, MrcListener reply) {
        sendMessage(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return MrcMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return MrcMessage.getExitProgMode();
    }

    /**
     * static function returning the MrcTrafficController instance to use.
     * @return The registered MrcTrafficController instance for general use,
     *         if need be creating one.
     */
    static public MrcTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new MrcTrafficController object");
            self = new MrcTrafficController();
        }
        return self;
    }

    static protected MrcTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new MrcReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // for now, _every_ character is a message
        return true;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MrcTrafficController.class.getName());
}


/* @(#)MrcTrafficController.java */

