// VirtualSignalMast.javaa
package jmri.implementation;
import java.util.*;

import jmri.*;

 /**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
<pre>
IF:basic:one-searchlight:(IH1)(IH2)
</pre>
 * The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(IH1)(IH2) - colon-separated list of names for SignalHeads
 * </ul>
 * There was an older form where the names where colon separated:  IF:basic:one-searchlight:IH1:IH2
 * This was deprecated because colons appear in e.g. SE8c system names.
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>IH1:IH2 - colon-separated list of names for SignalHeads
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 19027 $
 */
public class VirtualSignalMast extends AbstractSignalMast {

    public VirtualSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public VirtualSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }
        
    void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) { 
            log.error("SignalMast system name needs at least three parts: "+systemName);
            throw new IllegalArgumentException("System name needs at least three parts: "+systemName);
        }
        if (!parts[0].equals("IF$vsm")) {
            log.warn("SignalMast system name should start with IF: "+systemName);
        }

        String system = parts[1];
        String mast = parts[2];
        // new style
        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("($")+2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            if (autoNumber > lastRef) {
                lastRef = autoNumber;
            } 
        } catch (NumberFormatException e){
            log.warn("Auto generated SystemName "+ systemName + " is not in the correct format");
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }
    
    void configureSignalSystemDefinition(String name) {
        systemDefn = InstanceManager.signalSystemManagerInstance().getSystem(name);
        if (systemDefn == null) {
            log.error("Did not find signal definition: "+name);
            throw new IllegalArgumentException("Signal definition not found: "+name);
        }
    }
    
    void configureAspectTable(String signalSystemName, String aspectMapName) {
        map = DefaultSignalAppearanceMap.getMap(signalSystemName, aspectMapName);
    }
    
    /**
    * returns a list of all the valid aspects, that have not been disabled
    */
    public Vector<String> getValidAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            if(!disabledAspects.contains(aspect))
                v.add(aspect);
        }
        return v;
    }
    
    /**
    * returns a list of all the known aspects for this mast, including those that have been disabled
    */
    public Vector<String> getAllKnownAspects(){
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    @Override
    public void setAspect(String aspect) { 
        // check it's a choice
        if ( !map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: "+aspect+" on mast: "+getDisplayName());
        }  else if (disabledAspects.contains(aspect)){
            log.warn("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: "+aspect+" on mast: "+getDisplayName());
        }
        super.setAspect(aspect);
    }
    
    ArrayList<String> disabledAspects = new ArrayList<String>(1);

    public void setAspectDisabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(!disabledAspects.contains(aspect))
            disabledAspects.add(aspect);
    }
    
    public void setAspectEnabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(disabledAspects.contains(aspect))
            disabledAspects.remove(aspect);
    }
    
    public List<String> getDisabledAspects(){
        return disabledAspects;
    }

    public SignalSystem getSignalSystem() {
        return systemDefn;
    }
    
    public SignalAppearanceMap getAppearanceMap() {
        return map;
    }

    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;
    
    public static int getLastRef(){ return lastRef; }
    
    static int lastRef = 0;
    
    static final protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VirtualSignalMast.class.getName());
}

/* @(#)VirtualSignalMast.java */
