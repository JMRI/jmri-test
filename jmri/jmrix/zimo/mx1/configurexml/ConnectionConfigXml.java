package jmri.jmrix.zimo.mx1.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.zimo.mx1.ConnectionConfig;
import jmri.jmrix.zimo.mx1.Mx1Adapter;

/**
 * Handle XML persistance of layout connections by persistening
 * the Mx1Adapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the Mx1Adapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.3 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = Mx1Adapter.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}