package jmri.jmrit.withrottle;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */

import jmri.util.JmriJFrame;
import java.util.ResourceBundle;


public class WiThrottlePrefsFrame extends JmriJFrame{

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    public WiThrottlePrefsFrame(){
        
        this.setModifiedFlag(true);
        
        WiThrottlePrefsPanel prefs = new WiThrottlePrefsPanel(this);
        prefs.enableSave(); //  Makes save and cancel visible
        getContentPane().add(prefs);

        this.setTitle(rb.getString("TitleWiThrottlePreferences"));
        this.pack();
        this.setVisible(true);
    }

    

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottlePrefsFrame.class.getName());
}
