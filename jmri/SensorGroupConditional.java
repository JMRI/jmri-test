package jmri;

import jmri.Timebase;
import jmri.jmrit.Sound;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.ResourceBundle;
import java.beans.PropertyChangeEvent;
import javax.swing.Timer;

/**
 * Conditional.java
 *
 * A Conditional type to provide runtime support for Densor Groups.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */


public class SensorGroupConditional extends DefaultConditional
    implements java.io.Serializable {

    public SensorGroupConditional(String systemName, String userName) {
        super(systemName, userName);
    }

	public int calculate (boolean enabled, PropertyChangeEvent evt) {
        int currentState = super.calculate(false, evt);
        if (!enabled || evt == null) {
            return currentState;
        }
        String listener = ((Sensor)evt.getSource()).getSystemName(); 
        log.debug("SGConditional \""+getUserName()+"\" ("+getSystemName()+") has event from \""+listener+"\"");
        if (Sensor.INACTIVE == ((Integer)evt.getNewValue()).intValue()) {
            return currentState;
        }
		for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            Sensor sn = InstanceManager.sensorManagerInstance().getSensor(action.getDeviceName());
            if (sn == null) {
                log.error("invalid sensor name in action - "+action.getDeviceName());
            }
            if (!listener.equals(action.getDeviceName())) // don't change who triggered the action
            {   // find the old one and reset it
                if (sn.getState() != action.getActionData())

                    try {
                        sn.setKnownState(action.getActionData());
                    } 
                    catch (JmriException e) {
                        log.warn("Exception setting sensor "+action.getDeviceName()+" in action");
                    }
            }
		}
        log.debug("SGConditional \""+getUserName()+"\" ("+getSystemName()+"), state= "+currentState+
                  "has set the group actions for "+listener);
        return currentState;
	}


static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorGroupConditional.class.getName());
}
