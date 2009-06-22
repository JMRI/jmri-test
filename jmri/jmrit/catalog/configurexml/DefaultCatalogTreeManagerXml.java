package jmri.jmrit.catalog.configurexml;

import jmri.InstanceManager;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.CatalogTreeLeaf;

import jmri.jmrit.XmlFile;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;

import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.io.File;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;

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
public class DefaultCatalogTreeManagerXml extends XmlFile
                    implements jmri.configurexml.XmlAdapter {

	private static String defaultFileName = XmlFile.prefsDir()+"catalogTrees.xml";

    public DefaultCatalogTreeManagerXml() {
    }

	/*
	 *  Writes out tree values to a file in the user's preferences directory
	 */
	public void writeCatalogTrees() throws org.jdom.JDOMException, java.io.IOException {
		if (log.isDebugEnabled()) log.debug("entered writeCatalogTreeValues");
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
		List<String> trees = manager.getSystemNameList();
        boolean found = false;
        Iterator iter = manager.getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sname = (String)iter.next();
            CatalogTree tree = manager.getBySystemName(sname);
            if (log.isDebugEnabled()) {
                log.debug("Tree: sysName= "+sname+", userName= "+tree.getUserName());
                CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
                log.debug("enumerateTree called for root= "+root.toString()+
                              ", has "+root.getChildCount()+" children");
                Enumeration e =root.depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
                    log.debug("nodeName= "+n.getUserObject()+" has "+n.getLeaves().size()+
                              " leaves and "+n.getChildCount()+" subnodes.");
                }
            }
            if (sname != null && sname.charAt(1) == CatalogTree.XML) {
                found =true;
                break;
            }
        }
		if (found) {
			// there are trees defined, create root element
			Element root = new Element("catalogTrees");
			Document doc = newDocument(root, dtdLocation+"catalogTree.dtd");
			
			// add XSLT processing instruction
			// <?xml-stylesheet type="text/xsl" href="XSLT/tree-values.xsl"?>
			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
			m.put("type", "text/xsl");
			m.put("href", xsltLocation+"panelfile.xsl");
			org.jdom.ProcessingInstruction p = new org.jdom.ProcessingInstruction("xml-stylesheet", m);
			doc.addContent(0,p);

			store(root, trees);
			
            try {
                if (!checkFile(defaultFileName)) {
                    // file does not exist, create it
                    File file = new File(defaultFileName);
                    file.createNewFile();
                }
                // write content to file
                writeXML(findFile(defaultFileName),doc);
                // memory consistent with file
                jmri.jmrit.catalog.ImageIndexEditor._indexChanged = false;
            }
            catch (java.io.IOException ioe) {
                log.error("IO Exception "+ioe);
                throw (ioe);
            }
            catch (org.jdom.JDOMException jde) {
                log.error("JDOM Exception "+jde);
                throw (jde);
            }
		}
	}
	
    /**
     * Default implementation for storing the contents of a CatalogTreeManager
     * @param cat Element to load with contents
     * @param trees List of contents
     */
    public void store(Element cat, List<String> trees) {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        cat.setAttribute("class","jmri.jmrit.catalog.DefaultCatalogTreeManagerConfigXML");
        Iterator iter = trees.iterator();
        while (iter.hasNext()) {
            String sname = (String)iter.next();
            if (sname==null) log.error("System name null during store");
            if (log.isDebugEnabled()) log.debug("system name is "+sname);
            if (sname.charAt(1) != CatalogTree.XML) {
                continue;
            }
            CatalogTree ct = manager.getBySystemName(sname);
            Element elem = new Element("catalogTree");
            elem.setAttribute("systemName", sname);                           
            String uname = ct.getUserName();
            if (uname!=null) elem.setAttribute("userName", uname);

            storeNode(elem, (CatalogTreeNode)ct.getRoot());

            if (log.isDebugEnabled()) log.debug("store CatalogTree "+sname);
            cat.addContent(elem);
        }
    }


    /**
     * Recursively store a CatalogTree
     */
    public void storeNode(Element parent, CatalogTreeNode node) {
        if (log.isDebugEnabled()) log.debug("storeNode "+node.toString()+
                                 ", has "+node.getLeaves().size()+" leaves.");
        Element element = new Element("node");
        element.setAttribute("nodeName", node.toString());                           
        List leaves = node.getLeaves();
        for (int i=0; i<leaves.size(); i++) {
            Element el = new Element("leaf");
            CatalogTreeLeaf leaf = (CatalogTreeLeaf)leaves.get(i);
            el.setAttribute("name", leaf.getName());                           
            el.setAttribute("path", leaf.getPath());                           
            element.addContent(el);
        }
        parent.addContent(element);
        Enumeration e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            storeNode(element, n);
        }
    }


    public Element store(Object o) {
        return null;
    }

	/*
	 *  Reads CatalogTree values from a file in the user's preferences directory
	 */
	public void readCatalogTrees() {
		if (log.isDebugEnabled()) log.debug("entered readCatalogTrees");
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        try {
            // check if file exists
            if (checkFile(defaultFileName)) {
                Element root = rootFromName(defaultFileName);
                if (root!=null) {
                    load(root);
                }
            } else if (log.isDebugEnabled()) log.debug("File: "+defaultFileName+" not Found");
        }
        catch (org.jdom.JDOMException jde) { log.error("Exception reading CatalogTrees: "+jde); }                           
        catch (java.io.IOException ioe) { log.error("Exception reading CatalogTrees: "+ioe); }   
	}
	
    public void load(Element element, Object o) throws Exception {
    }

    /**
     * Create a CatalogTreeManager object of the correct class, then
     * register and fill it.
     * @param catalogTrees Top level Element to unpack.
     */
    public void load(Element catalogTrees) {
        loadCatalogTrees(catalogTrees);
    }

    /**
     * Utility method to load the individual CatalogTree objects.
     */
    public void loadCatalogTrees(Element catalogTrees) {
        List catList = catalogTrees.getChildren("catalogTree");
        if (log.isDebugEnabled()) log.debug("loadCatalogTrees: found "+catList.size()+" CatalogTree objects");
        CatalogTreeManager mgr = InstanceManager.catalogTreeManagerInstance();

        for (int i=0; i<catList.size(); i++) {
            Element elem = (Element)catList.get(i);
            Attribute attr = elem.getAttribute("systemName");
            if ( attr == null) {
                log.warn("unexpected null systemName. elem= "+elem+", attrs= "+elem.getAttributes());
                continue;
            }
            String sysName = attr.getValue();
            String userName = null;
            attr = elem.getAttribute("userName");
            if ( attr == null) {
                log.warn("unexpected null userName. attrs= "+elem.getAttributes());
                continue;
            } else {
                userName = attr.getValue();
            }
            DefaultTreeModel ct = (DefaultTreeModel)mgr.getBySystemName(sysName);
            if (ct != null) {
                continue;   // tree already registered
            }
            ct = (DefaultTreeModel)mgr.newCatalogTree(sysName, userName);
            if (log.isDebugEnabled()) log.debug("CatalogTree: sysName= "+sysName+", userName= "+userName);
            CatalogTreeNode root = (CatalogTreeNode)ct.getRoot();
            elem = elem.getChild("node");
            loadNode(elem, root, ct);
        }
    }

    private void addLeaves(Element element, CatalogTreeNode node) {
        List leafList = element.getChildren("leaf");
        for (int i=0; i<leafList.size(); i++) {
            Element elem = (Element)leafList.get(i);
            Attribute attr = elem.getAttribute("name");
            if ( attr == null) {
                log.warn("unexpected null leaf name. elem= "+elem+", attrs= "+elem.getAttributes());
            }
            String name = attr.getValue();
            attr = elem.getAttribute("path");
            if ( attr == null) {
                log.error("unexpected null leaf path. elem= "+elem+", attrs= "+elem.getAttributes());
                continue;
            }
            String path = attr.getValue();
            node.addLeaf(name, path);
        }
    }

    /**
    * Recursively load a CatalogTree
    */
    public void loadNode(Element element, CatalogTreeNode parent, DefaultTreeModel model) {
        List nodeList = element.getChildren("node");
        if (log.isDebugEnabled()) log.debug("Found "+nodeList.size()+" CatalogTreeNode objects");
        for (int i=0; i<nodeList.size(); i++) {
            Element elem = (Element)nodeList.get(i);
            Attribute attr = elem.getAttribute("nodeName");
            if ( attr == null) {
                log.warn("unexpected null nodeName. elem= "+elem+", attrs= "+elem.getAttributes());
                continue;
            }
            String nodeName = attr.getValue();
            CatalogTreeNode n = new CatalogTreeNode(nodeName);
            addLeaves(elem, n);
            model.insertNodeInto(n, parent, parent.getChildCount());
            loadNode(elem, n, model);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultCatalogTreeManagerXml.class.getName());
}
