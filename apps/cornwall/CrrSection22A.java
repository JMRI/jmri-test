// CrrSection22A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 22A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection22A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 22A");
        inputs = new NamedBean[]{ tu[13], bo[17], si[24] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean tu13 = ( tu[13].getKnownState() == Sensor.ACTIVE);

        boolean bo17 = ( bo[17].getKnownState() == Sensor.ACTIVE);

        boolean si24 = ( si[24].getCommandedState() == THROWN);

        int value = GREEN;
        if ( !tu13 && bo17 )
            value = RED;
        if ( tu13 && bo17 )
            value = FLASHYELLOW;

        if (value == GREEN && si24)
            value = YELLOW;

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection22A.java */
