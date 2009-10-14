package jmri.jmrit.blockboss.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.blockboss.BlockBossLogic;
import java.util.Enumeration;

import java.util.List;
import org.jdom.Element;

/**
 * Handle XML persistance of Simple Signal Logic objects.
 *
 * <p>
 * In JMRI 2.1.5, the XML written by this 
 * package was changed. 
 * <p>
 * Previously, it wrote a single
 * "blocks" element, which contained multiple "block" elements 
 * to represent each individual BlockBoss (Simple Signal Logic) object.
 * <p>
 * These names were too generic, and conflicted with storing true Block
 * objects.
 * <p>
 * Starting in JMRI 2.1.5 (May 2008), these were changed to 
 * "signalelements" and "signalelement" respectively.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2005
 * @version $Revision: 1.17 $
 * 
 * Revisions to add facing point sensors, approach lighting, 
 * and limited speed.                 Dick Bronson (RJB) 2006
 */

public class BlockBossLogicXml implements XmlAdapter {

    public BlockBossLogicXml() {
    }

    /**
     * Default implementation for storing the contents of
     * all the BLockBossLogic elements.
     * <P>
     * Static members in the BlockBossLogic class record the
     * complete set of items.  This function writes those out
     * as a single XML element.
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        Enumeration<BlockBossLogic> e = BlockBossLogic.entries();
        if (!e.hasMoreElements()) return null;  // nothing to write!
        Element blocks = new Element("signalelements");
        blocks.setAttribute("class", this.getClass().getName());

        while ( e.hasMoreElements()) {
            BlockBossLogic p = e.nextElement();
            Element block = new Element("signalelement");
            block.setAttribute("signal", p.getDrivenSignal());
            block.setAttribute("mode", ""+p.getMode());

            if (p.getApproachSensor1()!=null) {
                block.setAttribute("approachsensor1", p.getApproachSensor1());
            }

            if (p.getSensor1()!=null) block.addContent(storeSensor(p.getSensor1()));
            if (p.getSensor2()!=null) block.addContent(storeSensor(p.getSensor2()));
            if (p.getSensor3()!=null) block.addContent(storeSensor(p.getSensor3()));
            if (p.getSensor4()!=null) block.addContent(storeSensor(p.getSensor4()));

            if (p.getTurnout()!=null) {
                block.setAttribute("watchedturnout", p.getTurnout());
            }
            if (p.getWatchedSignal1()!=null) {
                block.setAttribute("watchedsignal1", p.getWatchedSignal1());
            }
            if (p.getWatchedSignal1Alt()!=null) {
                block.setAttribute("watchedsignal1alt", p.getWatchedSignal1Alt());
            }
            if (p.getWatchedSignal2()!=null) {
                block.setAttribute("watchedsignal2", p.getWatchedSignal2());
            }
            if (p.getWatchedSignal2Alt()!=null) {
                block.setAttribute("watchedsignal2alt", p.getWatchedSignal2Alt());
            }
            if (p.getWatchedSensor1()!=null) {
                block.setAttribute("watchedsensor1", p.getWatchedSensor1());
            }
            if (p.getWatchedSensor1Alt()!=null) {
                block.setAttribute("watchedsensor1alt", p.getWatchedSensor1Alt());
            }
            if (p.getWatchedSensor2()!=null) {
                block.setAttribute("watchedsensor2", p.getWatchedSensor2());
            }
            if (p.getWatchedSensor2Alt()!=null) {
                block.setAttribute("watchedsensor2alt", p.getWatchedSensor2Alt());
            }
  
            block.setAttribute("limitspeed1", ""+p.getLimitSpeed1());
            block.setAttribute("limitspeed2", ""+p.getLimitSpeed2());
            block.setAttribute("useflashyellow", ""+p.getUseFlash());
            block.setAttribute("distantsignal", ""+p.getDistantSignal());

            // add comment, if present
            if (p.getComment() != null) {
                Element c = new Element("comment");
                c.addContent(p.getComment());
                block.addContent(c);
            }
    
            blocks.addContent(block);

        }

        return blocks;
    }

    Element storeSensor(String name) {
        Element e = new Element("sensorname");
        e.addContent(name);
        return e;
    }

    /**
     * Update static data from XML file
     * @param element Top level blocks Element to unpack.
     * @return true if successful
      */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
    	boolean result = true;
        List<Element> l = element.getChildren("signalelement");
        
