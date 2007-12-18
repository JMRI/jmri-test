// EasyDccCommandStation.java

package jmri.jmrix.easydcc;

import jmri.CommandStation;

/**
 * EasyDcc implementation of the CommandStation interface.
 *
 * @author			Bob Jacobsen Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */
public class EasyDccCommandStation implements CommandStation {

	public EasyDccCommandStation() {}

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats Number of times to repeat the transmission,
     * capped at 9
     */
    public void sendPacket(byte[] packet, int repeats) {

        if (repeats>9) repeats = 9;
        if (repeats<0) {
            log.error("repeat count out of range: "+repeats);
            repeats = 1;
        }
        
		EasyDccMessage m = new EasyDccMessage(4+3*packet.length);
		int i = 0; // counter to make it easier to format the message
		m.setElement(i++, 'S');  // "S 02 " means send it twice
		m.setElement(i++, ' ');
		m.setElement(i++, '0');
		m.setElement(i++, '0'+repeats);
		
		for (int j=0; j<packet.length; j++) {
		    m.setElement(i++, ' ');
            String s = Integer.toHexString((int)packet[j]&0xFF).toUpperCase();
            if (s.length() == 1) {
                m.setElement(i++, '0');
                m.setElement(i++, s.charAt(0));
            } else {
                m.setElement(i++, s.charAt(0));
                m.setElement(i++, s.charAt(1));
            }
        }

		EasyDccTrafficController.instance().sendEasyDccMessage(m, null);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccCommandStation.class.getName());

}


/* @(#)EasyDccCommandStation.java */
