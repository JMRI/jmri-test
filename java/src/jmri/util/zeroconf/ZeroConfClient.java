package jmri.util.zeroconf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class ZeroConfClient {

    private ServiceListener mdnsServiceListener = null;

    // mdns related routines.
    public void startServiceListener(String service) {
        log.debug("StartServiceListener called for service: " + service);
        if (mdnsServiceListener == null) {
            mdnsServiceListener = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent serviceEvent) {
                    // Test service is discovered. requestServiceInfo() will
                    // trigger serviceResolved() callback.
                    String serviceUrl = serviceEvent.getInfo().getURL();
                    log.debug("serviceAdded: " + serviceUrl);
                    //mdnsService.requestServiceInfo("xpressnet", serviceEvent.getName());
                }

                @Override
                public void serviceRemoved(ServiceEvent serviceEvent) {
                    // Test service is disappeared.
                }

                @Override
                public void serviceResolved(ServiceEvent serviceEvent) {
                    // Test service info is resolved.
                    String serviceUrl = serviceEvent.getInfo().getURL();
                    // serviceURL is usually something like
                    // http://192.168.11.2:6666/my-service-name
                    log.debug("serviceResolved: " + serviceUrl);
                }
            };
        }
        ZeroConfService.jmdns().addServiceListener(service, mdnsServiceListener);
        //ServiceInfo[] infos = mdnsService.list(Constants.mdnsServiceType);
        // Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
    }

    public void stopServiceListener(String service) {
        ZeroConfService.jmdns().removeServiceListener(service, mdnsServiceListener);
    }

    public void listService(String service) {
        ServiceInfo[] infos = ZeroConfService.jmdns().list(service);
        System.out.println("List " + service);
        for (ServiceInfo info : infos) {
            System.out.println(info);
        }
        System.out.println();
    }

    static Logger log = LoggerFactory.getLogger(ZeroConfClient.class.getName());

}
