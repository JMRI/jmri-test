// SimpleServer.java

package jmri.jmris.simpleserver;

import jmri.jmris.*;

import java.io.*;

import java.util.ResourceBundle;

import jmri.InstanceManager;


/**
 * This is an implementaiton of a simple server for JMRI.
 * There is currently no handshaking in this server.  You may just start 
 * sending commands.
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 *
 */
public class SimpleServer extends JmriServer{

     private static JmriServer _instance = null;

     static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.simpleserver.SimpleServerBundle");

     public static JmriServer instance(){
         if(_instance==null) {
           int port=java.lang.Integer.parseInt(rb.getString("SimpleServerPort"));
           _instance=new SimpleServer(port);
         }
         return _instance;
     }

     // Create a new server using the default port
     public SimpleServer() {
	super(2048);
     }

     public SimpleServer(int port) {
	super(port);
     }

     // Handle communication to a client through inStream and outStream
	 @SuppressWarnings("deprecation")
     public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        java.util.Scanner inputScanner=new java.util.Scanner(new InputStreamReader(inStream));

        // Listen for commands from the client until the connection closes
	String cmd; 

        // interface components
        SimplePowerServer powerServer = new SimplePowerServer(inStream,outStream);
        SimpleTurnoutServer turnoutServer = new SimpleTurnoutServer(inStream,outStream);
        SimpleLightServer lightServer = new SimpleLightServer(inStream,outStream);
        SimpleSensorServer sensorServer = new SimpleSensorServer(inStream,outStream);
        SimpleReporterServer reporterServer = new SimpleReporterServer(inStream,outStream);

        // Start by sending a welcome message
        outStream.writeBytes("JMRI " + jmri.Version.name() + " \n");

	    while(true) {
           inputScanner.skip("[\r\n]*");// skip any stray end of line characters.
	   // Read the command from the client
           try {
              cmd = inputScanner.nextLine();
           } catch(java.util.NoSuchElementException nse) {
             // we get an nse when we are finished with this client
             // so break out of the loop.
             break;
           }
           
           if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
              if(cmd.startsWith("POWER")){
	             try {
                        powerServer.parseStatus(cmd);
		        powerServer.sendStatus(InstanceManager.powerManagerInstance().getPower());
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("TURNOUT")){
	             try {
                       turnoutServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("LIGHT")){
	             try {
                       lightServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("SENSOR")){
	             try {
                       sensorServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else if(cmd.startsWith("REPORTER")){
	             try {
                       reporterServer.parseStatus(cmd);
                     } catch(jmri.JmriException je) {
                       outStream.writeBytes("not supported\n");
                     }
                 } else {
	      outStream.writeBytes("Unknown Command " + cmd +"\n");
           } 
	 }	
       }

     static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleServer.class.getName());
}
