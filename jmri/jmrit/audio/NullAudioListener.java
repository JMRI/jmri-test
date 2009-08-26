// NullAudioListener.java

package jmri.jmrit.audio;

/**
 * Null implementation of the Audio Listener sub-class.
 * <P>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 *
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
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.1 $
 */
public class NullAudioListener extends AbstractAudioListener {

    /**
     * Constructor for new NullAudioListener with system name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     */
    public NullAudioListener(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) log.debug("New NullAudioListener: "+systemName);
    }

    /**
     * Constructor for new NullAudioListener with system name and user name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     * @param userName AudioListener object user name
     */
    public NullAudioListener(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) log.debug("New NullAudioListener: "+userName+" ("+systemName+")");
    }

    protected void cleanUp() {
        if (log.isDebugEnabled()) log.debug("Cleanup NullAudioBuffer (" + this.getSystemName() + ")");
        this.dispose();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NullAudioListener.class.getName());

}

/* $(#)NullAudioListener.java */