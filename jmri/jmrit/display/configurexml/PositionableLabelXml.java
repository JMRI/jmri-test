package jmri.jmrit.display.configurexml;

import jmri.configurexml.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.ToolTip;
import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Handle configuration for display.PositionableLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.62 $
 */
public class PositionableLabelXml extends AbstractXmlAdapter {

    public PositionableLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionableLabel
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableLabel p = (PositionableLabel)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("positionablelabel");
        storeCommonAttributes(p, element);

        if (p.isText()) {
            if (p.getText()!=null) element.setAttribute("text", p.getText());
            storeTextInfo(p, element);
        }
        
        if (p.isIcon() && p.getIcon()!=null) {
            element.setAttribute("icon", "yes");
            element.addContent(storeIcon("icon", (NamedIcon)p.getIcon()));
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.PositionableLabelXml");
        return element;
    }

    /**
     * Store the text formatting information.
     * <p>
     * This is always stored, even if the icon isn't in text mode,
     * because some uses (subclasses) of PositionableLabel flip
     * back and forth between icon and text, and want to remember their
     * formatting.
     */
    protected void storeTextInfo(Positionable p, Element element) {
        //if (p.getText()!=null) element.setAttribute("text", p.getText());
        jmri.jmrit.display.PositionablePopupUtil util = p.getPopupUtility();
        element.setAttribute("size", ""+util.getFontSize());
        element.setAttribute("style", ""+util.getFontStyle());
        if (!util.getForeground().equals(Color.black)) {
            element.setAttribute("red", ""+util.getForeground().getRed());
            element.setAttribute("green", ""+util.getForeground().getGreen());
            element.setAttribute("blue", ""+util.getForeground().getBlue());
        }
        if(p.isOpaque()){
            element.setAttribute("redBack", ""+util.getBackground().getRed());
            element.setAttribute("greenBack", ""+util.getBackground().getGreen());
            element.setAttribute("blueBack", ""+util.getBackground().getBlue());
        }
        if (util.getMargin()!=0)
            element.setAttribute("margin", ""+util.getMargin());
        if (util.getBorderSize()!=0){
            element.setAttribute("borderSize", ""+util.getBorderSize());
            element.setAttribute("redBorder", ""+util.getBorderColor().getRed());
            element.setAttribute("greenBorder", ""+util.getBorderColor().getGreen());
            element.setAttribute("blueBorder", ""+util.getBorderColor().getBlue());
        } 
        if (util.getFixedWidth()!=0)
            element.setAttribute("fixedWidth", ""+util.getFixedWidth());
        if (util.getFixedHeight()!=0)
            element.setAttribute("fixedHeight", ""+util.getFixedHeight());

        String just;
        switch (util.getJustification()){
            case 0x02 : just="right";
                        break;
            case 0x04 : just ="centre";
                        break;
            default :   just="left";
                        break;
            }
        element.setAttribute("justification", just);
        
        //return element;
    }
    /**
     * Default implementation for storing the common contents of an Icon
     * @param element Element in which contents are stored
     */
    public void storeCommonAttributes(Positionable p, Element element) {

        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("forcecontroloff", !p.isControlling()?"true":"false");
        element.setAttribute("hidden", p.isHidden()?"yes":"no");
        element.setAttribute("positionable", p.isPositionable()?"true":"false");
        element.setAttribute("showtooltip", p.showTooltip()?"true":"false");        
        element.setAttribute("editable", p.isEditable()?"true":"false");        
        ToolTip tip = p.getTooltip();
        String txt = tip.getText();
        if (txt!=null) {
            Element elem = new Element("toolTip").addContent(txt);
            element.addContent(elem);
        }
    }

