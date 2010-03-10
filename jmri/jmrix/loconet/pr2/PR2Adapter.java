// PR2Adapter.java

package jmri.jmrix.loconet.pr2;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.*;

import gnu.io.SerialPort;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new Digitrax PR2
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005, 2006
 * @version			$Revision: 1.10 $
 */
public class PR2Adapter extends LocoBufferAdapter {


    public PR2Adapter() {
        super();
        m2Instance = this;
    }

    /**
     * Always use flow control, not considered a user-setable option
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 57600;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i<validBaudNumber().length; i++ )
            if (validBaudRates()[i].equals(mBaudRate))
                baud = validBaudNumber()[i];
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                                             SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in Mac DIN8; on main connector, this is DTR

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; 
        if (mOpt1.equals(validOption1[1]))
            flow = SerialPort.FLOWCONTROL_NONE;
        activeSerialPort.setFlowControlMode(flow);
        log.debug("Found flow control "+activeSerialPort.getFlowControlMode()
                  +" RTSCTS_OUT="+SerialPort.FLOWCONTROL_RTSCTS_OUT
                  +" RTSCTS_IN= "+SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR2
     * connected to this port. This overrides the version in
     * loconet.locobuffer, but it has to duplicate much of the
     * functionality there, so the code is basically copied.
     */
    public void configure() {
        // connect to a packetizing traffic controller
        // that does echoing
        jmri.jmrix.loconet.pr2.LnPacketizer packets = new jmri.jmrix.loconet.pr2.LnPacketizer();
        packets.connectPort(this);

        // create memo
        PR2SystemConnectionMemo memo 
            = new PR2SystemConnectionMemo(packets, new SlotManager(packets));

        // do the common manager config
        memo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
        memo.configureManagers(packets);

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    protected String [] validSpeeds = new String[]{"57,600 baud"};
    /**
     * Get an array of valid baud rates as integers. This allows subclasses
     * to change the arrays of speeds.
     */
    public int[] validBaudNumber() {
        return validSpeedValues;
    }
    protected int [] validSpeedValues = new int[]{57600};

    /**
     * Option 1 controls flow control option
     */
    public String option1Name() { return "PR2 connection uses "; }
    public String[] validOption1() { return validOption1; }
    // meanings are assigned to these above, so make sure the order is consistent
    protected String [] validOption1 = new String[]{"hardware flow control (recommended)", "no flow control"};

    /**
     * The PR2 is itself a command station, so fix that choice
     * by providing just the one option
     */
    public String[] validOption2() { 
        String[] retval = {"PR2"}; 
        return retval;
    }


    static public LocoBufferAdapter instance() {
        if (m2Instance == null) {
        	m2Instance = new PR2Adapter();
        	log.debug("new default instance in Pr2Adapter");
        }
        log.debug("PR2Adapter.instance returns object of class "+m2Instance.getClass().getName());
        return m2Instance;
    }
    static private PR2Adapter m2Instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PR2Adapter.class.getName());
}
