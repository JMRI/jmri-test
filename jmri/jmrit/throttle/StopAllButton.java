package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JButton;

import jmri.jmrit.catalog.NamedIcon;

public class StopAllButton extends JButton {
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");

	public StopAllButton() {
		//   	stop.setText(throttleBundle.getString("ThrottleToolBarStopAll"));
		setIcon(new NamedIcon("resources/icons/throttles/Stop24.gif","resources/icons/throttles/Stop24.gif"));
		setToolTipText(throttleBundle.getString("ThrottleToolBarStopAllToolTip"));
		setVerticalTextPosition(JButton.BOTTOM);
		setHorizontalTextPosition(JButton.CENTER);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Enumeration<ThrottleFrame> tpi = jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesListPanel().getEnumeration() ;
				while ( tpi.hasMoreElements() )
					tpi.nextElement().getControlPanel().stop();
			}
		});		
	}
}
