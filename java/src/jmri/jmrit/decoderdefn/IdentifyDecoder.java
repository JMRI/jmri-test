// IdentifyDecoder.java
package jmri.jmrit.decoderdefn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interact with a programmer to identify the DecoderIndexFile entry for a decoder
 * on the programming track. Create a subclass of this which implements {@link #done}
 * to handle the results of the identification.
 * <p>
 * This is a class (instead of a Roster member function) to simplify use of
 * ProgListener callbacks.
 * <p>
 * Contains manufacturer-specific code to generate a 3rd "productID" identifier, in addition 
 * to the manufacturer ID and model ID:<ul>
 *     <li>QSI: (mfgID == 113)   write 254=>CV49, write 4=>CV50, then CV56 is high byte, write 5=>CV50, then CV56 is low byte of ID
 *     <li>Harman:  (mfgID = 98)  CV112 is high byte, CV113 is low byte of ID
 *     <li>TCS: (mfgID == 153)  CV249 is ID
 *     <li>Zimo: (mfgID == 145)  CV250 is ID
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 * @author Howard G. Penny Copyright (C) 2005
 * @version $Revision$
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.symbolicprog.CombinedLocoSelPane
 * @see jmri.jmrit.symbolicprog.NewLocoSelPane
 */
abstract public class IdentifyDecoder extends jmri.jmrit.AbstractIdentify {

    int mfgID = -1; 	// cv8
    int modelID = -1;	// cv7
    int productIDhigh = -1;
    int productIDlow = -1;
    int productID = -1;

    // steps of the identification state machine
    public boolean test1() {
        // read cv8
        statusUpdate("Read MFG ID - CV 8");
        readCV(8);
        return false;
    }

    public boolean test2(int value) {
        mfgID = value;
        statusUpdate("Read MFG version - CV 7");
        readCV(7);
        return false;
    }

    public boolean test3(int value) {
        modelID = value;
        if (mfgID == 113) {  // QSI 
            statusUpdate("Set PI for Read Product ID High Byte");
            writeCV(49, 254);
            return false;
        } else if (mfgID == 153) {  // TCS
            statusUpdate("Read decoder ID CV 249");
            readCV(249);
            return false;
        } else if (mfgID == 145) {  // Zimo
            statusUpdate("Read decoder ID CV 250");
            readCV(250);
            return false;
        } else if (mfgID == 98) {  // Harman
            statusUpdate("Read decoder ID high CV 112");
            readCV(112);
            return false;
        }
        return true;
    }

    public boolean test4(int value) {
        if (mfgID == 113) {
            statusUpdate("Set SI for Read Product ID High Byte");
            writeCV(50, 4);
            return false;
        } else if (mfgID == 153) {
            productID = value;
            return true;
        } else if (mfgID == 145) {
            productID = value;
            return true;
        } else if (mfgID == 98) {
            productIDhigh = value;
            statusUpdate("Read decoder ID low CV 113");
            readCV(113);
            return false;
        }
        log.error("unexpected step 4 reached with value: " + value);
        return true;
    }

    public boolean test5(int value) {
        if (mfgID == 113) {
            statusUpdate("Read Product ID High Byte");
            readCV(56);
            return false;
        } else if (mfgID == 98) {
            productIDlow = value;
            productID = (productIDhigh << 8) | productIDlow;
            return true;
        }
        log.error("unexpected step 5 reached with value: " + value);
        return true;
    }

    public boolean test6(int value) {
        if (mfgID == 113) {
            productIDhigh = value;
            statusUpdate("Set SI for Read Product ID Low Byte");
            writeCV(50, 5);
            return false;
        }
        log.error("unexpected step 6 reached with value: " + value);
        return true;
    }

    public boolean test7(int value) {
        if (mfgID == 113) {
            statusUpdate("Read Product ID Low Byte");
            readCV(56);
            return false;
        }
        log.error("unexpected step 7 reached with value: " + value);
        return true;
    }

    public boolean test8(int value) {
        if (mfgID == 113) {
            productIDlow = value;
            productID = (productIDhigh * 256) + productIDlow;
            return true;
        }
        log.error("unexpected step 8 reached with value: " + value);
        return true;
    }

    protected void statusUpdate(String s) {
        message(s);
        if (s.equals("Done")) {
            done(mfgID, modelID, productID);
        } else if (log.isDebugEnabled()) {
            log.debug("received status: " + s);
        }
    }

    /** 
     * Invoked with the identifier numbers when the identification is complete.
     */
    abstract protected void done(int mfgID, int modelID, int productID);

    /**
     * Invoked to provide a user-readable message about progress
     */
    abstract protected void message(String m);

    // initialize logging
    static Logger log = LoggerFactory.getLogger(IdentifyDecoder.class.getName());

}
