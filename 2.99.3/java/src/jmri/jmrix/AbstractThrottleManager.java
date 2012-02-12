package jmri.jmrix;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Vector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract implementation of a ThrottleManager.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision$
 */
abstract public class AbstractThrottleManager implements ThrottleManager {
	
    
    public AbstractThrottleManager(){}
    
    public AbstractThrottleManager(SystemConnectionMemo memo){
        adapterMemo = memo;
    }
    
    protected SystemConnectionMemo adapterMemo;
    
    protected String userName = "Internal";
    
    public String getUserName(){ 
        if(adapterMemo!=null)   
            return adapterMemo.getUserName();
        return userName;
    }
	/**
	 * throttleListeners is indexed by the address, and
	 * contains as elements an ArrayList of ThrottleListener
	 * objects.  This allows more than one to request a throttle
	 * at a time, the entries in this Hashmap are only valid during the 
     * throttle setup process.
	 */
    private HashMap<DccLocoAddress,ArrayList<ThrottleListener>> throttleListeners = new HashMap<DccLocoAddress,ArrayList<ThrottleListener>>(5);
    
    /**
	 * listenerOnly is indexed by the address, and
	 * contains as elements an ArrayList of propertyChangeListeners
	 * objects that have requested notification of changes to a throttle that 
     * hasn't yet been created/
     * The entries in this Hashmap are only valid during the throttle setup process.
	 */
    private HashMap<DccLocoAddress,ArrayList<PropertyChangeListener>> listenerOnly = new HashMap<DccLocoAddress,ArrayList<PropertyChangeListener>>(5);
    
    //This keeps a map of all the current active DCC loco Addresses that are in use.
    /**
	 * addressThrottles is indexed by the address, and
	 * contains as elements a subclass of the throttle assigned to an address and 
     * the number of requests and active users for this address.
	 */
    private Hashtable<DccLocoAddress,Addresses> addressThrottles = new Hashtable<DccLocoAddress,Addresses>();

	/**
	 * Does this DCC system allow a Throttle (e.g. an address) to be used
	 * by only one user at a time?
	 */
	protected boolean singleUse() { return true; }

