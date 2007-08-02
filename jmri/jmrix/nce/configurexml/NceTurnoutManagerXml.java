package jmri.jmrix.nce.configurexml;

import org.jdom.Element;
import jmri.jmrix.nce.NceTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring NceTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.6 $
 */
public class NceTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public NceTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.nce.configurexml.NceTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        NceTurnoutManager mgr = NceTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManagerXml.class.getName());
}