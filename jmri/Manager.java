// Manager.java

package jmri;

import java.util.List;

/**
 * Basic interface for access to named, managed objects.
 * <P>
 * {@link NamedBean} objects represent various real elements, and
 * have a "system name" and perhaps "user name".  A specific Manager
 * object provides access to them by name, and serves as a factory for
 * new objects.
 * <P>
 * Right now, this interface just contains the members needed
 * by {@link InstanceManager} to handle
 * managers for more than one system.
 * <P>
 * Although they are not defined here because their return type differs, any
 * specific Manager subclass
 * provides "get" methods to locate specific objects, and a "new" method
 * to create a new one via the Factory pattern.
 * The "get" methods will
 * return an existing object or null, and will never create a new object.
 * The "new" method will log a warning if an object already exists with
 * that system name.
 * <P>
 * add/remove PropertyChangeListener methods are provided. At a minimum,
 * subclasses must notify of changes to the list of available NamedBeans;
 * they may have other properties that will also notify.
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
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.15 $
 */
public interface Manager {

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter();

    /**
     * @return The type letter for a specific implementation
     */
    public char typeLetter();

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s);

    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose();

    public String[] getSystemNameArray();
    public List<String> getSystemNameList();

	/**
	 * At a minimum,
 	 * subclasses must notify of changes to the list of available NamedBeans;
     * they may have other properties that will also notify.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
	/**
	 * At a minimum,
 	 * subclasses must notify of changes to the list of available NamedBeans;
     * they may have other properties that will also notify.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers
     * use this method extensively.
     */
    public void register(NamedBean n);

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(NamedBean n);
}


/* @(#)Manager.java */
