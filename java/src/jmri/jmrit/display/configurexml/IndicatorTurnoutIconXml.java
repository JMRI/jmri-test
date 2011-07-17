package jmri.jmrit.display.configurexml;

import jmri.NamedBean;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.IndicatorTurnoutIconXml objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2010
 * @version $Revision: 1.10 $
 */
public class IndicatorTurnoutIconXml extends PositionableLabelXml {

    public IndicatorTurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * IndicatorTurnoutIcon
     * @param o Object to store, of type IndicatorTurnoutIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        IndicatorTurnoutIcon p = (IndicatorTurnoutIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("indicatorturnouticon");
        storeCommonAttributes(p, element);

        Turnout t = p.getTurnout();
        if (t!=null) {
            element.addContent(storeBean("turnout", t));
        }
        OBlock b = p.getOccBlock();
        if (b!=null) {
            element.addContent(storeBean("occupancyblock", b));
        }
        Sensor s = p.getOccSensor();
        if (s!=null) {
            element.addContent(storeBean("occupancysensor", s));
        }
        /*
        s = p.getErrSensor();
        if (s!=null) {
            element.addContent(storeBean("errorsensor", s));
        }  */

        Element elem = new Element("showTrainName");
        String show = "no";
        if (p.showTrain()) { show = "yes"; }
        elem.addContent(show);
        element.addContent(elem);

        Hashtable<String, Hashtable<Integer, NamedIcon>> iconMaps = p.getIconMaps();
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = iconMaps.entrySet().iterator();
        Element el = new Element("iconmaps");
        String family = p.getFamily();
        if (family!=null) {
            el.setAttribute("family", family);
        }
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> ent = it.next();
            elem = new Element(ent.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = ent.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> entry = iter.next();
                elem.addContent(storeIcon(p.getStateName(entry.getKey()), entry.getValue()));
            }
            el.addContent(elem);
        }
        element.addContent(el);

        elem = new Element("paths");
        Iterator<String> iter = p.getPaths();
        if (iter!=null) {
            while (iter.hasNext()) {
                Element e = new Element("path");
                e.addContent(iter.next());
                elem.addContent(e);
            }
            element.addContent(elem);
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.IndicatorTurnoutIconXml");

        return element;
    }

    Element storeBean(String elemName, NamedBean nb) {
        Element elem = new Element(elemName);
        elem.addContent(nb.getSystemName());
        return elem;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a IndicatorTurnoutIcon, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
	public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor)o;

        IndicatorTurnoutIcon l = new IndicatorTurnoutIcon(p);
        Element name = element.getChild("turnout");
        
        if (name==null) {
            log.error("incorrect information for turnout; must use turnout name");
        } else {
            l.setTurnout(name.getText());
        }
        Element elem = element.getChild("iconmaps");
        if (elem!=null) {
            @SuppressWarnings("unchecked")
            List<Element>maps = elem.getChildren();
            if (maps.size()>0) {
                for (int i=0; i<maps.size(); i++) {
                    String status = maps.get(i).getName();
                    @SuppressWarnings("unchecked")
                    List<Element>states = maps.get(i).getChildren();
                    for (int k=0; k<states.size(); k++) {
                        String msg = "IndicatorTurnout \""+l.getNameString()+"\" icon \""+states.get(k).getName()+"\" ";
                        NamedIcon icon = loadIcon(l, states.get(k).getName(), maps.get(i),
                                                  msg, p);
                        if (icon!=null) {
                            l.setIcon(status, states.get(k).getName(), icon);
                        } else {
                            log.info(msg+" removed for url= "+name);
                            return;
                        }
                    }
                }
            }
            Attribute attr = elem.getAttribute("family");
            if (attr!=null) {
                l.setFamily(attr.getValue());
            }
        }

        name = element.getChild("occupancyblock");
        if (name!=null) {
            l.setOccBlock(name.getText());
        }
        name = element.getChild("occupancysensor");
        if (name!=null) {
            l.setOccSensor(name.getText());
        }
/*        name = element.getChild("errorsensor");
        if (name!=null) {
            l.setErrSensor(name.getText());
        }  */
        
        l.setShowTrain(false);
        name = element.getChild("showTrainName");
        if (name!=null) {
            if ("yes".equals(name.getText())) l.setShowTrain(true);
        }
        
        elem = element.getChild("paths");
        if (elem!=null) {
            ArrayList<String> paths = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            List<Element>pth = elem.getChildren();
            for (int i=0; i<pth.size(); i++) {
                paths.add(pth.get(i).getText());
            }
            l.setPaths(paths);
        }
            
        l.updateSize();
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTurnoutIconXml.class.getName());
}
