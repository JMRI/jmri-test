package jmri.jmrix.lenz.hornbyelite.configurexml;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring XNetTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Paul Bender Copyright: Copyright (c) 2008
 * @version $Revision: 1.6 $
 */
public class EliteXNetTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EliteXNetTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.lenz.configurexml.XNetTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteXNetTurnoutManagerXml.class.getName());

}
