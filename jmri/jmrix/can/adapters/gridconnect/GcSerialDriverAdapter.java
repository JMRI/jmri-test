// SerialDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.adapters.gridconnect.GcPortController;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

/**
 * Implements SerialPortAdapter for the GridConnect protocol.
 * <P>
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2002
 * @author			Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.9 $
 */
public class GcSerialDriverAdapter extends GcPortController  implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;


    public String openPort(String portName, String appName)  {
        String [] baudRates = validBaudRates();
        int [] baudValues = validBaudValues();
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = baudValues[0];  // default, but also defaulted in the initial value of selectedSpeed
                for (int i = 0; i<baudRates.length; i++ )
                    if (baudRates[i].equals(mBaudRate))
                        baud = baudValues[i];
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            int count = serialStream.available();
            log.debug("input stream shows "+count+" bytes available");
            while ( count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud, sees "
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
						+" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }

            opened = true;

        } catch (javax.comm.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with a CAN RS adapter
     * connected to this port
     */
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        GcTrafficController.instance();
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        GcTrafficController.instance().connectPort(this);

        // do central protocol-specific configuration    
        jmri.jmrix.can.ConfigurationManager.configure(mOpt1);

    }
    
    // base class methods for the PortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        }
        catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    
    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return validSpeedValues;
    }
    
    protected String [] validSpeeds = new String[]{"57,600", "115,200", "250,000", "333,333", "460,800", "500,000"};
    protected int [] validSpeedValues = new int[]{57600, 115200, 250000, 333333, 460800, 500000};
    
    /**
     * Option 1 is CAN-based protocol
     */
    public String[] validOption1() { return jmri.jmrix.can.ConfigurationManager.getSystemOptions(); }
    
    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return "Connection Protocol"; }

    /**
     * Set the CAN protocol option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

    /**
     * Option 2 
     */
    public String[] validOption2() { return new String[]{""}; }
    public String option2Name() { return "Option 2"; }
    public void configureOption2(String value) { mOpt2 = value; }
    protected String mOpt2 = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[0];
        return mOpt2;
    }
   
    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GcSerialDriverAdapter.class.getName());

}
