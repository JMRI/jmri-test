package jmri.jmrit.logix.configurexml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.Attribute;

import jmri.InstanceManager;
import jmri.BeanSetting;
import jmri.Path;
import jmri.Sensor;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;

/**
 * Provides the abstract base and store functionality for
 * configuring the CatalogTreeManager.
 * <P>
 * Typically, a subclass will just implement the load(Element catalogTree)
 * class, relying on implementation here to load the individual CatalogTree objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 * 
 */
public class OBlockManagerXml // extends XmlFile
                    extends jmri.configurexml.AbstractXmlAdapter {

    public OBlockManagerXml() {
    }
	
    /**
     * Store the contents of a OBlockManager.
     *
     * @param o Object to store, of type BlockManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element blocks = new Element("oblocks");
        blocks.setAttribute("class","jmri.jmrit.logix.configurexml.OBlockManagerXml");
        OBlockManager manager = (OBlockManager) o;
        Iterator<String> iter = manager.getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sname = iter.next();
            OBlock block = manager.getBySystemName(sname);
            String uname = block.getUserName();
            if (log.isDebugEnabled())
                log.debug("OBlock: sysName= "+sname+", userName= "+uname);
            Element elem = new Element("oblock");
            elem.setAttribute("systemName", sname);
            if (uname==null) uname = "";
            elem.setAttribute("userName", uname);
            String comment = block.getComment();
            if (comment != null) {
                Element c = new Element("comment");
                c.addContent(comment);
                elem.addContent(c);
            }
            elem.setAttribute("length", ""+block.getLengthMm());
            elem.setAttribute("curve", ""+block.getCurvature());
            Sensor s = block.getSensor();
            if (s!=null) {
                Element se = new Element("sensor");
                se.setAttribute("systemName", s.getSystemName());
                elem.addContent(se);
            }
            List paths = block.getPaths();
            for (int j=0; j<paths.size(); j++) {
                elem.addContent(storePath((OPath)paths.get(j)));
            }
            List <Portal> portals = block.getPortals();
            for (int i=0; i<portals.size(); i++) {
                elem.addContent(storePortal(portals.get(i)));
            }
            // and put this element out
            blocks.addContent(elem);
        }

        return blocks;
    }

    Element storePortal(Portal portal) {
        Element elem = new Element("portal");
        elem.setAttribute("portalName", portal.getName());
        OBlock block = portal.getFromBlock();
        if (block!=null) {
            Element fromElem = new Element("fromBlock");
            fromElem.setAttribute("blockName", block.getSystemName());
            List <OPath> paths = portal.getFromPaths();
            if (paths!=null) {
                for (int i=0; i<paths.size(); i++) {
                    OPath path = paths.get(i);
                    fromElem.addContent(storePathKey(path));
                }
            }
            elem.addContent(fromElem);
        }
        NamedBean signal = portal.getFromSignal();
        if (signal!=null) {
            Element fromElem = new Element("fromSignal");
            fromElem.setAttribute("signalName", signal.getSystemName());
            elem.addContent(fromElem);
        }
        block = portal.getToBlock();
        if (block!=null) {
            Element toElem = new Element("toBlock");
            toElem.setAttribute("blockName", block.getSystemName());
            List <OPath> paths = portal.getToPaths();
            if (paths!=null) {
                for (int i=0; i<paths.size(); i++) {
                    OPath path = paths.get(i);
                    toElem.addContent(storePathKey(path));
                }
            }
            elem.addContent(toElem);
        }
        signal = portal.getToSignal();
        if (signal!=null) {
            Element toElem = new Element("toSignal");
            toElem.setAttribute("signalName", signal.getSystemName());
            elem.addContent(toElem);
        }
        return elem;
    }

    /**
    * Key is sufficient to mark the Portal's knowledge of the path.
    * Full path info will get loaded from the HashMap
    */
    Element storePathKey(OPath path) {
        Element elem = new Element("path");
        elem.setAttribute("pathName", path.getName());
        elem.setAttribute("blockName", ""+path.getBlock().getSystemName());
        return elem;
    }

    Element storePath(OPath path) {
        Element elem = new Element("path");
        elem.setAttribute("pathName", path.getName());
        elem.setAttribute("blockName", ""+path.getBlock().getSystemName());
        String portal = path.getFromPortalName();
        if (portal != null) {
            elem.setAttribute("fromPortal", portal);
        }
        portal = path.getToPortalName();
        if (portal != null) {
            elem.setAttribute("toPortal", portal);
        }
        List <BeanSetting> list = path.getSettings();        
        for (int i=0; i<list.size(); i++) {
            BeanSetting bs = list.get(i);
            Element e = new Element("setting");
            Turnout to = (Turnout)bs.getBean();
            e.setAttribute("turnout", to.getSystemName());
            e.setAttribute("set", ""+bs.getSetting());
            elem.addContent(e);
        }
        elem.setAttribute("fromDirection", ""+path.getFromBlockDirection());
        elem.setAttribute("toDirection", ""+path.getToBlockDirection());
        return elem;
    }

    /**
    * Due to the forward and backward referencing among OBlock, OPath and Portal
    * no precedence order exists to fully create these objects in one pass.
    * The unique naming of these objects allows the use of Hashmaps to hold them
    * for update. 
    */
    HashMap <String, OBlock> _blockMap;
    HashMap <String, OPath> _pathMap;
    HashMap <String, Portal> _portalMap;
    OBlockManager _manager;

    OBlock getBlock(String sysName) {
        OBlock block = _blockMap.get(sysName);
        if (block == null) {
            block = _manager.provideOBlock(sysName);
            if (block == null) {
                block = _manager.createNewOBlock(sysName, null);
                if (log.isDebugEnabled()) log.debug("create OBlock: ("+sysName+")");
            } else {
                _blockMap.put(sysName, block);
            }
        }
        return block;
    }

    OPath getPath(OBlock block, String name) {
        String key = block.getSystemName()+name;
        OPath path = _pathMap.get(key);
        if (path == null) {
            path = new OPath(block, name);
            _pathMap.put(key, path);
            if (log.isDebugEnabled()) log.debug("create OPath: ("+name+") in block ("+block.getSystemName()+")");
        }
        return path;
    }

    Portal getPortal(OBlock fromBlock, String name, OBlock toBlock) {
        Portal portal = _portalMap.get(name);
        if (portal == null) {
            portal = new Portal(fromBlock, name, toBlock);
            _portalMap.put(name, portal);
            if (log.isDebugEnabled()) log.debug("create Portal: ("+name+")");
        }
        return portal;
    }

    /**
     * Create a OBlock object of the correct class, then
     * register and fill it.
     * @param blocks Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element blocks) {
        _blockMap = new HashMap <String, OBlock>();
        _pathMap = new HashMap <String, OPath>();
        _portalMap = new HashMap <String, Portal>();
        _manager = InstanceManager.oBlockManagerInstance();

        List<Element> blockList = blocks.getChildren("oblock");
        if (log.isDebugEnabled()) log.debug("Found "+blockList.size()+" OBlock objects");
        for (int i=0; i<blockList.size(); i++) {
            Element elem = blockList.get(i);
            if (elem.getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+elem+" "+elem.getAttributes());
                break;
            }
            String sysName = elem.getAttribute("systemName").getValue();
            String userName = null;
            if (elem.getAttribute("userName") != null)
                userName = elem.getAttribute("userName").getValue();
            // Portal may have already created a skeleton of this block
            OBlock block = getBlock(sysName);
            if (block==null) {
                log.error("Null block!! sysName= "+sysName+", userName= "+userName);
                continue;
            }
            block.setUserName(userName);
            String c = elem.getChildText("comment");
            if (c != null) {
                block.setComment(c);
            }
			if (elem.getAttribute("length") != null) {
				block.setLength(Float.valueOf(elem.getAttribute("length").getValue()).floatValue());
			}
			if (elem.getAttribute("curve") != null) {
				block.setCurvature(Integer.parseInt((elem.getAttribute("curve")).getValue()));
			}
            List<Element> sensors = elem.getChildren("sensor");
            if (sensors.size()>1) log.error("More than one sensor present: "+sensors.size());
            if (sensors.size()>0) {
                // sensor
                String name = sensors.get(0).getAttribute("systemName").getValue();
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(name);
                block.setSensor(sensor);
            }
            List<Element> paths = elem.getChildren("path");
            for (int j=0; j<paths.size(); j++) {
                block.addPath(loadPath(paths.get(j), block));
            }
            List<Element> portals = elem.getChildren("portal");
            for (int k=0; k<portals.size(); k++) {
                block.addPortal(loadPortal(portals.get(k)));
            }
        }
        // patch in Paths to Portals
        addPathsToPortals();
        return true;
    }

    private void addPathsToPortals() {
        Iterator <Portal> iter = _portalMap.values().iterator();
        while (iter.hasNext()) {
            Portal portal = iter.next();
            OBlock block = portal.getFromBlock();
            if (block==null){
            	log.warn("Portal "+portal.getName()+" has null FromBlock.");
            	continue;
            }
            String fromName = block.getSystemName();
            block = portal.getToBlock();
            if (block==null){
            	log.warn("Portal "+portal.getName()+" has null ToBlock.");
            	continue;
            }
            String toName = block.getSystemName();
            String portalName = portal.getName();
            // get paths from blocks and let Portal pick the ones that are missing
            Iterator <OBlock> it = _blockMap.values().iterator();
            while (it.hasNext()) {
            	block = it.next();
                String name = block.getSystemName();
                // find blocks of this portal
            	if(fromName.equals(name) || toName.equals(name)) {
                    List <Path> list = block.getPaths();
                    for (int i=0; i<list.size(); i++) {
                        OPath path = (OPath)list.get(i);
                        if (portalName.equals(path.getToPortalName()) ||
                                portalName.equals(path.getFromPortalName()) ) {
                            portal.addPath(path);
                        }
                    }
            	}
            }
        }

    }
	
    public void load(Element element, Object o) throws Exception {
        log.error("load called. Invalid method.");
    }
    
    @SuppressWarnings("unchecked")
    Portal loadPortal(Element elem) {
        String portalName = elem.getAttribute("portalName").getValue();
        OBlock fromBlock = null;
        ArrayList <OPath> fromPaths = new ArrayList <OPath> ();
        OBlock toBlock = null;
        ArrayList <OPath> toPaths = new ArrayList <OPath> ();
        
        List<Element> eBlocks = elem.getChildren("fromBlock");
        if (eBlocks.size()>1) log.error("More than one fromBlock present: "+eBlocks.size());
        if (eBlocks.size()>0) {
            Element eBlk = eBlocks.get(0); 
            String blockName = eBlk.getAttribute("blockName").getValue();
            fromBlock = getBlock(blockName);
            List<Element> ePaths = eBlk.getChildren("path");
            for (int i=0; i<ePaths.size(); i++) {
                Element e = ePaths.get(i);
                Attribute attr = e.getAttribute("fromPortal");
                if (attr != null){
                    if (portalName.equals(attr.getValue()) ) {
                        fromPaths.add(loadPath(e, fromBlock));
                    }
                }
                attr = e.getAttribute("toPortal");
                if (attr != null){
                    if (portalName.equals(attr.getValue()) ) {
                        fromPaths.add(loadPath(e, fromBlock));
                    }
                }
            }
        }
        eBlocks = elem.getChildren("toBlock");
        if (eBlocks.size()>1) log.error("More than one toBlock present: "+eBlocks.size());
        if (eBlocks.size()>0) {
            Element eBlk = eBlocks.get(0); 
            String blockName = eBlk.getAttribute("blockName").getValue();
            toBlock = getBlock(blockName);
            List<Element> ePaths = eBlk.getChildren("path");
            for (int i=0; i<ePaths.size(); i++) {
                Element e = ePaths.get(i);
                Attribute attr = e.getAttribute("fromPortal");
                if (attr != null){
                    if (portalName.equals(attr.getValue()) ) {
                        toPaths.add(loadPath(e, fromBlock));
                    }
                }
                attr = e.getAttribute("toPortal");
                if (attr != null){
                    if (portalName.equals(attr.getValue()) ) {
                        toPaths.add(loadPath(e, fromBlock));
                    }
                }
            }
        }
        Portal portal = getPortal(fromBlock, portalName, toBlock);
        if (fromBlock != null) {
            fromBlock.addPortal(portal);
            for (int i=0; i<fromPaths.size(); i++) {
                if (!portal.addPath(fromPaths.get(i))) {
                    log.error("loadPortal: portal \""+portal.getName()+"\" failed to add path \""+
                              fromPaths.get(i).getName()+"\" in block \""+fromBlock.getSystemName()+"\"");
                }
            }
        }
        if (toBlock != null) {
            toBlock.addPortal(portal);
            for (int i=0; i<toPaths.size(); i++) {
                if (!portal.addPath(toPaths.get(i))) {
                    log.error("loadPortal: portal \""+portal.getName()+"\" failed to add path \""+
                              toPaths.get(i).getName()+"\" in block \""+toBlock.getSystemName()+"\"");
                }
            }
        }
        List<Element> eSignals = elem.getChildren("fromSignal");
        if (eSignals.size()>1) log.error("More than one fromSignal present: "+eSignals.size());
        if (eSignals.size()>0) {
            Element eSig = eSignals.get(0); 
            portal.setFromSignal(eSig.getAttribute("signalName").getValue());
        }
        eSignals = elem.getChildren("toSignal");
        if (eSignals.size()>1) log.error("More than one toSignal present: "+eSignals.size());
        if (eSignals.size()>0) {
            Element eSig = eSignals.get(0); 
            portal.setToSignal(eSig.getAttribute("signalName").getValue());
        }
        if (log.isDebugEnabled()) log.debug("Load portal "+portalName);
        return portal;
    }

    @SuppressWarnings("unchecked")
    OPath loadPath(Element elem, OBlock block) {
        String pName = elem.getAttribute("pathName").getValue();
        OPath path = getPath(block, pName);
        try {
            Attribute attr = elem.getAttribute("fromDirection");
            if (attr != null){
                path.setFromBlockDirection(attr.getIntValue());
            }
            attr = elem.getAttribute("toDirection");
            if (attr != null){
                path.setToBlockDirection(attr.getIntValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Could not parse path ("+pName+") block ("+block.getSystemName()+") attribute");
        }
        Attribute attr = elem.getAttribute("fromPortal");
        if (attr != null){
            path.setFromPortalName(attr.getValue());
        }
        attr = elem.getAttribute("toPortal");
        if (attr != null){
            path.setToPortalName(attr.getValue());
        }
        List<Element> settings = elem.getChildren("setting");
        log.debug("Path ("+pName+") has "+settings.size()+" settings.");
        for (int i=0; i<settings.size(); i++) {
            Element setElem = settings.get(i);
            int setting = 0;
            try {
                setting = setElem.getAttribute("set").getIntValue();
            } catch (org.jdom.DataConversionException e) {
                log.error("Could not parse setting attribute for path ("+pName+
                          ") block ("+block.getSystemName()+")");
            }
            String sysName = setElem.getAttribute("turnout").getValue();
            Turnout to = InstanceManager.turnoutManagerInstance().provideTurnout(sysName);

            BeanSetting bs = new BeanSetting(to, setting);
            path.addSetting(bs);
        }
        return path;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OBlockManagerXml.class.getName());
}
