package jmri.jmrit.throttle;

import java.awt.Color;

import jmri.DccThrottle;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.ResizableImagePanel;

public class BackgroundPanel extends ResizableImagePanel implements AddressListener {

	public BackgroundPanel() {
		super();
		setBackground(Color.GRAY);
		setRespectAspectRatio(true);
        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isResizingWindow() )
        	setResizingContainer(true);
	}
	
    AddressPanel addressPanel = null;
    public void setAddressPanel(AddressPanel addressPanel){
    	this.addressPanel = addressPanel; 
    }

	public void notifyAddressThrottleFound(DccThrottle t) {
		RosterEntry rosterEntry = null;
		if (addressPanel != null)
			rosterEntry = addressPanel.getRosterEntry();
		if ( rosterEntry != null ) {
			setImagePath(rosterEntry.getImagePath());
		}
		else {
			if ( t.getLocoAddress().toString().compareTo("3(S)") == 0 )  // default DCC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.png"));
			if ( t.getLocoAddress().toString().compareTo("0(S)") == 0 )  // default DC address
				setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.png"));
		}
	}

	public void notifyAddressReleased(int address, boolean isLong)  {
		setImagePath(null);
		setVisible(false);
	}
	
	public void notifyAddressChosen(int newAddress, boolean isLong) {		
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BackgroundPanel.class.getName());
}
