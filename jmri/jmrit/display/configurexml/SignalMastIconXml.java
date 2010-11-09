// SignalMastIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.SignalMast;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalMastIcon;
import jmri.util.NamedBeanHandle;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalMastIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision: 1.12 $
 */
public class SignalMastIconXml extends PositionableLabelXml {

    public SignalMastIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalMastIcon
     * @param o Object to store, of type SignalMastIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SignalMastIcon p = (SignalMastIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("signalmasticon");
        
        element.setAttribute("signalmast", ""+p.getPName());
        storeCommonAttributes(p, element);
        element.setAttribute("rotation", ""+p.getRotation());
        element.setAttribute("scale", String.valueOf(p.getScale()));
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalMastIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a SignalMastIcon, then add
     * @param element Top level Element to unpack.
     * @param o  an Editor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor)o;
        SignalMastIcon l = new SignalMastIcon(ed);
        String name;
        
        Attribute attr;
        /*
         * We need to set the rotation and scaling first, prior to setting the
         * signalmast, otherwise we end up in a situation where by the icons do
         * not get rotated or scaled correctly.
         **/
        try {
            attr = element.getAttribute("rotation");
            int rotation = attr.getIntValue();
            l.rotate(rotation);
            attr = element.getAttribute("scale");
            l.setScale(attr.getDoubleValue());
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert rotation or scale attribute");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        attr = element.getAttribute("signalmast"); 
        if (attr == null) {
            log.error("incorrect information for signal mast; must use signalmast name");
            return;
        } else {
            name = attr.getValue();
            if (log.isDebugEnabled()) log.debug("Load SignalMast "+name);
        }
        
        SignalMast sh = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(name);

        if (sh != null) {
            l.setSignalMast(new NamedBeanHandle<SignalMast>(name, sh));
        } else {
            log.error("SignalMast named '"+attr.getValue()+"' not found.");
            return;
        }
                        
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SIGNALS, element);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastIconXml.class.getName());

}
