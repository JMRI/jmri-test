// CanSendFrame.java

package jmri.jmrix.can.swing.send;

import jmri.util.StringUtil;

import jmri.jmrix.can.CanInterface;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

// This makes it a bit CBUS specific
// May need refactoring one day
import jmri.jmrix.can.cbus.CbusConstants;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.*;

/**
 * User interface for sending CAN frames to exercise the system
 * <P>
 * When  sending a sequence of operations:
 * <UL>
 * <LI>Send the next message and start a timer
 * <LI>When the timer trips, repeat if buttons still down.
 * </UL>
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class CanSendFrame extends jmri.util.JmriJFrame implements CanListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public CanSendFrame() {
        super();
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[]   = new JTextField[MAXSEQUENCE];
    JCheckBox  mUseField[]      = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[]    = new JTextField[MAXSEQUENCE];
    JToggleButton    mRunButton = new JToggleButton("Go");

    public void initComponents() throws Exception {

        setTitle("Send Can Frame");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // handle single-packet part
        getContentPane().add(new JLabel("Send one frame:"));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText("Frame:");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send frame");

            packetTextField.setToolTipText("Frame packet as hex pairs, e.g. 82 7D; checksum should be present but is recalculated");


            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);
            pane1.add(Box.createVerticalGlue());

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButtonActionPerformed(e);
                    }
                });

            getContentPane().add(pane1);
        }

        getContentPane().add(new JSeparator());

        // Configure the sequence
        getContentPane().add(new JLabel("Send sequence of frames:"));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE+2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel("Send"));
        pane2.add(new JLabel("packet"));
        pane2.add(new JLabel("wait (msec)"));
        for (int i=0;i<MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i+1)));
            mUseField[i]=new JCheckBox();
            mPacketField[i]=new JTextField(10);
            mDelayField[i]=new JTextField(10);
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        getContentPane().add(pane2);

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runButtonActionPerformed(e);
                }
            });

        addHelpMenu("package.jmri.jmrix.can.swing.send.CanSendFrame", true);
        
        // pack to cause display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        CanMessage m = createPacket(packetTextField.getText());
        log.debug("sendButtonActionPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendNextItem();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to handle a timeout and send next item
     */
    synchronized protected void timeout() {
        sendNextItem();
    }
    
    /**
     * Run button pressed down, start the sequence operation
     * @param e
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (!mRunButton.isSelected()) return;
        // make sure at least one is checked
        boolean ok = false;
        for (int i=0; i<MAXSEQUENCE; i++) {
            if (mUseField[i].isSelected()) ok = true;
        }
        if (!ok) {
            mRunButton.setSelected(false);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }

    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = Integer.parseInt(mDelayField[mNextSequenceElement].getText());
        // increment to next line at completion
        mNextSequenceElement++;
        // start timer
        restartTimer(delay);
    }

    /**
     * Send next item; may be used for the first item or
     * when a delay has elapsed.
     */
    void sendNextItem() {
        // check if still running
        if (!mRunButton.isSelected()) return;
        // have we run off the end?
        if (mNextSequenceElement>=MAXSEQUENCE) {
            // past the end, go back
            mNextSequenceElement = 0;
        }
        // is this one enabled?
        if (mUseField[mNextSequenceElement].isSelected()) {
            // make the packet
            CanMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            tc.sendCanMessage(m, this);
            startSequenceDelay();
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed message from a String
     * String is expected to be space seperated hex bytes or compact event
     * notation, e.g.:
     *      12 34 56
     *      +n4e1
     * @param s
     * @return The packet, with contents filled-in
     */
    CanMessage createPacket(String s) {
        CanMessage m;
        boolean eventNotation = false;
        if ((s.startsWith("+") || s.startsWith("-"))
                && (s.toLowerCase().charAt(1)=='n')
                && (s.toLowerCase().indexOf('e') >= 3)) {
            eventNotation = true;
        }
        if (eventNotation) {
            int element = 0;
            int node = 0;
            int event = 0;
            int idx = 2;
            m = new CanMessage();
            if (s.startsWith("+")) {
                m.setElement(element++, CbusConstants.CBUS_OP_EV_ON);
            } else {
                m.setElement(element++, CbusConstants.CBUS_OP_EV_OFF);
            }
            try {
                node = Integer.parseInt(s.toLowerCase().substring(2, s.indexOf('e')));
                event = Integer.parseInt(s.toLowerCase().substring(s.indexOf('e')+1));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            m.setElement(element++, (node>>16)&0xFF);
            m.setElement(element++, node&0xFF);
            m.setElement(element++, (event>>16)&0xFF);
            m.setElement(element++, event&0xFF);
            m.setNumDataElements(5);
        } else {
            // gather bytes in result
            byte b[] = StringUtil.bytesFromHexString(s);
            if (b.length == 0) return null;  // no such thing as a zero-length message
            m = new CanMessage(b.length);
            // Use &oxff to ensure signed bytes are stored as unsigned ints
            for (int i=0; i<b.length; i++) m.setElement(i, b[i]&0xff);
        }
        return m;
    }

    // connect to the CanInterface
    public void connect(CanInterface t) {
        tc = t;
        tc.addCanListener(this);
    }

    /**
     * Don't pay attention to messages
     */
    public void message(CanMessage m) {
    }

    /**
     * Don't pay attention to replies
     */
    public void reply(CanReply m) {
    }
    

    /**
     * When the window closes, 
     * stop any sequences running
     */
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }
    
    // private data
    private CanInterface tc = null;
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CanSendFrame.class.getName());

}