        // try old format if there are no new entries
        // this is for backward compatibility only
        if (l.size() == 0)
            l = element.getChildren("block");
            
        // process each item
        for (int i = 0; i<l.size(); i++) {
            Element block = l.get(i);
            BlockBossLogic bb = BlockBossLogic.getStoppedObject(block.getAttributeValue("signal"));
            if (block.getAttribute("approachsensor1")!=null)
                bb.setApproachSensor1(block.getAttributeValue("approachsensor1"));
            if (block.getAttribute("watchedsensor")!=null)   // for older XML files
                bb.setSensor1(block.getAttributeValue("watchedsensor"));

            // old form of sensors with system names
            List<Element> sl = block.getChildren("sensor");
            if (sl.size()>=1 && sl.get(0)!= null) bb.setSensor1(sl.get(0).getAttributeValue("systemName"));
            if (sl.size()>=2 && sl.get(1)!= null) bb.setSensor2(sl.get(1).getAttributeValue("systemName"));
            if (sl.size()>=3 && sl.get(2)!= null) bb.setSensor3(sl.get(2).getAttributeValue("systemName"));
            if (sl.size()>=4 && sl.get(3)!= null) bb.setSensor4(sl.get(3).getAttributeValue("systemName"));

            // new form of sensors with system names
            sl = block.getChildren("sensorname");
            if (sl.size()>=1 && sl.get(0)!= null) bb.setSensor1(sl.get(0).getText());
            if (sl.size()>=2 && sl.get(1)!= null) bb.setSensor2(sl.get(1).getText());
            if (sl.size()>=3 && sl.get(2)!= null) bb.setSensor3(sl.get(2).getText());
            if (sl.size()>=4 && sl.get(3)!= null) bb.setSensor4(sl.get(3).getText());

            try {
                bb.setMode(block.getAttribute("mode").getIntValue());
                if (block.getAttribute("distantsignal")!=null)
                    bb.setDistantSignal(block.getAttribute("distantsignal").getBooleanValue());
                if (block.getAttribute("limitspeed1")!=null)
                    bb.setLimitSpeed1(block.getAttribute("limitspeed1").getBooleanValue());
                if (block.getAttribute("limitspeed2")!=null)
                    bb.setLimitSpeed2(block.getAttribute("limitspeed2").getBooleanValue());
                if (block.getAttribute("watchedturnout")!=null)
                    bb.setTurnout(block.getAttributeValue("watchedturnout"));
                if (block.getAttribute("watchedsignal1")!=null)
                    bb.setWatchedSignal1(block.getAttributeValue("watchedsignal1"),
                                        block.getAttribute("useflashyellow").getBooleanValue());
                if (block.getAttribute("watchedsignal1alt")!=null)
                    bb.setWatchedSignal1Alt(block.getAttributeValue("watchedsignal1alt"));
                if (block.getAttribute("watchedsignal2")!=null)
                    bb.setWatchedSignal2(block.getAttributeValue("watchedsignal2"));
                if (block.getAttribute("watchedsignal2alt")!=null)
                    bb.setWatchedSignal2Alt(block.getAttributeValue("watchedsignal2alt"));
                if (block.getAttribute("watchedsensor1")!=null)
                    bb.setWatchedSensor1(block.getAttributeValue("watchedsensor1"));
                if (block.getAttribute("watchedsensor1alt")!=null)
                    bb.setWatchedSensor1Alt(block.getAttributeValue("watchedsensor1alt"));
                if (block.getAttribute("watchedsensor2")!=null)
                    bb.setWatchedSensor2(block.getAttributeValue("watchedsensor2"));
                if (block.getAttribute("watchedsensor2alt")!=null)
                    bb.setWatchedSensor2Alt(block.getAttributeValue("watchedsensor2alt"));
            
                // load comment, if present
                String c = block.getChildText("comment");
                if (c != null) {
                    bb.setComment(c);
                }

            } catch (org.jdom.DataConversionException e) {
                log.warn("error reading blocks from file"+e);
                result = false;
            }
            bb.retain();
            bb.start();
        }
        return result;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BlockBossLogicXml.class.getName());

}
