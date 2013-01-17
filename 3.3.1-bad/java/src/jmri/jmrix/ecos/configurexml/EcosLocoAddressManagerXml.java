// EcosLocoAddressManagerXml.java

package jmri.jmrix.ecos.configurexml;

import org.jdom.Element;

/**
 * This class is here to prevent error messages
 * being presented to the user on opening JMRI
 * or saving the panel file, when connected to
 * an Ecos.  It currently serves no other function.
 * <P>
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 * @version $Revision$
 */

public class EcosLocoAddressManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML{

    public EcosLocoAddressManagerXml() { }
    
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element locoaddress) {
        return true;
    }

    public Element store(Object o){
        return null;
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoAddressManagerXml.class.getName());
}
