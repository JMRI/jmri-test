package jmri.configurexml;

import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.DefaultMemoryManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Persistency implementation for the default MemoryManager persistance.
 * <P>The state of memory objects is not persisted, just their existance.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class DefaultMemoryManagerXml extends AbstractMemoryManagerConfigXML {

    public DefaultMemoryManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param memorys The top-level element being created
     */
    public void setStoreElementClass(Element memorys) {
        memorys.addAttribute("class","jmri.configurexml.DefaultMemoryManagerXml");
    }

    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param memorys Top level Element to unpack.
     */
    public void load(Element memorys) {
        // create the master object
        DefaultMemoryManager mgr = DefaultMemoryManager.instance();
        // load individual routes
        loadMemorys(memorys);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultMemoryManagerXml.class.getName());
}