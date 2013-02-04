/**
 * XNetProgrammerTest.java
 *
 * Description:	    JUnit tests for the XNetProgrammer class
 * @author			Bob Jacobsen
 * @version         $Revision$
 */

package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XNetProgrammerTest extends TestCase {

	public void testWriteCvSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t){
                        protected synchronized void restartTimer(int delay) {
                            super.restartTimer(200);
                        }
                };

		// and do the write
		p.writeCV(29, 34, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
                Assert.assertEquals("write message contents", "23 16 1D 22 0A", t.outbound.elementAt(0).toString());
                // send reply
                XNetReply mr1 = new XNetReply();
                mr1.setElement(0,0x61);
                mr1.setElement(1,0x02);
                mr1.setElement(2,0x63);
                t.sendTestMessage(mr1);

	        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
                Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

                // send a result string
                XNetReply mr2 = new XNetReply();
                mr2.setElement(0,0x63);
                mr2.setElement(1,0x14);
                mr2.setElement(2,0x1D);
                mr2.setElement(3,0x22);
                mr2.setElement(4,0x48);
                t.sendTestMessage(mr2);

                // At this point, the standard XPressNet programmer 
                // should send a result to the programmer listeners, and 
                // wait for either the next read/write request or for the 
                // traffic controller to exit from service mode.  We just
                // need to wait a few seconds and see that the listener we
                // registered earlier received the values we expected.

                jmri.util.JUnitUtil.releaseThread(this,1000);

                 //failure in this test occurs with the next line.
                 Assert.assertFalse("Receive Called by Programmer",l.getRcvdInvoked()==0); 
                 Assert.assertEquals("Direct mode received value",34,l.getRcvdValue());
    }

	public void testWriteRegisterSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t){
                        protected synchronized void restartTimer(int delay) {
                            super.restartTimer(200);
                        }
                };

                // set register mode
                p.setMode(Programmer.REGISTERMODE);

		// and do the write
		p.writeCV(29, 12, l);
		// check "prog mode" message sent
		Assert.assertEquals("read message sent", 1, t.outbound.size());
                Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

                // send reply
                XNetReply mr1 = new XNetReply();
                mr1.setElement(0,0x61);
                mr1.setElement(1,0x02);
                mr1.setElement(2,0x63);
                t.sendTestMessage(mr1);

	        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
                Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

                // send a result string
                XNetReply mr2 = new XNetReply();
                mr2.setElement(0,0x63);
                mr2.setElement(1,0x10);
                mr2.setElement(2,0x05);
                mr2.setElement(3,0x0C);
                mr2.setElement(4,0x7A);
                t.sendTestMessage(mr2);

                // At this point, the standard XPressNet programmer 
                // should send a result to the programmer listeners, and 
                // wait for either the next read/write request or for the 
                // traffic controller to exit from service mode.  We just
                // need to wait a few seconds and see that the listener we
                // registered earlier received the values we expected.

                jmri.util.JUnitUtil.releaseThread(this,1000);

                //failure in this test occurs with the next line.
                Assert.assertFalse("Receive Called by Programmer",l.getRcvdInvoked()==0);

                Assert.assertEquals("Register mode received value",12,l.getRcvdValue());
    }

	public void testReadCvSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t){
                        protected synchronized void restartTimer(int delay) {
                            super.restartTimer(200);
                        }
                };

		// and do the read
		p.readCV(29, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 1D 2A", t.outbound.elementAt(0).toString());

                // send reply
                XNetReply mr1 = new XNetReply();
                mr1.setElement(0,0x61);
                mr1.setElement(1,0x02);
                mr1.setElement(2,0x63);
                t.sendTestMessage(mr1);

	        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
                Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());


                // send a result string
                XNetReply mr2 = new XNetReply();
                mr2.setElement(0,0x63);
                mr2.setElement(1,0x14);
                mr2.setElement(2,0x1D);
                mr2.setElement(3,0x22);
                mr2.setElement(4,0x48);
                t.sendTestMessage(mr2);

                // At this point, the standard XPressNet programmer 
                // should send a result to the programmer listeners, and 
                // wait for either the next read/write request or for the 
                // traffic controller to exit from service mode.  We just
                // need to wait a few seconds and see that the listener we
                // registered earlier received the values we expected.

                jmri.util.JUnitUtil.releaseThread(this,1000);
 
                //failure in this test occurs with the next line.
                Assert.assertFalse("Receive Called by Programmer",l.getRcvdInvoked()==0);

                Assert.assertEquals("Register mode received value",34,l.getRcvdValue());
	}

	public void testReadRegisterSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t){
                        protected synchronized void restartTimer(int delay) {
                            super.restartTimer(200);
                        }
                };

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply (enter service mode )
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0,0x61);
        mr1.setElement(1,0x02);
        mr1.setElement(2,0x63);
        t.sendTestMessage(mr1);
 
	Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0,0x63);
        mr2.setElement(1,0x10);
        mr2.setElement(2,0x05);
        mr2.setElement(3,0x22);
        mr2.setElement(4,0x54);
        t.sendTestMessage(mr2);

                // At this point, the standard XPressNet programmer 
                // should send a result to the programmer listeners, and 
                // wait for either the next read/write request or for the 
                // traffic controller to exit from service mode.  We just
                // need to wait a few seconds and see that the listener we
                // registered earlier received the values we expected.

                jmri.util.JUnitUtil.releaseThread(this,1000);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer",l.getRcvdInvoked()==0);
        Assert.assertEquals("Register mode received value",34,l.getRcvdValue());
    }
	

    // from here down is testing infrastructure

	public XNetProgrammerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetProgrammerTest.class);
		return suite;
	}

    // The minimal setup is for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); } 
    protected void tearDown() { apps.tests.Log4JFixture.tearDown();}

	static Logger log = Logger.getLogger(XNetProgrammerTest.class.getName());

}
