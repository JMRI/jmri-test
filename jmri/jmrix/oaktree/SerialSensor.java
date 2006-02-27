// SerialSensor.java

package jmri.jmrix.oaktree;

import jmri.AbstractSensor;
import jmri.Sensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 * @version     $Revision: 1.1 $
 */
public class SerialSensor extends AbstractSensor {

    public SerialSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    public void dispose() {}

    /**
     * Request an update on status.
     * <P>
     * Since status is continually
     * being updated, this isn't active now.  Eventually, we may
     * want to have this move the related AIU to the top of the
     * polling queue.
     */
    public void requestUpdateFromLayout() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSensor.class.getName());

}

/* @(#)SerialSensor.java */
