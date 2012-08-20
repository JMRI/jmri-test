// ZeroConfService.java
package jmri.util.zeroconf;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.web.server.WebServerManager;

/**
 * ZeroConfService objects manage a zeroConf network service advertisement.
 * <P>
 * ZeroConfService objects encapsulate zeroConf network services created using
 * JmDNS, providing methods to start and stop service advertisements and to
 * query service state. Typical usage would be:
 * <pre>
 * ZeroConfService myService = ZeroConfService.create("_withrottle._tcp.local.", port);
 * myService.publish();
 * </pre>
 * or, if you do not wish to retain the ZeroConfService object:
 * <pre>
 * ZeroConfService.create("_http._tcp.local.", port).publish();
 * </pre>
 * ZeroConfService objects can also be created with a HashMap of properties that
 * are included in the TXT record for the service advertisement. This HashMap 
 * should remain small, but it could include information such as the XMLIO path
 * (for a web server), the default path (also for a web server), a specific 
 * protocol version, or other information. Note that all service advertisements
 * include the JMRI version, using the key "version", and the JMRI version numbers
 * in a string "major.minor.test" with the key "jmri"
 * <P>
 * All ZeroConfServices are automatically stopped when the JMRI application 
 * shuts down. A collection of all ZeroConfService objects is available with:
 * <pre>ZeroConfService.allServices()</pre>.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author      Randall Wood Copyright (C) 2011
 * @version	$Revision$
 * @see         javax.jmdns.JmDNS
 * @see         javax.jmdns.ServiceInfo
 */
public class ZeroConfService {

