// AbstractProxyManager.java

package jmri.managers;

import jmri.Manager;
import jmri.NamedBean;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;

import jmri.util.SystemNameComparator;

/**
 * Implementation of a Manager that can serves as a proxy
 * for multiple system-specific implementations.  
 * <P>
 * The first to
 * be added is the "Primary", used if a system letter is not provided.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.12 $
 */
public class AbstractProxyManager implements Manager {

    public void dispose() {
        for (int i=0; i<mgrs.size(); i++)
            mgrs.get(i).dispose();
        mgrs.clear();
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * Forwards the register request to the matching system
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        char systemLetter = systemName.charAt(0);

        for (int i = 0; i<mgrs.size(); i++)
             if ( systemLetter == (mgrs.get(i)).systemLetter()) {
                (mgrs.get(i)).register(s);
                return;
            }
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * Forwards the deregister request to the matching system
     */
    public void deregister(NamedBean s) {
        String systemName = s.getSystemName();
        char systemLetter = systemName.charAt(0);

        for (int i = 0; i<mgrs.size(); i++)
             if ( systemLetter == (mgrs.get(i)).systemLetter()) {
                (mgrs.get(i)).deregister(s);
                return;
            }
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<mgrs.size(); i++)
            (mgrs.get(i)).addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<mgrs.size(); i++)
            (mgrs.get(i)).removePropertyChangeListener(l);
    }

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    public char systemLetter() {
	try {
          return (mgrs.get(0)).systemLetter();
        } catch(IndexOutOfBoundsException ie) {
          return '\0';
        }
    }

    public String getSystemPrefix() { 
	try {
          return (mgrs.get(0)).getSystemPrefix();
        } catch(IndexOutOfBoundsException ie) {
          return "\0";
        }
    }

    /**
     * @return The type letter for turnouts
     */
    public char typeLetter() {
        return (mgrs.get(0)).typeLetter();
    }

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s) {
        return (mgrs.get(0)).makeSystemName(s);
    }

    public String[] getSystemNameArray() {
        TreeSet<String> ts = new TreeSet<String>(new SystemNameComparator());
        for (int i = 0; i<mgrs.size(); i++) {
            ts.addAll( (mgrs.get(i)).getSystemNameList() );
        }
        String[] arr = new String[ts.size()];
        Iterator<String> it = ts.iterator();
        int i=0;
        while(it.hasNext()) {
            arr[i++] = it.next();
        }
        return arr;
    }

    /**
     * Get a list of all system names.
     */
    public List<String> getSystemNameList() {
        TreeSet<String> ts = new TreeSet<String>(new SystemNameComparator());
        for (int i = 0; i<mgrs.size(); i++) {
            ts.addAll(mgrs.get(i).getSystemNameList());
        }
        return new ArrayList<String>(ts);
    }

    List<Manager> mgrs = new ArrayList<Manager>();

    public void addManager(Manager m) {
        mgrs.add(m);
        log.debug("added manager");
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractProxyManager.class.getName());
}

/* @(#)AbstractProxyManager.java */
