/**
 * NceMonPane.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version
 * @author kcameron
 * 	copied from EcosMonPane.java
 */

package jmri.jmrix.nce.ncemon;

import jmri.jmrix.nce.*;
import jmri.jmrix.nce.swing.*;


public class NceMonPanel extends jmri.jmrix.AbstractMonPane implements NceListener, NcePanelInterface{

    public NceMonPanel() {
        super();
    }
    
    public String getHelpTarget() { return null; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append("Command Monitor");
        return x.toString(); 
    }

    public void dispose() {
        // disconnect from the NceTrafficController
        memo.getNceTrafficController().removeNceListener(this);
        // and unwind swing
        super.dispose();
    }
    
    public void init() {}
    
    NceSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo ) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(NceSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        memo.getNceTrafficController().addNceListener(this);
    }

    public synchronized void message(NceMessage l) {  // receive a message and log it
        if (l.isBinary())
          	nextLine("binary cmd: "+l.toString()+"\n", null);
        else
            nextLine("cmd: \""+l.toString()+"\"\n", null);
	}
    
	public synchronized void reply(NceReply l) {  // receive a reply message and log it
	    String raw = "";
	    for (int i=0;i<l.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i)&0xFF, raw);
        }
	        
	    if (l.isUnsolicited()) {    
            nextLine("msg: \""+l.toString()+"\"\n", raw);
        } else {
            nextLine("rep: \""+l.toString()+"\"\n", raw);
        }
	}
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {
        public Default() {
            super("Nce Command Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                NceMonPanel.class.getName(), 
                jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceMonPanel.class.getName());

}


/* @(#)MonAction.java */
