// MrcTrafficController.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import java.io.DataInputStream;
import static jmri.jmrix.AbstractMRTrafficController.AUTORETRYSTATE;
import static jmri.jmrix.AbstractMRTrafficController.IDLESTATE;
import static jmri.jmrix.AbstractMRTrafficController.WAITMSGREPLYSTATE;
import static jmri.jmrix.mrc.MrcPackets.locoSoleControlCode;

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
    final private static int throttlePacketLength = MrcPackets.getThrottlePacketLength();
    final private static int functionGroupLength = MrcPackets.getFunctionPacketLength();
    final private static int readCVLength = MrcPackets.getReadCVPacketLength();
    final private static int readCVReplyLength = MrcPackets.getReadCVPacketReplyLength();
    final private static int readDecoderAddressLength = MrcPackets.getReadDecoderAddressLength();
    final private static int writeCVPROGLength = MrcPackets.getWriteCVPROGPacketLength();
    final private static int writeCVPOMLength = MrcPackets.getWriteCVPOMPacketLength();
    final private static int setClockRatioLength = MrcPackets.getSetClockRatioPacketLength();
    final private static int setClockTimeLength = MrcPackets.getSetClockTimePacketLength();
    final private static int setClockAMPMLength = MrcPackets.getSetClockAmPmPacketLength();
    
    
    static final int MISSEDPOLL = 60;
    
    int expectedSize  = 0;
    
    /* this is also used to classify the packet and notify the xmt when it can send a packet out*/
    protected boolean endOfMessage(AbstractMRReply msg) {
        //We expect a minimum of two bytes for a reply.
        if(msg.getNumDataElements()<2) return false;
        if(msg.getNumDataElements()==expectedSize){
            if(msg.getElement(1)==0x01){
                if(msg.getElement(0)==cabAddress){
                    waiting = true;
                    //log.info("poll for us");
                    unsolicited = false;
                } else if(mCurrentState == WAITMSGREPLYSTATE){
                    log.info("we have missed our send message window");
                        //Hope by setting the currentstate to autoretry then the transmit will pick this up and add the message back to the queue.
                    synchronized (xmtRunnable) {
                        mCurrentState = MISSEDPOLL;
                    }
                }
            }
            if(mCurrentState == WAITMSGREPLYSTATE){
                if(msg.getElement(0)!=MrcPackets.badCmdRecievedCode && msg.getElement(0)!=MrcPackets.goodCmdRecievedCode){
                    log.info("reply not as expected correct format " + msg.toString());
                    ((MrcReply)msg).setPacketInError();
                    synchronized(xmtRunnable) {
                        log.info("Flag for retransmission");
                        mCurrentState = AUTORETRYSTATE;
                    }
                }
            }
            expectedSize=0;
            return true;
        } else if (msg.getNumDataElements()<expectedSize){
            return false;
        }
        waiting = false;
        //Poll message is put first as we need to react quickly to it.
        /*if(msg.getElement(0)==cabAddress && msg.getElement(1)==0x01){
            //Poll message for us
            ((MrcReply)msg).setPollMessage();
            unsolicited = false;
            if(msg.getNumDataElements()>=6){
                //triggers off the sending of a message
                 waiting = true;
                
                 //Any recieved reply will be unsolicited (ie reply to a message we send) until the next poll is recieved.
                log.info("poll for us");
                expectedSize=0;
                return true;
            }
            expectedSize = 6;
            return false;
        }*/
        if(/*msg.getElement(0)>0x00 &&*/ msg.getElement(0)<=0x20){
            if(msg.getElement(1)==0x01){
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

                /*if(msg.getNumDataElements()>=6){
                    msg.setUnsolicited();
                    ((MrcReply)msg).setPollMessage();
                    expectedSize=0;
                    return true;
                }*/
                msg.setUnsolicited();
                ((MrcReply)msg).setPollMessage();
                expectedSize = 6;
                return false;
            } else if (msg.getElement(1)==0x00){
                if(msg.getElement(0)==0x00) {
                    //Remove the polled "No Data" Packets and do not process them.
                    //removeErroredPackets = true;
                    //return true; //false;
                }
                if(msg.getNumDataElements()==4){
                    expectedSize=0;
                    return true;
                }
                expectedSize=4;
                return false;
            } /*else if (msg.getElement(0)==0x00 && msg.getElement(0)==0x00){
                log.info("poll msg");
                return true;
            } */else {
                log.info("Corrupt?");
                if(msg.getElement(0)==0x00 && msg.getElement(1)!=0x00){
                    //Our bytes are out of sync, so allow a three byte packet to get back into sync
                    log.info("Our Bytes appear to be out of sync " + msg.toString());
                    if(msg.getNumDataElements()==3) { 
                        //removeErroredPackets = true;
                        expectedSize=0;
                        return true;
                    }
                    expectedSize=3;
                    return false;
                } else {
                    ((MrcReply)msg).setPacketInError();
                    msg.setUnsolicited();
                    expectedSize=0;
                    return true;
                }
            }
        }
        
        /*if(unsolicited){
            msg.setUnsolicited();
        }*/
        //if(msg.getNumDataElements()==4){
           /* if(msg.getElement(0)==0x00 && msg.getElement(1)==0x00){
                expectedSize=4;
                return false;
            }*/
            if(mCurrentState == WAITMSGREPLYSTATE){
                if(msg.getElement(0)!=MrcPackets.badCmdRecievedCode && msg.getElement(0)!=MrcPackets.goodCmdRecievedCode){
                    log.info("reply not as expected correct format " + msg.toString());
                    ((MrcReply)msg).setPacketInError();
                    synchronized(xmtRunnable) {
                        log.info("Flag for retransmission");
                        mCurrentState = AUTORETRYSTATE;
                        expectedSize=0;
                        return true;
                    }
                }
                log.info("Exit here finished");
                expectedSize=4;
                return false;
            }
        //}

        /*if(mCurrentState == WAITMSGREPLYSTATE){
            log.info("Exit no finished");
            expectedSize=4;
            return false;
        }*/
        
        /*if(msg.getNumDataElements()==4){//only do this check the once at 4 bytes.
            //int num = msg.getNumDataElements();
            if(msg.getElement(0)!=msg.getElement(2)){
                expectedSize=0;
                return true;
            }
        }*/
        log.info("fall through to here");
        int requiredLength = 0;
        switch(msg.getElement(0)&0xff){
            case 0x00 : requiredLength = 4;
                        break;

            case MrcPackets.throttlePacketCmd : requiredLength = throttlePacketLength;
                                                break;
            case MrcPackets.functionGroup1PacketCmd : 
            case MrcPackets.functionGroup2PacketCmd : 
            case MrcPackets.functionGroup3PacketCmd : 
            case MrcPackets.functionGroup4PacketCmd : 
            case MrcPackets.functionGroup5PacketCmd : 
            case MrcPackets.functionGroup6PacketCmd : requiredLength = functionGroupLength;
                                                      break;
            case MrcPackets.readCVCmd :               requiredLength = readCVLength;
                                                      break;
            case MrcPackets.readDecoderAddressCmd :   requiredLength = readDecoderAddressLength;
                                                      break;
            case MrcPackets.writeCVPROGCmd :          requiredLength = writeCVPROGLength;
                                                      break;
            case MrcPackets.writeCVPOMCmd :           requiredLength = writeCVPOMLength;
                                                      break;
            case MrcPackets.setClockRatioCmd :        requiredLength = setClockRatioLength;
                                                      break;
            case MrcPackets.setClockTimeCmd :         requiredLength = setClockTimeLength;
                                                      break;
            case MrcPackets.setClockAmPmCmd :         requiredLength = setClockAMPMLength;
                                                      break;
            case MrcPackets.readCVHeaderReplyCode :     requiredLength = readCVReplyLength;
                                                      break;
            case MrcPackets.locoDblControlCode :      synchronized(xmtRunnable) {
                                                        mCurrentState = WAITMSGREPLYSTATE;
                                                    }
            case MrcPackets.badCmdRecievedCode :
            case MrcPackets.goodCmdRecievedCode :       
            case MrcPackets.locoSoleControlCode :     
                                                      requiredLength = 4;
                                                      break;
            default : requiredLength = 2; //Unknown
        }
        if(msg.getNumDataElements()>=requiredLength){expectedSize=0; return true;}
        //Need to double check this one. think it catches where things go out of sync
        log.info(""+requiredLength + " e0 " + msg.getElement(1) + " Not waiting " + !waiting);
        expectedSize = requiredLength;
        if(requiredLength==0 && msg.getElement(1)==0x00 && !waiting){
            expectedSize=0;
            return true;
        }

        //For some reason we see the odd two byte packet that doesn't match anything we know about, so at this stage ignore it, this could possibly be a corruption of the nodata bytes.
        /*if(msg.getNumDataElements()==2 && ((msg.getElement(0)&0xff)==0xF8 ||(msg.getElement(0)&0xff)==0xE0 || (msg.getElement(0)&0xff)==0xFE || (msg.getElement(0)&0xff)==0x80)){
            ((MrcReply)msg).setPacketInError();
            log.info("In Error");
            removeErroredPackets = true;
        }*/
        return false;
    }
    
    boolean removeErroredPackets = false;
    
    protected void loadCharstst(AbstractMRReply msg, DataInputStream istream)
                           throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            //if (log.isDebugEnabled()) log.debug("char: "+(char1&0xFF)+" i: "+i);
            // if there was a timeout, flush any char received and start over
            if(flushReceiveChars){
                log.warn("timeout flushes receive buffer: "+ msg.toString());
                msg.flush();
                i = 0;  // restart
                flushReceiveChars = false;
            }
            if (canReceive()) {
                msg.setElement(i, char1);
                if (endOfMessage(msg))
                    break;
                /*if(removeErroredPackets){
                    log.info("Flush");
                    msg.flush();
                    i = 0;
                    removeErroredPackets = false;
                }*/
            } else {
                i--; // flush char
                log.error("unsolicited character received: "+ Integer.toHexString(char1));
            }
        }
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
                            log.info("State Set and wait");
                        }
                        log.info("Message sent " + m.toString());
                        Runnable r = new XmtNotifier(m, mLastSender, this);
                        javax.swing.SwingUtilities.invokeLater(r);
                        // reply expected?
                        if (m.replyExpected()) {
                            log.info("Reply expected");
                            // wait for a reply, or eventually timeout
                            // @todo Need to see if this works or not
                            transmitWait(m.getTimeout(), WAITMSGREPLYSTATE, "transmitLoop interrupted");
                            checkReplyInDispatch();
                            if (mCurrentState == WAITMSGREPLYSTATE) {
                                log.info("Handle timeout");
                                handleTimeout(m,l);
                            } else if (mCurrentState == MISSEDPOLL && m.getRetries()>=0) {
                                 log.info("Message missed poll added back to front of queue: " + m.toString());
                                 msgQueue.addFirst(m);
                                 listenerQueue.addFirst(l);
                                 synchronized (xmtRunnable) {
                                       mCurrentState = IDLESTATE;
                                 }
                            } else if (mCurrentState == AUTORETRYSTATE && m.getRetries()>=0) {
                                 log.info("Auto Retry send message added back to queue: " + m.toString());
                                 m.setRetries(m.getRetries() - 1);
                                 msgQueue.addFirst(m);
                                 listenerQueue.addFirst(l);
                                 synchronized (xmtRunnable) {
                                       mCurrentState = IDLESTATE;
                                 }
                            } else {
                                log.info("Reset Time out");
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
                        //log.info("Send no data");
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

