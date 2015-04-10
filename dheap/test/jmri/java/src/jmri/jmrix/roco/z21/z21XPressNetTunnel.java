// z21XPressNetTunnel.java
package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * Interface between z21 messages and an XPressNet stream.</P>
 * <P>
 * Parts of this code are derived from the
 * jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter class.</P>
 *
 * @author	Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class z21XPressNetTunnel implements z21Listener, XNetListener, Runnable {

    private jmri.jmrix.lenz.XNetStreamPortController xsc = null;
    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // feed pin
    private DataInputStream inpipe = null; // feed pout
    private z21SystemConnectionMemo _memo = null;
    private Thread sourceThread;

    /*
     * build a new XPressNet tunnel.
     */
    public z21XPressNetTunnel(z21SystemConnectionMemo memo) {
        // save the SystemConnectionMemo.
        _memo = memo;

        // configure input and output pipes to use for
        // the communication with the XPressNet implementation.
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: " + e.toString());
            return;
        }

        // start a thread to read from the input pipe.
        sourceThread = new Thread(this);
        sourceThread.start();

        // Then use those pipes as the input and output pipes for
        // a new XNetStreamPortController object.
        xsc = new z21XNetStreamPortController(pin, pout, "None");

        // configure the XPressNet connections properties.
        xsc.getSystemConnectionMemo().setSystemPrefix(_memo.getSystemPrefix() + "X");

        // register as a z21Listener, so we can receive replies
        _memo.getTrafficController().addz21Listener(this);

        // start the XPressNet configuration.
        xsc.configure();

    }

    public void run() { // start a new thread
        // this thread has one task.  It repeatedly reads from the input pipe
        // and writes modified data to the output pipe.  This is the heart
        // of the command station simulation.
        if (log.isDebugEnabled()) {
            log.debug("Simulator Thread Started");
        }
        for (;;) {
            XNetMessage m = readMessage();
            message(m);
        }
    }

    // readMessage reads one incoming message from the buffer
    // and sets outputBufferEmpty to true.
    private XNetMessage readMessage() {
        XNetMessage msg = null;
        try {
            msg = loadChars();
        } catch (java.io.IOException e) {
            // should do something meaningful here.

        }
        return (msg);
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @returns filled message
     * @throws IOException when presented by the input source.
     */
    private XNetMessage loadChars() throws java.io.IOException {
        int i;
        byte char1;
        char1 = readByteProtected(inpipe);
        int len = (char1 & 0x0f) + 2;  // opCode+Nbytes+ECC
        XNetMessage msg = new XNetMessage(len);
        msg.setElement(0, char1 & 0xFF);
        for (i = 1; i < len; i++) {
            char1 = readByteProtected(inpipe);
            msg.setElement(i, char1 & 0xFF);
        }
        return msg;
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a gnu.io port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     *
     */
    private byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        byte[] rcvBuffer = new byte[1];
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }

    // z21Listener interface methods.
    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message from the layout.
     *
     * @param msg The received z21 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void reply(z21Reply msg) {
        // This funcction forwards the payload of an XPressNet message 
        // tunneled in a z21 message and forwards it to the XPressNet
        // implementation's input stream.
        if (msg.isXPressNetTunnelMessage()) {
            XNetReply reply = msg.getXNetReply();
            log.debug("Z21 Reply {} forwarded to XPressNet implementation as {}",
                    msg, reply);
            for (int i = 0; i < reply.getNumDataElements(); i++) {
                try {
                    outpipe.writeByte(reply.getElement(i));
                } catch (java.io.IOException ioe) {
                    log.error("Error writing XPressNet Reply to XPressNet input stream.");
                }
            }
        }
    }

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message sent to the layout. Normally, this function will do
     * nothing.
     *
     * @param msg The received z21 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(z21Message msg) {
        // this function does nothing.
    }

    // XNetListener Interface methods.
    /**
     * Member function that will be invoked by a XNetInterface implementation to
     * forward a XNet message from the layout.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(XNetReply msg) {
        // we don't do anything with replies.
    }

    /**
     * Member function that will be invoked by a XNetInterface implementation to
     * forward a XNet message sent to the layout. Normally, this function will
     * do nothing.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(XNetMessage msg) {
        // when an XPressNet message shows up here, package it in a z21Message
        z21Message message = new z21Message(msg);
        if (log.isDebugEnabled()) {
            log.debug("XPressNet Message {} forwarded to z21 Interface as {}",
                    msg, message);
        }
        // and send the z21 message to the interface
        _memo.getTrafficController().sendz21Message(message, this);
    }

    /**
     * Member function invoked by an XNetInterface implementation to notify a
     * sender that an outgoing message timed out and was dropped from the queue.
     */
    public void notifyTimeout(XNetMessage msg) {
        // we don't do anything with timeouts.
    }

    static Logger log = LoggerFactory.getLogger(z21XPressNetTunnel.class.getName());

}

/* @(#)z21XPressNetTunnel.java */
