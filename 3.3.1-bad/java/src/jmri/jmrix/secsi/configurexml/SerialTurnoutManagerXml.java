package jmri.jmrix.secsi.configurexml;

import org.jdom.Element;
import jmri.jmrix.secsi.*;

/**
 * Provides load and store functionality for
 * configuring SerialTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007, 2008
 * @version $Revision$
 */
public class SerialTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public SerialTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.secsi.configurexml.SerialTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        SerialTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManagerXml.class.getName());
}