// LnTcpDriverAdapter.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Vector;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network connection.
 * <P>This connects
 * a Loconet via a telnet connection.
 * Normally controlled by the LnTcpDriverFrame class.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @author      Alex Shepherd Copyright (C) 2003, 2006
 * @version     $Revision: 1.9 $
 */

public class LnTcpDriverAdapter extends LnPortController {

    /**
     * set up all of the other objects to operate with a LocoNet
     * connected via this class.
     */
    public void configure() {
        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer();
        packets.connectPort(this);

        // do the common manager config
        configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
        configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    private Thread sinkThread;

    // base class methods for the LnPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
        }
        try {
            return new DataInputStream(socket.getInputStream());
        } catch (java.io.IOException ex1) {
            log.error("Exception getting input stream: "+ex1);
            return null;
        }
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(socket.getOutputStream());
        }
        catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
        }
        return null;
    }

    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;
    private Socket socket = null;

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening LocoNetOverTcp network connection: "+e);
        }
    }

    static private LnTcpDriverAdapter mInstance = null;
    static public synchronized LnTcpDriverAdapter instance() {
        if (mInstance == null) mInstance = new LnTcpDriverAdapter();
        return mInstance;
    }

    public Vector getPortNames() {
        log.error("Unexpected call to getPortNames");
        return null;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }

    public String[] getCommandStationNames() { return commandStationNames; }
    public String   getCurrentCommandStation() { return commandStationName; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTcpDriverAdapter.class.getName());

}
