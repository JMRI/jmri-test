// EcosSensor.java

package jmri.jmrix.ecos;

import jmri.implementation.AbstractSensor;
//import jmri.Sensor;

/**
 * Implement a Sensor via Ecos communications.
 * <P>
 * This object doesn't listen to the Ecos communications.  This is because
 * it should be the only object that is sending messages for this sensor;
 * more than one Sensor object pointing to a single device is not allowed.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau (C) 2007
 * @version	$Revision: 1.2 $
 */
public class EcosSensor extends AbstractSensor {

    final static String prefix = "US";

    int objectNumber = 0;

    public EcosSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public EcosSensor(String systemName) {
        super(systemName);
        init(systemName);
    }
    
    private void init(String id) { }
    
    void setObjectNumber(int o) { 
        objectNumber = o;

    }

    public void requestUpdateFromLayout(){ }

    static String[] modeNames = null;
    static int[] modeValues = null;
        
    public int getObject() { return objectNumber; }

    public void setOwnState(int state) {
        super.setOwnState(state);
    }
 
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosSensor.class.getName());
}

/* @(#)EcosSensor.java */


