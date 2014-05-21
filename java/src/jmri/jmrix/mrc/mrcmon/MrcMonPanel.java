/**
 * MrcMonPane.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version		$Revision: 22942 $
 * @author		kcameron Copyright (C) 2011
 * 	copied from SerialMonPane.java
 * @author		Daniel Boudreau Copyright (C) 2012
 *  added human readable format
 */

package jmri.jmrix.mrc.mrcmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.mrc.*;
import jmri.jmrix.mrc.swing.*;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;


public class MrcMonPanel extends jmri.jmrix.AbstractMonPane implements MrcListener, MrcTrafficListener, MrcPanelInterface{

	private static final long serialVersionUID = 6106790197336170348L;

	public MrcMonPanel() {
        super();
    }
    
    //MrcMonBinary mrcMon = new MrcMonBinary();
    
    public String getHelpTarget() { return null; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("MRC_");
    	}
		x.append(": ");
    	x.append("Command Monitor");
        return x.toString(); 
    }

    public void dispose() {
        if(memo.getMrcTrafficController()!=null){
        // disconnect from the LnTrafficController
            memo.getMrcTrafficController().removeTrafficListener(MrcTrafficListener.MRC_TRAFFIC_ALL, this);
            //memo.getMrcTrafficController().removeMrcListener(~0,this);
        }
        // and unwind swing
        super.dispose();
    }
    
    public void init() {}
    
    MrcSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof MrcSystemConnectionMemo ) {
            initComponents((MrcSystemConnectionMemo) context);
        }
    }
    
    JCheckBox excludePoll = new JCheckBox("Exclude Poll Messages");
    
    private int trafficFilter = MrcTrafficListener.MRC_TRAFFIC_ALL;
    
    public void initComponents(MrcSystemConnectionMemo memo) {
        this.memo = memo;
        add(excludePoll);
        // connect to the LnTrafficController
        if(memo.getMrcTrafficController()==null){
            log.error("No traffic controller is available");
            return;
        }
        memo.getMrcTrafficController().addTrafficListener(trafficFilter, this);
		//memo.getMrcTrafficController().addMrcListener(~0, this);
    }

    public synchronized void message(MrcMessage m) {  // receive a message and log it
	    String raw = "";
	    for (int i=0;i<m.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
        }
        
        // send the raw data, to display if requested
        //String raw = m.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        //nextLine( raw );
        //nextLine("cmd: \""+m.toString()+"\"\n", raw);
	}
    
    MrcMessage previousPollMessage;
    
    private boolean filterEcho = true;
    private MrcMessage lastLoggedTxMessage = null;
    
    public synchronized void notifyXmit(Date timestamp, MrcMessage m) {
    	if(excludePoll.isSelected() && (m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL){
            return;
        }
    	//if (useSimpleLogging) return;
    	
    	logMessage(timestamp, m, "Tx:");
    	lastLoggedTxMessage = m;
    }
    
    public synchronized void notifyFailedXmit(Date timestamp, MrcMessage m) {
    	
    	//if (useSimpleLogging) return;
    	
    	//logMessage(timestamp, m, "Tx");
    	//lastLoggedTxMessage = m;
    }
    
    public synchronized void notifyRcv(Date timestamp, MrcMessage m) {
    	
    	//if (useSimpleLogging) return;

    	/*if (filterEcho) {
    		if ((lastLoggedTxMessage != null) && (lastLoggedTxMessage.equals(m))) {     	
    			return;
    		}
    	}*/
        String raw = "";
        if(excludePoll.isSelected() && (m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL){
            //Do not show poll messages
            previousPollMessage = m;
            return;
        } else if (previousPollMessage!=null) {
            if((m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL){
                previousPollMessage = null;
                return;
            }
            for (int i=0;i<previousPollMessage.getNumDataElements(); i++) {
                if (i>0) raw+=" ";
                raw = jmri.util.StringUtil.appendTwoHexFromInt(previousPollMessage.getElement(i)&0xFF, raw);
            }
            nextLine("msg: \""+previousPollMessage.toString()+"\"\n", raw);
            raw = "";
            previousPollMessage = null;
        }
    	logMessage(timestamp, m, "Rx:");
    }
    
    private void logMessage(Date timestamp, MrcMessage m, String src) {  // receive a LocoNet message and log it
        // send the raw data, to display if requested
        //String raw = src + " - " + l.toString();
        String raw = "";
        for (int i=0;i<m.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
        }

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLineWithTime(timestamp, src + " " + m.toString()+"\"\n", raw );
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.mrc.swing.MrcNamedPaneAction {

		private static final long serialVersionUID = -7644336249246783644L;

		public Default() {
            super("Mrc Command Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MrcMonPanel.class.getName(), 
                jmri.InstanceManager.getDefault(MrcSystemConnectionMemo.class));
        }
    }

	static Logger log = LoggerFactory.getLogger(MrcMonPanel.class.getName());

}


/* @(#)MonAction.java */
