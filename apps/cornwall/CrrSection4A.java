// CrrSection4A.java

package apps.cornwall;

import jmri.*;

/**
 * Automate section 4A of the Cornwall RR.
 * <P>
 * Based on Crr0029.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection4A extends CrrSection {

    void defineIO() {
        sig  = InstanceManager.signalHeadManagerInstance().getByUserName("Signal 4A");
        sensors = new Sensor[]{ tu[4], tu[5], tu[10], bo[6], bo[7], bo[8] };
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo6  = ( bo[ 6].getKnownState() == Sensor.ACTIVE);
        boolean bo7  = ( bo[ 7].getKnownState() == Sensor.ACTIVE);
        boolean bo8  = ( bo[ 8].getKnownState() == Sensor.ACTIVE);

        boolean tu4  = ( tu[ 4].getKnownState() == Sensor.ACTIVE);
        boolean tu5  = ( tu[ 5].getKnownState() == Sensor.ACTIVE);
        boolean tu10 = ( tu[10].getKnownState() == Sensor.ACTIVE);

        int value = RED;
        if (
                ( tu4 || bo6)
             || ( bo7 && bo8 )
             || ( bo8 && !tu5 )
             || ( tu10 && tu5 )
             || ( !tu10 && tu5 && bo7)
            ) {
            value = RED;
        } else {
            value = GREEN;
        }

        sig.setAppearance(value);
    }
}

/* @(#)CrrSection4A.java */
