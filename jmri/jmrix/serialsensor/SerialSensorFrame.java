// SerialSensorFrame.java

package jmri.jmrix.serialsensor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Frame to control and connect a serial port as two sensors
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class SerialSensorFrame extends jmri.jmrix.SerialPortFrame {

    public SerialSensorFrame() {
        super("Open serial port sensor connection");
        adapter = new SerialSensorAdapter();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.openPort((String) portBox.getSelectedItem(),"SerialSensorFrame");

            adapter.configure();

            // hide this frame, since we're done
            hide();
        } else {
            // not selected
            JOptionPane.showMessageDialog(this, "Please select a port name first");
        }
    }

}
