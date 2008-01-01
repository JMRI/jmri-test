// ConnectionConfig.java

package jmri.jmrix.grapevine.serialdriver;

import javax.swing.*;
import jmri.jmrix.grapevine.nodeconfig.*;

/**
 * Definition of objects to handle configuring a Grapevine layout connection
 *
 * @author      Bob Jacobsen   Copyright (C) 2003, 2006, 2007
 * @version	$Revision: 1.1 $
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }
	
    public void loadDetails(JPanel details) {
    	// have to embed the usual one in a new JPanel
    	
    	JPanel p = new JPanel();
        super.loadDetails(p);

		details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
		details.add(p);

		// add another button
		JButton b = new JButton("Configure nodes");

		details.add(b);
						
		b.addActionListener(new NodeConfigAction());		
        
    }
    public String name() { return "Grapevine (ProTrak) Layout Bus"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}

