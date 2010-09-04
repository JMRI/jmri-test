// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under
// the terms of version 2 of the GNU General Public License as published
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
//
// JMRI is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

package jmri.implementation;

import jmri.*;

/**
 * This class implements a SignalHead the maps the various appearances values to
 * aspect values in the <B>Extended Accessory Decoder Control Packet Format</B> and
 * outputs that packet to the DCC System via the generic CommandStation interface
 * <P>
 * The mapping is as follows:
 * <P>
 *    0 = DARK        <BR>
 *    1 = RED         <BR>
 *    2 = YELLOW      <BR>
 *    3 = GREEN       <BR>
 *    4 = FLASHRED    <BR>
 *    5 = FLASHYELLOW <BR>
 *    6 = FLASHGREEN  <BR>
 * <P>
 * The FLASH appearances are expected to be implemented in the decoder.
 *
 * @author Alex Shepherd Copyright (c) 2008
 * @version $Revision: 1.2 $
 */
public class DccSignalHead extends AbstractSignalHead {

  public DccSignalHead( String sys, String user ) {
    super(sys, user);

    if (( sys.length() > 2 ) && (( sys.charAt(1) == 'H' ) || ( sys.charAt(1) == 'h' )))
      DccSignalDecoderAddress = Integer.parseInt(sys.substring(2,sys.length()));
    else
      DccSignalDecoderAddress = Integer.parseInt(sys);
  }

  public DccSignalHead( String sys ) {
    super(sys);

    if (( sys.length() > 2 ) && (( sys.charAt(1) == 'H' ) || ( sys.charAt(1) == 'h' )))
      DccSignalDecoderAddress = Integer.parseInt(sys.substring(2,sys.length()));
    else
      DccSignalDecoderAddress = Integer.parseInt(sys);
  }

  public void setAppearance(int newAppearance) {
    int oldAppearance = mAppearance;
    mAppearance = newAppearance;

    if (oldAppearance != newAppearance) {
      updateOutput();

      // notify listeners, if any
      firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
    }
  }

  public void setLit(boolean newLit) {
    boolean oldLit = mLit;
    mLit = newLit;
    if (oldLit != newLit) {
      updateOutput();
      // notify listeners, if any
      firePropertyChange("Lit", new Boolean(oldLit), new Boolean(newLit));
    }
  }

  /**
   * Set the held parameter.
   * <P>
   * Note that this does not directly effect the output on the layout;
   * the held parameter is a local variable which effects the aspect
   * only via higher-level logic
   */

  public void setHeld(boolean newHeld) {
    boolean oldHeld = mHeld;
    mHeld = newHeld;
    if (oldHeld != newHeld) {
      // notify listeners, if any
      firePropertyChange("Held", new Boolean(oldHeld), new Boolean(newHeld));
    }
  }

  protected void updateOutput() {
    CommandStation c = InstanceManager.commandStationInstance();
    if (c != null) {
      int aspect = 0 ;  // SignalHead.DARK

      if( getLit() ) {
        switch( mAppearance ){
          case SignalHead.DARK:        aspect = 0 ; break;
          case SignalHead.RED:         aspect = 1 ; break;
          case SignalHead.YELLOW:      aspect = 2 ; break;
          case SignalHead.GREEN:       aspect = 3 ; break;
          case SignalHead.FLASHRED:    aspect = 4 ; break;
          case SignalHead.FLASHYELLOW: aspect = 5 ; break;
          case SignalHead.FLASHGREEN:  aspect = 6 ; break;
        }
      }

      c.sendPacket( NmraPacket.accSignalDecoderPkt( DccSignalDecoderAddress, aspect ), 3);
    }
  }

  int DccSignalDecoderAddress ;
}
