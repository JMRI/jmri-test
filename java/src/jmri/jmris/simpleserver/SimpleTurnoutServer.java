//SimpleTurnoutServer.java

package jmri.jmris.simpleserver;

import java.io.*;

import jmri.Turnout;
import jmri.InstanceManager;

import jmri.jmris.AbstractTurnoutServer;

/**
 * Simple Server interface between the JMRI turnout manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.3 $
 */

public class SimpleTurnoutServer extends AbstractTurnoutServer {

   private DataOutputStream output;

   public SimpleTurnoutServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(String turnoutName,int Status) throws IOException
     {
        addTurnoutToList(turnoutName);
	if(Status==Turnout.THROWN){
		output.writeBytes("TURNOUT " + turnoutName + " THROWN\n");
        } else if (Status==Turnout.CLOSED){
		output.writeBytes("TURNOUT " + turnoutName + " CLOSED\n");
        } else {
               //  unknown state
		output.writeBytes("TURNOUT " + turnoutName + " UNKNOWN\n");
        }
     }

     public void sendErrorStatus(String turnoutName) throws IOException {
 	output.writeBytes("TURNOUT ERROR\n");
     }

     public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException {
            int index;
            index=statusString.indexOf(" ")+1;
            log.error("stautsString");
	    if(statusString.contains("THROWN")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout THROWN");
                   throwTurnout(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else if(statusString.contains("CLOSED")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Turnout CLOSED");
                   closeTurnout(statusString.substring(index,statusString.indexOf(" ",index+1)));
            } else {
            // default case, return status for this turnout
            sendStatus(statusString.substring(index),
            InstanceManager.turnoutManagerInstance().provideTurnout(statusString.substring(index)).getKnownState());
            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleLightServer.class.getName());

}
