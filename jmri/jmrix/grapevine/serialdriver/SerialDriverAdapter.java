// SerialDriverAdapter.java

package jmri.jmrix.grapevine.serialdriver;

import jmri.jmrix.grapevine.SerialPortController;
import jmri.jmrix.grapevine.SerialSensorManager;
import jmri.jmrix.grapevine.SerialTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;

/**
 * Provide access to ProTrak Grapevine via a serial comm port.
 * Normally controlled by the serialdriver.SerialDriverFrame class.
 * @author			Bob Jacobsen   Copyright (C) 2006, 2007
 * @version			$Revision: 1.2 $
 */
public class SerialDriverAdapter extends SerialPortController implements jmri.jmrix.SerialPortAdapter {

    Vector portNameVector = null;
    SerialPort activeSerialPort = null;

    public Vector getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector();
        Enumeration portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portIDs.nextElement();
            // accumulate the names in a vector
            portNameVector.addElement(id.getName());
        }
        return portNameVector;
    }

    public String openPort(String portName, String appName)  {
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for serial
            try {
                setSerialPort();
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }


            // set timeout; no framing in Grapevine
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: "+et);
            }
            
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
                // report now
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud with"
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
                         +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }
            if (log.isDebugEnabled()) {
                // report additional status
                log.debug(" port flow control shows "+
                          (activeSerialPort.getFlowControlMode()==SerialPort.FLOWCONTROL_RTSCTS_OUT?"hardware flow control":"no flow control"));
            }
            if (log.isDebugEnabled()) {
                // arrange to notify later
                activeSerialPort.addEventListener(new SerialPortEventListener(){
                        public void serialEvent(SerialPortEvent e) {
                            int type = e.getEventType();
                            switch (type) {
                            case SerialPortEvent.DATA_AVAILABLE:
                                log.info("SerialEvent: DATA_AVAILABLE is "+e.getNewValue());
                                return;
                            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                                log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is "+e.getNewValue());
                                return;
                            case SerialPortEvent.CTS:
                                log.info("SerialEvent: CTS is "+e.getNewValue());
                                return;
                            case SerialPortEvent.DSR:
                                log.info("SerialEvent: DSR is "+e.getNewValue());
                                return;
                            case SerialPortEvent.RI:
                                log.info("SerialEvent: RI is "+e.getNewValue());
                                return;
                            case SerialPortEvent.CD:
                                log.info("SerialEvent: CD is "+e.getNewValue());
                                return;
                            case SerialPortEvent.OE:
                                log.info("SerialEvent: OE (overrun error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.PE:
                                log.info("SerialEvent: PE (parity error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.FE:
                                log.info("SerialEvent: FE (framing error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.BI:
                                log.info("SerialEvent: BI (break interrupt) is "+e.getNewValue());
                                return;
                            default:
                                log.info("SerialEvent of unknown type: "+type+" value: "+e.getNewValue());
                                return;
                            }
                        }
                    }
                                                  );
                try { activeSerialPort.notifyOnFramingError(true); }
                catch (Exception e) { log.debug("Could not notifyOnFramingError: "+e); }

                try { activeSerialPort.notifyOnBreakInterrupt(true); }
                catch (Exception e) { log.debug("Could not notifyOnBreakInterrupt: "+e); }

                try { activeSerialPort.notifyOnParityError(true); }
                catch (Exception e) { log.debug("Could not notifyOnParityError: "+e); }

                try { activeSerialPort.notifyOnOverrunError(true); }
                catch (Exception e) { log.debug("Could not notifyOnOverrunError: "+e); }

            }

            opened = true;

        } catch (javax.comm.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate
     * connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.grapevine.SerialTurnoutManager());
        jmri.InstanceManager.setLightManager(new jmri.jmrix.grapevine.SerialLightManager());

        SerialSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.grapevine.SerialSensorManager());
        SerialTrafficController.instance().setSensorManager(s);
        jmri.jmrix.grapevine.ActiveFlag.setActive();
    }

    private Thread sinkThread;

    // base class methods for the SerialPortController interface
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
            log.error("getOutputStream exception: "+e.getMessage());
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws javax.comm.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 38400;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i<validSpeeds.length; i++ )
            if (validSpeeds[i].equals(selectedSpeed))
                baud = validSpeedValues[i];
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        activeSerialPort.setFlowControlMode(flow);
    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Set the baud rate.
     */
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: "+rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    String[] stdOption1Values = new String[]{""};

    /**
     * Option 1 is not used for anything
     */
    public String[] validOption1() { return new String[]{""}; }
    String opt1CurrentValue = null;

    /**
     * Option 1 not used, so return a null string.
     */
    public String option1Name() { return ""; }

    /**
     * The first port option isn't used, so just ignore this call.
     */
    public void configureOption1(String value) {}

    protected String [] validSpeeds = new String[]{"38,400 baud"};
    protected int [] validSpeedValues = new int[]{38400};
    protected String selectedSpeed=validSpeeds[0];

    /**
     * Get an array of valid values for "option 2"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption2() { return new String[]{""}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return ""; }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}
