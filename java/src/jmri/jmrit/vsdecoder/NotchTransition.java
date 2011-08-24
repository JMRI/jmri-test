package jmri.jmrit.vsdecoder;

/* NotchTransition
 *
 * This class holds the information needed about a transitional sound
 * between notches of a Diesel locomotive engine.
*/


/*
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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 18233 $
 */



class NotchTransition extends SoundBite {
    private int prev_notch;
    private int next_notch;
    private int length;

    public NotchTransition(String name) {
	super(name);
	prev_notch = 0;
	next_notch = 0;
	length = 0;
    }

    public NotchTransition(VSDFile vf, String filename, String sname, String uname) {
	super(vf, filename, sname, uname);
	prev_notch = 0;
	next_notch = 0;
	this.setLength();
    }

    public int getPrevNotch() { return(prev_notch); }
    public int getNextNotch() { return(next_notch); }

    public void setPrevNotch(int p) { prev_notch = p; } 
    public void setNextNotch(int p) { next_notch = p; }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NotchTransition.class.getName());
 
 }