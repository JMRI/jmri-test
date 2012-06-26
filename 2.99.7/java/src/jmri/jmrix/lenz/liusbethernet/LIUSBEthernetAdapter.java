// LIUSBEthernetAdapter.java

package jmri.jmrix.lenz.liusbethernet;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitilizationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetTrafficController;

import java.io.*;


/**
 * Provide access to XPressNet via a the Lenz LIUSBEthernet.
 * NOTES:  By default, the LIUSBEthernet has an IP address of
 * 192.168.0.200 and listens to port 5550.  
 * The LIUSBEtherenet disconnects both ports if there is 60 seconds of inactivity
 * on the port.
 *
 * @author			Paul Bender (C) 2011
 * @version			$Revision$
 */

public class LIUSBEthernetAdapter extends XNetNetworkPortController {

	static final int COMMUNICATION_TCP_PORT= 5550;
	static final String DEFAULT_IP_ADDRESS = "192.168.0.200";

        private javax.swing.Timer keepAliveTimer; // Timer used to periodically
                                                  // send a message to both
                                                  // ports to keep the ports 
                                                  // open
        private static final int keepAliveTimeoutValue = 30000; // Interval 
                                                            // to send a message
                                                            // Must be < 60s.

	private DataOutputStream pout=null; // for output to other classes
    	private DataInputStream pin = null; // for input from other classes

        public LIUSBEthernetAdapter(){
	    super();
            if(log.isDebugEnabled()) log.debug("Constructor Called");
            setHostName(DEFAULT_IP_ADDRESS);
            setPort(COMMUNICATION_TCP_PORT);
        }


        @Override
    public void connect() throws Exception {
        super.connect();
        if(log.isDebugEnabled()) log.debug("openPort called");
        // open the port in XPressNet mode
        try {
            pout=getOutputStream();
            PipedOutputStream tempPipeO=new PipedOutputStream();
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        }
        catch (java.io.IOException e) {
              log.error("init (pipe): Exception: "+e.toString());
        }
        catch (Exception ex) {
            log.error("init (connect): Exception: "+ex.toString());
        }
        keepAliveTimer();
        //return null; // normal operation
    }

        /**
         * Can the port accept additional characters?
         * return true if the port is opened.
         */
        @Override
        public boolean okToSend() {
          return status();
        }
   
        @Override
    public boolean status() {return (pout!=null && pin!=null);}
    
	/**
	 * set up all of the other objects to operate with a LIUSB Ethernet 
	 * interface
	 */
        @Override
	public void configure() {
            if(log.isDebugEnabled()) log.debug("configure called");
            // connect to a packetizing traffic controller
            XNetTrafficController packets = (new LIUSBEthernetXNetPacketizer(new LenzCommandStation()));
            packets.connectPort(this);

       	    // start operation
            // packets.startThreads();
            adaptermemo.setXNetTrafficController(packets);
 
            new XNetInitilizationManager(adaptermemo);

            jmri.jmrix.lenz.ActiveFlag.setActive();
	
       }

	/**
	 * Local method to do specific configuration
	 */

        @Deprecated
	static public LIUSBEthernetAdapter instance() {
		if (mInstance == null) mInstance = new LIUSBEthernetAdapter();
		return mInstance;
	}
	volatile static LIUSBEthernetAdapter mInstance = null;

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if(keepAliveTimer==null) {
            keepAliveTimer = new javax.swing.Timer(keepAliveTimeoutValue,new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        // If the timer times out, send a request for status
                        adaptermemo.getXNetTrafficController()
                                   .sendXNetMessage(
                                   jmri.jmrix.lenz.XNetMessage.getCSStatusRequestMessage(),
                                   null);
                    }
                });
        }
        keepAliveTimer.stop();
        keepAliveTimer.setInitialDelay(keepAliveTimeoutValue);
        keepAliveTimer.setRepeats(true);
        keepAliveTimer.start();
    }

    String manufacturerName = jmri.jmrix.DCCManufacturerList.LENZ;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBEthernetAdapter.class.getName());

}
