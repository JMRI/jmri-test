package jmri.implementation;

import jmri.*;
import jmri.NamedBeanHandle;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;


/**
 * Conditional.java
 *
 * A Conditional type to provide runtime support for Densor Groups.
 * <P>
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
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */


public class DefaultSignalGroup extends AbstractNamedBean implements jmri.SignalGroup{

    public DefaultSignalGroup(String systemName, String userName) {
        super(systemName, userName);
    }
    
    public DefaultSignalGroup(String systemName) {
        super(systemName, null);
    }
    
    ArrayList <String> _signalMastAppearances = new ArrayList<String>();
    
    private NamedBeanHandle<SignalMast> _signalMast;
    
    private boolean headactive=false;
    
    private boolean enabled=true;
    
    public void setEnabled(boolean boo){
        enabled = boo;
    }
    
    public boolean getEnabled(){
        return enabled;
    }
    

    public void setSignalMast(String pName){

        SignalMast mMast = InstanceManager.signalMastManagerInstance().getBySystemName(pName);
        if (mMast == null) mMast = InstanceManager.signalMastManagerInstance().getByUserName(pName);
        if (mMast == null) {
            log.warn("did not find a SignalHead named "+pName);
            return;
        }
        if (_signalMast!=null){
            getSignalMast().removePropertyChangeListener(mSignalMastListener);
        }
        _signalMast = new NamedBeanHandle<SignalMast>(pName, mMast);
        getSignalMast().addPropertyChangeListener(mSignalMastListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Aspect")){
                    String now = ((String) e.getNewValue());
                    if (isSignalMastAppearanceIncluded(now)){
                        setHead();
                    } else {
                        resetHeads();
                    }
                }
            }
        });
    }
    
    public SignalMast getSignalMast(){
        return _signalMast.getBean();
    }
    
    public String getSignalMastName(){
        return _signalMast.getName();
    }
    
    public void addSignalMastAppearance(String appearance){
        if(isSignalMastAppearanceIncluded(appearance)){
            return;
        }
        _signalMastAppearances.add(appearance);
    }
    
    public boolean isSignalMastAppearanceIncluded(String appearance){
        for (int i=0; i<_signalMastAppearances.size(); i++) {
            if ( _signalMastAppearances.get(i).equals(appearance) ) {
                // Found Appearance
                return true;
            }
        }
        return false;
    }
    
    public void deleteSignalMastAppearance(String appearance){
        _signalMastAppearances.remove(appearance);
    }
    
    public int getNumSignalMastAppearances() {
        return _signalMastAppearances.size();
    }
    
    public String getSignalMastAppearanceByIndex(int x){
        try {
            return _signalMastAppearances.get(x);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    public void clearSignalMastAppearance(){
        _signalMastAppearances = new ArrayList<String>();
    }
    
    /*
        Add a new signalhead to the group
    */
    public void addSignalHead(NamedBeanHandle<SignalHead> sh){
        SignalHeadItem signalitem = new SignalHeadItem(sh);
        _signalHeadItem.add(signalitem);
    }
    /*
        Add a new signalhead to the group
    */
    public void addSignalHead(String pName){
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            addSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }
    
    protected PropertyChangeListener mSignalMastListener = null;
    
    public void setSignalHeadAlignTurnout(String mHead, String mTurn, int state){
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addTurnout(mTurn, state);
    }
    
    public void setSignalHeadAlignSensor(String mHead, String mSen, int state){
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addSensor(mSen, state);
    }
    

    /*
    Returns the 'n' signalheaditem
    */
    private SignalHeadItem getSignalHeadItemByIndex(int n)
    {
        try {
            return _signalHeadItem.get(n);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    
    public String getSignalHeadItemNameByIndex(int n)
    {
        try {
            return getSignalHeadItemByIndex(n).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /*
        Returns the number of signalheads in this group
    */
    public int getNumSignalHeadItems() {
        return _signalHeadItem.size();
    }
    
    public int getSignalHeadOnState(String name){
        try {
            return getSignalHeadItem(name).getOnAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }
    
    public int getSignalHeadOffState(String name){
        try {
            return getSignalHeadItem(name).getOffAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }
    
    public int getSignalHeadOnStateByIndex(int n){
        try {
            return getSignalHeadItemByIndex(n).getOnAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    
    public int getSignalHeadOffStateByIndex(int n){
        try {
            return getSignalHeadItemByIndex(n).getOffAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    
    public void deleteSignalHead(String pName){
            _signalHeadItem.remove(getSignalHeadItem(pName));
    }
    
    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh){
        _signalHeadItem.remove(getSignalHeadItem(sh.getName()));
    }
    
    public void setSignalHeadOnState(String name, int state){
        getSignalHeadItem(name).setOnAppearance(state);
    }
    
    public void setSignalHeadOffState(String name, int state){
        getSignalHeadItem(name).setOffAppearance(state);
    }
    
    public boolean isSignalIncluded(String pName) {
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).getSignal() == mHead ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }
    /*
        Returns a signalhead item
    */
    private SignalHeadItem getSignalHeadItem(String name) {
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).getName().equals(name) ) {
                // Found turnout
                return _signalHeadItem.get(i);
            }
        }
        return null;
	}
    
    public boolean isTurnoutIncluded(String pSignal, String pTurnout){
        return getSignalHeadItem(pSignal).isTurnoutIncluded(pTurnout);
    }
    
    public int getTurnoutState(String pSignal, String pTurnout){
        SignalHeadItem sig = getSignalHeadItem(pSignal);
        if (sig != null)
            return sig.getTurnoutState(pTurnout);
        return -1;
    }
    
    public int getTurnoutStateByIndex(int x, String pTurnout){
        try {
            return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
   
    public int getTurnoutStateByIndex(int x, int pTurnout){
        try {
            return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    public String getTurnoutNameByIndex(int x, int pTurnout){
        try {
            return getSignalHeadItemByIndex(x).getTurnoutName(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    public int getSensorStateByIndex(int x, int pSensor){
        try {
            return getSignalHeadItemByIndex(x).getSensorState(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    
    public String getSensorNameByIndex(int x, int pSensor){
        try {
            return getSignalHeadItemByIndex(x).getSensorName(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public boolean isSensorIncluded(String pSignal, String pSensor){
        return getSignalHeadItem(pSignal).isSensorIncluded(pSensor);
    }
    
    public int getSensorState(String pSignal, String pSensor){
        SignalHeadItem sig = getSignalHeadItem(pSignal);
        if (sig != null)
            return sig.getSensorState(pSensor);
        return -1;
    }
    
    public boolean getSensorTurnoutOper(String pSignal){
        return getSignalHeadItem(pSignal).getSensorTurnoutOper();
    }
    
    public boolean getSensorTurnoutOperByIndex(int x){
        return getSignalHeadItemByIndex(x).getSensorTurnoutOper();
    }
    
    public void setSensorTurnoutOper(String pSignal, boolean boo){
        getSignalHeadItem(pSignal).setSensorTurnoutOper(boo);
    }
    
    public void clearSignalTurnout(String pSignal){
        getSignalHeadItem(pSignal).clearSignalTurnouts();
    }
    public void clearSignalSensor(String pSignal){
        getSignalHeadItem(pSignal).clearSignalSensors();
    }
    
    private void resetHeads(){
        if (!headactive)
            return;
        for (int i=0; i<_signalHeadItem.size(); i++) {
            _signalHeadItem.get(i).getSignal().setAppearance(_signalHeadItem.get(i).getOffAppearance());
        }
        headactive=false;
    }
    
    private void setHead(){
        boolean active = false;
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).checkActive() ) {
                if (active)
                    log.warn("two signal heads in the group should not be active at once");
                active = true;
                headactive = true;
            }
        }
    }
    
    public int getNumSignalHeadSensorsByIndex(int x){
        try {
            return getSignalHeadItemByIndex(x).getNumSensors();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    
    public int getNumSignalHeadTurnoutsByIndex(int x){
        try {
            return getSignalHeadItemByIndex(x).getNumTurnouts();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    ArrayList <SignalHeadItem> _signalHeadItem = new ArrayList<SignalHeadItem>();
    private static class SignalHeadItem {
        SignalHeadItem(NamedBeanHandle<SignalHead> sh){
            namedHead = sh;
            if (namedHead.getBean().getClass().getName().contains("SingleTurnoutSignalHead")){
                jmri.implementation.SingleTurnoutSignalHead Signal = (jmri.implementation.SingleTurnoutSignalHead) namedHead.getBean();
                if ((onAppearance==0x00) && (offAppearance==0x00)){
                    onAppearance=Signal.getOnAppearance();
                    offAppearance=Signal.getOffAppearance();
                }
            }
        }
        
        private NamedBeanHandle<SignalHead> namedHead;
        
        public String getName(){
            return namedHead.getName();
        }
        
        public SignalHead getSignal(){
            return namedHead.getBean();
        }
        
        private int onAppearance = 0x00;
        private int offAppearance = 0x00;
        
        public void setOnAppearance(int app){
            onAppearance = app;
        }
        
        public int getOnAppearance(){
            return onAppearance;
        }
        
        public void setOffAppearance(int app){
            offAppearance = app;
        }
        
        public int getOffAppearance(){
            return offAppearance;
        }
        //Used to determine if we are using an AND or OR when testing the Sensors and Signals
        private boolean turnoutSensorOper = true;
        public boolean getSensorTurnoutOper(){ return turnoutSensorOper;}
        public void setSensorTurnoutOper(boolean boo) { turnoutSensorOper=boo; }
        
        //Don't yet have the AND or OR set yet.
        public boolean checkActive(){
            boolean state = false;
            for (int x = 0; x<_signalTurnoutList.size(); x++){
                log.debug ("Real state " + _signalTurnoutList.get(x).getName()+ " " + _signalTurnoutList.get(x).getTurnout().getKnownState() + " state we testing for " + _signalTurnoutList.get(x).getState());
                if (_signalTurnoutList.get(x).getTurnout().getKnownState()==_signalTurnoutList.get(x).getState())
                    state = true;
                else {
                    state = false;
                    break;
                }
            }

            for (int x = 0; x<_signalSensorList.size(); x++){
                if (_signalSensorList.get(x).getSensor().getKnownState()==_signalSensorList.get(x).getState())
                    state = true;
                else {
                    state = false;
                    break;
                }
            }
            if (state)
                getSignal().setAppearance(onAppearance);
            else
                getSignal().setAppearance(offAppearance);
            return state;
        }
        
        ArrayList <SignalTurnout> _signalTurnoutList = new ArrayList<SignalTurnout>();
        private static class SignalTurnout {
            NamedBeanHandle<Turnout> _turnout;
            int _state;

            SignalTurnout(String pName, int state) {
                Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(pName);
                _turnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, turnout);
                setState(state);
            }

            String getName() {
                if (_turnout != null)
                {
                    return _turnout.getName();
                }
                return null;
            }
            boolean setState(int state) {
                if (_turnout == null) {
                    return false;
                }
                if ((state!=Turnout.THROWN) && (state!=Turnout.CLOSED)) {
                    log.warn("Illegal Turnout state " +state + ": "+getName() );
                    return false;
                }        
                _state = state;
                return true;
            }
            int getState() {
                return _state;
            }
            Turnout getTurnout() {
                return _turnout.getBean();
            }
        }
                  
        void addTurnout(String name, int state){
            SignalTurnout signalTurnout = new SignalTurnout(name, state);
            _signalTurnoutList.add(signalTurnout);
        }
        
        int getTurnoutState(String name) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            for (int i=0; i<_signalTurnoutList.size(); i++) {
                if( _signalTurnoutList.get(i).getTurnout() == t1 ) {
                    return _signalTurnoutList.get(i).getState();
                }
            }
            return -1;
        }
        
        int getNumTurnouts() {
            return _signalTurnoutList.size();
        }
        
        String getTurnoutName(int x){
            return _signalTurnoutList.get(x).getName();
        }
        
        int getTurnoutState(int x){
            return _signalTurnoutList.get(x).getState();
        }
        
        boolean isTurnoutIncluded(String pName) {
            for (int i=0; i<_signalTurnoutList.size(); i++) {
                if ( _signalTurnoutList.get(i).getName().equals(pName) ) {
                    return true;
                }
            }
            return false;
        }
        
        void clearSignalTurnouts() {
            _signalTurnoutList = new ArrayList<SignalTurnout>();
        }
        
        void clearSignalSensors() {
            _signalSensorList = new ArrayList<SignalSensor>();
        }
        
        ArrayList <SignalSensor> _signalSensorList = new ArrayList<SignalSensor>();
        private static class SignalSensor {
            NamedBeanHandle<Sensor> _Sensor;
            int _state;

            SignalSensor(String pName, int state) {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                _Sensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor);
                setState(state);
            }

            String getName() {
                if (_Sensor != null)
                {
                    return _Sensor.getName();
                }
                return null;
            }
            boolean setState(int state) {
                if (_Sensor == null) {
                    return false;
                }
                if ((state!=Sensor.ACTIVE) && (state!=Sensor.INACTIVE)) {
                    log.warn("Illegal Sensor state "+state +" for : "+getName() );
                    return false;
                }        
                _state = state;
                return true;
            }
            int getState() {
                return _state;
            }
            Sensor getSensor() {
                return _Sensor.getBean();
            }
            
        }
        
        void addSensor(String name, int state){
            SignalSensor signalSensor = new SignalSensor(name, state);
            _signalSensorList.add(signalSensor);
        }
        
        int getSensorState(String name) {
            Sensor t1 = InstanceManager.sensorManagerInstance().provideSensor(name);
            for (int i=0; i<_signalSensorList.size(); i++) {
                if( _signalSensorList.get(i).getSensor() == t1 ) {
                    return _signalSensorList.get(i).getState();
                }
            }
            return -1;
        }
        
        int getNumSensors() {
            return _signalSensorList.size();
        }
        
        /*SignalSensor getSignalSensorByIndex(int x){
            return _signalSensorList.get(x);
        }*/
        
        String getSensorName(int x){
            return _signalSensorList.get(x).getName();
        }
        
        int getSensorState(int x){
            return _signalSensorList.get(x).getState();
        }
        
        boolean isSensorIncluded(String pName) {
            for (int i=0; i<_signalSensorList.size(); i++) {
                if ( _signalSensorList.get(i).getName().equals(pName) ) {
                    // Found Sensor
                    return true;
                }
            }
            return false;
        }
    }

    public int getState() {
        return 0x00;
    }
    
    public void setState(int state) {

    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalGroup.class.getName());
}