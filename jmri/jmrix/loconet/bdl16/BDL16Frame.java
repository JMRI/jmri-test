// BDL16Frame.java

package jmri.jmrix.loconet.bdl16;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * Frame displaying and programming a BDL16 configuration.
 * <P>
 * The read and write require a sequence of operations, which
 * we handle with a state variable.
 * <P>
 * Programming of the BDL16 is done via configuration messages, so
 * the BDL16 should not be put into programming mode via the
 * built-in pushbutton while this tool is in use.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.6 $
 */
public class BDL16Frame extends JFrame implements LocoNetListener {

    public BDL16Frame() {
        super("BDL16 programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
            pane0.add(new JLabel("Unit address: "));
            pane0.add(addrField);
            pane0.add(readAllButton);
            pane0.add(writeAllButton);
        appendLine(pane0);


        appendLine(commonrail);
        appendLine(polarity);
        appendLine(transpond);
        appendLine(rx4connected1);
        appendLine(rx4connected2);
        appendLine(forceoccupied);
        appendLine(section16qualpower);
        appendLine(nomaster);
        appendLine(noterminate);
        appendLine(delayhalfsecond);
        appendLine(highthreshold);
        appendLine(drivefromswitch);
        appendLine(decodefromloconet);
        appendLine(setdefault);

        appendLine(status);

        // install read all, write all button handlers
        readAllButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	readAll();
                }
            }
        );
        writeAllButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeAll();
                }
            }
        );

        // add status
        appendLine(status);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // listen for BDL16 traffic
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No LocoNet connection available, can't function");

        // and prep for display
        pack();
        addrField.setText("1");
    }

    /**
     * Handle layout details during construction.
     * <P>
     * @param c component to put on a single line
     */
    void appendLine(JComponent c) {
        c.setAlignmentX(0.f);
        getContentPane().add(c);
    }

    boolean read = false;
    int state = 0;

    void readAll() {
        // Start the first operation
        read = true;
        state = 1;
        nextRequest();
    }

    void nextRequest() {
        if (read) {
            // read op
            status.setText("Reading opsw "+state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x62);
            l.setElement(2, (Integer.parseInt(addrField.getText())-1)&0x7F);
            l.setElement(3, 0x71);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2);
            LnTrafficController.instance().sendLocoNetMessage(l);
        } else {
            //write op
            status.setText("Writing opsw "+state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x72);
            l.setElement(2, (Integer.parseInt(addrField.getText())-1)&0x7F);
            l.setElement(3, 0x71);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2+(opsw[state]?1:0));
            LnTrafficController.instance().sendLocoNetMessage(l);
        }
    }

    void writeAll() {
        // copy over the display
        opsw[ 1] = commonrail.isSelected();
        opsw[ 3] = polarity.isSelected();
        opsw[ 5] = transpond.isSelected();
        opsw[ 6] = rx4connected1.isSelected();
        opsw[ 7] = rx4connected2.isSelected();
        opsw[ 9] = forceoccupied.isSelected();
        opsw[10] = section16qualpower.isSelected();
        opsw[11] = nomaster.isSelected();
        opsw[12] = noterminate.isSelected();
        opsw[13] = delayhalfsecond.isSelected();
        opsw[19] = highthreshold.isSelected();
        opsw[25] = drivefromswitch.isSelected();
        opsw[26] = decodefromloconet.isSelected();
        opsw[40] = setdefault.isSelected();

        // Start the first operation
        read = false;
        state = 1;
        nextRequest();
    }

    /**
     * True is "closed", false is "thrown". This matches how we
     * do the check boxes also, where we use the terminology for the
     * "closed" option.
     */
    boolean[] opsw = new boolean[64];

    public void message(LocoNetMessage m) {
        if (log.isDebugEnabled()) log.debug("get message "+m);
        // are we reading? If not, ignore
        if (state == 0) return;
        // check for right type, unit
        if (m.getOpCode() != 0xb4 || m.getElement(1) != 0x00)  return;

        // LACK with 0 in opcode; assume its to us.  Note that there
        // should be a 0x50 in the opcode, not zero, but this is what we
        // see...

        boolean value = false;
        if ( (m.getElement(2)&0x20) != 0) value = true;

        // record this bit
        opsw[state] = value;

        // show what we've got so far
        if (read) updateDisplay();

        // and continue through next state, if any
        state = nextState();
        if (state == 0) {
            // done
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText("Done");
            return;
        } else {
            // create next
            nextRequest();
            return;
        }
    }

    void updateDisplay() {
        commonrail.setSelected(opsw[ 1]);
        polarity.setSelected(opsw[ 3]);
        transpond.setSelected(opsw[ 5]);
        rx4connected1.setSelected(opsw[ 6]);
        rx4connected2.setSelected(opsw[ 7]);
        forceoccupied.setSelected(opsw[ 9]);
        section16qualpower.setSelected(opsw[10]);
        nomaster.setSelected(opsw[11]);
        noterminate.setSelected(opsw[12]);
        delayhalfsecond.setSelected(opsw[13]);
        highthreshold.setSelected(opsw[19]);
        drivefromswitch.setSelected(opsw[25]);
        decodefromloconet.setSelected(opsw[26]);
        setdefault.setSelected(opsw[40]);
    }

    int nextState() {
        switch (state) {
            case  1: return 3;
            case  3: return 5;
            case  5: return 6;
            case  6: return 7;
            case  7: return 9;
            case  9: return 10;
            case 10: return 11;
            case 11: return 12;
            case 12: return 13;
            case 13: return 19;
            case 19: return 25;
            case 25: return 26;
            case 26: return 40;
            case 40: return  0;   // done!
            default:
                log.error("unexpected state "+state);
                return 0;
        }
    }

    JTextField addrField = new JTextField(4);


    JCheckBox commonrail            = new JCheckBox("Common rail wiring");  // opsw 01
    JCheckBox polarity              = new JCheckBox("Reverse polarity for detection"); // opsw 03
    JCheckBox transpond             = new JCheckBox("Enable transponding"); // opsw 05
    JCheckBox rx4connected1         = new JCheckBox("Reserved (Unset if RX4 connected)"); // opsw 06
    JCheckBox rx4connected2         = new JCheckBox("Reserved (Unset if RX4 connected)"); // opsw 07
    JCheckBox forceoccupied         = new JCheckBox("Show unoccupied when power off");  // opsw 09
    JCheckBox section16qualpower    = new JCheckBox("Section 16 used to sense power");  // opsw 10
    JCheckBox nomaster              = new JCheckBox("Do not allow BDL16 to be LocoNet master");  // opsw 11
    JCheckBox noterminate           = new JCheckBox("Do not allow BDL16 to terminate LocoNet");  // opsw 12
    JCheckBox delayhalfsecond       = new JCheckBox("Delay only 1/2 second at power up");  // opsw 13
    JCheckBox highthreshold         = new JCheckBox("High threshold sense (10kohms)");  // opsw 19
    JCheckBox drivefromswitch       = new JCheckBox("Drive LEDs from switch commands, not occupancy");  // opsw 25
    JCheckBox decodefromloconet     = new JCheckBox("Decode switch commands from LocoNet");  // opsw 26
    JCheckBox setdefault            = new JCheckBox("Restore factory default, including address");  // opsw 40

    JLabel status = new JLabel("The BDL16 should be in normal mode (Don't push the buttons on the BDL16)");

    JToggleButton readAllButton = new JToggleButton("Read from BDL16");
    JToggleButton writeAllButton = new JToggleButton("Write to BDL16");

    // Destroy the window when the close box is clicked, as there is no
    // way to get it to show again
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // Drop loconet connection
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().removeLocoNetListener(~0, this);

        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BDL16Frame.class.getName());

}
