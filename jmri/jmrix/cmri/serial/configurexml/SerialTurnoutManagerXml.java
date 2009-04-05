package jmri.jmrix.cmri.serial.configurexml;

import org.jdom.Element;

import jmri.jmrix.cmri.serial.*;

/**
 * Provides load and store functionality for
 * configuring SerialTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.5 $
 */
public class SerialTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public SerialTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.cmri.serial.configurexml.SerialTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        SerialTurnoutManager mgr = SerialTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManagerXml.class.getName());
}