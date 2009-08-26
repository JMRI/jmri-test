// AudioListener.java

package jmri.jmrit.audio;

import jmri.Audio;
import jmri.Vector3D;


/**
 * Represent an AudioListener, a place to store or control sound information.
 * <P>
 * The AbstractAudio class contains a basic implementation of the state and messaging
 * code, and forms a useful start for a system-specific implementation.
 * Specific implementations in the jmrix package, e.g. for LocoNet and NCE, will
 * convert to and from the layout commands.
 * <P>
 * The states  and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * Each AudioListener object has a two names.  The "user" name is entirely free form, and
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
public interface AudioListener extends Audio {

    /**
     * Sets the position of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @param pos 3d position vector
     */
    public void setPosition(Vector3D pos);

    /**
     * Sets the position of this AudioListener object in x, y and z planes
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void setPosition(float x, float y, float z);

    /**
     * Sets the position of this AudioListener object in x and z planes with
     * y plane position fixed at zero
     * <p>
     * Equivalent to setPosition(x, 0.0f, z)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @param x x-coordinate
     * @param z z-coordinate
     */
    public void setPosition(float x, float z);

    /**
     * Returns the position of this AudioListener object as a
     * 3-dimensional vector.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @return 3d position vector
     */
    public Vector3D getPosition();

    /**
     * Sets the velocity of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @param vel 3d velocity vector
     */
    public void setVelocity(Vector3D vel);

    /**
     * Returns the velocity of this AudioListener object
     *
     * Applies only to sub-types:
     *   - Listener
     *   - Source
     *
     * @return 3d velocity vector
     */
    public Vector3D getVelocity();

    /**
     * Set the orientation of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     * @param at 3d vector representing the position
     * @param up 3d vector representing the look-at point
     */
    public void setOrientation(Vector3D at, Vector3D up);

    /**
     * Return the orientation of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     * @param which the orientation vector to return:
     *              == AT - position;
     *              == UP - look-at point
     * @return vector representing the chosen orientation vector
     */
    public Vector3D getOrientation(int which);

    /**
     * Return the current gain setting
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @return float gain
     */
    public float getGain();

    /**
     * Set the gain of this AudioListener object
     * <p>
     * Applicable values 0.0f to 1.0f
     * <p>
     * When applied to Listeners, has the effect of altering the master gain
     * (or volume)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     * @param gain
     */
    public void setGain(float gain);

    /**
     * Method to set the Meters per unit ratio for all distance calculations.
     * <p>
     * Default value = 1.0f (i.e. 1 unit == 1 metre)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     * @param metersPerUnit Meters per unit ratio
     */
    public void setMetersPerUnit(float metersPerUnit);

    /**
     * Retrieve the current Meters per unit ratio to use for all distance
     * calculations.
     * <p>
     * Default value = 1.0f (i.e. 1 unit == 1 metre)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     * @return Meters per unit ratio
     */
    public float getMetersPerUnit();

}

/* @(#)AudioListener.java */