    public boolean requestThrottle(int address, boolean isLongAddress, ThrottleListener l) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return requestThrottle(la, l);

    }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param la DccLocoAddress of the decoder desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(DccLocoAddress la, ThrottleListener l) {
        boolean throttleFree = true;

		// put the list in if not present
        if (!throttleListeners.containsKey(la))
            throttleListeners.put(la, new ArrayList<ThrottleListener>());
		// get the corresponding list to check length
        ArrayList<ThrottleListener> a = throttleListeners.get(la);

        if (addressThrottles.containsKey(la)){
            log.debug("A throttle to address " + la.getNumber() + " already exists, so will return that throttle");
            a.add(l);
            notifyThrottleKnown(addressThrottles.get(la).getThrottle(), la);
            return throttleFree;
        } else {
            log.debug(la.getNumber() + " has not been created before");
        }
        
        if (log.isDebugEnabled()) log.debug("After request in ATM: "+a.size());
		// check length
        if (singleUse() && (a.size()>0)) {
            throttleFree= false;
            if (log.isDebugEnabled()) log.debug("case 1");           
        } else if (a.size() == 0) {
            a.add(l);
            if (log.isDebugEnabled()) log.debug("case 2: "+la+";"+a);
            requestThrottleSetup(la, true);
        } else {
        	a.add(l);
            if (log.isDebugEnabled()) log.debug("case 3");
        }
        return throttleFree;
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) isLong = false;
        return requestThrottle(address, isLong, l);
    }

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(LocoAddress a, boolean control);

     /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     */
    public void requestThrottleSetup(LocoAddress a) {
        requestThrottleSetup(a, true);
    }

    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        if (throttleListeners != null) {
            DccLocoAddress la = new DccLocoAddress(address, isLong);
            ArrayList<ThrottleListener> a = throttleListeners.get(la);
            if (a==null) return;
            for (int i = 0; i<a.size(); i++) {
                    if (l == a.get(i))
                            a.remove(i);
            }
        }
        /*if (addressThrottles.contains(la)){
            addressThrottles.get(la).decrementUse();
        }*/
    }

    /**
     * Cancel a request for a throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) isLong = false;
        cancelThrottleRequest(address, isLong, l);
    }

    /**
     * If the system-specific ThrottleManager has been unable to create the DCC
     * throttle then it needs to be removed from the throttleListeners, otherwise
     * any subsequent request for that address results in the address being reported
     * as already in use, if singleUse is set.
     * This also sends a notification message back to the requestor with a string
     * reason as to why the request has failed.
     * @param address The DCC Loco Address that the request failed on.
     * @param reason A text string passed by the ThrottleManae as to why
     */
    public void failedThrottleRequest(DccLocoAddress address, String reason) {
        ArrayList<ThrottleListener> a = throttleListeners.get(address);
        if (a==null) {
            log.warn("notifyThrottleKnown with zero-length listeners: "+address);
        } else {
            for (int i = 0; i<a.size(); i++) {
                ThrottleListener l = a.get(i);
                l.notifyFailedThrottleRequest(address, reason);
            }
        }
        throttleListeners.remove(address);
        ArrayList<PropertyChangeListener> p = listenerOnly.get(address);
        if (p==null) {
            log.debug("notifyThrottleKnown with zero-length PropertyChange listeners: "+address);
        } else {
            for (int i = 0; i<p.size(); i++) {
                PropertyChangeListener l = p.get(i);
                l.propertyChange(new PropertyChangeEvent(this, "attachFailed", address, null));
            }
        }
        listenerOnly.remove(address);
    }

    /**
     * Handle throttle information when it's finally available, e.g. when
     * a new Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, LocoAddress addr) {
        log.debug("notifyThrottleKnown for "+addr);
        DccLocoAddress dla = (DccLocoAddress) addr;
        if (!addressThrottles.containsKey(dla)){
            log.debug("Address " + addr + "doesn't already exists so will add");
            addressThrottles.put(dla, new Addresses(throttle));
        } else {
            addressThrottles.get(dla).setThrottle(throttle);
        }
        ArrayList<ThrottleListener> a = throttleListeners.get(dla);
		if (a==null) {
		    log.debug("notifyThrottleKnown with zero-length listeners: "+addr);
		} else {
            for (int i = 0; i<a.size(); i++) {
                ThrottleListener l = a.get(i);
                log.debug("Notify listener");
                l.notifyThrottleFound(throttle);
                addressThrottles.get(dla).incrementUse();
                addressThrottles.get(dla).addListener(l);
            }
            throttleListeners.remove(dla);
        }
        ArrayList<PropertyChangeListener> p = listenerOnly.get(dla);
        if (p==null) {
		    log.debug("notifyThrottleKnown with zero-length propertyChangeListeners: "+addr);
		} else {
            for (int i = 0; i<p.size(); i++) {
                PropertyChangeListener l = p.get(i);
                log.debug("Notify propertyChangeListener");
                l.propertyChange(new PropertyChangeEvent(this, "throttleAssigned", null, dla));
                throttle.addPropertyChangeListener(l);
            }
            listenerOnly.remove(dla);
        }
    }
  
    /**
     * Check to see if the Dispatch Button should be enabled or not 
     * Default to true, override if necessary
     **/
    public boolean hasDispatchFunction() { return true; }

    /**
     * What speed modes are supported by this system?                       
     * value should be xor of possible modes specifed by the 
     * DccThrottle interface
     */
    public int supportedSpeedModes() {
        return(DccThrottle.SpeedStepMode128);
    }
    
    public void attachListener(DccLocoAddress la, java.beans.PropertyChangeListener p){
        if (addressThrottles.containsKey(la)){
            addressThrottles.get(la).getThrottle().addPropertyChangeListener(p);
            p.propertyChange(new PropertyChangeEvent(this, "throttleAssigned", null, la));
            return;
        } else {    
            if (!listenerOnly.containsKey(la))
                listenerOnly.put(la, new ArrayList<PropertyChangeListener>());

		    // get the corresponding list to check length
            ArrayList<PropertyChangeListener> a = listenerOnly.get(la);
            a.add(p);
            //Only request that the throttle is set up if it hasn't already been
            //requested.
            if ((!throttleListeners.containsKey(la)) && (a.size()==1)){
                requestThrottleSetup(la, false);
            }
        }
    }

    public void removeListener(DccLocoAddress la, java.beans.PropertyChangeListener p){
        if (addressThrottles.containsKey(la)){
            addressThrottles.get(la).getThrottle().removePropertyChangeListener(p);
            p.propertyChange(new PropertyChangeEvent(this, "throttleRemoved", la, null));
            return;
        }
        p.propertyChange(new PropertyChangeEvent(this, "throttleNotFoundInRemoval", la, null));
    }
    
    public boolean addressStillRequired(DccLocoAddress la){
        if (addressThrottles.containsKey(la)){
            log.debug("usage count is " + addressThrottles.get(la).getUseCount());
            if(addressThrottles.get(la).getUseCount()>0){
                return true;
            }
        }
        return false;
    }

    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        disposeThrottle(t, l);
    }

    public boolean disposeThrottle(DccThrottle t, ThrottleListener l){
//        if (!active) log.error("Dispose called when not active");  <-- might need to control this in the sub class
        DccLocoAddress la = (DccLocoAddress) t.getLocoAddress();
        if (addressReleased(la, l)){
            log.debug("Address " + t.getLocoAddress() + " still has active users");
            return false;
        }
        if(t.getListeners().size()>0){
            log.debug("Throttle " + t.getLocoAddress() + " still has active propertyChangeListeners registered to the throttle");
            return false;
        }
        if (addressThrottles.containsKey(la)){
            addressThrottles.remove(la);
            log.debug("Loco Address removed from the stack " + la);
        } else {
            log.debug("Loco Address not found in the stack " + la);
        }
        return true;
    }

    public void dispatchThrottle(DccThrottle t, ThrottleListener l){
        releaseThrottle(t, l);
    }
    
    protected boolean addressReleased(DccLocoAddress la, ThrottleListener l){
        if (addressThrottles.containsKey(la)){
            if(addressThrottles.get(la).containsListener(l)){
                log.debug("decrementUse called with listener " + l);
                addressThrottles.get(la).decrementUse();
                addressThrottles.get(la).removeListener(l);
            } else if (l==null){
                log.debug("decrementUse called withOUT listener");
                /*The release release has been called, but as no listener has 
                been specified, we can only decrement the use flag*/
                addressThrottles.get(la).decrementUse();
            }
        }
        if (addressThrottles.containsKey(la)){
            if(addressThrottles.get(la).getUseCount()>0)
                return true;
        }
        return false;
    }
    
    public Object getThrottleInfo(DccLocoAddress la, String item){
        DccThrottle t;
        if (addressThrottles.containsKey(la)){
            t = addressThrottles.get(la).getThrottle();
        } 
        else {
            return null;
        }
        if (item.equals("IsForward"))
            return t.getIsForward();
        else if(item.startsWith("Speed")){
            if (item.equals("SpeedSetting"))
                return t.getSpeedSetting();
            else if (item.equals("SpeedIncrement"))
                return t.getSpeedIncrement();
            else if (item.equals("SpeedStepMode"))
                return t.getSpeedStepMode();
        }
        else if (item.equals("F0"))
            return t.getF0();
        else if(item.startsWith("F1")){
            if (item.equals("F1"))
                return t.getF1();
            else if (item.equals("F10"))
                return t.getF10();
            else if (item.equals("F11"))
                return t.getF11();
            else if (item.equals("F12"))
                return t.getF12();
            else if (item.equals("F13"))
                return t.getF13();
            else if (item.equals("F14"))
                return t.getF14();
            else if (item.equals("F15"))
                return t.getF15();
            else if (item.equals("F16"))
                return t.getF16();
            else if (item.equals("F17"))
                return t.getF17();
            else if (item.equals("F18"))
                return t.getF18();
            else if (item.equals("F19"))
                return t.getF19();
        }
        else if(item.startsWith("F2")){
            if (item.equals("F2"))
                return t.getF2();
            else if (item.equals("F20"))
                return t.getF20();
            else if (item.equals("F21"))
                return t.getF21();
            else if (item.equals("F22"))
                return t.getF22();
            else if (item.equals("F23"))
                return t.getF23();
            else if (item.equals("F24"))
                return t.getF24();
            else if (item.equals("F25"))
                return t.getF25();
            else if (item.equals("F26"))
                return t.getF26();
            else if (item.equals("F27"))
                return t.getF27();
            else if (item.equals("F28"))
                return t.getF28();
        }
        else if (item.equals("F3"))
            return t.getF3();
        else if (item.equals("F4"))
            return t.getF4();
        else if (item.equals("F5"))
            return t.getF5();
        else if (item.equals("F6"))
            return t.getF6();
        else if (item.equals("F7"))
            return t.getF7();
        else if (item.equals("F8"))
            return t.getF8();
        else if (item.equals("F9"))
            return t.getF9();
        return null;
    }

    /**
     * This subClass, keeps track of which loco address have been requested and
     * by whom, it primarily uses a increment count to keep track of all the the
     * Addresses in use as not all external code will have been refactored over
     * to use the new disposeThrottle.
     */

    protected static class Addresses{

        int useActiveCount = 0;
        DccThrottle throttle = null;
        ArrayList<ThrottleListener> listeners = new ArrayList<ThrottleListener>();

        protected Addresses(DccThrottle throttle){
            this.throttle = throttle;
        }
    
        void incrementUse(){
            useActiveCount++;
            log.debug(throttle.getLocoAddress() + " increased Use Size to " + useActiveCount);
        }

        void decrementUse(){
            //Do want to go below 0 on the usage front!
            if (useActiveCount >0)
                useActiveCount--;
            log.debug(throttle.getLocoAddress() + " decreased Use Size to " + useActiveCount);
        }
        
        int getUseCount() { return useActiveCount; }
        
        DccThrottle getThrottle() {
            return throttle;
        }
        
        void setThrottle(DccThrottle throttle){
            DccThrottle old = this.throttle;
            this.throttle = throttle;
            if ((old==null) || (old==throttle)){
                return;
            }

            //As the throtte has changed, we need to inform the listeners
            //However if a throttle hasn't used the new code, it will not have been
            //removed and will get a notification.
            log.debug(throttle.getLocoAddress() + " throttle assigned " +
                    "has been changed need to notify throttle users");

            this.throttle = throttle;
            for (int i = 0; i<listeners.size(); i++){
                listeners.get(i).notifyThrottleFound(throttle);
            }
            //This handles moving the listeners from the old throttle to the new one
            DccLocoAddress la = (DccLocoAddress) this.throttle.getLocoAddress();
            Vector<PropertyChangeListener> v = old.getListeners();
            for (PropertyChangeListener prop : v)
            {
                this.throttle.addPropertyChangeListener(prop);
                prop.propertyChange(new PropertyChangeEvent(this, "throttleAssignmentChanged", null, la));
            }
        }

        void addListener(ThrottleListener l){
            // Will need to do a check for duplication here
            listeners.add(l);
        }

        void removeListener(ThrottleListener l){
            listeners.remove(l);
        }
        
        boolean containsListener(ThrottleListener l){
            return listeners.contains(l);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractThrottleManager.class.getName());
}
