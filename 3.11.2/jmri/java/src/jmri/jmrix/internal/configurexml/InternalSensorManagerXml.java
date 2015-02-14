package jmri.jmrix.internal.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

/**
 * Provides load and store functionality for
 * configuring InternalSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 * @version $Revision$
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification="name assigned historically")
public class InternalSensorManagerXml extends jmri.managers.configurexml.InternalSensorManagerXml {

    public InternalSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    static Logger log = LoggerFactory.getLogger(InternalSensorManagerXml.class.getName());
}
