// SE8Frame.java

package jmri.jmrix.loconet.se8;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Display and modify an SE8c configuration.
 * <P>
 * The read and write require a sequence of operations, which
 * we handle with a state variable.
 * <P>
 * Programming of the SE8c is done via configuration messages, so
 * the SE8c should not be put into programming mode via the
 * built-in pushbutton while this tool is in use.
 * <P>
 * Throughout, the terminology is "closed" == true, "thrown" == false.
 * Variables are named for their closed state.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author  Bob Jacobsen   Copyright (C) 2003
 * @version $Revision: 1.5 $
 */
public class SE8Frame extends JFrame implements LocoNetListener {

    public SE8Frame() {
        super("SE8 programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        appendLine(provideAddressing());  // add read/write buttons, address

        JPanel panel2;
        appendLine(fullmode);
        appendLine(twoaspects);
            panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel("Cables 1-4 are "));
            panel2.add(section1to4mode);
        appendLine(panel2);
            panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel("Cables 5-8 are "));
            panel2.add(section5to8mode);
        appendLine(panel2);
            panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel("4th aspect is "));
            panel2.add(fourthAspect);
        appendLine(panel2);
        appendLine(semaphore);
        appendLine(pulsed);
        appendLine(disableDS);
        appendLine(fromloconet);
        appendLine(disablelocal);
        appendLine(sigaddress);
        appendLine(bcastaddress);
        appendLine(semaddress);
        appendLine(setdefault);
        appendLine(exercise);

        appendLine(status);

        // add status
        getContentPane().add(status);

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
        show();
    }

    /**
     * Provide read, write buttons and address
     */
    JPanel provideAddressing() {
        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
            pane0.add(new JLabel("Unit address: "));
            pane0.add(addrField);
            pane0.add(readAllButton);
            pane0.add(writeAllButton);
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
        return pane0;
    }
    JToggleButton readAllButton  = new JToggleButton("Read from SE8");
    JToggleButton writeAllButton = new JToggleButton("Write to SE8");
    JTextField addrField = new JTextField(4);
    
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
        // check the address
        try {
            setAddress(255);
        } catch (Exception e) {
            log.debug("readAll aborted due to invalid address");
        }
        // Start the first operation
        read = true;
        state = 1;
        nextRequest();
    }

    int address = 0;
        
    void nextRequest() {
        if (read) {
            // read op
            status.setText("Reading opsw "+state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x62);
            l.setElement(2, address&0x7F);
            l.setElement(3, 0x72);
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
            l.setElement(2, address&0x7F);
            l.setElement(3, 0x72);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2+(opsw[state]?1:0));
            LnTrafficController.instance().sendLocoNetMessage(l);
        }
    }

    /**
      * Turn the textfield containing the address into 
      * a valid integer address, handling user-input
      * errors as needed
      */
    protected void setAddress(int maxValid) throws Exception {
        try {
            address = (Integer.parseInt(addrField.getText())-1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Invalid Address",
                    "Error",JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing Rate Field: "+e);
            throw e;
        }
        // parsed OK, check range
        if (address > (maxValid+1) || address < 0) {
            JOptionPane.showMessageDialog(this,
                "Address out of range, must be 1 to "+(maxValid+1),
                "Error",JOptionPane.ERROR_MESSAGE);
            log.error("Invalid address value");
            throw new jmri.JmriException("Address out of range: "+address);
        }
        return;  // OK
    }
    
    /**
     * Copy from the GUI to the opsw array. 
     * <p>
     * Used before write operations start
     */
     
    void copyToOpsw() {
        opsw[ 1] = fullmode.isSelected();
        opsw[ 2] = twoaspects.isSelected();

        opsw[11] = semaphore.isSelected();
        opsw[12] = pulsed.isSelected();
        opsw[13] = disableDS.isSelected();
        opsw[14] = fromloconet.isSelected();
        opsw[15] = disablelocal.isSelected();
        opsw[17] = sigaddress.isSelected();
        opsw[18] = bcastaddress.isSelected();
        opsw[19] = semaddress.isSelected();
        opsw[20] = setdefault.isSelected();
        opsw[21] = exercise.isSelected();

        // value based
        int value = section1to4mode.getSelectedIndex();
        if ((value&0x01) != 0) opsw[5] = true;
        else opsw[5] = false;
        if ((value&0x02) != 0) opsw[4] = true;
        else opsw[4] = false;
        if ((value&0x04) != 0) opsw[3] = true;
        else opsw[3] = false;

        value = section5to8mode.getSelectedIndex();
        if ((value&0x01) != 0) opsw[8] = true;
        else opsw[8] = false;
        if ((value&0x02) != 0) opsw[7] = true;
        else opsw[7] = false;
        if ((value&0x04) != 0) opsw[6] = true;
        else opsw[6] = false;

        value = fourthAspect.getSelectedIndex();
        if ((value&0x01) != 0) opsw[10] = true;
        else opsw[10] = false;
        if ((value&0x02) != 0) opsw[9] = true;
        else opsw[9] = false;
    }
    
    void writeAll() {
        // check the address
        try {
            setAddress(255);
        } catch (Exception e) {
            log.debug("writeAll aborted due to invalid address"+e);
        }
        
        // copy over the display
        copyToOpsw();
        
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
        // binary switches
        fullmode.setSelected(opsw[ 1]);
        twoaspects.setSelected(opsw[ 2]);
        semaphore.setSelected(opsw[11]);
        pulsed.setSelected(opsw[12]);
        disableDS.setSelected(opsw[13]);
        fromloconet.setSelected(opsw[14]);
        disablelocal.setSelected(opsw[15]);
        sigaddress.setSelected(opsw[17]);
        bcastaddress.setSelected(opsw[18]);
        semaddress.setSelected(opsw[19]);
        setdefault.setSelected(opsw[20]);
        exercise.setSelected(opsw[21]);

        // value-based switches
        int value = 0;
        if (opsw[5]) value += 1;
        if (opsw[4]) value += 2;
        if (opsw[3]) value += 4;
        section1to4mode.setSelectedIndex(value);

        value = 0;
        if (opsw[8]) value += 1;
        if (opsw[7]) value += 2;
        if (opsw[6]) value += 4;
        section5to8mode.setSelectedIndex(value);

        value = 0;
        if (opsw[10]) value += 1;
        if (opsw[9]) value += 2;
        fourthAspect.setSelectedIndex(value);

    }

    int nextState() {
        switch (state) {
            case  1: return 2;
            case  2: return 3;
            case  3: return 4;
            case  4: return 5;
            case  5: return 6;
            case  6: return 7;
            case  7: return 8;
            case  8: return 9;
            case  9: return 10;
            case 10: return 11;
            case 11: return 12;
            case 12: return 13;
            case 13: return 14;
            case 14: return 15;
            case 15: return 17;
            case 17: return 18;
            case 18: return 19;
            case 19: return 20;
            case 20: return 21;
            case 21: return  0;   // done!
            default:
                log.error("unexpected state "+state);
                return 0;
        }
    }


    JCheckBox fullmode            = new JCheckBox("Reserved (OpSw 1)");  // opsw 01
    JCheckBox twoaspects          = new JCheckBox("Two aspects (one turnout address) per head");  // opsw 02
    JComboBox section1to4mode     = new JComboBox(new String[] {
                                              "3 LEDs common anode","3 LEDs common cathode",
                                              "3-wire searchlight common anode","3-wire searchlight common cathode",
                                              "Reserved (4)", "Reserved (5)",
                                              "2-wire searchlight common anode","2-wire searchlight common cathode"
                                              });  // opsw 3, 4, 5
    JComboBox section5to8mode     = new JComboBox(new String[] {
                                              "3 LEDs common anode","3 LEDs common cathode",
                                              "3-wire searchlight common anode","3-wire searchlight common cathode",
                                              "Reserved (4)", "Reserved (5)",
                                              "2-wire searchlight common anode","2-wire searchlight common cathode"
                                              });  // opsw 6, 7, 8
    JComboBox fourthAspect        = new JComboBox(new String[] {
                                              "flashing yellow", "flashing red",
                                              "dark","flashing green"
                                              });  // opsw 9, 10
    JCheckBox semaphore           = new JCheckBox("Semaphore mode");  // opsw 11
    JCheckBox pulsed              = new JCheckBox("Pulsed switch outputs");  // opsw 12
    JCheckBox disableDS           = new JCheckBox("Disable DS input");  // opsw 13
    JCheckBox fromloconet         = new JCheckBox("Enable switch command from loconet");  // opsw 14
    JCheckBox disablelocal        = new JCheckBox("Disable local switch control");  // opsw 15
    JCheckBox sigaddress          = new JCheckBox("Next switch command sets signal address");  // opsw 17
    JCheckBox bcastaddress        = new JCheckBox("Next switch command sets broadcast address");  // opsw 18
    JCheckBox semaddress          = new JCheckBox("Next switch command sets semaphore address");  // opsw 19
    JCheckBox setdefault          = new JCheckBox("Restore factory default, including address");  // opsw 20
    JCheckBox exercise            = new JCheckBox("Show LED exercise pattern");  // opsw 21

    JLabel status = new JLabel("The SE8 should be in normal mode (Don't push the buttons on the SE8!)");

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SE8Frame.class.getName());

}
