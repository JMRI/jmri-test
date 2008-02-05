// SerialMonFrame.java

package jmri.jmrix.powerline.serialmon;

import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;

import jmri.jmrix.powerline.X10;

/**
 * Frame displaying (and logging) serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001, 2006, 2007, 2008
 * @version         $Revision: 1.3 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Powerline Device Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        int len = l.getNumDataElements();
        String text;
        switch (l.getElement(0)&0xFF) {
            case 0xFB : text = "Macro load reply"; break;
            case 0x9B : text = "Set CM11 time"; break;
            case 0x00 : if (len == 1) {
                    text = "OK for transmission"; break;
                } // else fall through
            default: {
                if ((l.getElement(0)&0x02) == 0x02) 
                    text = "House "+X10.decode((l.getElement(1)>>4)&0x0F)+" function: "+X10.functionName(l.getElement(1)&0x0f);
                else
                    text = "House "+X10.decode((l.getElement(1)>>4)&0x0F)+" address device "+X10.decode(l.getElement(1)&0x0f);
            }
        }
        nextLine(text+"\n",l.toString());
        return;
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() == 1) {
            String val;
            int msg = l.getElement(0);
            switch (msg&0xFF) {
                case X10.POLL_REQ: val = "Data Available\n";break;
                case X10.TIME_REQ: val = "CP11 time request\n";break;
                case 0xA6: val = "CP10 time request\n";break;
                case 0xF3: val = "Input Filter Failed\n";break;
                case X10.READY_REQ: val = "Interface Ready\n";break;
                default: val = "One byte, probably CRC\n";break;
            }
            nextLine(val, l.toString());
            return;
        } else if ((l.getNumDataElements() == 2) && (l.getElement(1) == X10.READY_REQ)) {
            nextLine("CRC 0x"+jmri.util.StringUtil.twoHexFromInt(l.getElement(0))+" and Interface Ready\n",
                l.toString());
            return;     
        } else { // must be data reply
            String s = "Receive addr="+l.getAddr()+" IB=";
            for (int i=2; i<4; i++)
                s+=Integer.toHexString(l.getElement(i))+" ";
            nextLine(s+"\n", l.toString());
            return;
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}
