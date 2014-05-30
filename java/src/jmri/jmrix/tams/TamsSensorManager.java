// TamsSensorManager.java

package jmri.jmrix.tams;

import java.util.Hashtable;
import jmri.JmriException;
import jmri.Sensor;

/**
 * Implement sensor manager for Tams systems.
 * The Manager handles all the state changes.
 * <P>
 * System names are "USnnn:yy", where nnn is the Tams Object Number for a given
 * s88 Bus Module and yy is the port on that module.
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 20820 $
 */
public class TamsSensorManager extends jmri.managers.AbstractSensorManager
                                implements TamsListener {

    public TamsSensorManager(TamsSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getTrafficController();
        startPolling();
        // connect to the TrafficManager
        //tc.addTamsListener(this);
    }
    
    TamsSystemConnectionMemo memo;
    TamsTrafficController tc;
    //The hash table simply holds the object number against the TamsSensor ref.
    private Hashtable <Integer, Hashtable<Integer, TamsSensor>> _ttams = new Hashtable<Integer, Hashtable<Integer, TamsSensor>>();   // stores known Tams Obj
    
    public String getSystemPrefix() { return memo.getSystemPrefix(); }
    
    public Sensor createNewSensor(String systemName, String userName) {
        TamsSensor s = new TamsSensor(systemName, userName);
        if(systemName.contains(":")){
            int board = 0;
            int channel = 0;
            
            String curAddress = systemName.substring(getSystemPrefix().length()+1, systemName.length());
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                if(!_ttams.containsKey(board)){
                    _ttams.put(board, new Hashtable<Integer, TamsSensor>());
                    if(_ttams.size()==1)
                        startPolling();
                }
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
            Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
            try {
                channel = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
                if(!sensorList.containsKey(channel)){
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
        }
        
        return s;
    }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        if(!curAddress.contains(":")){
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Hardware Address passed should be past in the form 'Module:port'");
        }

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(":");
        try {
            board = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
        } catch (NumberFormatException ex) { 
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Module Address passed should be a number");
        }
        try {
            port = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
        } catch (NumberFormatException ex) { 
            log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
            throw new JmriException("Port Address passed should be a number");
        }
        
        if(port==0 || port>16){
            log.error("Port number must be between 1 and 16");
            throw new JmriException("Port number must be between 1 and 16");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrefix());
        sb.append("S");
        sb.append(board);
        sb.append(":");
        //Little work around to pad single digit address out.
        padPortNumber(port, sb);
        return sb.toString();
    }
    
    int board = 0;
    int port = 0;
    
    public String getNextValidAddress(String curAddress, String prefix){

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
            showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if(s!=null){
            port++;
            while(port<17){
                try{
                    tmpSName = createSystemName(board+":"+port, prefix);
                } catch (Exception e){
                    log.error("Error creating system name for " + board + ":" + port);
                }
                s = getBySystemName(tmpSName);
                if(s==null){
                    StringBuilder sb = new StringBuilder();
                    sb.append(board);
                    sb.append(":");
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    return sb.toString();
                }
                port++;
            }
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(board);
            sb.append(":");
            //Little work around to pad single digit address out.
            padPortNumber(port, sb);
            return sb.toString();
        }
        
    }
    
    void padPortNumber (int portNo, StringBuilder sb){
        if (portNo<10){
            sb.append("0");
        }
        sb.append(portNo);
    }

    // to listen for status changes from Tams system
    public void reply(TamsReply r) {
        log.info("Sensor massage Reply " + r.toString());
        if(r.getElement(0)==0x00){
            int status = r.getElement(1);
            status = (status<<8) + (r.getElement(2));
            decodeSensorState(boardRequest, status);
        }
        synchronized (this) {
            awaitingReply = false;
            this.notify();
        }
    }
    
    Thread pollThread;
    boolean stopPolling = true;
    
    protected void startPolling(){
        stopPolling = false;
        log.debug("Completed build of active readers " + _ttams.size());
        if (_ttams.size()>0) {
            
            if (pollThread==null) {
                pollThread = new Thread(new Runnable()
                        { public void run() { pollManager(); }},"Tams Sensor Poll");
                pollThread.start();
            }
        } else {
            log.debug("No active boards found");
        }
    }
    
    private final int shortCycleInterval = 550;
    private final int pollTimeout = 550;				// in case of lost response
    private boolean awaitingReply = false;
    private int boardRequest;
    private boolean processing;
    
    void pollManager(){
        while(!stopPolling){
            for(int board: _ttams.keySet()){
                if(log.isDebugEnabled())
                    log.debug("Poll board " + board);
                TamsMessage m = new TamsMessage(new byte[] {(byte)0x78,(byte)0x53,(byte)0x53,(byte)board});  //Did have (byte)0x31 before board.
                m.setTimeout(500);
                boardRequest = board;
                synchronized (this) {
                    log.debug("queueing poll request for board "+board);
                    tc.sendTamsMessage(m, this);
                    awaitingReply = true;
                    try {
                        wait(pollTimeout);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                }
                int delay = shortCycleInterval;
                synchronized (this){
                    if (awaitingReply) {
                        log.warn("timeout awaiting poll response for board "+board);
                        delay = pollTimeout;
                    }
                    try {
                        wait(delay);
                        while(processing){
                            processing=false;
                            wait(20);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    } finally { /*awaitingDelay = false;*/ }
                }
                if(stopPolling)
                    return;
    		}
            try {
                Thread.sleep(100);
            } catch (java.lang.InterruptedException e){

            } 
        }
    }
    
    public void handleTimeout(TamsMessage m){
        if(log.isDebugEnabled())
            log.debug("timeout recieved to our last message " + m.toString());

        if(!stopPolling){
            if(log.isDebugEnabled())
                log.debug("time out to board message : " + boardRequest);
            synchronized (this) {
                awaitingReply = false;
                this.notify();
            }
        }
    }
    

    public void message(TamsMessage m) {
        // messages are ignored
    }
    
    private void decodeSensorState(int board, int intState){
        log.info("Decoder Sensor State for board " + board + " state " + intState);
        TamsSensor ms;
        int k = 1;
        int result;
        
        String sensorprefix = getSystemPrefix()+"S"+board+":";
        Hashtable<Integer, TamsSensor> sensorList = _ttams.get(board);
        for(int port =16; port>=0; port--){
            result = intState & k;
            ms= sensorList.get(port);
            if(ms==null){
                StringBuilder sb = new StringBuilder();
                sb.append(sensorprefix);
                //Little work around to pad single digit address out.
                padPortNumber(port, sb);
                ms = (TamsSensor)provideSensor(sb.toString());
            }
            if(ms!=null){
                if (result==0)
                    ms.setOwnState(Sensor.INACTIVE);
                else {
                    ms.setOwnState(Sensor.ACTIVE);
                }
            }
            k=k*2;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsSensorManager.class.getName());
}

/* @(#)TamsSensorManager.java */
