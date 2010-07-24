/**
 * MS100Frame.java
 *
 * Description:		Frame to control and connect LocoNet via MS100 interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet.ms100;

import javax.swing.*;
@Deprecated 
public class MS100Frame extends jmri.jmrix.SerialPortFrame {

	public MS100Frame() {
		super("Open MS100");
		adapter = new MS100Adapter();
	}

	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
		if ((String) portBox.getSelectedItem() != null) {
			// connect to the port
			adapter.configureOption2((String)opt2Box.getSelectedItem());
			adapter.openPort((String) portBox.getSelectedItem(),"MS100Frame");
			adapter.configure();
			// hide this frame, since we're done
      setVisible(false);
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}

// Data members

}
