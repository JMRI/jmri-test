// MrcPacketizer.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Calendar;
import java.util.Arrays;

/**
 * Converts Stream-based I/O to/from Mrc messages.  The "MrcInterface"
 * side sends/receives MrcMessage objects.  The connection to
 * a MrcPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.
 *<P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread.  Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects.  Those are internal
 * classes defined here. The thread priorities are:
 *<P><UL>
 *<LI>  RcvHandler - at highest available priority
 *<LI>  XmtHandler - down one, which is assumed to be above the GUI
 *<LI>  (everything else)
 *</UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 23315 $
 *
 */
public class MrcPacketizer extends MrcTrafficController {

    final static boolean fulldebug = false;
  
  	boolean debug = false;
  	
  	/**
  	 * true if the external hardware is not echoing messages,
  	 * so we must
  	 */
  	protected boolean echo = true;  // echo messages here, instead of in hardware
  	
    public MrcPacketizer() {
    	debug = log.isDebugEnabled();
   	}


    // The methods to implement the MrcInterface


    public boolean status() { return (ostream != null & istream != null);
    }


    /**
     * Synchronized list used as a transmit queue.
     * <P>
     * This is public to allow access from the internal class(es) when compiling with Java 1.1
     */
    public LinkedList<byte[]> xmtList = new LinkedList<byte[]>();

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    protected Runnable xmtHandler ;

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    protected Runnable rcvHandler ;

    /**
     * Forward a preformatted MrcMessage to the actual interface.
     *
     * Checksum is computed and overwritten here, then the message
     * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
     */
    public void sendMrcMessage(MrcMessage m) {
        // update statistics
        transmittedMsgCount++;
        
        // set the error correcting code byte(s) before transmittal
        //m.setParity();

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        byte msg[] = new byte[len];
        for (int i=0; i< len; i++)
            msg[i] = (byte) m.getElement(i);

        if (debug) log.debug("queue Mrc packet: "+m.toString());
        // in an atomic operation, queue the request and wake the xmit thread
        try {
            synchronized(xmtHandler) {
                xmtList.addLast(msg);
                xmtHandler.notify();
            } 
        }
        catch (Exception e) {
            log.warn("passing to xmit: unexpected exception: "+e);
        }
    }

    /**
     * Implement abstract method to signal if there's a backlog
     * of information waiting to be sent.
     * @return true if busy, false if nothing waiting to send
     */
    public boolean isXmtBusy() {
        if (controller == null) return false;
        
        return (!controller.okToSend());
    }

    // methods to connect/disconnect to a source of data in a MrcPortController
    // This is public to allow access from the internal class(es) when compiling with Java 1.1
    public MrcPortController controller = null;

    /**
     * Make connection to existing MrcPortController object.
     * @param p Port controller for connected. Save this for a later
     *              disconnect call
     */
    public void connectPort(MrcPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        controller = p;
    }

    /**
     * Break connection to existing MrcPortController object. Once broken,
     * attempts to send via "message" member will fail.
     * @param p previously connected port
     */
    public void disconnectPort(MrcPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected MrcPortController");
        controller = null;
    }

