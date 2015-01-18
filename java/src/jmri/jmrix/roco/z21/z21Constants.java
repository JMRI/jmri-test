/**
 * z21Constants.java
 *
 * Description:		Constants to represent values seen in z21 traffic
 * @author		Paul Bender  Copyright (C) 2003-2009
 * @version             $ Revision: 1.9 $
 *
 * Variable prefix abreviation keys (Some of these are from the z21
                                     protocol documentation):
 * LAN_X_ is for constants specific to the XPressNet tunnel
 * LAN_LOCONET_ is for constants specific to the LocoNet tunnel
 *
 * A few variables don't have a prefix.  The name should be self 
 * explanitory, but a prefix may be added later.
 */

package jmri.jmrix.roco.z21;

public final class z21Constants {

/* XPressNet Tunnel Constants */

/* XPressNet Programming Constants */
public final static int LAN_X_CV_READ_XHEADER = 0x23;
public final static int LAN_X_CV_READ_DB0 = 0x11;
public final static int LAN_X_CV_WRITE_XHEADER = 0x24;
public final static int LAN_X_CV_WRITE_DB0 = 0x12;
public final static int LAN_X_CV_RESULT_XHEADER = 0x64;
public final static int LAN_X_CV_RESULT_DB0 = 0x14;

/* LocoNet Tunnel Constants */

}


/* @(#)XNetConstants.java */
