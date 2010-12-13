// NceNamedPaneAction.java

package jmri.jmrix.nce.swing;

import javax.swing.*;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.util.swing.*;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * Copied from LocoNet
 * @author kcameron
 * @version		$Revision: 1.1.2.1 $
 */
 
public class NceNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public NceNamedPaneAction(String s, WindowInterface wi, String paneClass, NceSystemConnectionMemo memo) {
    	super(s, wi, paneClass);
    	this.memo = memo;
    }
    
 	public NceNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, NceSystemConnectionMemo memo) {
    	super(s, i, wi, paneClass);
    	this.memo = memo;
    }
    
    NceSystemConnectionMemo memo;
    
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) return null;
        
        try {
            ((NcePanelInterface)p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: "+paneClass+" due to:"+ex);
            ex.printStackTrace();
        }      
        
        return p;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceNamedPaneAction.class.getName());
}

/* @(#)LnNamedPaneAction.java */
