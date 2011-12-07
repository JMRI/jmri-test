// DefaultSignalMastManager.java

package jmri.managers;

import jmri.*;
import jmri.managers.AbstractManager;
import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of a SignalMastManager.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision$
 */
public class DefaultSignalMastManager extends AbstractManager
    implements SignalMastManager, java.beans.PropertyChangeListener {

    public DefaultSignalMastManager() {
        super();
    }

    public int getXMLOrder(){
        return Manager.SIGNALMASTS;
    }
    
    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'F'; }

    public SignalMast getSignalMast(String name) {
        if (name==null || name.length()==0) { return null; }
        SignalMast t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalMast provideSignalMast(String prefix, // nominally IF$shsm
                                        String signalSystem,
                                        String mastName,
                                        String[] heads) {
        StringBuilder name = new StringBuilder(prefix);
        name.append(":");
        name.append(signalSystem);
        name.append(":");
        for (String s : heads) {
            name.append("(");
            name.append(jmri.util.StringUtil.parenQuote(s));
            name.append(")");
        }
        return provideSignalMast(new String(name));
    }
    
    /*public SignalMast provideSignalMast(String prefix, String signalSystem, String mastName, HashMap<String,HashMap<Turnout, Integer>> map){
        StringBuilder name = new StringBuilder(prefix);
        name.append(":");
        name.append("signalSystem");
        name.append(":");
        name.append(lastAutoMastRef);
        lastAutoMastRef++;
        jmri.implementation.TurnoutSignalMast m = (jmri.implementation.TurnoutSignalMast)provideTurnoutSignalMast(new String(name));
        for (String key : map.keySet()){
            HashMap<Turnout, Integer> temp = map.get(key);
            int state =0;
            Turnout turn = null;
            for (Turnout turnout : temp.keySet()){
                turn = turnout;
                state = temp.get(turn);
            }
            if(turn!=null)
                m.setTurnout(key, turn, state);
        
        }
        return m;
    }*/
    
    public SignalMast provideTurnoutSignalMast(String name){
        SignalMast m = getSignalMast(name);
        if(m==null){
            m = new jmri.implementation.TurnoutSignalMast(name);
            register(m);
        }
        return m;
    }
    

    public SignalMast provideSignalMast(String name) {
        SignalMast m = getSignalMast(name);
        if (m == null) {
            m = new jmri.implementation.SignalHeadSignalMast(name);

            register(m);
        }
        return m;
    }

    public SignalMast getBySystemName(String key) {
        return (SignalMast)_tsys.get(key);
    }

    public SignalMast getByUserName(String key) {
        return (SignalMast)_tuser.get(key);
    }
    
    public List<SignalHead> getSignalHeadsUsed(){
        List<SignalHead> headsUsed = new ArrayList<SignalHead>();
        for(NamedBean val : _tsys.values()){
            if(val instanceof jmri.implementation.SignalHeadSignalMast){
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast)val).getHeadsUsed();
                for(NamedBeanHandle<SignalHead> bean : masthead){
                    headsUsed.add(bean.getBean());
                }
            }
        }
        return headsUsed;
    }
    
    public String isHeadUsed(SignalHead head){
        for(NamedBean val : _tsys.values()){
            if(val instanceof jmri.implementation.SignalHeadSignalMast){
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast)val).getHeadsUsed();
                for(NamedBeanHandle<SignalHead> bean : masthead){
                    if((bean.getBean())==head)
                        return ((jmri.implementation.SignalHeadSignalMast)val).getDisplayName();
                }
            }
        }
        return null;
    
    }
    
    int lastAutoMastRef = 0;


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalMastManager.class.getName());
}

/* @(#)DefaultSignalMastManager.java */
