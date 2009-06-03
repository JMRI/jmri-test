package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.DoubleTurnoutSignalHead;
import jmri.Turnout;

import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for DoubleTurnoutSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008
 * @version $Revision: 1.2 $
 */
public class DoubleTurnoutSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DoubleTurnoutSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * DoubleTurnoutSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DoubleTurnoutSignalHead p = (DoubleTurnoutSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getGreen()));
        element.addContent(addTurnoutElement(p.getRed()));

        return element;
    }

    Element addTurnoutElement(Turnout to) {
        String user = to.getUserName();
        String sys = to.getSystemName();

        Element el = new Element("turnout");
        el.setAttribute("systemName", sys);
        if (user!=null) el.setAttribute("userName", user);

        return el;
    }

    /**
     * Create a DoubleTurnoutSignalHead
     * @param element Top level Element to unpack.
     */
    @SuppressWarnings("unchecked")
	public void load(Element element) {
        List<Element> l = element.getChildren("turnout");
        Turnout green = loadTurnout(l.get(0));
        Turnout red = loadTurnout(l.get(1));
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new DoubleTurnoutSignalHead(sys, green, red);
        else
            h = new DoubleTurnoutSignalHead(sys, a.getValue(), green, red);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    Turnout loadTurnout(Object o) {
        Element e = (Element)o;

        // we don't create the Turnout, we just look it up.
        String sys = e.getAttribute("systemName").getValue();
        return InstanceManager.turnoutManagerInstance().getBySystemName(sys);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DoubleTurnoutSignalHeadXml.class.getName());
}