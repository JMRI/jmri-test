package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.Roster;
import org.jdom.Element;

/**
 * Handle configuration for display.LocoIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.16 $
 */
public class LocoIconXml extends PositionableLabelXml {

    public LocoIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LocoIcon
     * @param o Object to store, of type LocoIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LocoIcon p = (LocoIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("locoicon");
        storeCommonAttributes(p, element);

        // include contents
        element.setAttribute("text", p.getText());
        element.setAttribute("icon", "yes");
        element.addContent(storeIcon("icon", (NamedIcon)p.getIcon()));
        RosterEntry entry = p.getRosterEntry();
        if (entry != null)
        	element.setAttribute("rosterentry", entry.getId());
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LocoIconXml");

        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  an Editor as an Object
     */
    public void load(Element element, Object o) {
		Editor ed = (Editor) o;
        LocoIcon l= new LocoIcon(ed);
        
        loadCommonAttributes(l, Editor.MARKERS, element);
        
       // create the objects
        String name = "error";
        try {
            name = element.getAttribute("text").getValue();
         } catch ( Exception e) {
            log.error("failed to get loco text attribute ex= "+e);
        }
        l.setText (name);
        
        NamedIcon icon;
        try {
			name = element.getAttribute("icon").getValue();
		} catch (Exception e) {
			log.error("failed to get icon attribute ex= "+e);
		}
    	if (name.equals("yes")){
    		icon = loadIcon(l, "icon", element);
    	}else{
    		icon = NamedIcon.getIconByName(name);
    	}
    	l.updateIcon(icon);
    	
        String rosterId = null;
		try{
			rosterId = element.getAttribute("rosterentry").getValue();
			RosterEntry entry = Roster.instance().entryFromTitle(rosterId);
			l.setRosterEntry(entry);
		} catch (Exception e) {
			log.debug("no roster entry for "+rosterId+", ex= "+e);
		}
        ed.putLocoIcon(l);
     }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIconXml.class.getName());
}