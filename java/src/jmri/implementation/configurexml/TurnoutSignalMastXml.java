package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.implementation.TurnoutSignalMast;
import jmri.SignalAppearanceMap;
import java.util.List;
import org.jdom.Element;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 18102 $
 */
public class TurnoutSignalMastXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TurnoutSignalMastXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        TurnoutSignalMast p = (TurnoutSignalMast)o;
        Element e = new Element("signalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e);
        SignalAppearanceMap appMap = p.getAppearanceMap();
        if(appMap!=null){
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while(aspects.hasMoreElements()){
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("turnout").addContent(p.getTurnoutName(key)));
                if(p.getTurnoutState(key)==Turnout.CLOSED)
                    el.addContent(new Element("turnoutstate").addContent("closed"));
                else
                    el.addContent(new Element("turnoutstate").addContent("thrown"));
                e.addContent(el);
            }
        }
        if(p.resetPreviousStates())
            e.addContent(new Element("resetPreviousStates").addContent("yes"));
        return e;
    }

    /**
     * Create a DefaultSignalMastManager
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element element) {
        TurnoutSignalMast m;
        String sys = getSystemName(element);
        m = new jmri.implementation.TurnoutSignalMast(sys);
        
        if (getUserName(element) != null)
            m.setUserName(getUserName(element));
        
        loadCommon(m, element);
        
        @SuppressWarnings("unchecked")
        List<Element> list = element.getChildren("aspect");
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String aspect = e.getAttribute("defines").getValue();
            String turnout = e.getChild("turnout").getText();
            String turnoutState = e.getChild("turnoutstate").getText();
            int turnState = Turnout.THROWN;
            if(turnoutState.equals("closed"))
                turnState = Turnout.CLOSED;
            m.setTurnout(aspect, turnout, turnState);
        }
        if (( element.getChild("resetPreviousStates") != null) && 
            element.getChild("resetPreviousStates").getText().equals("yes") ){
                m.resetPreviousStates(true);
        }
        
        InstanceManager.signalMastManagerInstance()
            .register(m);
        
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutSignalMastXml.class.getName());
}