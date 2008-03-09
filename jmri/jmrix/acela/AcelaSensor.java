// AcelaSensor.java

package jmri.jmrix.acela;

import jmri.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for Acela systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version     $Revision: 1.1 $
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008
 *              Based heavily on CMRI serial example.
 */

public class AcelaSensor extends AbstractSensor {

    public AcelaSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public AcelaSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    public void dispose() {}

    /**
     * Request an update on status.
     * <P>
     * Since status is continually being updated, this isn't active now.
     */
    public void requestUpdateFromLayout() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaSensor.class.getName());
}

/* @(#)AcelaSensor.java */