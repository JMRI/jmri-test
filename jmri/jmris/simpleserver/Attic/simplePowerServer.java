//simplePowerServer.java

package jmri.jmris.simpleserver;

import java.io.*;
import java.lang.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

import jmri.jmris.AbstractPowerServer;

/**
 * Simple Server interface between the JMRI power manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.2 $
 */

public class simplePowerServer extends AbstractPowerServer {

   private DataOutputStream output;

   public simplePowerServer(DataInputStream inStream,DataOutputStream outStream){

        output=outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */

     public void sendStatus(int Status) throws IOException
     {
	if(Status==PowerManager.ON){
		output.writeBytes("POWER ON\n");
        } else if (Status==PowerManager.OFF){
		output.writeBytes("POWER OFF\n");
        } else {
               // power unknown
        }
     }

     public void sendErrorStatus() throws IOException {
 	output.writeBytes("POWER ERROR\n");
     }

     public void parseStatus(String statusString) throws jmri.JmriException {
	    if(statusString.contains("ON")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Power ON");
                   setOnStatus();
            } else if(statusString.contains("OFF")){
                   if(log.isDebugEnabled())
                      log.debug("Setting Power OFF");
                   setOffStatus();
            }
     }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(simplePowerServer.class.getName());

}
