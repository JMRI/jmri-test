package jmri.managers.configurexml;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import java.util.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring MemoryManagers, working with
 * AbstractMemoryManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element memories)
 * class, relying on implementation here to load the individual Memory objects.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Memory or AbstractMemory subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 1.5 $
 */
public abstract class AbstractMemoryManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractMemoryManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * MemoryManager
     * @param o Object to store, of type MemoryManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element memories = new Element("memories");
        setStoreElementClass(memories);
        MemoryManager tm = (MemoryManager) o;
        if (tm!=null) {
            java.util.Iterator<String> iter =
                                    tm.getSystemNameList().iterator();

            // don't return an element if there are not memories to include
            if (!iter.hasNext()) return null;
            
            // store the memories
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Memory m = tm.getBySystemName(sname);
                Element elem = new Element("memory")
                            .setAttribute("systemName", sname);
                            
                // store common part
                storeCommon(m, elem);
                // store value if non-null; null values omitted
                Object obj = m.getValue();
                if (obj != null)
                {
                    String value = obj.toString();
                    elem.setAttribute("value", value);
                }

                log.debug("store Memory "+sname);
                memories.addContent(elem);

            }
        }
        return memories;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param memories The top-level element being created
     */
    abstract public void setStoreElementClass(Element memories);

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param memories Top level Element to unpack.
     * @return true if successful
     */
    abstract public boolean load(Element memories);

    /**
     * Utility method to load the individual Memory objects.
     * If there's no additional info needed for a specific Memory type,
     * invoke this with the parent of the set of Memory elements.
     * @param memories Element containing the Memory elements to load.
     */
    @SuppressWarnings("unchecked")
	public void loadMemories(Element memories) {
        List<Element> memoryList = memories.getChildren("memory");
        if (log.isDebugEnabled()) log.debug("Found "+memoryList.size()+" Memory objects");
        MemoryManager tm = InstanceManager.memoryManagerInstance();

        for (int i=0; i<memoryList.size(); i++) {
            if (memoryList.get(i).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+(memoryList.get(i))+" "+(memoryList.get(i)).getAttributes());
                break;
            }
            String sysName = memoryList.get(i).getAttribute("systemName").getValue();
            String userName = null;
            if (memoryList.get(i).getAttribute("userName") != null)
                userName = memoryList.get(i).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create Memory: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            Memory m = tm.newMemory(sysName, userName);
            if (memoryList.get(i).getAttribute("value") != null) {
                String value = memoryList.get(i).getAttribute("value").getValue();
                m.setValue(value);
            }
            // load common parts
            loadCommon(m, memoryList.get(i));
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMemoryManagerConfigXML.class.getName());
}