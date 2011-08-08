package jmri.jmrix.xpa.configurexml;

import org.jdom.Element;
import jmri.jmrix.xpa.XpaTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring XpaTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Paul Bender Copyright (c) 2004
 * @version $Revision$
 */
public class XpaTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public XpaTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.xpa.configurexml.XpaTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        XpaTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XpaTurnoutManagerXml.class.getName());
}
