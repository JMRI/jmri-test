// ReportAction.java

package jmri.jmrit.mailreport;

import java.awt.event.*;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a ReportFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public class ReportAction extends AbstractAction {

    public ReportAction(String s) { 
	    super(s);
    }

    public ReportAction() { 
        this(java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle").getString("Title"));
    }

    public void actionPerformed(ActionEvent e) {
        ReportFrame f = new ReportFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception in startup", ex);
        }
        f.setVisible(true);
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ReportFrame.class.getName());
}

/* @(#)ReportAction.java */
