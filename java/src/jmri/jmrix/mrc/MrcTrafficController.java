// MrcTrafficController.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import static jmri.jmrix.AbstractMRTrafficController.AUTORETRYSTATE;
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
            String raw = "";
            for (int i=0;i<m.getNumDataElements(); i++) {
                if (i>0) raw+=" ";
                raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
            }
            log.info(raw);
            ((MrcMessage)m).setByte();
        }
        super.sendMessage(m, reply);
    }
    
    boolean unsolicited = true; //Used to detemine if the messages received are a result of a message we sent out or not.
    
    //We keep a copy of the lengths here to save on time on each request later.
    final private static int throttlePacketLength = MrcMessage.getThrottlePacketLength();
    final private static int functionGroupLength = MrcMessage.getFunctionPacketLength();
    final private static int readCVLength = MrcMessage.getReadCVPacketLength();
    final private static int readDecoderAddressLength = MrcMessage.getReadDecoderAddressLength();
    final private static int writeCVPROGLength = MrcMessage.getWriteCVPROGPacketLength();
    final private static int writeCVPOMLength = MrcMessage.getWriteCVPOMPacketLength();
    
    static final int MISSEDPOLL = 60;
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
                    mCurrentState = MISSEDPOLL;
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
        
        if(MrcReply.startsWith(msg, MrcMessage.throttlePacketHeader)){
            //Thottle speed packet from another handset, need to wait until all is recieved
            if(msg.getNumDataElements()>=throttlePacketLength){
                return true;
            }
            return false;
        }
        
        if(MrcReply.startsWith(msg, MrcReply.readCVHeaderReply)){
            //return of a read programming packet
            if(msg.getNumDataElements()>=8){
                return true;
            }
            return false;
        }
               
        if(mCurrentState == WAITMSGREPLYSTATE){
            //log.info("waiting for reply");
            if(msg.getNumDataElements()==4){
                return true;
            }
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.readDecoderAddress)){
            if(msg.getNumDataElements()>=readDecoderAddressLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.readCVHeader)){
            if(msg.isUnsolicited()) log.info("Good read marked as unsolicited");
            if(msg.getNumDataElements()>=readCVLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.writeCVPROGHeader)){
            if(msg.getNumDataElements()>=writeCVPROGLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.writeCVPOMHeader)){
            if(msg.getNumDataElements()>=writeCVPOMLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.functionGroup1PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.functionGroup2PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
            if(MrcReply.startsWith(msg, MrcMessage.functionGroup3PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.functionGroup4PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.functionGroup5PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
        if(MrcReply.startsWith(msg, MrcMessage.functionGroup6PacketHeader)){
            if(msg.getNumDataElements()>=functionGroupLength) return true;
            return false;
        }
        //Error occured during read
        if(msg.getNumDataElements()>=4){
            if(MrcReply.startsWith(msg, MrcReply.locoDblControl)){
                //A loco that is also under the control of another handset requires us to send the command a second time round, so hopefully this will trigger the xmt loop to re add the message to the front of the queue
                synchronized(xmtRunnable) {
                    mCurrentState = WAITMSGREPLYSTATE;
                }
                return true;
            }
            if(MrcReply.startsWith(msg, MrcReply.badCmdRecieved)){
                return true;
            }
            if(msg.getElement(0)==0x00 && msg.getElement(1)==0x00 && msg.getElement(2)==0x00 && msg.getElement(3)==0x00){
                ((MrcReply)msg).setPollMessage();
                return true;
            }

            if(msg.getElement(1)==0x00 && !waiting){
                return true;
            }
        }
        //For some reason we see the odd two byte packet that doesn't match anything we know about, so at this stage ignore it, this could possibly be a corruption of the nodata bytes.
        if(msg.getNumDataElements()==2 && ((msg.getElement(0)&0xff)==0xF8 ||(msg.getElement(0)&0xff)==0xE0 || (msg.getElement(0)&0xff)==0xFE || (msg.getElement(0)&0xff)==0x80)){
            return true;
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
                        //We do not know at what stage (or if) the last poll message was sent while retrieving this message, therefore we will set it wait until the next poll, otherwise the message could be sent out at the incorrect time.
                        waiting=false;
                    }
                }
            }
            while(waiting){
                if(m!=null){
                    try {
                        ostream.write(m.getByte());
                        ostream.flush();
                        synchronized(xmtRunnable) {
                            mCurrentState = WAITMSGREPLYSTATE;
                        }
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
                            } else if (mCurrentState == MISSEDPOLL && m.getRetries()>=0) {
                                 log.info("Message missed poll added back to front of queue: " + m.toString());
                                 msgQueue.addFirst(m);
                                 listenerQueue.addFirst(l);
                                 synchronized (xmtRunnable) {
                                       mCurrentState = IDLESTATE;
                                 }
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
                    } catch (java.io.IOException e) {
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

