// AbstractSignalHeadManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;


/**
 * Abstract partial implementation of a SignalHeadManager.
 * <P>
 * Not truly an abstract class, this might have been better named
 * DefaultSignalHeadManager.  But we've got it here for the eventual
 * need to provide system-specific implementations.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision$
 */
public class AbstractSignalHeadManager extends AbstractManager
    implements SignalHeadManager, java.beans.PropertyChangeListener {

    public AbstractSignalHeadManager() {
        super();
    }
    
    public int getXMLOrder(){
        return Manager.SIGNALHEADS;
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'H'; }

    public SignalHead getSignalHead(String name) {
        if (name==null || name.length()==0) { return null; }
        SignalHead t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalHead getBySystemName(String name) {
        return (SignalHead)_tsys.get(name);
    }

    public SignalHead getByUserName(String key) {
        return (SignalHead)_tuser.get(key);
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSignalHeadManager.class.getName());
}

/* @(#)AbstractSignalHeadManager.java */
