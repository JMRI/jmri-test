// AbstractManager.java

package jmri;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;


/**
 * Abstract partial implementation for all Manager-type classes
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.2 $
 */
abstract public class AbstractManager
    implements Manager, java.beans.PropertyChangeListener {

    public AbstractManager() {
        // register the result for later configuration
        InstanceManager.configureManagerInstance().register(this);
        log.debug("register");
    }

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        InstanceManager.configureManagerInstance().deregister(this);
        _tsys.clear();
        _tuser.clear();
    }

    protected Hashtable _tsys = new Hashtable();   // stores known Turnout instances by system name
    protected Hashtable _tuser = new Hashtable();   // stores known Turnout instances by user name

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
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        _tsys.put(systemName, s);
        String userName = s.getUserName();
        if (userName != null) _tuser.put(userName, s);
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of SignalHead user name changes.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered SignalHead objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();
            String now = (String) e.getNewValue();
            Object t = getInstanceByUserName(old);
            _tuser.remove(old);
            _tuser.put(now, t);
        }
    }

    public List getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List out = new ArrayList();
        Enumeration en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractManager.class.getName());

}

/* @(#)AbstractSignalHeadManager.java */