    // internal data members
    private ServiceInfo _serviceInfo = null;
    // static data objects
    private static HashMap<String, ZeroConfService> _services = null;
    private static JmDNS _jmdns = null;
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZeroConfService.class.getName());

    /**
     * Create a ZeroConfService with the minimal required settings. This method
     * calls <pre>create(type, port, props)</pre> with an empty props HashMap.
     * 
     * @param type The service protocol
     * @param port The port the service runs over
     */
    public static ZeroConfService create(String type, int port) {
        return create(type, port, new HashMap<String, String>());
    }

    /**
     * Create a ZeroConfService with an automatically detected server name. This
     * method calls <pre>create</pre> with the default weight and priority, and
     * with the name "<em>hostName</em>" with dots and dashes replaced
     * with spaces.
     * 
     * @param type The service protocol
     * @param port The port the service runs over
     * @param properties Additional information to be listed in service advertisement
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
     * that service is returned unmodified instead of a new object.
     * <!-- after upgrading to JmDNS 3.4 this should change, since 3.4 advertises changes to text properties on a service -->
     * 
     * @param name The name of service listed on client devices
     * @param type The service protocol
     * @param port The port the service runs over
     * @param weight Default value is 0
     * @param priority Default value is 0
     * @param properties Additional information to be listed in service advertisement
     */
    public static ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> properties) {
        ZeroConfService s = null;
        if (ZeroConfService.services().containsKey(ZeroConfService.key(type, name))) {
            s = ZeroConfService.services().get(ZeroConfService.key(type, name));
            if (log.isDebugEnabled()) {
                log.debug("Using existing ZeroConfService " + s.key());
            }
        } else {
            properties.put("version", jmri.Version.name());
            // use the major.minor.test version string for jmri since we have potentially
            // tight space constraints in terms of the number of bytes that properties 
            // can use, and there are some unconstrained properties that we would like to use.
            properties.put("jmri", jmri.Version.getCanonicalVersion());
            s = new ZeroConfService(ServiceInfo.create(type, name, port, weight, priority, properties));
            if (log.isDebugEnabled()) {
                log.debug("Creating new ZeroConfService " + s.key());
            }
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
     */
    public String key() {
        // JmDNS 3.4 supports a getKey() method, but 3.0 does not
        return _serviceInfo.getQualifiedName().toLowerCase();
    }

    /**
     * Generate a ZeroConfService key for searching in the HashMap of running
     * services.
     *
     * @param type
     * @param name
     */
    protected static String key(String type, String name) {
        return (name + "." + type).toLowerCase();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set
     * when creating the object.
     * 
     */
    public String name() {
        return _serviceInfo.getName();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set
     * when creating the object.
     * 
     */
    public String type() {
        return _serviceInfo.getType();
    }

    /**
     * Get the ServiceInfo property of the object. This is the JmDNS
     * implementation of a zeroConf service.
     * 
     */
    public ServiceInfo serviceInfo() {
        return _serviceInfo;
    }

    /**
     * Get the state of the service. True if the service is being advertised,
     * and false otherwise.
     * 
     */
    public Boolean isPublished() {
        return ZeroConfService.services().containsKey(key());
    }

    /**
     * Start advertising the service.
     */
    public void publish() {
        if (!isPublished()) {
            try {
                ZeroConfService.jmdns().registerService(_serviceInfo);
                ZeroConfService.services().put(key(), this);
                if (log.isDebugEnabled()) {
                    log.debug("Publishing zeroConf service for " + key() + " on");
                    for (int i = 0; i < _serviceInfo.getInetAddresses().length; i++) {
                        if (_serviceInfo.getInetAddresses()[i] != null) {
                            log.debug("\t" + _serviceInfo.getInetAddresses()[i]);
                        }
                    }
                }
            } catch (IOException ex) {
                log.error("Unable to publish service for " + key() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Stop advertising the service.
     */
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping ZeroConfService " + key());
        }
        if (ZeroConfService.services().containsKey(key())) {
            ZeroConfService.jmdns().unregisterService(_serviceInfo);
            ZeroConfService.services().remove(key());
        }
    }

    /**
     * Stop advertising all services.
     */
    public static void stopAll() {
        log.debug("Stopping all ZeroConfServices");
        ZeroConfService.jmdns().unregisterAllServices();
        ZeroConfService.services().clear();
    }

    /**
     * A list of published ZeroConfServices 
     * 
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
    private static JmDNS jmdns() {
    	if (_jmdns == null) {
			if (log.isDebugEnabled()) {
				log.debug("JmDNS version: " + JmDNS.VERSION);
			}
    		try {
    			//get good host address to pass to jmdns.create(), null no longer works on ubuntu w/dhcp
    			InetAddress hostAddress = Inet4Address.getLocalHost();
    			if (hostAddress == null || hostAddress.isLoopbackAddress()) {
    				hostAddress = hostAddress();  //lookup from interfaces
    			}
    			if (log.isDebugEnabled()) log.debug("Calling JMDNS.create("+hostAddress.getHostAddress()+", "+hostAddress.getHostName()+")");
    			_jmdns = JmDNS.create(hostAddress, hostAddress.getHostName());  //NOTE name is ignored by jmdns 3.4.1
    			
    			if (jmri.InstanceManager.shutDownManagerInstance() != null) {
    				ShutDownTask task = new QuietShutDownTask("Stop ZeroConfServices") {

                        @Override
                        public boolean execute() {
                            jmri.util.zeroconf.ZeroConfService.stopAll();
                            try {
                                jmri.util.zeroconf.ZeroConfService.jmdns().close();
                            } catch (IOException e) {
                                log.debug("jmdns.close() returned IOException: " + e.getMessage());
                            }
                            return true;
                        }
                    };
                    jmri.InstanceManager.shutDownManagerInstance().register(task);
                }
            } catch (IOException ex) {
                log.warn("Unable to create JmDNS with error: " + ex.getMessage());
            }
        }
        return _jmdns;
    }

    /**
     * Return the system name or "computer" if the system name cannot be
     * determined. This method uses the JmDNS.getHostName() method.
     * 
     */
    public static String hostName() {
        // getHostName returns the FQDN or "computer" for the host
        String hostName = ZeroConfService.jmdns().getHostName() + ".";
        // we would have to check for the existance of . if we did not add .
        // to the string above.
        return hostName.substring(0, hostName.indexOf('.'));
    }

    /**
     * Return the fully qualified domain name or "computer" if the system name
     * cannot be determined. This method uses the JmDNS.getHostName() method.
     */
    public static String FQDN() {
        return ZeroConfService.jmdns().getHostName();
    }

    /**
     * Return the non-loopback ipv4 address of the host, or null if none found.
     */
    public static InetAddress hostAddress() {
        // hostAddress returns the IPv4 address for the host
		InetAddress hostAddress = null;
		Enumeration<NetworkInterface> ifs=null;
		try {
			log.debug("Attempting to enumerate all network interfaces");
			ifs = NetworkInterface.getNetworkInterfaces();

			// Iterate all interfaces
			while (ifs.hasMoreElements() && hostAddress == null) {
				NetworkInterface iface = ifs.nextElement();

				// Fetch all IP addresses on this interface
				Enumeration<InetAddress> ips = iface.getInetAddresses();

				// Iterate the IP addresses
				while (ips.hasMoreElements()) {
					InetAddress ip=ips.nextElement();
					// use the last ipv4 that's not a loopback
					if((ip instanceof Inet4Address) && !ip.isLoopbackAddress()) {
						hostAddress = ip;
						if (log.isDebugEnabled()) log.debug("  found: " + hostAddress.getHostAddress());
					}
				}
			}
		} catch (SocketException se) {
			log.warn("Could not enumerate network interfaces");
		}
		return hostAddress;
    }

}

/* @(#)ZeroConfService.java */
