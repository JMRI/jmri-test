// MrcTrafficController.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version			$Revision$
 */
public class MrcTrafficController extends AbstractMRTrafficController
	implements MrcInterface {

    public MrcTrafficController() {
        super();
        setAllowUnexpectedReply(true);
    }

    public void setCabNumber(int x){
        cabAddress = x;
    }
    
    int cabAddress = 0;
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

    protected AbstractMRReply newReply() { return new MrcReply(); }
    
    /**
     * instance use of the traffic controller is no longer used for multiple connections
     */
	@Deprecated
    public void setInstance(){}

    protected boolean endOfMessage(AbstractMRReply msg) {
        // for now, _every_ character is a message
        if(msg.getElement(1)==0x01 && msg.getNumDataElements()>=6){
            if(msg.getElement(0)==cabAddress){
                //Need to use this to trigger sending any commands?
            }
            return true;
        }
        if(msg.getElement(1)==0x00 && msg.getNumDataElements()>=4){
            msg.setUnsolicited();
            return true;
        }
        return false;
    }
    
        /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        //if (! m.isBinary()){
            msg[offset] = 0x00;
           // msg[offset+1] = 0x00;
        //}
    }
    
    public void setAdapterMemo(MrcSystemConnectionMemo memo){
        adaptermemo = memo;
    }
    
    MrcSystemConnectionMemo adaptermemo;
    
    public String getUserName() { 
        if(adaptermemo==null) return "MRC";
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "MR";
        return adaptermemo.getSystemPrefix();
    }

    static Logger log = LoggerFactory.getLogger(MrcTrafficController.class.getName());
}


/* @(#)MrcTrafficController.java */

