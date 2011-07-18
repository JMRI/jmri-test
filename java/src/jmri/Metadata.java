package jmri;

import java.util.Arrays;
import java.util.List;
/**
 * Meta data concerning the JMRI application.
 *<P>
 * Meta data is static information concerning the JMRI application. This
 * class provides a single container for listing and storing JMRI meta data.
 * This class is used by the DefaultXmlIOServer object.
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
 * @author			Randall Wood Copyright (C) 2011
 * @version			$Revision$
 */
public class Metadata {
    
    /**
     * Return the value of the named meta data, or any valid system property.
     * 
     * @param name
     * @return String value of requested data or null
     */
    public static String getBySystemName(String name) {
        if (name.equalsIgnoreCase("JMRIVERSION")) {
            return jmri.Version.name();
        } else if (name.equalsIgnoreCase("JVMVERSION")) {
            return System.getProperty("java.version", "<unknown>");
        } else if (name.equalsIgnoreCase("JVMVENDOR")) {
            return System.getProperty("java.vendor", "<unknown>");
        }
        // returns null if name is not a system property
        return System.getProperty(name);
    }

    /**
     * An array of known meta data names.
     * 
     * @return String[]
     */
    public static String[] getSystemNameArray() {
        String[] names = {"JMRIVERSION","JVMVERSION","JVMVENDOR"};
        return names;
    }

    /**
     * A list of known meta data names.
     * 
     * @return List<String>
     */
    public static List<String> getSystemNameList() {
        return Arrays.asList(Metadata.getSystemNameArray());
    }

}