// MrcTrafficController.java

package jmri.jmrix.mrc;

import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import static jmri.jmrix.AbstractMRTrafficController.IDLESTATE;
import static jmri.jmrix.AbstractMRTrafficController.WAITMSGREPLYSTATE;

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
        attention = MrcMessage.getAttention((byte)cabAddress);
        attention.setByte();
        setAllowUnexpectedReply(true);
    }

    public void setCabNumber(int x){
        cabAddress = x;
        attention = MrcMessage.getAttention((byte)cabAddress);
        attention.setByte();
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
    
    MrcMessage attention;
    synchronized protected void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
        //We only need to send the get attention once when we send a fresh command.
        msgQueue.addLast(attention);
        listenerQueue.addLast(reply);
        ((MrcMessage)m).setByte();
        msgQueue.addLast(m);
        listenerQueue.addLast(reply);
        log.info("Something in queue");
        if(m!=null)
            log.debug("just notified transmit thread with message " +m.toString());
    }
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        waiting = false;
        // for now, _every_ character is a message
        if(msg.getElement(1)==0x01 && msg.getNumDataElements()>=6){
            if(msg.getElement(0)==cabAddress){
                waiting = true;
                   /*synchronized (xmtRunnable) {
    ommands?                   mCurrentState = NOTIFIEDSTATE;
                       replyInDispatch = false;
                       xmtRunnable.notify();
                   }*/
                //Need to use this to trigger sending any c
            }
            return true;
        }
        if(mCurrentState == WAITMSGREPLYSTATE){
            if(msg.getNumDataElements()==2)
                return true;
            return false;
        }
        if(msg.getElement(0)==0x25 && msg.getElement(1)==0x00){
            //Thottle speed , need to wait until all is recieved
            if(msg.getNumDataElements()>=14){
                return true;
            }
            return false;
        } 
        if(msg.getElement(1)==0x00 && msg.getNumDataElements()>=4){
            msg.setUnsolicited();
            return true;
        }
        return false;
    }
    
    boolean waiting = false;
    
    protected void transmitLoop() {
        log.info("Transmit loop");
        while(!connectionError) {
            MrcMessage m = null;
            AbstractMRListener l = null;
            while(waiting){
                // check for something to do
                //synchronized(selfLock) {
                    if (msgQueue.size()!=0) {
                        log.info("transmit loop has something to do: "+m);
                        // yes, something to do
                        m = (MrcMessage)msgQueue.getFirst();
                        l = listenerQueue.getFirst();
                        mLastSender = l;
                        try {
                            ostream.write(m.getByte());
                            ostream.flush();
                        } catch (Exception e) {
                            log.error("Unable to send");
                        }
                        Runnable r = new XmtNotifier(m, mLastSender, this);
                        javax.swing.SwingUtilities.invokeLater(r);
                        //forwardToPort(m, l);
                        
                        listenerQueue.removeFirst();
                        msgQueue.removeFirst();
                        mCurrentState = IDLESTATE;
                        if(!m.getAttention()){
                            waiting = false;
                            mCurrentState = WAITMSGREPLYSTATE;
                        }
                        
                        
                    } else { // release lock here to proceed in parallel
                        waiting = false;
                        mCurrentState = IDLESTATE;
                   }
            }
            
        }

    }
    
        synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) log.debug("forwardToPort message: ["+m+"]");
        // remember who sent this
        mLastSender = reply;
        
        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(m)];
        // add header
        int offset = addHeaderToOutput(msg, m);

        // add data content
        int len = m.getNumDataElements();
        for (int i=0; i< len; i++)
            msg[i+offset] = (byte) m.getElement(i);
        // add trailer
        addTrailerToOutput(msg, len+offset, m);
        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder("formatted message: ");
                    for (int i = 0; i<msg.length; i++) {
                        f.append(Integer.toHexString(0xFF&msg[i]));
                        f.append(" ");
                    }
                    log.debug(new String(f));
                }
                while(m.getRetries()>=0) {
                    if(portReadyToSend(controller)) {
                        ostream.write(msg);
                        ostream.flush();
                        log.debug("written, msg timeout: "+m.getTimeout()+" mSec");
                        break;
                    } else if(m.getRetries()>=0) {
                        if (log.isDebugEnabled()) {
                            StringBuilder b = new StringBuilder("Retry message: ");
                            b.append(m.toString());
                            b.append(" attempts remaining: ");
                            b.append(m.getRetries());
                            log.debug(new String(b));
                        }
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized(xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interupted");
                        }
                    } else log.warn("sendMessage: port not ready for data sending: " +java.util.Arrays.toString(msg));
                }
            } else {  // ostream is null
                // no stream connected
                connectionWarn();
            }
     } catch (Exception e) {
        	// TODO Currently there's no port recovery if an exception occurs
        	// must restart JMRI to clear xmtException.
        	xmtException = true;
            portWarn(e);
        }
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

