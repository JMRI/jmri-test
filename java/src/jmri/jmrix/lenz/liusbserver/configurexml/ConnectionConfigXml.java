package jmri.jmrix.lenz.liusbserver.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.lenz.liusbserver.ConnectionConfig;
import jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the LIUSB Server (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the LIUSB Server.
 * <P>
 * NOTE: The LIUSB Server currently has no options, so this class does 
 * not store any.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author   Paul Bender Copyright (C) 2009	
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * An LIUSBServer connection needs no extra information, so
     * we reimplement the superclass method to just write the
     * necessary parts.
     * @param o
     * @return Formatted element containing no attributes except the class name
     */
    public Element store(Object o) {
        getInstance();

        Element e = new Element("connection");

        e.setAttribute("class", this.getClass().getName());

        return e;
    }


   /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
      */
    public boolean load(Element e) throws Exception {
        boolean result = true;
        // start the "connection"
        /*jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter adapter = 
                        new jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter();
                        String errCode = adapter.openPort("localhost","LIUSBServer");
                        if (errCode == null)    {
                                adapter.configure();
                }*/
        // register, so can be picked up
        getInstance();
        register();
        return result;
    }

    @Override
    protected void getInstance() {
        if(adapter == null){ //adapter=new LIUSBServerAdapter();
            adapter =  new LIUSBServerAdapter();
            String errCode = ((LIUSBServerAdapter)adapter).openPort("localhost","LIUSBServer");
            if (errCode == null)    {
                adapter.configure();
            }
        
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter=((ConnectionConfig) object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
static Logger log = Logger.getLogger(ConnectionConfigXml.class.getName());

}
