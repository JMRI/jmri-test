// ZeroConfService.java
package jmri.util.zeroconf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.web.server.WebServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZeroConfService objects manage a zeroConf network service advertisement.
 * <P>
 * ZeroConfService objects encapsulate zeroConf network services created using
 * JmDNS, providing methods to start and stop service advertisements and to
 * query service state. Typical usage would be:
 * <pre>
 * ZeroConfService myService = ZeroConfService.create("_withrottle._tcp.local.", port);
 * myService.publish();
 * </pre> or, if you do not wish to retain the ZeroConfService object:
 * <pre>
 * ZeroConfService.create("_http._tcp.local.", port).publish();
 * </pre> ZeroConfService objects can also be created with a HashMap of
 * properties that are included in the TXT record for the service advertisement.
 * This HashMap should remain small, but it could include information such as
 * the XMLIO path (for a web server), the default path (also for a web server),
 * a specific protocol version, or other information. Note that all service
 * advertisements include the JMRI version, using the key "version", and the
 * JMRI version numbers in a string "major.minor.test" with the key "jmri"
 * <P>
 * All ZeroConfServices are automatically stopped when the JMRI application
 * shuts down. Use {@link #allServices() } to get a collection of all
 * ZeroConfService objects.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Randall Wood Copyright (C) 2011, 2013
 * @version	$Revision$
 * @see javax.jmdns.JmDNS
 * @see javax.jmdns.ServiceInfo
 */
public class ZeroConfService {

    // internal data members
    private ServiceInfo _serviceInfo = null;
    // static data objects
    private static HashMap<String, ZeroConfService> _services = null;
    private static HashMap<InetAddress, JmDNS> _netServices = null;
    private static boolean hasNetServices = false;
    private final List<ZeroConfServiceListener> _listeners = new ArrayList<ZeroConfServiceListener>();
    private static final Logger log = LoggerFactory.getLogger(ZeroConfService.class.getName());

    /**
     * Create a ZeroConfService with the minimal required settings. This method
     * calls {@link #create(java.lang.String, int, java.util.HashMap)} with an
     * empty props HashMap.
     *
     * @param type The service protocol
     * @param port The port the service runs over
     * @return An unpublished ZeroConfService
     * @see #create(java.lang.String, java.lang.String, int, int, int,
     * java.util.HashMap)
     */
    public static ZeroConfService create(String type, int port) {
        return create(type, port, new HashMap<String, String>());
    }

    /**
     * Create a ZeroConfService with an automatically detected server name. This
     * method calls
     * {@link #create(java.lang.String, java.lang.String, int, int, int, java.util.HashMap)}
     * with the default weight and priority, and with the result of
     * {@link jmri.web.server.WebServerPreferences#getRailRoadName()}
     * reformatted to replace dots and dashes with spaces.
     *
     * @param type The service protocol
     * @param port The port the service runs over
     * @param properties Additional information to be listed in service
     * advertisement
     * @return An unpublished ZeroConfService
     */
    public static ZeroConfService create(String type, int port, HashMap<String, String> properties) {
        return create(type, WebServerManager.getWebServerPreferences().getRailRoadName(), port, 0, 0, properties);
    }

    /**
     * Create a ZeroConfService. The property <i>version</i> is added or
     * replaced with the current JMRI version as its value. The property
     * <i>jmri</i> is added or replaced with the JMRI major.minor.test version
     * string as its value.
     * <p>
     * If a service with the same key as the new service is already published,
     * the original service is returned unmodified.
     *
     * @param type The service protocol
     * @param name The name of the JMRI server listed on client devices
     * @param port The port the service runs over
     * @param weight Default value is 0
     * @param priority Default value is 0
     * @param properties Additional information to be listed in service
     * advertisement
     * @return An unpublished ZeroConfService
     */
    public static ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> properties) {
        ZeroConfService s;
        if (ZeroConfService.services().containsKey(ZeroConfService.key(type, name))) {
            s = ZeroConfService.services().get(ZeroConfService.key(type, name));
            log.debug("Using existing ZeroConfService {}", s.key());
        } else {
            properties.put("version", jmri.Version.name());
            // use the major.minor.test version string for jmri since we have potentially
            // tight space constraints in terms of the number of bytes that properties 
            // can use, and there are some unconstrained properties that we would like to use.
            properties.put("jmri", jmri.Version.getCanonicalVersion());
            s = new ZeroConfService(ServiceInfo.create(type, name, port, weight, priority, properties));
            log.debug("Creating new ZeroConfService {}", s.key());
        }
        return s;
    }

    /**
     * Create a ZeroConfService object.
     *
     * @param service
     */
    protected ZeroConfService(ServiceInfo service) {
        _serviceInfo = service;
    }

    /**
     * Get the key of the ZeroConfService object. The key is fully qualified
     * name of the service in all lowercase, jmri._http.local.
     *
     * @return The fully qualified name of the service.
     */
    public String key() {
        return _serviceInfo.getKey();
    }

    /**
     * Generate a ZeroConfService key for searching in the HashMap of running
     * services.
     *
     * @param type
     * @param name
     * @return The combination of the name and type of the service.
     */
    protected static String key(String type, String name) {
        return (name + "." + type).toLowerCase();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set when
     * creating the object.
     *
     * @return The service name as reported by the
     * {@link javax.jmdns.ServiceInfo} object.
     */
    public String name() {
        return _serviceInfo.getName();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set when
     * creating the object.
     *
     * @return The service type as reported by the
     * {@link javax.jmdns.ServiceInfo} object.
     */
    public String type() {
        return _serviceInfo.getType();
    }

    private ServiceInfo newServiceInfo() {
        return _serviceInfo.clone();
    }

    /**
     * Get the ServiceInfo property of the object. This is the JmDNS
     * implementation of a zeroConf service.
     *
     * @return The serviceInfo object.
     */
    public ServiceInfo serviceInfo() {
        return _serviceInfo;
    }

    /**
     * Get the state of the service.
     *
     * @return True if the service is being advertised, and false otherwise.
     */
    public Boolean isPublished() {
        return ZeroConfService.services().containsKey(key());
    }

    /**
     * Start advertising the service.
     */
    public void publish() {
        if (!isPublished()) {
            for (ZeroConfServiceListener listener : this._listeners) {
                listener.serviceQueued(new ZeroConfServiceEvent(this, null));
            }
            if (!ZeroConfService.hasNetServices) {
                ZeroConfService.netServices();
                (new Timer()).schedule(new QueueTask(this), 500);
                return;
            }
            for (JmDNS netService : ZeroConfService.netServices().values()) {
                ZeroConfServiceEvent event;
                ServiceInfo info;
                try {
                    // JmDNS requires a 1-to-1 mapping of serviceInfo to InetAddress
                    try {
                        info = _serviceInfo;
                        netService.registerService(info);
                    } catch (IllegalStateException ex) {
                        info = _serviceInfo.clone();
                        // TODO: need to catch cloned serviceInfo
                        netService.registerService(info);
                    }
                    event = new ZeroConfServiceEvent(this, netService);
                } catch (IOException ex) {
                    log.error("Unable to publish service for {}: {}", key(), ex.getMessage());
                    break;
                }
                ZeroConfService.services().put(key(), this);
                for (ZeroConfServiceListener listener : this._listeners) {
                    listener.servicePublished(event);
                }
                log.debug("Publishing zeroConf service for {} on", key(), info.getInetAddresses()[0]);
            }
        }
    }

    /**
     * Stop advertising the service.
     */
    public void stop() {
        log.debug("Stopping ZeroConfService {}", key());
        if (ZeroConfService.services().containsKey(key())) {
            for (JmDNS netService : ZeroConfService.netServices().values()) {
                netService.unregisterService(_serviceInfo);
                for (ZeroConfServiceListener listener : this._listeners) {
                    listener.serviceUnpublished(new ZeroConfServiceEvent(this, netService));
                }
            }
            ZeroConfService.services().remove(key());
        }
    }

    /**
     * Stop advertising all services.
     */
    public static void stopAll() {
        log.debug("Stopping all ZeroConfServices");
        for (JmDNS netService : ZeroConfService.netServices().values()) {
            netService.unregisterAllServices();
        }
        ZeroConfService.services().clear();
    }

    /**
     * A list of published ZeroConfServices
     *
     * @return Collection of ZeroConfServices
     */
    public static Collection<ZeroConfService> allServices() {
        return ZeroConfService.services().values();
    }

    /* return a list of published services */
    private static HashMap<String, ZeroConfService> services() {
        if (_services == null) {
            _services = new HashMap<String, ZeroConfService>();
        }
        return _services;
    }

    /* return the JmDNS handler */
    public static HashMap<InetAddress, JmDNS> netServices() {  // package protected, so we only have one.
        if (_netServices == null) {
            _netServices = new HashMap<InetAddress, JmDNS>();
            log.debug("JmDNS version: {}", JmDNS.VERSION);
            try {
                for (InetAddress address : hostAddresses()) {
                    log.debug("Calling JmDNS.create({})", address.getHostAddress());
                    _netServices.put(address, JmDNS.create(address));
                }

                if (jmri.InstanceManager.shutDownManagerInstance() != null) {
                    ShutDownTask task = new QuietShutDownTask("Stop ZeroConfServices") {
                        @Override
                        public boolean execute() {
                            ZeroConfService.stopAll();
                            for (JmDNS service : ZeroConfService.netServices().values()) {
                                try {
                                    service.close();
                                } catch (IOException e) {
                                    log.debug("jmdns.close() returned IOException: {}", e.getMessage());
                                }
                            }
                            return true;
                        }
                    };
                    jmri.InstanceManager.shutDownManagerInstance().register(task);
                }
            } catch (IOException ex) {
                log.warn("Unable to create JmDNS with error: {}", ex.getMessage());
            }
            hasNetServices = true;
        }
        return _netServices;
    }

    /**
     * Return the system name or "computer" if the system name cannot be
     * determined. This method returns the first part of the fully qualified
     * domain name from {@link #FQDN()}.
     *
     * @param address The {@link java.net.InetAddress} for the host name.
     * @return The hostName associated with the first interface encountered.
     */
    public static String hostName(InetAddress address) {
        String hostName = ZeroConfService.FQDN(address) + ".";
        // we would have to check for the existance of . if we did not add .
        // to the string above.
        return hostName.substring(0, hostName.indexOf('.'));
    }

    /**
     * Return the fully qualified domain name or "computer" if the system name
     * cannot be determined. This method uses the
     * {@link javax.jmdns.JmDNS#getHostName()} method to get the name.
     *
     * @param address The {@link java.net.InetAddress} for the FQDN.
     * @return The fully qualified domain name.
     */
    public static String FQDN(InetAddress address) {
        return ZeroConfService.netServices().get(address).getHostName();
    }

    /**
     * Return the non-loopback ipv4 address of the host, or null if none found.
     *
     * @return The last non-loopback IP address on the host.
     */
    public static List<InetAddress> hostAddresses() {
        List<InetAddress> addrList = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> IFCs = null;
        try {
            IFCs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            log.error("Unable to get network interfaces.", ex);
        }
        if (IFCs != null) {
            while (IFCs.hasMoreElements()) {
                NetworkInterface IFC = IFCs.nextElement();
                try {
                    if (IFC.isUp()) {
                        Enumeration<InetAddress> addresses = IFC.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();
                            if (!address.isLoopbackAddress()) {
                                addrList.add(addresses.nextElement());
                            }
                        }
                    }
                } catch (SocketException ex) {
                    log.error("Unable to read network interface {}.", IFC.toString(), ex);
                }
            }
        }
        return addrList;
    }

    public void addEventListener(ZeroConfServiceListener l) {
        this._listeners.add(l);
    }

    public void removeEventListener(ZeroConfServiceListener l) {
        this._listeners.remove(l);
    }

    private static class NetworkListener implements NetworkTopologyListener {

        @Override
        public void inetAddressAdded(NetworkTopologyEvent nte) {
            if (!ZeroConfService._netServices.containsKey(nte.getInetAddress())) {
                for (ZeroConfService service : ZeroConfService.allServices()) {
                    try {
                        nte.getDNS().registerService(service.serviceInfo());
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }

        @Override
        public void inetAddressRemoved(NetworkTopologyEvent nte) {
            ZeroConfService._netServices.remove(nte.getInetAddress());
            nte.getDNS().unregisterAllServices();
        }

    }

    private class QueueTask extends TimerTask {

        private final ZeroConfService service;

        protected QueueTask(ZeroConfService service) {
            this.service = service;
        }

        @Override
        public void run() {
            this.service.publish();
        }

    }
}

/* @(#)ZeroConfService.java */
