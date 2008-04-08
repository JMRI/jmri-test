// SpecificLightManager.java

package jmri.jmrix.powerline.cp290;

import jmri.Light;

/**
 * Implement light manager for powerline serial systems with CP290 adapters
 * <P>
 * Just provide the specific objects at creation time.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version	$Revision: 1.1 $
 */
public class SpecificLightManager extends jmri.jmrix.powerline.SerialLightManager {

    public SpecificLightManager() {
        super();
    }

    /** 
     * Create light of a specific type for the interface
     */
    protected Light createNewSpecificLight(String systemName, String userName) {
        return new SpecificLight(systemName, userName);
    }
    
}

/* @(#)SpecificLightManager.java */
