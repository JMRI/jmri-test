package jmri.jmrix.mrc;

import java.util.Date;

/**
 * MrcTrafficListener provides the call-back interface for notification when a
 * new Mrc message arrives from the layout.
 *<P>
 * In contrast to MrcListener this interface defines separate methods to notify
 *  transmitted or received mrc messages. Further, the actual time stamp when a 
 *  message was passed to the hardware interface or was first seen is provided. 
 *  As most functions in JMRI do not depend on the actual time a message was sent
 *  or received, this interface may help in debugging communication. 
 *  Currently the Mrc Monitor is the only user of this interface. 
 *
 *
 * @author			Matthias Keil  Copyright (C) 2013
 * @version 		$Revision: $
 *
 */
public interface MrcTrafficListener {

	public final static int MRC_TRAFFIC_NONE  = 0x00;
	public final static int MRC_TRAFFIC_RX    = 0x01;
	public final static int MRC_TRAFFIC_TX    = 0x02;
	public final static int MRC_TRAFFIC_ALL   = 0x03;

	
	public void notifyXmit(Date timestamp, MrcMessage m);
	public void notifyRcv(Date timestamp, MrcMessage m);
    public void notifyFailedXmit(Date timestamp, MrcMessage m);
}
