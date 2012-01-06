// ConfigurationManager.java

package jmri.jmrix.can.nmranet;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.InstanceManager;
import java.util.ResourceBundle;

/**
 * Does configuration for Nmra Net communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision: 17977 $
 */
public class NmraConfigurationManager extends jmri.jmrix.can.ConfigurationManager {
    
    public NmraConfigurationManager(CanSystemConnectionMemo memo){
        super(memo);
        //At this stage without the multiple connections we can do this, but afterwards we can not.
        adapterMemo.setUserName("Nmra Net");
        adapterMemo.setSystemPrefix("M");
        InstanceManager.store(cf = new jmri.jmrix.can.nmranet.swing.NmraNetComponentFactory(adapterMemo), 
            jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, NmraConfigurationManager.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    public void configureManagers(){
    
        ActiveFlag.setActive();
    }
    
        
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled())
            return false;
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled())
            return null;
        return null; // nothing, by default
    }
        
    public void dispose(){
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.deregister(this, NmraConfigurationManager.class);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        //No actions that can be loaded at startup
        return null;
    }


}

/* @(#)ConfigurationManager.java */
