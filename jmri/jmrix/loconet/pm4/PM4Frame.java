// PM4Frame.java

package jmri.jmrix.loconet.pm4;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Frame displaying and programming a PM4 configuration.
 * <P>The read and write require a sequence of operations, which
 * we handle with a state variable.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class PM4Frame extends JFrame implements LocoNetListener {

    public PM4Frame() {
        super("PM4 programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
            pane0.add(new JLabel("Unit address: "));
            pane0.add(addrField);
            pane0.add(readAllButton);
            pane0.add(writeAllButton);
        getContentPane().add(pane0);

        JPanel panec = new JPanel();
        panec.setLayout(new FlowLayout());
            panec.add(new JLabel("Current limit: "));
            panec.add(current);
            current.setSelectedIndex(1);
        getContentPane().add(panec);

        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
            pane1.add(new JLabel("Section 1:  Slow "));
            pane1.add(slow1);
            pane1.add(new JLabel(" Autoreversing "));
            pane1.add(rev1);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
            pane2.add(new JLabel("Section 2:  Slow "));
            pane2.add(slow2);
            pane2.add(new JLabel(" Autoreversing "));
            pane2.add(rev2);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
            pane3.add(new JLabel("Section 3:  Slow "));
            pane3.add(slow3);
            pane3.add(new JLabel(" Autoreversing "));
            pane3.add(rev3);
        getContentPane().add(pane3);

        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
            pane4.add(new JLabel("Section 4:  Slow "));
            pane4.add(slow4);
            pane4.add(new JLabel(" Autoreversing "));
            pane4.add(rev4);
        getContentPane().add(pane4);

        getContentPane().add(status);

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
        getContentPane().add(status);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // listen for PM4 traffic
        LnTrafficController.instance().addLocoNetListener(~0, this);

        // and prep for display
        pack();
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
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x62);
            l.setElement(2, Integer.parseInt(addrField.getText())-1);
            l.setElement(3, 0x70);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2);
            LnTrafficController.instance().sendLocoNetMessage(l);
        } else {
            //write op
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x72);
            l.setElement(2, Integer.parseInt(addrField.getText())-1);
            l.setElement(3, 0x70);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2+(opsw[state]?1:0));
            LnTrafficController.instance().sendLocoNetMessage(l);
        }
    }

    void writeAll() {
        // copy over the display
        opsw[5] = slow1.isSelected();
        opsw[6] = rev1.isSelected();
        opsw[13] = slow2.isSelected();
        opsw[14] = rev2.isSelected();
        opsw[21] = slow3.isSelected();
        opsw[22] = rev3.isSelected();
        opsw[29] = slow4.isSelected();
        opsw[30] = rev4.isSelected();
        // get current limit
        // bit 9 is the low bit, but "closed" is a 0 in the calculation
        // bit 1 is the middle bit, closed is a 1
        // bit 2 is the MSB, closed is a 1
        int index = current.getSelectedIndex();
        opsw[2] = ((index&0x04)!=0) ? true : false;
        opsw[1] = ((index&0x02)!=0) ? true : false;
        opsw[9] = ((index&0x01)!=0) ? false : true;

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
        // are we reading? If not, ignore
        if (state == 0) return;
        // check for right type, unit
        if (m.getOpCode() != 0xb4 || m.getElement(1) != 0x50)  return;
        // LACK to D0; assume its us
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
            return;
        } else {
            // create next
            nextRequest();
            return;
        }
    }

    void updateDisplay() {
        slow1.setSelected(opsw[5]);
        rev1.setSelected(opsw[6]);
        slow2.setSelected(opsw[13]);
        rev2.setSelected(opsw[14]);
        slow3.setSelected(opsw[21]);
        rev3.setSelected(opsw[22]);
        slow4.setSelected(opsw[29]);
        rev4.setSelected(opsw[30]);
        // display current limit
        // bit 9 is the low bit, but "closed" is a 0 in the calculation
        // bit 1 is the middle bit, closed is a 1
        // bit 2 is the MSB, closed is a 1
        int index = 0;
        if (opsw[2]) index += 4;
        if (opsw[1]) index += 2;
        if (!opsw[9]) index += 1;
        current.setSelectedIndex(index);
    }

    int nextState() {
        switch (state) {
            case 1: return 2;
            case 2: return 5;
            case 5: return 6;
            case 6: return 9;
            case 9: return 13;
            case 13: return 14;
            case 14: return 21;
            case 21: return 22;
            case 22: return 29;
            case 29: return 30;
            case 30: return 0;   // done!
            default:
                log.error("unexpected state "+state);
                return 0;
        }
    }

    JTextField addrField = new JTextField("1");

    JComboBox current = new JComboBox(new String[]{ "1.5 amps", "3 amps", "4.5 amps", "6 amps",
                				"7.5 amps", "9 amps", "10.5 amps", "12 amps"});

    JCheckBox slow1 = new JCheckBox();
    JCheckBox rev1  = new JCheckBox();
    JCheckBox slow2 = new JCheckBox();
    JCheckBox rev2  = new JCheckBox();
    JCheckBox slow3 = new JCheckBox();
    JCheckBox rev3  = new JCheckBox();
    JCheckBox slow4 = new JCheckBox();
    JCheckBox rev4  = new JCheckBox();

    JLabel status = new JLabel("             ");

    JToggleButton readAllButton = new JToggleButton("Read from PM4");
    JToggleButton writeAllButton = new JToggleButton("Write to PM4");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PM4Frame.class.getName());

}
