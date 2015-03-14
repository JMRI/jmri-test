package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

public class LocoNetSlotTest extends NbTestCase {

    public LocoNetSlotTest(String s) {
        super(s);
    }

    public void testGetSlotSend() {
        SlotManager slotmanager = new SlotManager(lnis);
        SlotListener p2 = new SlotListener() {
            public void notifyChangedSlot(LocoNetSlot l) {
            }
        };
        slotmanager.slotFromLocoAddress(21, p2);
        Assert.assertEquals("slot request message",
                "BF 00 15 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    LocoNetInterfaceScaffold lnis;

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoNetSlotTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(LocoNetSlotTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();

        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
