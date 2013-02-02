// ConnectionConfig.java

package jmri.jmrix.can.adapters.gridconnect.net;

import org.apache.log4j.Logger;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Definition of objects to handle configuring a connection
 * via a NetworkDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision: 21889 $
 */
 public class MergConnectionConfig  extends ConnectionConfig {

	public final static String NAME = "CAN via MERG Network Interface";
    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    
    public MergConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        super(p);
    }
    
    public String name() { return NAME; }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public MergConnectionConfig() {
        super();
    }
    
    protected void setInstance() {
        if (adapter==null){
            adapter = new MergNetworkDriverAdapter();
            adapter.setPort(5550);
        }
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
    
    static Logger log = Logger.getLogger(MergConnectionConfig.class.getName());
}

