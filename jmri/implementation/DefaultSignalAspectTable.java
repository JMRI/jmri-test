// DefaultSignalAspectTable.java

package jmri.implementation;

import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.Enumeration;

import jmri.SignalAspectTable;

 /**
 * Default implementation of a basic signal aspect table.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.3 $
 */
public class DefaultSignalAspectTable extends AbstractNamedBean implements SignalAspectTable  {

    public DefaultSignalAspectTable(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAspectTable(String systemName) {
        super(systemName);
    }

    public void setProperty(String aspect, String key, Object value) {
        getTable(aspect).put(key, value);
        if (! keys.contains(key)) keys.add(key);
    }
    
    public Object getProperty(String aspect, String key) {
        return getTable(aspect).get(key);
    }

    protected Hashtable<String, Object> getTable(String aspect) {
        Hashtable<String, Object> t = aspects.get(aspect);
        if ( t == null) {
            t = new Hashtable<String, Object>();
            aspects.put(aspect, t);
        }
        return t;
    }
    
    public Enumeration<String> getAspects() {
        return aspects.keys();
    }

    public Enumeration<String> getKeys() {
        return keys.elements();
    }

    public boolean checkAspect(String aspect) {
        return aspects.get(aspect) != null;
    }

    public void loadDefaults() {
        
        ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

        log.debug("start loadDefaults");
        
        String aspect;
        String key = rb.getString("SignalAspectKey");
        String value;
        
        aspect = rb.getString("SignalAspectDefaultRed");
        value = rb.getString("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

        aspect = rb.getString("SignalAspectDefaultYellow");
        value = rb.getString("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

        aspect = rb.getString("SignalAspectDefaultGreen");
        value = rb.getString("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

    }
    

    public int getState() {
        throw new NoSuchMethodError();
    }
    
    public void setState(int s) {
        throw new NoSuchMethodError();
    }

    protected java.util.Hashtable<String, Hashtable<String, Object>> aspects
            = new jmri.util.OrderedHashtable<String, Hashtable<String, Object>>();

    protected java.util.Vector<String> keys = new java.util.Vector<String>();
    
    public String toString() {
        String retval = "SignalAspectTable "+getSystemName()+"\n";
        Enumeration<String> e1 = getAspects();
        while (e1.hasMoreElements()) {
            String s1 = e1.nextElement();
            retval += "  "+s1+"\n";
            Enumeration<String> e2 = getKeys();
            while (e2.hasMoreElements()) {
                String s2 = e2.nextElement();
                retval += "    "+s2+": "+getProperty(s1, s2)+"\n";
            }
        }
        return retval;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalAspectTable.class.getName());
}

/* @(#)DefaultSignalAspectTable.java */
