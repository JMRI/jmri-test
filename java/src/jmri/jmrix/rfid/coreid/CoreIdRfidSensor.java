// MergRfidReporter.java

package jmri.jmrix.rfid.coreid;

import jmri.IdTag;
import jmri.jmrix.rfid.RfidSensor;

/**
 * CORE-ID specific implementation of an RfidSensor.
 * <p>
 * The CORE-ID RFID readers only send a message when an RFID tag is within
 * the proximity of the reader - no message is sent when it leaves.
 * <p>
 * As a result, this implementation simulates this message using a timeout
 * mechanism - if no further tags are sensed within a pre-defined time period,
 * the Sensor state reverts to {@link IdTag#UNSEEN}.
 * <hr>
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
 *
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class CoreIdRfidSensor extends RfidSensor {
    
    /**
     * Timeout in ms
     */
    private static final int timeout = 1000;

    /**
     * Time when something was last sensed by this object
     */
    private long whenLastSensed = 0;

    /**
     * Reference to the timeout thread for this object
     */
    private transient TimeoutThread timeoutThread = null;

    private boolean logDebug = log.isDebugEnabled();

    public CoreIdRfidSensor(String systemName) {
        super(systemName);
    }

    public CoreIdRfidSensor(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public void notify(IdTag t) {
        super.notify(t);
        whenLastSensed = System.currentTimeMillis();
        if (timeoutThread==null)
            (timeoutThread = new TimeoutThread()).start();
    }

    private void cleanUpTimeout() {
        if (logDebug) log.debug("Cleanup timeout thread for "+mSystemName);
        timeoutThread = null;
    }

    private class TimeoutThread extends Thread {

        TimeoutThread() {
            super();
            this.setName("Timeout-"+mSystemName);
        }

        @Override
        public void run() {
            while((whenLastSensed+timeout)>System.currentTimeMillis()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) { }
            }
            CoreIdRfidSensor.super.notify(null);
            if (logDebug) log.debug("Timeout-"+mSystemName);
            cleanUpTimeout();
        }

    }

    static final long serialVersionUID = 5290531989069550265L;

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CoreIdRfidSensor.class.getName());

}

/* @(#)CoreIdRfidSensor.java */