// SpecificTrafficController.java

package jmri.jmrix.powerline.cm11;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialNode;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialSensorManager;

/**
 * Converts Stream-based I/O to/from messages.  The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @version			$Revision: 1.5 $
 */
public class SpecificTrafficController extends SerialTrafficController {

	public SpecificTrafficController() {
        super();
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

        initNodes();
    }

    /**
     * Send a sequence of X10 messages
     * <p>
     * Makes them into the local messages and then queues in order
     */
    synchronized public void sendX10Sequence(X10Sequence s, SerialListener l) {
        s.reset();
        X10Sequence.Command c;
        while ( (c = s.getCommand() ) !=null) {
            SpecificMessage m;
            if (c.isAddress()) 
                m = SpecificMessage.getAddress(c.getHouseCode(), ((X10Sequence.Address)c).getAddress());
            else {
                X10Sequence.Function f = (X10Sequence.Function)c;
                if (f.getDimCount() > 0)
                    m = SpecificMessage.getFunctionDim(f.getHouseCode(), f.getFunction(), f.getDimCount());
                else
                    m = SpecificMessage.getFunction(f.getHouseCode(), f.getFunction());
            }
            sendSerialMessage(m, l);
        }
    }
    
    /**
     * This system provides 22 dim steps
     */
    public int maxX10DimStep() { return 22; }
    
    /**
     * Get a message of a specific length for filling in.
     */
    public SerialMessage getSerialMessage(int length) {
        return new SpecificMessage(length);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((SerialMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRReply newReply() { 
        SpecificReply reply = new SpecificReply();
        return reply;
    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    boolean expectLength = false;  // next byte is length of read
    boolean countingBytes = false; // counting remainingBytes into reply buffer
    int remainingBytes = 0;        // count of bytes _left_
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        // check if this byte is length
        if (expectLength) {
            expectLength = false;
            countingBytes = true;
            remainingBytes = msg.getElement(1)&0xF; // 0 was the read command; max 9, really
            if (logDebug) log.debug("Receive count set to "+remainingBytes);
            return false;
        }
        if (remainingBytes>0) {
            if (remainingBytes>8) {
                log.error("Invalid remainingBytes: "+remainingBytes);
                remainingBytes = 0;
                return true;
            }
            remainingBytes--;
            if (remainingBytes == 0) {
                countingBytes = false;
                return true;  // done
            }
            return false; // wait for one more
        }
        // check for data available
        //System.out.println(" got "+(msg.getElement(0)&0xFF));
        if ((msg.getElement(0)&0xFF)==Constants.POLL_REQ) {
            // get message
            SerialMessage m = new SpecificMessage(1);
            m.setElement(0, Constants.POLL_ACK);
            expectLength = true;  // next byte is length
            forwardToPort(m, null);
            return false;  // reply message will get data appended            
        }
        // check for request time
        if ((msg.getElement(0)&0xFF)==Constants.TIME_REQ) {
            SerialMessage m = SpecificMessage.setCM11Time(X10Sequence.encode(1));
            forwardToPort(m, null);
            return true;  // message done
        }
        // if the interlock is present, send it
        if (sendInterlock) {
        	if (logDebug) log.debug("Send interlock");
            sendInterlock = false;
            SerialMessage m = new SpecificMessage(1);
            m.setElement(0,0); // not really needed, but this is a slow protocol anyway
            forwardToPort(m, null);
            return false; // just leave in buffer
        }
        if (logDebug) log.debug("end of message: "+msg);
        return true;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificTrafficController.class.getName());
}


/* @(#)SpecificTrafficController.java */
