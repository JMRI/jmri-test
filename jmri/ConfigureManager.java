// ConfigureManager.java

package jmri;

import java.io.File;


/**
 * Provide load/store capabilities for general configuration.
 * <P>
 * Areas of responsibility:
 * <UL>
 * <LI>Register and deregister configuration objects so they can
 * eventually be stored.
 * <LI>Invoke the load and store operations as needed
 * <LI>Give access to the configuration objects for independent GUIs
 * </UL>
 *<P>
 *The managed items are divided into four types:
 *<OL>
 *<LI>"Prefs" - handled first on read, these are the general preferences
 *controlling how the program starts up
 *<LI>"Config" - layout configuration information, e.g. turnout, signal, etc
 *<LI>"Tool" - (Not really clear yet, but present)
 *<LI>"User" - typically information about panels and windows, these are handled
 *last during startup
 *</OL>
 *<P>
 *The configuration manager is generally located through the InstanceManager.
 *<P>
 *The original implementation was via the {@link jmri.configurexml} package.
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
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version     $Revision: 1.15 $
 * @see jmri.InstanceManager
 * @see jmri.configurexml.ConfigXmlManager
 */
public interface ConfigureManager {

    public void registerPref(Object o);
    public void removePrefItems();

    public void registerConfig(Object o);
    public void registerTool(Object o);
    public void registerUser(Object o);

    public void deregister(Object o);

    /**
     * Find the ith instance of an object of particular class
     * that's been registered for storage.
     *<p>
     * Note that the index of an object can change when
     * other objects are stored or removed.  The index is
     * for indexing over the objects stored at a moment,
     * not for use as an identification number.
     *<P>
     * There may be synchronization issues associated with
     * this, although they are expected to be rare in practice.
     * @param c Class of the desired objects
     * @param index a 1-based index of the object to return
     * @return an object of class c or null
     */
    public Object findInstance(Class<?> c, int index);

    /**
     * Stores prefs, config, tools and user information.
     * @param file Output file
     */
    public void storeAll(File file);

    /**
     * Stores just preferences information.
     * <p>
     * Where that information is stored is implementation-specific.
     */
    public void storePrefs();
    
    /**
     * Stores just preferences information.
     */
    public void storePrefs(File file);

    /**
     * Stores just configuration information.
     * @param file Output file
     */
    public void storeConfig(File file);

    /**
     * Stores just user information.
     * @param file Output file
     */
    public void storeUser(File file);

    /**
     * Create the objects defined in a particular configuration
     * file
     * @param file Input file
     * @return true if succeeded
     */
    public boolean load(File file) throws JmriException;

    /**
     * Provide a method-specific way of locating a file to be
     * loaded from a name.
     * @param filename Local filename, perhaps without path information
     * @return Corresponding File object
     */
    public File find(String filename);
    
    /**
     * Make a backup file.
     * @param file to be backed up
     * @return true if successful
     */
    public boolean makeBackup(File file);

}


/* @(#)ConfigureManager.java */
