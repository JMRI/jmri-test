package jmri.jmrit.throttle;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;

import jmri.DccLocoAddress;
import jmri.jmrit.roster.RosterIconFactory;

public class ThrottlesListCellRenderer extends JLabel implements ListCellRenderer {
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");
	private static final int height = 30;
	private static RosterIconFactory rosterIconFactory = new RosterIconFactory(height);
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		ImageIcon icon = null;
		String text = null;
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		if (value==null) return this;
		
		ThrottleFrame tf = (ThrottleFrame) value ;
		
		if (tf.getRosterEntry() != null) {
			icon = rosterIconFactory.getIcon( tf.getAddressPanel().getRosterEntry() );
			text = tf.getAddressPanel().getRosterEntry().getId();
		} else 
			if (tf.getLocoAddress() != null)
			{
				if ( ((DccLocoAddress)tf.getLocoAddress()).getNumber() == 0)
					text = throttleBundle.getString("ThrottleDCControl")+" - "+tf.getLocoAddress();
				else if ( ((DccLocoAddress)tf.getLocoAddress()).getNumber() == 3)
					text = throttleBundle.getString("ThrottleDCCControl")+" - "+tf.getLocoAddress();
				else
					text = throttleBundle.getString("ThrottleAddress")+" "+tf.getLocoAddress();
			}
			else
				text = throttleBundle.getString("ThrottleNotAssigned");

		if (icon != null) 
			icon.setImageObserver(list);
		
		setHorizontalAlignment(JLabel.CENTER);
		setIcon(icon);
		setText(text);
		setPreferredSize(new Dimension(-1,height+4));
		
		return this;
	}
}
