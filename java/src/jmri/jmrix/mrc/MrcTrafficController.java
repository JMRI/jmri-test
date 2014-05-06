// MrcTrafficController.java

package jmri.jmrix.mrc;

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
        setAllowUnexpectedReply(true);
    }

    public void setCabNumber(int x){
        cabAddress = x;
    }
    
    int cabAddress = 0;
    
    public int getCabNumber(){
        return cabAddress;
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

    protected AbstractMRReply newReply() { return new MrcReply(); }
    
    /**
     * instance use of the traffic controller is no longer used for multiple connections
     */
	@Deprecated
    public void setInstance(){}

//Test to see if we can use the standard sendMessage without causing too much delay.
    synchronized protected void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
        //We only need to send the get attention once when we send a fresh command.
        if(m!=null){
            log.info("Added " + m.toString());
            ((MrcMessage)m).setByte();
        }
        super.sendMessage(m, reply);
        //msgQueue.addLast(m);
        //listenerQueue.addLast(reply);
    }
    
    boolean unsolicited = true; //Used to detemine if the messages received are a result of a message we sent out or not.
    
    /* this is also used to classify the packet and notify the xmt when it can send a packet out*/
    protected boolean endOfMessage(AbstractMRReply msg) {
        //We expect a minimum of two bytes for a reply.
        if(msg.getNumDataElements()<2) return false;
        waiting = false;
        //Poll message is put first as we need to react quickly to it.
        if(msg.getElement(0)==cabAddress && msg.getElement(1)==0x01){
            //Poll message for us
            if(msg.getNumDataElements()>=6){
                //triggers off the sending of a message
                ((MrcReply)msg).setPollMessage();
                waiting = true;
                unsolicited = false; //Any recieved reply will be unsolicited (ie reply to a message we send) until the next poll is recieved.
                return true;
            }
            return false;
        }
        if(msg.getElement(0)<=0x20 && msg.getElement(1)==0x01){
            //Poll Message for cab addresses <31
            unsolicited = true;
            //Will have to see how this works out, if we are waiting for a reply and we recieve a poll message for another handset
            //then we will have to resend the command.
            if(mCurrentState == WAITMSGREPLYSTATE){
                log.info("we have missed our send message window");
                //Hope by setting the currentstate to autoretry then the transmit will pick this up and add the message back to the queue.
                synchronized (xmtRunnable) {
                    mCurrentState = AUTORETRYSTATE;
                }
            }
            
            if(msg.getNumDataElements()>=6){
                msg.setUnsolicited();
                ((MrcReply)msg).setPollMessage();
                return true;
            }
            return false;
        }
        
        if(unsolicited){
            msg.setUnsolicited();
        }
        
        if(msg.getElement(0)==0x25 && msg.getElement(1) ==0x00 && msg.getElement(2)==0x25 && msg.getElement(3)==0x00){
            //Thottle speed packet from another handset, need to wait until all is recieved
            if(msg.getNumDataElements()>=14){
                return true;
            }
            return false;
        }
        
        if(msg.getElement(0)==0x66 && msg.getElement(1) ==0x00 && msg.getElement(2)==0x66 && msg.getElement(3)==0x00){
            //return of a programming packet
            if(msg.getNumDataElements()>=8){
                return true;
            }
            return false;
        }
        
        if(mCurrentState == WAITMSGREPLYSTATE){
            log.info("waiting for reply");
            if(msg.getNumDataElements()==4){
                return true;
            }
            return false;
        }
        if(msg.getElement(0) ==0x42 && msg.getElement(2)==0x42){
            if(msg.getNumDataElements()>=8) return true;
            return false;
        }
        if(msg.getElement(0) ==0x43 && msg.getElement(2)==0x43){
            if(msg.getNumDataElements()>=10) return true;
            return false;
        }
        //Error occured during read
        if(msg.getNumDataElements()>=4){
            if(msg.getElement(0)==0xee && msg.getElement(2)==0xee){
                return true;
            }

            if(msg.getElement(1)==0x00 && !waiting){
                return true;
            }
        }
        return false;
    }
    
    boolean waiting = false; //Trigger to say that we can send a message
    
    byte[] noData = new byte[]{0x00,0x00,0x00,0x00};
    
    protected void transmitLoop() {
        MrcMessage m = null;
        AbstractMRListener l = null;
        while(!connectionError) {
            //Get the message we are ready to send sorted so that when we are polled we can send it off straight away.
            if(m==null){
                synchronized(selfLock) {
                    if (msgQueue.size()!=0) {
                        m = (MrcMessage)msgQueue.getFirst();
                        l = listenerQueue.getFirst();
                        mLastSender = l;
                        listenerQueue.removeFirst();
                        msgQueue.removeFirst();
                    }
                }
            }
            while(waiting){
                if(m!=null){
                    try {
                        synchronized(selfLock) {
                            mCurrentState = WAITMSGREPLYSTATE;
                        }
                        //log.info(""+m.getByte());
                        ostream.write(m.getByte());
                        ostream.flush();

                        Runnable r = new XmtNotifier(m, mLastSender, this);
                        javax.swing.SwingUtilities.invokeLater(r);
                        // reply expected?
                        if (m.replyExpected()) {
                            // wait for a reply, or eventually timeout
                            // @todo Need to see if this works or not
                            transmitWait(m.getTimeout(), WAITMSGREPLYSTATE, "transmitLoop interrupted");
                            checkReplyInDispatch();
                            if (mCurrentState == WAITMSGREPLYSTATE) {
                                handleTimeout(m,l);
                            } else if (mCurrentState == AUTORETRYSTATE && m.getRetries()>=0) {
                                 log.info("Message added back to queue: " + m.toString());
                                 m.setRetries(m.getRetries() - 1);
                                 msgQueue.addFirst(m);
                                 listenerQueue.addFirst(l);
                                 synchronized (xmtRunnable) {
                                       mCurrentState = IDLESTATE;
                                 }
                            } else {
                                resetTimeout(m);
                            }
                        }
                        m = null;
                    } catch (Exception e) {
                        log.error("Unable to send");
                    }
                } else { //Nothing to send so tell master.
                    try {
                        ostream.write(noData);
                        ostream.flush();
                    } catch (Exception e) {
                        log.error("Unable to send");
                    }
                    mCurrentState = IDLESTATE;
                }
                waiting = false;
            }
            
        }

    }
       
    /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        //if (! m.isBinary()){
            //msg[offset] = 0x00;
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

