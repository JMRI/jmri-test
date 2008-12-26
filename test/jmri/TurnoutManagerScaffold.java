// TurnoutManagerScaffold.java

package jmri;

 /**
 * Dummy implementation of TurnoutManager for testing purposes.
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
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class TurnoutManagerScaffold implements TurnoutManager {

    public Turnout provideTurnout(String name) { return null; }

    public Turnout getTurnout(String name)  { return null; }

    public Turnout getBySystemName(String systemName)  { return null; }

    public Turnout getByUserName(String userName)  { return null; }

    public Turnout newTurnout(String systemName, String userName)  { return null; }

    public java.util.List getSystemNameList()  { return null; }
	
    public String getClosedText()  { return null; }
	
	 public String getThrownText()  { return null; }
	 
	 public String[] getValidOperationTypes()  { return null; }
	
	 public int askNumControlBits(String systemName)  { return -1; }
	
	 public int askControlType(String systemName)  { return -1; }

    public char systemLetter() { return ' '; }

    public char typeLetter() { return ' '; }

    public String makeSystemName(String s)  { return null; }

    public void dispose() {}

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}
    
    public void register(NamedBean n) {}

    public void deregister(NamedBean n) {}

}


/* @(#)TurnoutManagerScaffold.java */
