package jmri.jmrit.display.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.PanelEditor;
import java.awt.Dimension;
import java.awt.Point;

import java.util.List;
import org.jdom.*;

/**
 * Handle configuration for {@link PanelEditor} panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.30 $
 */
public class PanelEditorXml implements XmlAdapter {

    public PanelEditorXml() {}

    /**
     * Default implementation for storing the contents of a
     * PanelEditor
     * @param o Object to store, of type PanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PanelEditor p = (PanelEditor)o;
        Element panel = new Element("paneleditor");

        Dimension size = p.getFrame().getSize();
        Point posn = p.getFrame().getLocation();

        panel.setAttribute("class", "jmri.jmrit.display.configurexml.PanelEditorXml");
        panel.setAttribute("name", ""+p.getFrame().getTitle());
        panel.setAttribute("x", ""+posn.x);
        panel.setAttribute("y", ""+posn.y);
        panel.setAttribute("height", ""+size.height);
        panel.setAttribute("width", ""+size.width);
        panel.setAttribute("editable", ""+(p.isEditable()?"yes":"no"));
        panel.setAttribute("positionable", ""+(p.isPositionable()?"yes":"no"));
        panel.setAttribute("showcoordinates", ""+(p.isShowCoordinates()?"yes":"no"));
        panel.setAttribute("controlling", ""+(p.isControlling()?"yes":"no"));
        panel.setAttribute("hide", p.isVisible()?"no":"yes");
        panel.setAttribute("panelmenu", p.hasPanelMenu()?"yes":"no");
        panel.setAttribute("scrollable", p.isScrollable()?"yes":"no");

        // include contents

        if (log.isDebugEnabled()) log.debug("N elements: "+p.contents.size());
        for (int i=0; i<p.contents.size(); i++) {
            Object sub = p.contents.get(i);
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e!=null) panel.addContent(e);
            } catch (Exception e) {
                log.error("Error storing panel element: "+e);
                e.printStackTrace();
            }
        }

        return panel;
    }


    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a PanelEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        // find the name
        String name = "Panel";
        if (element.getAttribute("name")!=null)
            name = element.getAttribute("name").getValue();
        // create the objects
        PanelEditor panel = new PanelEditor();
        panel.makeFrame(name);
        // confirm that panel hasn't already been loaded
        if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
        	log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
        }
		jmri.jmrit.display.PanelMenu.instance().addPanelEditorPanel(panel);
        panel.getFrame().setLocation(x,y);
        panel.getFrame().setSize(width,height);
        
        panel.setTitle();

        // load the contents
        List items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via "+adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item, panel);
            } catch (Exception e) {
                log.error("Exception while loading "+item.getName()+":"+e);
                e.printStackTrace();
            }
        }

        // Set contents state.
        // This has to be done after the items are
        // loaded, because it will over-write various
        // controls within them.
        Attribute a;
        boolean value = true;
        if ((a = element.getAttribute("editable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllEditable(value);

        value = true;
        if ((a = element.getAttribute("positionable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllPositionable(value);
        
        value = false;
        if ((a = element.getAttribute("showcoordinates"))!=null && a.getValue().equals("yes"))
            value = true;
        panel.setShowCoordinates(value);

        value = true;
        if ((a = element.getAttribute("controlling"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllControlling(value);

        boolean hide = false;
        if ((a = element.getAttribute("hide"))!=null && a.getValue().equals("yes"))
            hide = true;

        value = true;
        if ((a = element.getAttribute("panelmenu"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setPanelMenu(value);

        value = true;
        if ((a = element.getAttribute("scrollable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setScrollable(value);

        // display the results, with the editor in back
        panel.pack();

        if (!hide) panel.setVisible(true);    // show the editor if wanted

        // we don't pack the target frame here, because size was specified
        // TODO: Work out why, when calling this method, panel size is increased
        // vertically (at least on MS Windows)
        panel.getFrame().setVisible(true);    // always show the panel

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

        // reset the size and position, in case the display caused it to change
        panel.getFrame().setLocation(x,y);
        panel.getFrame().setSize(width,height);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}