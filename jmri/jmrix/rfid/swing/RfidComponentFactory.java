// RfidComponentFactory.java

package jmri.jmrix.rfid.swing;

import javax.swing.JMenu;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;

/**
 * Provide access to Swing components for the Rfid subsystem.
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
 * @version     $Revision: 1.2 $
 */
public class RfidComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    RfidSystemConnectionMemo memo;

    JMenu currentMenu;

    public RfidComponentFactory(RfidSystemConnectionMemo memo) {
        this.memo = memo;
    }

    public JMenu getMenu() {
        if (memo.getDisabled()) return null;
        if (currentMenu==null)
            currentMenu = new RfidMenu(memo);
        return currentMenu;
    }

}

/* @(#)RfidComponentFactory.java */