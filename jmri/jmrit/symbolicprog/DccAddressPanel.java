// DccAddressPanel.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;

import javax.swing.*;

/**
 * Provide a graphical representation of the DCC address, either long or short
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.9 $
 */
public class DccAddressPanel extends JPanel {

    JTextField val = new JTextField(6);

    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    EnumVariableValue addMode = null;

    VariableTableModel variableModel = null;

    /**
     * Ctor using default label for the address.
     * @param mod The current table of variables, used to locate the
     * status information needed.
     */
    public DccAddressPanel(VariableTableModel mod) {
        this(mod, "Active DCC Address: ");
    }
    public DccAddressPanel(VariableTableModel mod, String label) {
        variableModel = mod;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // arrange for the field to be updated when any of the variables change
        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
            };

        // connect to variables
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr==null) log.debug("DCC Address monitor did not find a Short Address variable");
        else primaryAddr.addPropertyChangeListener(dccNews);

        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr==null) log.debug("DCC Address monitor did not find an Long Address variable");
        else extendAddr.addPropertyChangeListener(dccNews);

        addMode = (EnumVariableValue)variableModel.findVar("Address Format");
        if (addMode==null) log.debug("DCC Address monitor didnt find an Address Format variable");
        else addMode.addPropertyChangeListener(dccNews);

        // show the selection
        if (addMode != null) {
            add(addMode.getRep("radiobuttons"));
        }

        // show address field
        add(new JLabel(label));
        val.setToolTipText("This field shows the DCC address currently in use. CV1 provides the short address; CV17 & 18 provide the long address");
        add(val);

        // update initial contents & color
        if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
            if (primaryAddr!=null) {
                // short address
                val.setBackground(primaryAddr.getValue().getBackground());
                val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
            }
        } else {
            // long address
            val.setBackground(extendAddr.getValue().getBackground());
            val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
        }

        // start listening for changes to this value
        val.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
                        if (primaryAddr!=null) {
                            // short address mode
                            primaryAddr.updatedTextField();
                            val.setBackground(primaryAddr.getValue().getBackground());
                            if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
                        }
                    }
                    else {
                        // long address
                        extendAddr.updatedTextField();
                        val.setBackground(extendAddr.getValue().getBackground());
                        if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
                    }
                }
            });
        val.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusGained");
                    enterField();
                }
                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusLost");
                    exitField();
                }
            });

    }

    String oldContents = "";

    /**
     * Handle focus entering the address field by recording the contents.
     */
    void enterField() {
        oldContents = val.getText();
    }

    /**
     * Handle focus leaving the address field by checking to see if the
     * contents changed.  We do this because we want to record that change
     * even if it hasn't been "entered" via return key et al.
     */
    void exitField() {
        if (!oldContents.equals(val.getText())) {
            if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
                if (primaryAddr!=null) {
                    // short address mode
                    primaryAddr.updatedTextField();
                    val.setBackground(primaryAddr.getValue().getBackground());
                    if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
                }
            }
            else {
                // long address
                extendAddr.updatedTextField();
                val.setBackground(extendAddr.getValue().getBackground());
                if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
            }
        }
    }

    /**
     * Handle a (possible) update to the active DCC address, either because
     * the state changed or the address mode changed.  Note that value changes
     * of the active address are directly reflected, so we don't have to do
     * anything on those, but we still go ahead and update the state color.
     */
    void updateDccAddress() {
        if (log.isDebugEnabled())
            log.debug("updateDccAddress: short "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
                      " long "+(extendAddr==null?"<null>":extendAddr.getValueString())+
                      " mode "+(addMode==null?"<null>":addMode.getValueString()));
        if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
            if (primaryAddr!=null) {
                // short address mode
                val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
                val.setBackground(primaryAddr.getValue().getBackground());
                if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
            }
        }
        else {
            // long address
            val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
            val.setBackground(extendAddr.getValue().getBackground());
            if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
        }
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DccAddressPanel.class.getName());

}
