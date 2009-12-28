// AbstractManager.java

package jmri.managers;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import jmri.*;


/**
 * Abstract partial implementation for all Manager-type classes.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
abstract public class AbstractManager
    implements Manager, java.beans.PropertyChangeListener {

    public AbstractManager() {
        // register the result for later configuration
        registerSelf();
    }

    /**
     * By default, register this manager to store as configuration
     * information.  Override to change that.
     **/
    protected void registerSelf() {
         if (InstanceManager.configureManagerInstance()!=null) {
            InstanceManager.configureManagerInstance().registerConfig(this);
            log.debug("register for config");
        }
    }
    
    public String makeSystemName(String s) {
        return ""+systemLetter()+typeLetter()+s;
    }

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        if (InstanceManager.configureManagerInstance()!= null)
            InstanceManager.configureManagerInstance().deregister(this);
        _tsys.clear();
        _tuser.clear();
    }

    protected Hashtable<String, NamedBean> _tsys = new Hashtable<String, NamedBean>();   // stores known Turnout instances by system name
    protected Hashtable<String, NamedBean> _tuser = new Hashtable<String, NamedBean>();   // stores known Turnout instances by user name

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.  This is intended to be used by
     * concrete classes to implement their getBySystemName method.
     * We can't call it that here because Java doesn't have polymorphic
     * return types.
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists. This is intended to be used by
     * concrete classes to implement their getBySystemName method.
     * We cant call it that here because Java doesn't have polymorphic
     * return types.
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceByUserName(String userName) {
        return _tuser.get(userName);
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers
     * use this method extensively.
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        _tsys.put(systemName, s);
        String userName = s.getUserName();
        if (userName != null) _tuser.put(userName, s);
        firePropertyChange("length", null, new Integer(_tsys.size()));
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(NamedBean s) {
        s.removePropertyChangeListener(this);
        String systemName = s.getSystemName();
        _tsys.remove(systemName);
        String userName = s.getUserName();
        if (userName != null) _tuser.remove(userName);
        firePropertyChange("length", null, new Integer(_tsys.size()));
        // listen for name and state changes to forward
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();  // OldValue is actually system name
            String now = (String) e.getNewValue();
            NamedBean t = (NamedBean)e.getSource();
            if (old!= null) _tuser.remove(old);
            if (now!= null) _tuser.put(now, t);
        }
    }

    public String[] getSystemNameArray() {
        String[] arr = new String[_tsys.size()];
        Enumeration<String> en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    public List<String> getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractManager.class.getName());

}

/* @(#)AbstractManager.java */
