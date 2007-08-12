
package jmri.jmrix.lenz;

import junit.framework.*;

/**
 * <p>Title: XNetTrafficRouterTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 2.5 $
 */
public class XNetTrafficRouterTest extends TestCase {

    public XNetTrafficRouterTest(String s) {
        super(s);
    }

    public void testConnectAndSend() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation());

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation())
        {
            protected void connectionWarn() {}
        };
        Assert.assertEquals("router is instance", XNetTrafficController.instance(), router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // send a message
        XNetMessage m = new XNetMessage(3);
	m.setElement(0,0x01);
	m.setElement(1,0x04);
	m.setElement(2,0x05);
        router.sendXNetMessage(m, null);

        // check receipt
        Assert.assertEquals("one message sent", 1, upstream.outbound.size());
        Assert.assertTrue(upstream.outbound.elementAt(0)==m);
    }

    static int count = 0;

    public void testReceiveAndForward() {
        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation())
        {
            protected void connectionWarn() {}
        };
        Assert.assertEquals("router is instance", XNetTrafficController.instance(), router);

        count = 0;
        // register a listener
        XNetListener l = new XNetListener(){
            public void message(XNetReply m) {
                count++;
            }
            public void message(XNetMessage m) {
            }
        };
        router.addXNetListener(~0, l);
        // send a message
        XNetReply m = new XNetReply();
	m.setElement(0,0x01);
	m.setElement(1,0x04);
	m.setElement(2,0x05);
	router.forwardReply(l,m);

        // check receipt
        Assert.assertEquals("one message sent", 1, count);
    }

    public void testConnectAndDisconnect() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation())
        {
            protected void connectionWarn() {}
        };

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation());
        Assert.assertEquals("router is instance", XNetTrafficController.instance(), router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // disconnect
        router.disconnectPort(upstream);
        Assert.assertTrue("not connected", !router.status());
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
