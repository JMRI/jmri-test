// LnTcpDriverFrame.java

package jmri.jmrix.loconet.loconetovertcp;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect to a LocoNet via LocoNetOverTcp protocol.
 * @author                      Bob Jacobsen   Copyright (C) 2003
 * @version                     $Revision: 1.5 $
 */
public class LnTcpDriverFrame extends jmri.jmrix.SerialPortFrame {

    public LnTcpDriverFrame() {
        super("Open LocoNetOverTcp network connection");
        adapter = LnTcpDriverAdapter.instance();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.openPort((String) portBox.getSelectedItem(),"LocoNetOverTcp");

            adapter.configure();

            // hide this frame, since we're done
            setVisible(false);
        } else {
            // not selected
            JOptionPane.showMessageDialog(this, "Please select a port name first");
        }
    }
    
    public jmri.jmrix.SerialPortAdapter getAdapter() {  return adapter; }

}
