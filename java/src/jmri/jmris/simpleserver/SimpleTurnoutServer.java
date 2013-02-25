//SimpleTurnoutServer.java
package jmri.jmris.simpleserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmris.AbstractTurnoutServer;

/**
 * Simple Server interface between the JMRI turnout manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class SimpleTurnoutServer extends AbstractTurnoutServer {

    private DataOutputStream output;
    private Connection connection;

    public SimpleTurnoutServer(Connection connection) {
    	this.connection = connection;
    }
    
    public SimpleTurnoutServer(DataInputStream inStream, DataOutputStream outStream) {

        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int Status) throws IOException {
        addTurnoutToList(turnoutName);
        if (Status == Turnout.THROWN) {
            this.sendMessage("TURNOUT " + turnoutName + " THROWN\n");
        } else if (Status == Turnout.CLOSED) {
            this.sendMessage("TURNOUT " + turnoutName + " CLOSED\n");
        } else {
            //  unknown state
            this.sendMessage("TURNOUT " + turnoutName + " UNKNOWN\n");
        }
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        this.sendMessage("TURNOUT ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException, java.io.IOException {
        int index;
        index = statusString.indexOf(" ") + 1;
        log.debug(statusString);
        if (statusString.contains("THROWN")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Turnout THROWN");
            }
            throwTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else if (statusString.contains("CLOSED")) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Turnout CLOSED");
            }
            closeTurnout(statusString.substring(index, statusString.indexOf(" ", index + 1)));
        } else {
            // default case, return status for this turnout
            sendStatus(statusString.substring(index),
                    InstanceManager.turnoutManagerInstance().provideTurnout(statusString.substring(index)).getKnownState());
        }
    }

    private void sendMessage(String message) throws IOException {
    	if (this.output != null) {
    		this.output.writeBytes(message);
    	} else {
    		this.connection.sendMessage(message);
    	}
    }
    
    static Logger log = LoggerFactory.getLogger(SimpleTurnoutServer.class.getName());
}
