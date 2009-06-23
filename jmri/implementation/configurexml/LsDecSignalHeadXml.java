// LsDecSignalHeadXml.java

package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.LsDecSignalHead;
import jmri.Turnout;

import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for LsDecSignalHead objects.
 *
 * This file is part of JMRI.
 * 
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * 
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Petr Koud'a  Copyright: Copyright (c) 2007
 * @version $Revision: 1.3 $
 */
public class LsDecSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LsDecSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * LsDecSignalHead
     * @param o Object to store, of type LsDecSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        LsDecSignalHead p = (LsDecSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getGreen(), p.getGreenState()));
        element.addContent(addTurnoutElement(p.getYellow(), p.getYellowState()));
        element.addContent(addTurnoutElement(p.getRed(), p.getRedState()));
        element.addContent(addTurnoutElement(p.getFlashGreen(), p.getFlashGreenState()));
        element.addContent(addTurnoutElement(p.getFlashYellow(), p.getFlashYellowState()));
        element.addContent(addTurnoutElement(p.getFlashRed(), p.getFlashRedState()));
        element.addContent(addTurnoutElement(p.getDark(), p.getDarkState()));
        
        return element;
    }

    Element addTurnoutElement(Turnout to, int s) {
        String user = to.getUserName();
        String sys = to.getSystemName();
        int state = s;
        
        Element el = new Element("turnout");
        el.setAttribute("systemName", sys);
        if (user!=null) el.setAttribute("userName", user);
        if (state == Turnout.THROWN) {
            el.setAttribute("state","THROWN");
        }
        else {
            el.setAttribute("state","CLOSED");
        }

        return el;
    }

    /**
     * Create a LsDecSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnout");
        Turnout green = loadTurnout(l.get(0));
        Turnout yellow = loadTurnout(l.get(1));
        Turnout red = loadTurnout(l.get(2));
        Turnout flashgreen = loadTurnout(l.get(3));
        Turnout flashyellow = loadTurnout(l.get(4));
        Turnout flashred = loadTurnout(l.get(5));
        Turnout dark = loadTurnout(l.get(6));
        int greenstatus = loadTurnoutStatus(l.get(0));
        int yellowstatus = loadTurnoutStatus(l.get(1));
        int redstatus = loadTurnoutStatus(l.get(2));
        int flashgreenstatus = loadTurnoutStatus(l.get(3));
        int flashyellowstatus = loadTurnoutStatus(l.get(4));
        int flashredstatus = loadTurnoutStatus(l.get(5));
        int darkstatus = loadTurnoutStatus(l.get(6));
        
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new LsDecSignalHead(sys, green, greenstatus, yellow, yellowstatus, red, redstatus, flashgreen, flashgreenstatus, flashyellow, flashyellowstatus, flashred, flashredstatus, dark, darkstatus);
        else
            h = new LsDecSignalHead(sys, a.getValue(), green, greenstatus, yellow, yellowstatus, red, redstatus, flashgreen, flashgreenstatus, flashyellow, flashyellowstatus, flashred, flashredstatus, dark, darkstatus);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    Turnout loadTurnout(Object o) {
        Element e = (Element)o;

        // we don't create the Turnout, we just look it up.
        String sys = e.getAttribute("systemName").getValue();
        return InstanceManager.turnoutManagerInstance().getBySystemName(sys);
    }

    int loadTurnoutStatus(Object o) {
        Element e = (Element)o;
        String rState = e.getAttribute("state").getValue();
        int tSetState = Turnout.CLOSED;
        if (rState.equals("THROWN")) {
            tSetState = Turnout.THROWN;
        }
        return tSetState;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LsDecSignalHeadXml.class.getName());
}
