// SerialDriverAdapter.java

package jmri.jmrix.cmri.serial.sim;

import jmri.jmrix.cmri.serial.SerialSensorManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

/**
 * Extends the serialdriver.SerialDriverAdapter class to 
 * act as simulated connection.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002, 2008
 * @version			$Revision: 1.12 $
 */
public class SerialDriverAdapter extends jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter {

    public String openPort(String portName, String appName)  {
            // don't even try to get port

            // get and save stream
            serialStream = null;

            opened = true;

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
        // install a traffic controller that doesn't time out
        new SerialTrafficController(){
            // timeout doesn't do anything
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage m,jmri.jmrix.AbstractMRListener l) {}
            // and make this the instance
            { self = this;}
        };
        
        // connect to the traffic controller
        SerialTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.cmri.serial.SerialTurnoutManager());
        jmri.InstanceManager.setLightManager(new jmri.jmrix.cmri.serial.SerialLightManager());

        SerialSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.cmri.serial.SerialSensorManager());
        SerialTrafficController.instance().setSensorManager(s);
        jmri.jmrix.cmri.serial.ActiveFlag.setActive();
    }

    // base class methods for the SerialPortController interface
    public DataInputStream getInputStream() {
        try {
            return new DataInputStream(new java.io.PipedInputStream(new java.io.PipedOutputStream()));
        } catch (Exception e ) { return null; }
        //return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        return new DataOutputStream(new java.io.OutputStream() {
            public void write(int b) throws java.io.IOException {
            }
        });
    }

    public boolean status() {return opened;}

    /**
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
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

    static public jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance;
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}
