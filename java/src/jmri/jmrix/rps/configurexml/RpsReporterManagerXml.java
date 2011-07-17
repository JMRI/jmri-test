package jmri.jmrix.rps.configurexml;

import jmri.jmrix.rps.RpsReporterManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring RpsReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.4 $
 * @since 2.3.1
 */
public class RpsReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public RpsReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element reporters) {
        // create the master object
        RpsReporterManager.instance();
        // load individual sensors
        return loadReporters(reporters);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RpsReporterManagerXml.class.getName());
}