    public Element storeIcon(String elemName, NamedIcon icon) {
        if (icon==null) {
            return null;
        }
        Element element = new Element(elemName);
        element.setAttribute("url", icon.getURL());        
        element.setAttribute("degrees", String.valueOf(icon.getDegrees()));
        element.setAttribute("scale", String.valueOf(icon.getScale()));

        // the "rotate" attribute was deprecated in 2.9.4, replaced by the "rotation" element
        element.addContent(new Element("rotation").addContent(String.valueOf(icon.getRotation())));
        
        return element;
    }
    
    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
    @SuppressWarnings("unchecked")
    public void load(Element element, Object o) {
        // create the objects
        PositionableLabel l = null;
        
        // get object class and determine editor being used
		Editor editor = (Editor)o;
        if (element.getAttribute("icon")!=null) {
        	NamedIcon icon = null;
        	String name = element.getAttribute("icon").getValue();
//            if (log.isDebugEnabled()) log.debug("icon attribute= "+name);
        	if (name.equals("yes")){
        		icon = getNamedIcon("icon", element);
        	}else{
        		icon = NamedIcon.getIconByName(name);
                if (icon==null) {
                    log.error("getIconByName: icon not found for url= "+name);
                }
        	}
            if (icon==null) {
                editor.loadFailed();
                return;
            }
            l = new PositionableLabel(icon, editor);
            l.setPopupUtility(null);        // no text 
            try {
                Attribute a = element.getAttribute("rotate");
                if (a!=null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom.DataConversionException e) {}

        	if (name.equals("yes")) {
                NamedIcon nIcon = loadIcon(l,"icon", element); 
                if (nIcon!=null) {
                    l.updateIcon(nIcon);
                } else {
                    editor.loadFailed();
                    return;
                }
            } else {   // for very old files 
                if (icon!=null) {
                    l.updateIcon(icon);
                }
            }

            //l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        } else if (element.getAttribute("text")!=null) {
            l = new PositionableLabel(element.getAttribute("text").getValue(), editor);
            loadTextInfo(l, element);
        
        } else {
        	log.error("PositionableLabel is null!");
            if (log.isDebugEnabled()) {
                java.util.List <Attribute> attrs = element.getAttributes();
                log.debug("\tElement Has "+attrs.size()+" Attributes:");
                for (int i=0; i<attrs.size(); i++) {
                    Attribute a = attrs.get(i);
                    log.debug("\t\t"+a.getName()+" = "+a.getValue());
                }
                java.util.List <Element> kids = element.getChildren();
                log.debug("\tElementHas "+kids.size()+" children:");
                for (int i=0; i<kids.size(); i++) {
                    Element e = kids.get(i);
                    log.debug("\t\t"+e.getName()+" = \""+e.getValue()+"\"");
                }
            }
            editor.loadFailed();
        	return;
        }
        editor.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.LABELS, element);
    }

