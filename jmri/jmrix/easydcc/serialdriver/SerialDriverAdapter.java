// SerialDriverAdapter.java

package jmri.jmrix.easydcc.serialdriver;

import jmri.jmrix.easydcc.EasyDccPortController;
import jmri.jmrix.easydcc.EasyDccProgrammer;
import jmri.jmrix.easydcc.EasyDccProgrammerManager;
import jmri.jmrix.easydcc.EasyDccTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * Implements SerialPortAdapter for the EasyDcc system.  This connects
 * an EasyDcc command station via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.27 $
 */
public class SerialDriverAdapter extends EasyDccPortController  implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;

    public String openPort(String portName, String appName)  {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

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

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an EasyDcc command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EasyDccTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setProgrammerManager(
                new EasyDccProgrammerManager(
                    new EasyDccProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.easydcc.EasyDccPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.easydcc.EasyDccTurnoutManager());

		// KSL 20040409 - Create an instance of EasyDccThrottleManager 
		jmri.InstanceManager.setThrottleManager(new jmri.jmrix.easydcc.EasyDccThrottleManager());

        // Create an instance of the consist manager.  Make sure this 
        // happens AFTER the programmer manager to override the default 
        // consist manager. 
        jmri.InstanceManager.setConsistManager(new jmri.jmrix.easydcc.EasyDccConsistManager());

        jmri.InstanceManager.setCommandStation(new jmri.jmrix.easydcc.EasyDccCommandStation());
        
        jmri.jmrix.easydcc.ActiveFlag.setActive();
    }

    // base class methods for the EasyDccPortController interface
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
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.EASYDCC;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}

