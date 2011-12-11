package jmri.jmrit.throttle;

import jmri.jmrit.catalog.NamedIcon;

public class LargePowerManagerButton extends PowerManagerButton {
	void loadIcons() {
    	powerOnIcon = new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png");
		powerOffIcon = new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png");
		powerXIcon = new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png");
	}

}
