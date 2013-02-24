package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import org.apache.log4j.Logger;

import jmri.NamedBeanHandle;
import jmri.configurexml.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.controlPanelEditor.shape.*;

import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 */
public class PositionableShapeXml extends AbstractXmlAdapter {

    public PositionableShapeXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionableShape
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableShape p = (PositionableShape)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("PositionableShape");
        storeCommonAttributes(p, element);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionableShapeXml");
        return element;
    }

    /**
     * Default implementation for storing the common contents
     * @param element Element in which contents are stored
     */
    public void storeCommonAttributes(PositionableShape p, Element element) {
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
        if (p.getDegrees()!=0) {
        	element.setAttribute("degrees", ""+p.getDegrees());
        }
        
        Element elem = storeColor("lineColor", p.getLineColor());
        if (elem!=null) {
            element.addContent(elem);        	
        }
        elem = storeColor("fillColor", p.getFillColor());
        if (elem!=null) {
            element.addContent(elem);        	
        }
        element.setAttribute("lineWidth", ""+p.getLineWidth());
        element.setAttribute("alpha", ""+p.getAlpha());

		NamedBeanHandle handle = p.getControlSensorHandle();
		if (handle!=null) {
	        element.setAttribute("controlSensor", handle.getName());
		}
    }
    
    public Element storeColor(String name, Color c) {
    	if (c==null) {
    		return null;
    	}
        Element elem = new Element(name);
        elem.setAttribute("red", ""+c.getRed());
        elem.setAttribute("green", ""+c.getGreen());
        elem.setAttribute("blue", ""+c.getBlue());
        return elem;
    }
   
    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
    @SuppressWarnings("unchecked")
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor)o;
        PositionableShape ps = new PositionableShape(ed);
        
        // get object class and determine editor being used
		Editor editor = (Editor)o;
        editor.putItem(ps);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }

	public void loadCommonAttributes(PositionableShape ps, int defaultLevel, Element element) {
        try {
        	Attribute attr = element.getAttribute("controlSensor");
        	if (attr!=null) {
                ps.setControlSensor(attr.getValue());
        	}
        } catch ( NullPointerException e) { 
            log.error("incorrect information for controlSensor of PositionableShape");
        }
        
       // find coordinates
        int x = getInt(element, "x");
        int y = getInt(element, "y");
        ps.setLocation(x,y);
        
        ps.setDisplayLevel(getInt(element, "level"));
        
        Attribute a = element.getAttribute("hidden");
        if ( (a!=null) && a.getValue().equals("yes")){
            ps.setHidden(true);
            ps.setVisible(false);
        }
        a = element.getAttribute("positionable");
        if ( (a!=null) && a.getValue().equals("true"))
            ps.setPositionable(true);
        else
            ps.setPositionable(false);
       
        a = element.getAttribute("showtooltip");
        if ( (a!=null) && a.getValue().equals("true"))
            ps.setShowTooltip(true);
        else
            ps.setShowTooltip(false);

        a = element.getAttribute("editable");
        if ( (a!=null) && a.getValue().equals("true"))
            ps.setEditable(true);
        else
            ps.setEditable(false);
        
        Element elem = element.getChild("toolTip");
        if (elem!=null) {
            ToolTip tip = ps.getTooltip();
            if (tip!=null) {
                tip.setText(elem.getText());
            }
        }
        ps.setLineWidth(getInt(element, "lineWidth"));
        ps.setAlpha(getInt(element, "alpha"));
        ps.setLineColor(getColor(element, "lineColor"));
        ps.setFillColor(getColor(element, "fillColor"));
        
        ps.makeShape();
        ps.rotate(getInt(element, "degrees"));
        
        ps.updateSize();
   }
	
	public Color getColor(Element element, String name) {
		Element elem = element.getChild(name);
		if (elem==null) {
			return null;
		}
		try {
	        int red = elem.getAttribute("red").getIntValue();
	        int blue = elem.getAttribute("blue").getIntValue();
	        int green = elem.getAttribute("green").getIntValue();
	        return new Color(red, green, blue);			
		} catch (Exception e) {
            log.warn("failed to convert color attribute for "+name+" - "+e);			
		}
		return null;
	}
	
	public int getInt(Element element, String name) {
		try {
			Attribute attr = element.getAttribute(name);
			if (attr!=null) {
		        int num  = attr.getIntValue();
		        return num;				
			}			
		} catch (Exception e) {
            log.error("failed to convert integer attribute for "+name+" - "+e);			
		}
		return 0;
	}
	
	public float getFloat(Element element, String name) {
		try {
			Attribute attr = element.getAttribute(name);
			if (attr!=null) {
		        float num  = attr.getFloatValue();
		        return num;
			}						
		} catch (Exception e) {
            log.error("failed to convert integer attribute for "+name+" - "+e);			
		}
		return 0;
	}
	
    static Logger log = Logger.getLogger(PositionableShapeXml.class.getName());
}
