package jmri.jmrix.ecos.configurexml;

import org.jdom.Element;
import jmri.jmrix.ecos.EcosTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring EcosTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 1.5 $
 */
public class EcosTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EcosTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.ecos.configurexml.EcosTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        EcosTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosTurnoutManagerXml.class.getName());
}