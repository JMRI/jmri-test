package jmri.jmrix.easydcc.configurexml;

import org.jdom.Element;

import jmri.jmrix.easydcc.EasyDccTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring EasyDccTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.9 $
 */
public class EasyDccTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EasyDccTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.easydcc.configurexml.EasyDccTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        EasyDccTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EasyDccTurnoutManagerXml.class.getName());
}