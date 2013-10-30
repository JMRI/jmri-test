package jmri.jmrit.symbolicprog.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.symbolicprog.ProgrammerConfigPane;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML persistance of symbolic programmer default values.
 * <P>
 * This class is named as being the persistant form of the
 * ProgrammerConfigPane class, but there's no object of that
 * form created when this is read back.  Instead, this persists static members of the
 * symbolicprog.CombinedLocoSelPane class.
 *<P>
 * This class sets the default programmer file in the ProgDefaults class.
 * On MacOS Classic, however, that information was being overwritten
 * by a second initialization of the class; in other words, the clinit
 * class initialization routine was being run later for unknown reasons.
 * The fix to this was to add an explicit construction of a 
 * CombinedLocoSelPane object.  It is <b>not</b> known why this works!
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
public class ProgrammerConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public ProgrammerConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        ProgrammerConfigPane p = (ProgrammerConfigPane) o;
        Element programmer = new Element("programmer");
        if (p.getSelectedItem() != null) { 
            programmer.setAttribute("defaultFile", p.getSelectedItem());
        }
        programmer.setAttribute("verifyBeforeWrite", "no");
        if (!p.getShowEmptyTabs()) programmer.setAttribute("showEmptyPanes", "no");
        if (p.getShowCvNums()) programmer.setAttribute("showCvNumbers", "yes");
        programmer.setAttribute("class", this.getClass().getName());
        return programmer;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element element) {
    	boolean result = true;
    	
    	if (element.getAttribute("defaultFile") != null) {
            if (log.isDebugEnabled()) log.debug("set programmer default file: "+element.getAttribute("defaultFile").getValue());
            jmri.jmrit.symbolicprog.ProgDefault.setDefaultProgFile(element.getAttribute("defaultFile").getValue());
        }
        
		// ugly hack to avoid static re-initialization, see comment at
		// top of file.
		new  jmri.jmrit.symbolicprog.CombinedLocoSelPane();

        Attribute a;
        if (null != (a = element.getAttribute("showEmptyPanes"))){
            if ( a.getValue().equals("no"))
                PaneProgFrame.setShowEmptyPanes(false);
            else
                PaneProgFrame.setShowEmptyPanes(true);
        }
        if (null != (a = element.getAttribute("showCvNumbers"))){
            if ( a.getValue().equals("yes"))
                PaneProgFrame.setShowCvNumbers(true);
            else
                PaneProgFrame.setShowCvNumbers(false);
        }
        jmri.InstanceManager.configureManagerInstance().registerPref(new jmri.jmrit.symbolicprog.ProgrammerConfigPane(true));
        return result;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.warn("unexpected call of 2nd load form");
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(ProgrammerConfigPaneXml.class.getName());

}
