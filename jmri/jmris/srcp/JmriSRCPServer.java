// JmriSRCPServer.java

package jmri.jmris.srcp;

import jmri.jmris.*;

import java.io.*;

// imports for ZeroConf.
import jmri.util.zeroconf.ZeroConfUtil;

import jmri.InstanceManager;

import jmri.jmris.srcp.parser.SRCPParser;
import jmri.jmris.srcp.parser.ParseException;
import jmri.jmris.srcp.parser.SRCPVisitor;
import jmri.jmris.srcp.parser.SimpleNode;
import jmri.jmris.srcp.parser.*;

/**
 * This is an implementaiton of SRCP for JMRI.
 * @author Paul Bender Copyright (C) 2009
 * @version $Revision: 1.5.2.1.2.1 $
 *
 */
public class JmriSRCPServer extends JmriServer{

    // There are 3 possible modes (from the SRCP standards).
    private static int HANDSHAKEMODE =1;
    private static int COMMANDMODE = 2;
    private static int INFOMODE = 4;

     private int SRCPSERVERMODE = 1;
     private static JmriServer _instance = null;


     public static JmriServer instance(){
         if(_instance==null) _instance=new JmriSRCPServer();
         return _instance;
     }

     // Create a new server using the default port
     public JmriSRCPServer() {
	super(12345);
     }

     public JmriSRCPServer(int port) {
	super(port);
     }

     // Advertise the service with ZeroConf
     protected void advertise(){
           try {
                serviceInfo = ZeroConfUtil.advertiseService(
                    ZeroConfUtil.getServerName("SRCP 0.8.3"),
                    "_srcp._tcp.local.",
                    portNo,
                    jmdns);
           
           } catch (java.io.IOException e) {
               log.error("JmDNS Failure");
           }
     
     }

     // Handle communication to a client through inStream and outStream
     @SuppressWarnings("deprecation")
     public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // Listen for commands from the client until the connection closes
        SRCPParser parser = null;
	String cmd; 
	int index=0;
        int runmode=HANDSHAKEMODE;

        // interface components
        JmriSRCPPowerServer powerServer = new JmriSRCPPowerServer(outStream);

        // Start by sending a welcome message
        outStream.writeBytes("SRCP 0.8.3\n");

	    while(true) {
	   // Read the command from the client
           
           index = 0;
           if(SRCPSERVERMODE == HANDSHAKEMODE) 
           {
              cmd = inStream.readLine(); 
              if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
              if(cmd.startsWith("SET")){
                 index = cmd.indexOf(" ",index)+1;
                 if(cmd.substring(index).startsWith("PROTOCOL SRCP")) {
                   if(cmd.contains("0.8"))
	            outStream.writeBytes("201 OK PROTOCOL SRCP\n");
                   else
	            outStream.writeBytes("400 ERROR unsupported protocol\n");
                                    
                 } else if(cmd.substring(index).startsWith("CONNECTIONMODE SRCP")) {
                        index=cmd.indexOf(" ",index)+1;
                        index=cmd.indexOf(" ",index)+1;
                        if(cmd.substring(index).startsWith("COMMAND")){
                           runmode=COMMANDMODE;
                           outStream.writeBytes("202 OK\n");
                        } else if(cmd.substring(index).startsWith("INFO")){
                           runmode=INFOMODE;
                           outStream.writeBytes("202 OK\n");
                        } else {
                           outStream.writeBytes("401 ERROR unsupported connection mode\n");
                        }
                 } else { 
	            outStream.writeBytes("500 ERROR out of resources\n");
                 }
              } else if (cmd.startsWith("GO")){
                if(runmode==0){
                  outStream.writeBytes("402 ERROR unsufficient data\n");
                } else {
                  SRCPSERVERMODE = runmode;
                  if(log.isDebugEnabled()) log.debug("Switching to runmode after GO");
                  outStream.writeBytes("200 OK 1\n");
                }
              } else {
                  outStream.writeBytes("402 ERROR unsufficient data\n");
	      }
           } else if (SRCPSERVERMODE == COMMANDMODE ){

              if(parser==null) parser = new SRCPParser(inStream); 
              try {
                  SimpleNode e = parser.command();
                  SRCPVisitor v = new SRCPVisitor();
                  e.jjtAccept(v,null);
              } catch (ParseException pe){
                   if(log.isDebugEnabled())
                   {
                      log.debug("Parse Exception");
                      pe.printStackTrace();
                   }
                   outStream.writeBytes("425 ERROR not supported\n");
              }
          /*    int bus;
              if(cmd.startsWith("GET")){
                        index=cmd.indexOf(" ",index)+1;
                        bus=Integer.parseInt(cmd.substring(index,cmd.indexOf(" ")));
                        index=cmd.indexOf(" ",index)+1;
                        if(cmd.substring(index).startsWith("POWER")){
			   try {
		 		powerServer.sendStatus(InstanceManager.powerManagerInstance().getPower());
                           } catch(jmri.JmriException je) {
                             outStream.writeBytes("425 ERROR not supported\n");
                           }
                        }
                        else {
                           outStream.writeBytes("425 ERROR not supported\n");
                        }
              } else if(cmd.startsWith("SET")){
                        index=cmd.indexOf(" ",index)+1;
                        bus=Integer.parseInt(cmd.substring(index,cmd.indexOf(" ",index)));
                        index=cmd.indexOf(" ",index)+1;
                        if(log.isDebugEnabled()) 
                           log.debug("Bus: " + bus);
                        if(cmd.substring(index).startsWith("POWER")){
			    try {
                                powerServer.parseStatus(cmd);
				powerServer.sendStatus(InstanceManager.powerManagerInstance().getPower());
                            } catch(jmri.JmriException je) {
                              outStream.writeBytes("425 ERROR not supported\n");
                            }
                        } else {
                           outStream.writeBytes("425 ERROR not supported\n");
                        }
              } else if(cmd.startsWith("CHECK")){
                  outStream.writeBytes("425 ERROR not supported\n");
              } else if(cmd.startsWith("WAIT")){
                  outStream.writeBytes("425 ERROR not supported\n");
              } else if(cmd.startsWith("INIT")){
                  outStream.writeBytes("425 ERROR not supported\n");
              } else if(cmd.startsWith("TERM")){
                  outStream.writeBytes("425 ERROR not supported\n");
              } else if(cmd.startsWith("RESET")){
                  outStream.writeBytes("425 ERROR not supported\n");
              } else if(cmd.startsWith("VERIFY")){
                  outStream.writeBytes("425 ERROR not supported\n");
              }*/
           } else if (SRCPSERVERMODE == INFOMODE) {
              cmd = inStream.readLine(); 
              if(log.isDebugEnabled()) log.debug("Received from client: " + cmd);
             // input commands are ignored in INFOMODE. 
           } else {
	      outStream.writeBytes("500 ERROR out of resources\n");
              outStream.close();
              inStream.close();
              return;
           } 
	 }	
       }

     static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriSRCPServer.class.getName());
}
