// LocoBufferAdapter.java

package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.*;
import gnu.io.SerialPort;
/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it
 * operates correctly with the Intellibox on-board serial port.
 * <P>
 * Since this is by definition connected to an Intellibox, 
 * the command station prompt is suppressed.
 *
 * @author			Alex Shepherd   Copyright (C) 2004
 * @author          Bob Jacobsen    Copyright (C) 2005, 2010
 * @version			$Revision: 17977 $
 */
public class UhlenbrockAdapter extends LocoBufferAdapter {


    public UhlenbrockAdapter() {
        super();

        validSpeeds = new String[]{"19200", "38400", "57600", "115200"};
        validSpeedValues = new int[]{19200, 38400, 57600, 115200};
        configureBaudRate("115200");
    }

    /**
 * Set up all of the other objects to operate with a LocoBuffer
 * connected to this port.
 */
public void configure() {
    // connect to a packetizing traffic controller
    IBLnPacketizer packets = new IBLnPacketizer();
    packets.connectPort(this);

    // create memo
    /*LocoNetSystemConnectionMemo memo 
        = new LocoNetSystemConnectionMemo(packets, new SlotManager(packets));*/
    adaptermemo.setSlotManager(new SlotManager(packets));
    adaptermemo.setLnTrafficController(packets);
    // do the common manager config
    adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
    adaptermemo.configureManagers();

    // start operation
    packets.startThreads();
    jmri.jmrix.loconet.ActiveFlag.setActive();
}

    /**
     * Get an array of valid baud rates. 
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Get an array of valid baud rates as integers. 
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public int[] validBaudNumber() {
        return validSpeedValues;
    }

    /**
     * Get an array of valid values for "option 1"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return ""; }

    /**
     * Set the second port option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

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

    /**
     * Set the second port option.
     */
    public void configureOption2(String value) { mOpt2 = value; }
    protected String mOpt2  = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[0];
        return mOpt2;
    }
    
    public boolean okToSend() {
        return true;
    }
    
        /**
     * Local method to do specific configuration, overridden in class
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                                             SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        log.info("Found flow control "+activeSerialPort.getFlowControlMode()
                  +" RTSCTS_OUT="+SerialPort.FLOWCONTROL_RTSCTS_OUT
                  +" RTSCTS_IN= "+SerialPort.FLOWCONTROL_RTSCTS_IN);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UhlenbrockAdapter.class.getName());

}
