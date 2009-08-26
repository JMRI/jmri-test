// AudioBuffer.java

package jmri.jmrit.audio;

import jmri.Audio;


/**
 * Represent an AudioBuffer, a place to store or control sound information.
 * <P>
 * The AbstractAudio class contains a basic implementation of the state and messaging
 * code, and forms a useful start for a system-specific implementation.
 * Specific implementations in the jmrix package, e.g. for LocoNet and NCE, will
 * convert to and from the layout commands.
 * <P>
 * The states  and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * Each AudioBuffer object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
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
 * @author  Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.1 $
 */
public interface AudioBuffer extends Audio {

    /**
     * Definition of unknown audio format
     */
    public static final int FORMAT_UNKNOWN      = 0x00;
    /**
     * Definition of 8-bit mono audio format
     */
    public static final int FORMAT_8BIT_MONO    = 0x11;
    /**
     * Definition of 16-bit mono audio format
     */
    public static final int FORMAT_16BIT_MONO   = 0x12;
    /**
     * Definition of 8-bit stereo audio format
     */
    public static final int FORMAT_8BIT_STEREO  = 0x21;
    /**
     * Definition of 16-bit stereo audio format
     */
    public static final int FORMAT_16BIT_STEREO = 0x22;

    /**
     * Definition of 8-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_QUAD    = 0x41;
    /**
     * Definition of 16-bit quad multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_QUAD   = 0x42;
    /**
     * Definition of 8-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_5DOT1   = 0x61;
    /**
     * Definition of 16-bit 5.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_5DOT1  = 0x62;
    /**
     * Definition of 8-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_6DOT1   = 0x71;
    /**
     * Definition of 16-bit 6.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_6DOT1  = 0x72;
    /**
     * Definition of 8-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_8BIT_7DOT1   = 0x81;
    /**
     * Definition of 16-bit 7.1 multi-channel audio format.
     * <p>
     * These formats are only supported by certain OpenAL implementations and
     * support is determined at runtime.
     * <p>
     * JavaSound and Null implementations do not support these formats.
     */
    public static final int FORMAT_16BIT_7DOT1  = 0x82;
    
//    /**
//     * Return reference to the DataStorageBuffer integer array
//     * <p>
//     * Applies only to sub-types:
//     * <ul>
//     * <li>Buffer
//     * </u>
//     * @return buffer[] reference to DataStorageBuffer
//     */
//    public int[] getDataStorageBuffer();

    /**
     * Return the url of the sound sample
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @return url
     */
    public String getURL();

    /**
     * Sets the url of the sound sample
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @param pUrl
     */
    public void setURL(String pUrl);

//    /**
//     * Method used to load the actual sound sample data.
//     * This will be sound-system specific.
//     *
//     * Applies only to sub-types:
//     *   - Buffer
//     *
//     * @return true if successful
//     */
//    public boolean loadBuffer();
//
//    /**
//     * Method used to unload the actual sound sample data.
//     * This will be sound-system specific.
//     *
//     * Applies only to sub-types:
//     *   - Buffer
//     *
//     * @return true if successful
//     */
//    public boolean unloadBuffer();

    /**
     * Retrieves the format of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @return constant representing format
     */
    public int getFormat();

    /**
     * Sets the start loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @param startLoopPoint position of start loop point in samples
     */
    public void setStartLoopPoint(long startLoopPoint);

    /**
     * Retrieves the start loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @return position of start loop point in samples
     */
    public long getStartLoopPoint();

    /**
     * Sets the end loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @param endLoopPoint position of end loop point in samples
     */
    public void setEndLoopPoint(long endLoopPoint);

    /**
     * Retrieves the end loop point of the sound sample stored in this buffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </ul>
     * @return position of end loop point in samples
     */
    public long getEndLoopPoint();

}

/* @(#)AudioBuffer.java */