
package jmri.jmrix.lenz;

import junit.framework.*;

import apps.tests.*;

/**
 * <p>Title: XNetTrafficRouterTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 */
public class XNetTrafficRouterTest extends TestCase {
    Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    public XNetTrafficRouterTest(String s) {
        super(s);
    }

    protected void setUp() {
        log4jfixtureInst.setUp();
    }

    protected void tearDown() {
        log4jfixtureInst.tearDown();
    }

    public void testConnectAndSend() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation());

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation());
        Assert.assertEquals("router is instance", XNetTrafficController.instance(), router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // send a message
        XNetMessage m = new XNetMessage(3);
        router.sendXNetMessage(m);

        // check receipt
        Assert.assertEquals("one message sent", 1, upstream.outbound.size());
        Assert.assertTrue(upstream.outbound.elementAt(0)==m);
    }

    static int count = 0;

    public void testReceiveAndForward() {
        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation());
        Assert.assertEquals("router is instance", XNetTrafficController.instance(), router);

        count = 0;
        // register a listener
        XNetListener l = new XNetListener(){
            public void message(XNetMessage m) {
                count++;
            }
        };
        router.addXNetListener(~0, l);
        // send a message
        XNetMessage m = new XNetMessage(3);
        router.message(m);

        // check receipt
        Assert.assertEquals("one message sent", 1, count);
    }

    public void testConnectAndDisconnect() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation());

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

}