    protected void loadTextInfo(Positionable l, Element element) {
        if (log.isDebugEnabled()) log.debug("loadTextInfo");
        jmri.jmrit.display.PositionablePopupUtil util = l.getPopupUtility();
        if (util==null) {
            log.warn("PositionablePopupUtil is null! "+element.toString());
            return;
        }
        Attribute a = element.getAttribute("size");
        try {
            if (a!=null) util.setFontSize(a.getFloatValue());
        } catch (DataConversionException ex) {
            log.warn("invalid size attribute value");
        }
        a = element.getAttribute("style");
        try {
            if (a!=null){
                int style = a.getIntValue();
                int drop = 0;
                switch (style){
                    case 0: drop = 1; //0 Normal
                            break;
                    case 2: drop = 1; //italic
                            break;
                }
                util.setFontStyle(style, drop);
            }
        } catch (DataConversionException ex) {
            log.warn("invalid style attribute value");
        }

        // set color if needed
        try {
            int red = element.getAttribute("red").getIntValue();
            int blue = element.getAttribute("blue").getIntValue();
            int green = element.getAttribute("green").getIntValue();
            util.setForeground(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        try {
            int red = element.getAttribute("redBack").getIntValue();
            int blue = element.getAttribute("blueBack").getIntValue();
            int green = element.getAttribute("greenBack").getIntValue();
            util.setBackgroundColor(new Color(red, green, blue));
         } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse background color attributes!");
        } catch ( NullPointerException e) {  
            util.setBackgroundColor(null);// if the attributes are not listed, we consider the background as clear.
        }
        
        int fixedWidth=0;
        int fixedHeight=0;
        try {
            fixedHeight=element.getAttribute("fixedHeight").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse fixed Height attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        try {
            fixedWidth=element.getAttribute("fixedWidth").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse fixed Width attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        if (!(fixedWidth==0 && fixedHeight==0))
            util.setFixedSize(fixedWidth, fixedHeight);
        int margin=0;
        if ((util.getFixedWidth()==0) || (util.getFixedHeight()==0)){
            try {
                margin=element.getAttribute("margin").getIntValue();
                util.setMargin(margin);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse margin attribute!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
        }
        try {
            util.setBorderSize(element.getAttribute("borderSize").getIntValue());
            int red = element.getAttribute("redBorder").getIntValue();
            int blue = element.getAttribute("blueBorder").getIntValue();
            int green = element.getAttribute("greenBorder").getIntValue();
            util.setBorderColor(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse border attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }

        a = element.getAttribute("justification");
        if(a!=null)
            util.setJustification(a.getValue());
        else
            util.setJustification("left");

    }

	public void loadCommonAttributes(Positionable l, int defaultLevel, Element element) {
        Attribute a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setControlling(false);
        else
            l.setControlling(true);
            
        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);
        
        // find display level
        int level = defaultLevel;
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        
        a = element.getAttribute("hidden");
        if ( (a!=null) && a.getValue().equals("yes")){
            l.setHidden(true);
            l.setVisible(false);
        }
        a = element.getAttribute("positionable");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setPositionable(true);
        else
            l.setPositionable(false);
       
        a = element.getAttribute("showtooltip");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setShowTooltip(true);
        else
            l.setShowTooltip(false);

        a = element.getAttribute("editable");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setEditable(true);
        else
            l.setEditable(false);

        Element elem = element.getChild("toolTip");
        if (elem!=null) {
            ToolTip tip = l.getTooltip();
            if (tip!=null) {
                tip.setText(elem.getText());
            }
        }
    }
    
	public NamedIcon loadIcon(PositionableLabel l, String attrName, Element element) {
        NamedIcon icon = getNamedIcon(attrName, element);
        if (icon != null) {
            try {
                int deg = 0;
                double scale = 1.0;
            	Element elem = element.getChild(attrName);
                if (elem!=null) {
                    Attribute a = elem.getAttribute("degrees");
                    if (a!=null) {
                        deg = a.getIntValue();
                    }
                    a =  elem.getAttribute("scale");
                    if (a!=null) {
                        scale = elem.getAttribute("scale").getDoubleValue();
                    }
                    icon.setLoad(deg, scale, l);
                    if ( deg==0) {
                        // "rotate" attribute is JMRI 2.9.3 and before
                        a = elem.getAttribute("rotate");
                        if (a!=null) {
                            int rotation = a.getIntValue();
                            // 2.9.3 and before, only unscaled icons rotate
                            if (scale == 1.0) icon.setRotation(rotation, l);
                        }
                        // "rotation" element is JMRI 2.9.4 and after
                        Element e = elem.getChild("rotation");
                        if (e!=null) {
                            // ver 2.9.4 allows orthogonal rotations of scaled icons
                            int rotation = Integer.parseInt(e.getText());
                            icon.setRotation(rotation, l);
                        }
                    }
                }
            } catch (org.jdom.DataConversionException dce) {}
        }
        return icon;
    }
    
    
    protected NamedIcon getNamedIcon(String childName, Element element){
        NamedIcon icon = null;
        Element elem = element.getChild(childName);
        if (elem != null) {
            String name = elem.getAttribute("url").getValue();
            icon = NamedIcon.getIconByName(name);
            if (icon==null) {
                log.error("getNamedIcon: element \""+childName+"\" icon not found for Attribute \"url\"= "+name);
            }
        } else {
            log.debug("getNamedIcon: child element \""+childName+"\" not found in element "+element.getName());
        }
        return icon;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableLabelXml.class.getName());
}