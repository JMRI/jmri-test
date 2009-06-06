/**
 * XnTcpXNetPacketizer.java
 */

package jmri.jmrix.lenz.xntcp;

import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.lenz.xntcp.XnTcpAdapter;

/**
 * This is an extension of the XNetPacketizer to handle the device 
 * specific requirements of the XnTcp.
 * <P>
 * In particular, XnTcpXNetPacketizer counts the number of commands received.
 * @author		Giorgio Terdina Copyright (C) 2008, based on LIUSB packetizer by Paul Bender, Copyright (C) 2005
 * @version 	$Revision: 1.3 $
 *
 */
public class XnTcpXNetPacketizer extends XNetPacketizer {

	public XnTcpXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        	super(pCommandStation);
		log.debug("Loading XnTcp Extension to XNetPacketizer");
    	}

/**
     * Get characters from the input source, and fill a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg message to fill
     * @param istream character source. 
     * @throws IOException when presented by the input source.
     */
	 
   protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
		int i, char1;
		i = 0;
		try {
			// Make sure we don't overwrite output buffer
			while (i < msg.maxSize()) {
				// Read a byte
				char1 = istream.read();
				// Was the communication closed by the XpressNet/Tcp interface, or lost?
				if(char1 < 0) {
					// We cannot communicate anymore!
					XnTcpAdapter.instance().XnTcpError();
					throw new java.io.EOFException("Lost communication with XnTcp interface");
				}
				// Store the byte.
				msg.setElement(i++, (byte)char1 &0xFF);
				log.debug("XnTcpNetPacketizer: received " + Integer.toHexString(char1 & 0xff));
				// If the XpressNet packet is completed, exit the loop
				if (endOfMessage(msg)) {
					break;
				}
			}
			// Packet received
			// Assume that the last packet sent was acknowledged, either by the Command Station,
			// either by the interface itself.
			XnTcpAdapter.instance().XnTcpSetPendingPackets(-1);
			log.debug("XnTcpNetPacketizer: received end of packet");
		}
		catch (java.io.IOException ex) {
			XnTcpAdapter.instance().XnTcpError();
			throw ex;
		}

    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XnTcpXNetPacketizer.class.getName());
}

/* @(#)XnTcpXNetPacketizer.java */

