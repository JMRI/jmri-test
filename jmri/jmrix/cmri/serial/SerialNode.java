// SerialNode.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Models a serial C/MRI node, consisting of a (S)USIC and attached cards.
 * <P>
 * Nodes are numbered ala the UA number, from 1 to 63.
 * Node number 1 carries sensors 1 to 999, node 2 1001 to 1999 etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.6 $
 */
public class SerialNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     * Note this is more than a traditional motherboard,
     * and should perhaps be smaller.  But it only sizes
     * int arrays, and doesn't effect runtime, so we're
     * leaving it for now.
     * <P>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERUA}
     */
    // class constants 
    public static final int SMINI = 1;          // SMINI node type
    public static final int USIC_SUSIC = 2;     // USIC/SUSIC node type 
    public static final byte INPUT_CARD = 1;    // USIC/SUSIC input card type for specifying location
    public static final byte OUTPUT_CARD = 2;   // USIC/SUSIC output card type for specifying location
    public static final byte NO_CARD = 0;       // USIC/SUSIC unused location
    // node definition instance variables
    public int nodeAddress = 0;                 // UA, Node address, 0-127 allowed
    protected int nodeType = SMINI;             // See above
    protected int numInputCards = 1;            // 1 on SMINI, 0-63 on USIC/SUSIC 
    protected int numOutputCards = 2;           // 2 on SMINI, 0-63 on USIC/SUSIC
                                                // Note:  Only 64 total cards allowed on USIC/SUSIC
    protected int bitsPerCard = 24;             // 24 for SMINI and USIC, 24 or 32 for SUSIC
    protected int num2LSearchLights = 0;        // SMINI only, 'NS' number of two lead bicolor signals
    protected byte[] locSearchLightBits = new byte[48]; // SMINI only, 0 = not searchlight LED, 
                                                //   1 = searchlight LED, 2*NS bits must be set to 1
    protected byte[] cardTypeLocation = new byte[64]; // USIC/SUSIC only, the first numInputCards+numOutputCards
                                                //   must be set to either INPUT_CARD or OUTPUT_CARD, and 
                                                //   the remaining locations must be set to NO_CARD
         
    static final int MAXSENSORS = 999;
    protected int LASTUSEDSENSOR = 1;  // grows as sensors defined

    public SerialNode() {
        sensorArray = new Sensor[MAXSENSORS+1];
        sensorLastSetting = new int[MAXSENSORS+1];

        for (int i = 0; i<MAXSENSORS+1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
        }
    }

    Sensor[] sensorArray;
    int[] sensorLastSetting;

    /**
     * Use the contents of the poll reply to mark changes
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        try {
            for (int i=0; i<=LASTUSEDSENSOR; i++) {
                int loc = i/8;
                int bit = i%8;
                int value = (l.getElement(loc+2)>>bit)&0x01;  // byte 2 is first of data
                // if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value);
                if ( value == 1) {
                    // bit set, considered ACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.ACTIVE) ) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                } else {
                    // bit reset, considered INACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.INACTIVE) ) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                }
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }

    /**
     * The numbers here are 0 to MAXSENSORS-1, not 1 to MAXSENSORS.
     * @param s
     * @param i 0 to MAXSENSORS-1 number of sensor on unit
     */
    public void registerSensor(Sensor s, int i) {
        if (i<0 || i> (MAXSENSORS-1)) log.warn("Unexpected sensor ordinal: "+i);
        log.debug("registerSensor "+i);
        sensorArray[i] = s;
        if (LASTUSEDSENSOR<i) LASTUSEDSENSOR=i;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
