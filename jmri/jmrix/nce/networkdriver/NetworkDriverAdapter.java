// NetworkDriverAdapter.java

package jmri.jmrix.nce.networkdriver;

import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceProgrammer;
import jmri.jmrix.nce.NceProgrammerManager;
import jmri.jmrix.nce.NceSensorManager;
import jmri.jmrix.nce.NceThrottleManager;
import jmri.jmrix.nce.NceTrafficController;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Implements SerialPortAdapter for the NCE system network connection.
 * <P>This connects
 * an NCE command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @version	$Revision: 1.5 $
 */
public class NetworkDriverAdapter extends NcePortController {

    /**
     * set up all of the other objects to operate with an NCE command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        NceTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setProgrammerManager(
                new NceProgrammerManager(
                    new NceProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.nce.NcePowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.nce.NceTurnoutManager());

        NceSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.nce.NceSensorManager());
        NceTrafficController.instance().setSensorManager(s);

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.nce.NceThrottleManager());

        jmri.jmrix.nce.ActiveFlag.setActive();

        if (getCurrentOption1Setting().equals(validOption2()[1])) {
            // setting binary mode
            NceMessage.setCommandOptions(NceMessage.OPTION_2006);
        } else {
            NceMessage.setCommandOptions(NceMessage.OPTION_2004);
        }
    }

    private Thread sinkThread;

    // base class methods for the NcePortController interface
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

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening NCE network connection: "+e);
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

    /**
     * Option 2 is binary vs ASCII command set.
     */
    public String[] validOption2() { return new String[]{"2004 or earlier", "2006 or later"}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return "Command Station EPROM"; }

    /**
     * Set the binary vs ASCII command set option.
     */
    public void configureOption2(String value) { mOpt2 = value; }
    protected String mOpt2 = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[0];
        return mOpt2;
    }

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) mInstance = new NetworkDriverAdapter();
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    Socket socket;

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NetworkDriverAdapter.class.getName());

}
