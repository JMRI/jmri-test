// SerialDriverAdapter.java

package jmri.jmrix.sprog.serialdriver;

import jmri.jmrix.AbstractSerialPortController;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogProgrammer;
import jmri.jmrix.sprog.SprogProgrammerManager;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects
 * an Sprog command station via a serial com port.
 * Also used for the USB SPROG, which appears to the computer as a
 * serial port.
  * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 * 
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge. Comments tagged with
 * "AJB" indicate changes or observations by me
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.29 $
 */
public class SerialDriverAdapter extends AbstractSerialPortController implements jmri.jmrix.SerialPortAdapter {

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
            //AJB: Removed Jan 2010 - 
            //Setting flow control mode to zero kills comms - SPROG doesn't send data
            //Concern is that will disabling this affect other SPROGs? Serial ones? 
            //activeSerialPort.setFlowControlMode(0);

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
            
            //AJB - add Sprog Traffic Controller as event listener
            try {
                activeSerialPort.addEventListener(SprogTrafficController.instance());
             } catch (TooManyListenersException e) {}
             
             // AJB - activate the DATA_AVAILABLE notifier
             activeSerialPort.notifyOnDataAvailable(true);
             
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

    public void setHandshake (int mode) {
      try {
        activeSerialPort.setFlowControlMode(mode);
      } catch (Exception ex) {
            log.error("Unexpected exception while setting COM port handshake mode trace follows: "+ex);
            ex.printStackTrace();
      }

    }

 
    // base class methods for the SprogPortController interface
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

    //public boolean status() {return opened;}

    /**
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    public String[] validBaudRates() {
        return new String[]{"9,600 bps","19,200 bps"};
    }

    InputStream serialStream = null;
    
	static public SerialDriverAdapter instance() {
        if (mInstance == null){
            mInstance = new SerialDriverAdapter();
            mInstance.setManufacturer(jmri.jmrix.DCCManufacturerList.SPROG);
        }
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;
    
    /**
     * set up all of the other objects to operate with an Sprog command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        //SprogTrafficController.instance().connectPort(this);
        SprogTrafficController control = SprogTrafficController.instance();
        control.connectPort(this);
        
        SprogSystemConnectionMemo memo 
            = new SprogSystemConnectionMemo(control, SprogMode.SERVICE);
        
        memo.configureCommandStation();
        memo.configureManagers();
//        jmri.jmrix.sprog.SprogProgrammer.instance();  // create Programmer in InstanceManager
        /*jmri.InstanceManager.setProgrammerManager(new SprogProgrammerManager(new SprogProgrammer(), SprogMode.SERVICE));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.sprog.SprogPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.sprog.SprogTurnoutManager());

        jmri.InstanceManager.setCommandStation(new jmri.jmrix.sprog.SprogCommandStation());

        jmri.InstanceManager.setSensorManager(new jmri.managers.InternalSensorManager());

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.sprog.SprogThrottleManager());*/

        jmri.jmrix.sprog.ActiveFlag.setActive();

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}
