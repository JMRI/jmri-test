// XNetMonFrame.java

package jmri.jmrix.lenz.mon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Frame displaying (and logging) XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version         $Revision: 2.1 $
 */
 public class XNetMonFrame extends jmri.jmrix.AbstractMonFrame implements XNetListener {

	public XNetMonFrame() {
		super();
	}

	protected String title() { return "XpressNet Traffic"; }

	public void dispose() {
		// disconnect from the LnTrafficController
		XNetTrafficController.instance().removeXNetListener(~0,this);
		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		XNetTrafficController.instance().addXNetListener(~0, this);
	}

	public synchronized void message(XNetReply l) {  // receive a XpressNet message and log it
		// display the raw data if requested
		String raw = "packet: ";
		if ( rawCheckBox.isSelected() ) {
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				raw += Integer.toHexString(l.getElement(i))+" ";
			raw+="\n";
		}

		// display the decoded data
		String text;
		// First, Decode anything that is sent by the LI10x, and 
                // not the command station 
		if(l.isOkMessage()) {
		   text=new String("Command Successfully Sent/Normal Operations Resumed after timeout");
		} else if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER) {
		  switch(l.getElement(1)) {
		  case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR: 
					text=new String("Error Occurred between the interface and the PC");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR: 
					text=new String("Error Occurred between the interface and the command station");
					break;		  
	          case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR: 
					text=new String("Unknown Communications Error");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR: 
					text=new String("Command Station no longer providing a timeslot for communications");
					break;
		  case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW: 
					text=new String("Buffer Overflow in interface");
					break;
		  default:
			text = l.toString();
		  }
		/* Next, check the "CS Info" messages */
		} else if(l.getElement(0)==XNetConstants.CS_INFO) {
		  switch(l.getElement(1)) {
		  case XNetConstants.BC_NORMAL_OPERATIONS:
				text= new String("Broadcast: Normal Operations Resumed");
				break;
		  case XNetConstants.BC_EVERYTHING_OFF:
				text= new String("Broadcast: Emergency Off (short circuit)");
				break;
		  case XNetConstants.BC_SERVICE_MODE_ENTRY:
				text= new String("Broadcast: Service Mode Entry");
				break;
		  case XNetConstants.PROG_SHORT_CIRCUIT:
				text= new String("Service Mode: Short Circuit");
				break;
		  case XNetConstants.PROG_BYTE_NOT_FOUND:
				text= new String("Service Mode: Data Byte Not Found");
				break;
		  case XNetConstants.PROG_CS_BUSY:
				text= new String("Service Mode: Command Station Busy");
				break;
		  case XNetConstants.PROG_CS_READY:
				text= new String("Service Mode: Command Station Ready");
				break;
		  case XNetConstants.CS_BUSY:
				text= new String("Command Station Busy");
				break;
		  case XNetConstants.CS_NOT_SUPPORTED:
				text= new String("XPressNet Instruction not supported by Command Station");
				break;
		  default:
			text = l.toString();
		  }
                /* Followed by Service Mode responces */
		} else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE) {
		  switch(l.getElement(1)) {
		  case XNetConstants.CS_SERVICE_DIRECT_RESPONSE:
				text = new String("Service Mode: Direct Programming Responce: CV:" +
				       l.getElement(2) +
				       " Value: " +
				       l.getElement(3));
				break;
		  case XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE:
				text = new String("Service Mode: Register or Paged Mode Responce: CV:" +
				       l.getElement(2) +
				       " Value: " +
				       l.getElement(3));
				break;
		  case XNetConstants.CS_SOFTWARE_VERSION:
				text = new String("Command Station Software Version: " + (l.getElementBCD(2).floatValue())/10 + "Type: ") ;
				switch(l.getElement(3)) {
				    case 0x00: text = text+ "LZ100/LZV100";
				               break;
				    case 0x01: text = text+ "LH200";
				               break;
				    case 0x02: text = text+ "Compact or Other";
				               break;
				    default:
					text = text + l.getElement(3);
				}
		  default:
			text = l.toString();
		  }
                /* Now Start decoding messages sent by the computer */
		/* Start with generic requests */
		} else if(l.getElement(0)==XNetConstants.CS_REQUEST) {
		  switch(l.getElement(1)) {
		  case XNetConstants.EMERGENCY_OFF: 
				text = new String("REQUEST: Emergency Off");
				break;
		  case XNetConstants.RESUME_OPS: 
				text = new String("REQUEST: Normal Operations Resumed");
				break;
		  case XNetConstants.SERVICE_MODE_CSRESULT: 
				text = new String("REQUEST: Service Mode Results");
				break;
		  case XNetConstants.CS_VERSION: 
				text = new String("REQUEST: Command Station Version");
				break;
		  case XNetConstants.CS_STATUS: 
				text = new String("REQUEST: Command Station Status");
				break;
		  default:
			text = l.toString();
		  }
		} else { 
		     text = l.toString(); 
		}
		// we use Llnmon to format, expect it to provide consistent \n after each line
		nextLine(text+"\n", raw);

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMonFrame.class.getName());

}