    // data members to hold the streams. These are public so the inner classes defined here
    // can access whem with a Java 1.1 compiler
    public DataInputStream istream = null;
    public OutputStream ostream = null;

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

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a gnu.io port is set to have a 
     * receive timeout (via the enableReceiveTimeout() method),
     * some will return zero bytes or an EOFException at the end of the timeout.
     * In that case, the read should be repeated to get the next real character.
     * 
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars>0) return rcvBuffer[0];
        }
    }
    // Defined this way to reduce new object creation
    private byte[] rcvBuffer = new byte[1];
    boolean xmtWindow = false;
    
    /**
     * Captive class to handle incoming characters.  This is a permanent loop,
     * looking for input messages in character form on the
     * stream connected to the MrcPortController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {
        /**
         * Remember the MrcPacketizer object
         */
        MrcPacketizer trafficController;
        public RcvHandler(MrcPacketizer lt) {
            trafficController = lt;
        }

        @SuppressWarnings("null")
		public void run() {
            int firstByte=0x00;
            int secondByte=0x00;
            try {
                firstByte = readByteProtected(istream)&0xFF;
                secondByte = readByteProtected(istream)&0xFF;
            } catch (java.io.IOException e) {
            }
            while (true) {   // loop permanently, program close will exit
                mainloop:
                try {
                    // start by looking for command -  skip if bit not set
                    while ( secondByte !=0x00 && secondByte != 0x01 )  {
                       log.info("In here " + firstByte + " " + secondByte);
                        firstByte = secondByte;
                        secondByte = readByteProtected(istream)&0xFF;
                        if (debug) log.debug("Skipping: "+Integer.toHexString(firstByte) + " " + Integer.toHexString(secondByte));
                    }
                    // here opCode is OK. Create output message
                    if (fulldebug) log.debug(" (RcvHandler) Start message with message: "+ Integer.toHexString(firstByte) + " " + Integer.toHexString(secondByte));
                    MrcMessage msg = null;
                    boolean pollForUs = false;
                    while (msg == null) {
                        // Capture 2nd byte, always present
                        //int byte2 = readByteProtected(istream)&0xFF;
                        //if (fulldebug) log.debug("Byte2: "+Integer.toHexString(byte2));
                        // Decide length
                        
                        if(secondByte==0x01){
                            
                            msg = new MrcMessage(6);
                            msg.setPollMessage();
                            if(firstByte==cabAddress){
                                //log.debug("Poll Message for us");
                                pollForUs = true;
                            } else if (mCurrentState == WAITFORCMDRECEIVED) {
                                log.debug("Missed our poll slot");
                                //synchronized(xmtHandler) {
                                mCurrentState = MISSEDPOLL;
                                //}
                            }
                            if(firstByte==0x00){
                               msg.setClockPacket();
                            }
                        } else {
                            switch(firstByte) {
                                case 0:     /* 2 No Data Poll */
                                    msg = new MrcMessage(4);
                                    msg.setNoDataReply();
                                    break;

                                case MrcPackets.throttlePacketCmd : msg = new MrcMessage(throttlePacketLength);
                                                                    break;
                                //$FALL-THROUGH$
                                case MrcPackets.functionGroup1PacketCmd : 
                                case MrcPackets.functionGroup2PacketCmd : 
                                case MrcPackets.functionGroup3PacketCmd : 
                                case MrcPackets.functionGroup4PacketCmd : 
                                case MrcPackets.functionGroup5PacketCmd : 
                                case MrcPackets.functionGroup6PacketCmd : msg = new MrcMessage(functionGroupLength);
                                                                          break;
                                case MrcPackets.readCVCmd :               msg = new MrcMessage(readCVLength);
                                                                          break;
                                case MrcPackets.readDecoderAddressCmd :   msg = new MrcMessage(readDecoderAddressLength);
                                                                          break;
                                case MrcPackets.writeCVPROGCmd :          msg = new MrcMessage(writeCVPROGLength);
                                                                          break;
                                case MrcPackets.writeCVPOMCmd :           msg = new MrcMessage(writeCVPOMLength);
                                                                          break;
                                case MrcPackets.setClockRatioCmd :        msg = new MrcMessage(setClockRatioLength);
                                                                          break;
                                case MrcPackets.setClockTimeCmd :         msg = new MrcMessage(setClockTimeLength);
                                                                          break;
                                case MrcPackets.setClockAmPmCmd :         msg = new MrcMessage(setClockAMPMLength);
                                                                          break;
                                case MrcPackets.readCVHeaderReplyCode :   msg = new MrcMessage(readCVReplyLength);
                                                                          break;
                                case MrcPackets.locoDblControlCode :    //synchronized(xmtHandler) {
                                                                        mCurrentState = DOUBLELOCOCONTROL;
                                                                        msg = new MrcMessage(4);
                                                                        break;
                                                                        //}
                                //$FALL-THROUGH$
                                case MrcPackets.locoSoleControlCode :    mCurrentState =IDLESTATE;
                                case MrcPackets.goodCmdRecievedCode : 
                                case MrcPackets.badCmdRecievedCode  :    mCurrentState =IDLESTATE;
                                                                         msg = new MrcMessage(4);
                                                                         break;
                                default : msg = new MrcMessage(2); //Unknown
                            }
                        }
                        
                        msg.setElement(0, firstByte);
                        msg.setElement(1, secondByte);
                        int b = readByteProtected(istream)&0xFF;
                        if(!msg.isClockPacket() && b!=firstByte){
                            log.info("incorrect pattern " + firstByte + " " + secondByte + " " + b);
                            msg=null;
                            firstByte = b;
                            secondByte=readByteProtected(istream)&0xFF;
                            break mainloop;
                        } else {
                           msg.setElement(2, b);

                            // message exists, now fill it
                            int len = msg.getNumDataElements();
                            if (fulldebug) log.debug("len: "+len);
                            for (int i = 3; i < len; i++)  {
                                // check for message-blocking error
                                b = readByteProtected(istream)&0xFF;
                                msg.setElement(i, b);
                                if (fulldebug) log.debug("char "+i+" is: "+Integer.toHexString(b));

                            }
                            if(pollForUs){
                                xmtWindow = true;//Trigger sending of packet.
                            }
                        }
                    }
                    if(msg!=null){
                        // check parity
                        if (msg.isPollMessage() && msg.getNumDataElements()>6 && !msg.validCheckSum()) {
                            log.warn("Ignore Mrc packet with bad checksum: "+msg.toString());
                            throw new MrcMessageException();
                        }
                        // message is complete, dispatch it !!
                        {
                            if (fulldebug) log.debug("queue message for notification: "+msg.toString());
                            final MrcMessage thisMsg = msg;
                            final MrcPacketizer thisTC = trafficController;
                            // return a notification via the queue to ensure end
                            Runnable r = new Runnable() {
                                    MrcMessage msgForLater = thisMsg;
                                    MrcPacketizer myTC = thisTC;
                                    public void run() {
                                        myTC.notifyRcv(new Date(), msgForLater);
                                    }
                                };
                            javax.swing.SwingUtilities.invokeLater(r);
                        }
                        //Set up the next read
                        firstByte = readByteProtected(istream)&0xFF;
                        secondByte = readByteProtected(istream)&0xFF;
                    }
                    // done with this one
            	}
                catch (MrcMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected MrcMessageException: "+e);
                }
                catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    if (fulldebug) log.debug("EOFException, is Mrc serial I/O using timeouts?");
                }
                catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    if (debug) log.debug("IOException, should only happen with HexFIle: "+e);
                    log.info("End of file");
                    disconnectPort(controller);
                    return;
                }
                // normally, we don't catch the unnamed Exception, but in this
                // permanently running loop it seems wise.
                catch (Exception e) {
                    log.warn("run: unexpected Exception: "+e);
                }
            } // end of permanent loop
        }
    }
    

    final static int IDLESTATE = 0x00;
    final static int WAITFORCMDRECEIVED = 0x01;
    final static int DOUBLELOCOCONTROL = 0x02;
    final static int MISSEDPOLL = 0x04;
    int mCurrentState=IDLESTATE;

    /**
     * Captive class to handle transmission
     */
    class XmtHandler implements Runnable {
        public void run() {
            boolean debug = log.isDebugEnabled();
            byte msg[] = null;
            while (true) {   // loop permanently
                // any input?
                try {
                    if(xmtList.size()!=0 && msg==null){ 
                        // get content; failure is a NoSuchElementException
                        if (fulldebug) log.debug("check for input");

                        synchronized (this) {
                            msg = xmtList.removeFirst();
                            xmtWindow = false;
                        }
                        log.info("grabbed a message");
                    }
                    if (ostream != null && msg!=null) {
                        if (!controller.okToSend()) log.debug("Mrc port not ready to receive");
                        //if (debug) log.debug("start write to stream  : "+jmri.util.StringUtil.hexStringFromBytes(msg));
                        if(xmtWindow){
                            // input - now send
                            try {
                                ostream.write(msg);
                                messageTransmited(msg);
                                mCurrentState = WAITFORCMDRECEIVED;
                                ostream.flush();
                                xmtWindow = false;
                                //mCurrentState = WAITFORCMDRECEIVED;
                                //synchronized(this) {
                                log.info("State Set and wait");
                                //}
                                if (fulldebug) log.debug("end write to stream: "+jmri.util.StringUtil.hexStringFromBytes(msg));
                                //messageTransmited(msg);
                                transmitWait(150, WAITFORCMDRECEIVED, "transmitLoop interrupted");
                                if(mCurrentState == WAITFORCMDRECEIVED){
                                    log.info("Timed out");
                                } else if (mCurrentState == MISSEDPOLL /*&& m.getRetries()>=0*/) {
                                    log.info("Missed add to front");
                                    xmtList.addFirst(msg);
                                     //synchronized (this) {
                                           mCurrentState = IDLESTATE;
                                     //}
                                } else if (mCurrentState == DOUBLELOCOCONTROL/* && m.getRetries()>=0*/) {
                                     log.info("Auto Retry send message added back to queue: " + Arrays.toString(msg));
                                     //m.setRetries(m.getRetries() - 1);
                                     xmtList.addFirst(msg);
                                     //synchronized (this) {
                                           mCurrentState = IDLESTATE;
                                     //}
                                }
                                msg = null;
                            }
                            catch (java.io.IOException e) {
                                log.warn("sendMrcMessage: IOException: "+e.toString());
                            }
                        }
                    } else {
                        // no stream connected
                        //log.warn("sendMrcMessage: no connection established");
                    }

                }
                catch (NoSuchElementException e) {
                    // message queue was empty, wait for input
                    if (fulldebug) log.debug("start wait");

                    new jmri.util.WaitHandler(this);  // handle synchronization, spurious wake, interruption

                    if (fulldebug) log.debug("end wait");
                }
            }
        }
    }
    
    protected void transmitWait(int waitTime, int state, String InterruptMessage){
		// wait() can have spurious wakeup!
    	// so we protect by making sure the entire timeout time is used
    	long currentTime = Calendar.getInstance().getTimeInMillis();
		long endTime = currentTime + waitTime;
		while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())){
			long wait = endTime - currentTime;
			try {
				synchronized(xmtHandler) { 
					// Do not wait if the current state has changed since we
					// last set it.
					if (mCurrentState != state)
						return;
					xmtHandler.wait(wait); // rcvr normally ends this w state change
				}
			} catch (InterruptedException e) { 
				Thread.currentThread().interrupt(); // retain if needed later
				log.error(InterruptMessage); 
			}
		}
		log.debug("Timeout in transmitWait, mCurrentState:" + mCurrentState);
    }

    /**
     * When a message is finally transmitted, forward it
     * to listeners if echoing is needed
     *
     */
     protected void messageTransmited(byte[] msg) {
        if (debug) log.debug("message transmitted");
        if (!echo) return;
        // message is queued for transmit, echo it when needed
        // return a notification via the queue to ensure end
        javax.swing.SwingUtilities.invokeLater(new Echo(this, new Date(), new MrcMessage(msg)));
    }
    
    static class Echo implements Runnable {
        Echo(MrcPacketizer t, Date _timestamp, MrcMessage m) {
            myTc = t;
            msgForLater = m;
            timestamp = _timestamp;
        }
        MrcMessage msgForLater;
        MrcPacketizer myTc;
        Date timestamp;
       
        public void run() {
            myTc.notifyXmit(timestamp, msgForLater);
        }
    }
    
    /**
     * Invoked at startup to start the threads needed here.
     */
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = "+priority+
                  " max available = "+Thread.MAX_PRIORITY+
                  " default = "+Thread.NORM_PRIORITY+
                  " min available = "+Thread.MIN_PRIORITY);

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY-1>priority ? Thread.MAX_PRIORITY-1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        if( xmtHandler == null )
          xmtHandler = new XmtHandler();
        Thread xmtThread = new Thread(xmtHandler, "Mrc transmit handler");
        log.debug("Xmt thread starts at priority "+xmtpriority);
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY-1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if( rcvHandler == null )
          rcvHandler = new RcvHandler(this) ;
        Thread rcvThread = new Thread(rcvHandler, "Mrc receive handler " + Thread.MAX_PRIORITY);
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

    }

    static Logger log = LoggerFactory.getLogger(MrcPacketizer.class.getName());
}

/* @(#)MrcPacketizer.java */
