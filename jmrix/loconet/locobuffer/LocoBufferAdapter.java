/** 
 * LocoBufferAdapter.java
 *
 * Title:			LocoBufferAdapter
 * Description:		Provide access to LocoNet via a LocoBuffer attached to a serial comm port.
 *					Normally controlled by the LocoBufferFrame class.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locobuffer;

import javax.comm.PortInUseException;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPortEventListener;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPort;
import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import jmri.jmrix.loconet.*;

public class LocoBufferAdapter extends LnPortController implements jmri.jmrix.SerialPortAdapter {

	Vector portNameVector = null;
	SerialPort activeSerialPort = null;
	
	public Vector getPortNames() {
		// first, check that the comm package can be opened and ports seen
		portNameVector = new Vector();
		Enumeration portIDs = CommPortIdentifier.getPortIdentifiers();
		// find the names of suitable ports
		while (portIDs.hasMoreElements()) {
		  CommPortIdentifier id = (CommPortIdentifier) portIDs.nextElement();
		  // accumulate the names in a vector
		  portNameVector.addElement(id.getName());
		  }
		return portNameVector;
	}
	
	public String openPort(String portName, String appName)  {
		// open the primary and secondary ports in LocoNet mode, check ability to set moderators
		try {
			// get and open the primary port
			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
			try {
	  			activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
	  			}
			catch (PortInUseException p) {
				log.error(portName+" port is in use: "+p.getMessage());
				return portName+" port is in use";
			}
			// try to set it for LocoNet via LocoBuffer
			try {
				activeSerialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			} catch (javax.comm.UnsupportedCommOperationException e) {
				log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());	
				return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
			}
			
			// set RTS high, DTR high - done early, so flow control can be configured later
			activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
			activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

			// activeSerialPort.setFlowControlMode(0);
			activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
			
			// set timeout
			// activeSerialPort.enableReceiveTimeout(1000);
			log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
						+" "+activeSerialPort.isReceiveTimeoutEnabled());
			
			// get and save stream
			serialStream = activeSerialPort.getInputStream();
			
			// purge contents, if any
			int count = serialStream.available();
			log.debug("input stream shows "+count+" bytes available");
			while ( count > 0) {
				serialStream.skip(count);
				count = serialStream.available();
			}
				
			// report status?
			if (log.isInfoEnabled()) {
				// report now
				log.info(portName+" port opened at "
						+activeSerialPort.getBaudRate()+" baud with"
						+" DTR: "+activeSerialPort.isDTR()
						+" RTS: "+activeSerialPort.isRTS()
						+" DSR: "+activeSerialPort.isDSR()
						+" CTS: "+activeSerialPort.isCTS()
						+"  CD: "+activeSerialPort.isCD()
					);
			}
			if (log.isDebugEnabled()) {
				// arrange to notify later
				activeSerialPort.addEventListener(new SerialPortEventListener(){
						public void serialEvent(SerialPortEvent e) {
							int type = e.getEventType();
							switch (type) {
								case SerialPortEvent.DATA_AVAILABLE:
									log.info("SerialEvent: DATA_AVAILABLE is "+e.getNewValue());
									return;
								case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
									log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is "+e.getNewValue());
									return;
								case SerialPortEvent.CTS:
									log.info("SerialEvent: CTS is "+e.getNewValue());
									return;
								case SerialPortEvent.DSR:
									log.info("SerialEvent: DSR is "+e.getNewValue());
									return;
								case SerialPortEvent.RI:
									log.info("SerialEvent: RI is "+e.getNewValue());
									return;
								case SerialPortEvent.CD:
									log.info("SerialEvent: CD is "+e.getNewValue());
									return;
								case SerialPortEvent.OE:
									log.info("SerialEvent: OE (overrun error) is "+e.getNewValue());
									return;
								case SerialPortEvent.PE:
									log.info("SerialEvent: PE (parity error) is "+e.getNewValue());
									return;
								case SerialPortEvent.FE:
									log.info("SerialEvent: FE (framing error) is "+e.getNewValue());
									return;
								case SerialPortEvent.BI:
									log.info("SerialEvent: BI (break interrupt) is "+e.getNewValue());
									return;
								default:
									log.info("SerialEvent of unknown type: "+type+" value: "+e.getNewValue());
									return;
							}
						}
					}
				);
				try { activeSerialPort.notifyOnFramingError(true); }
					catch (Exception e) { log.debug("Could not notifyOnFramingError: "+e); }
						
				try { activeSerialPort.notifyOnBreakInterrupt(true); }
					catch (Exception e) { log.debug("Could not notifyOnBreakInterrupt: "+e); }	
					
				try { activeSerialPort.notifyOnParityError(true); }
					catch (Exception e) { log.debug("Could not notifyOnParityError: "+e); }	
					
				try { activeSerialPort.notifyOnOverrunError(true); }
					catch (Exception e) { log.debug("Could not notifyOnOverrunError: "+e); }	
					
			}
						
			opened = true;
			
		}
		catch (Exception ex) {
			log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
			ex.printStackTrace();
			return "Unexpected error while opening port "+portName+": "+ex;
		}
		
		return null; // normal operation
	}

	/**
	 * set up all of the other objects to operate with a LocoBuffer
	 * connected to this port
	 */
	public void configure() {
			// connect to the traffic controller
			LnTrafficController.instance().connectPort(this);
		
			// If a jmri.Programmer instance doesn't exist, create a 
			// loconet.SlotManager to do that
			if (jmri.InstanceManager.programmerInstance() == null) 
				jmri.jmrix.loconet.SlotManager.instance();
				
			// If a jmri.PowerManager instance doesn't exist, create a 
			// loconet.LnPowerManager to do that
			if (jmri.InstanceManager.powerManagerInstance() == null) 
				jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

			// If a jmri.TurnoutManager instance doesn't exist, create a 
			// loconet.LnTurnoutManager to do that
			if (jmri.InstanceManager.turnoutManagerInstance() == null) 
				jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

			// start operation
			// sourceThread = new Thread(p);
			// sourceThread.start();
			sinkThread = new Thread(LnTrafficController.instance());
			sinkThread.start();
	}
	
	private Thread sinkThread;

// base class methods for the LnPortController interface
	public DataInputStream getInputStream() {
		if (!opened) log.error("getInputStream called before load(), stream not available");
		return new DataInputStream(serialStream);
	}
	
	public DataOutputStream getOutputStream() {
		if (!opened) log.error("getOutputStream called before load(), stream not available");
		try {
     		return new DataOutputStream(activeSerialPort.getOutputStream());
     		}
     	catch (java.io.IOException e) {
     		log.error("getOutputStream exception: "+e.getMessage());
     	}
     	return null;
	}
	
	public boolean status() {return opened;}
	
// private control members
	private boolean opened = false;
	InputStream serialStream = null;
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferAdapter.class.getName());

}
