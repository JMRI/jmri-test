// EcosSensorManager.java

package jmri.jmrix.ecos;

import java.util.Hashtable;
import jmri.Sensor;

/**
 * Implement sensor manager for Ecos systems.
 * The Manager handles all the state changes.
 * <P>
 * System names are "USnnn:yy", where nnn is the Ecos Object Number for a given
 * s88 Bus Module and yy is the port on that module.
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 1.2 $
 */
public class EcosSensorManager extends jmri.managers.AbstractSensorManager
                                implements EcosListener {

    public EcosSensorManager() {
        _instance = this;
        
        // listen for sensor creation
        // connect to the TrafficManager
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
                
        // ask to be notified
        //Not sure if we need to worry about newly created sensors on the layout.
        /*EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);*/
        
        // get initial state
        EcosMessage m = new EcosMessage("queryObjects(26, ports)");
        tc.sendEcosMessage(m, this);
    }

    EcosTrafficController tc;
    //The hash table simply holds the object number against the EcosSensor ref.
    protected static Hashtable <Integer, EcosSensor> _tecos = new Hashtable<Integer, EcosSensor>();   // stores known Ecos Object ids to DCC
    protected static Hashtable <Integer, Integer> _sport = new Hashtable<Integer, Integer>();   // stores known Ecos Object ids to DCC
    
    public char systemLetter() { return 'U'; }
    
    final static String prefix = "US";

    public Sensor createNewSensor(String systemName, String userName) {
        //int ports = Integer.valueOf(systemName.substring(2)).intValue();
        Sensor s = new EcosSensor(systemName, userName);
        //s.setUserName(userName);

        return s;
    }

    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // is this a list of sensors?
        EcosSensor es;
        int startobj;
        int endobj;

        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (msg.contains("<REPLY queryObjects(26, ports)>")) {
            for (int i = 1; i<lines.length-1; i++) {
                if (lines[i].contains("ports[")) { // skip odd lines
                    int start = 0;
                    int end = lines[i].indexOf(' ');
                    int object = Integer.parseInt(lines[i].substring(start, end));

                    if ( (100<=object) && (object<200)) { // only physical sensors
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        int ports = Integer.parseInt(lines[i].substring(start, end));
                        log.debug("Found sensor object "+object+" ports "+ports);
                        
                        if ((ports == 8) || (ports == 16)){
                            Sensor s;
                            String sensorSystemName;
                            _sport.put(object, ports);
                            for (int j=1; j<=ports; j++){
                                StringBuffer sb = new StringBuffer();
                                sb.append(prefix);
                                sb.append(object);
                                sb.append(":");
                                //Little work around to pad single digit address out.
                                if (j<10)
                                    sb.append("0");
                                sb.append(j);
                                sensorSystemName = sb.toString();
                                s=getSensor(sensorSystemName);
                                if(s==null){
                                    es = (EcosSensor)provideSensor(sensorSystemName);
                                    es.setObjectNumber(object);
                                    _tecos.put(object, es);
                                }
                            }
                            EcosMessage em = new EcosMessage("request("+object+", view)");
                            tc.sendEcosMessage(em, this);
                            
                            em = new EcosMessage("get("+object+",state)");
                            tc.sendEcosMessage(em, this);
                        } else {
                            log.debug("Invalid number of ports returned for Module " + object);
                        }
                    } 
                }
            }
        } else if (lines[0].contains("<REPLY get(") ){
            /*
            Potentially we could have received a message that is for a Loco or sensor
            rather than for a sensor or route
            We therefore need to extract the object number to check.
             */
            startobj=lines[0].indexOf("(")+1;
            endobj=(lines[0].substring(startobj)).indexOf(",")+startobj;
            //The first part of the messages is always the object id.
            int object = Integer.parseInt(lines[0].substring(startobj, endobj));
            if ((100<=object) && (object<200)){
                es = _tecos.get(object);
                if(lines[0].contains("state")){
                    int startstate = msg.indexOf("state[");
                    int endstate = msg.indexOf("]");
                    if (startstate>0 && endstate >0) {
                        String val = msg.substring(startstate+8, endstate);
                        int intState = Integer.valueOf(val,16).intValue();
                        EcosSensorState(object, intState);
                    }
                } 
            }
        } else if (lines[0].contains("<EVENT ")){
            //So long as the event information is for a sensor we will determine
            //which sensor it is for and let that deal with the message.
            startobj=lines[0].indexOf(" ")+1;
            endobj=(lines[0].substring(startobj)).indexOf(">")+startobj;
            //The first part of the messages is always the object id.
            int object = Integer.parseInt(lines[0].substring(startobj, endobj));
            if ((100<=object) && (object<200)){
                //es = _tecos.get(object);
                int startstate = msg.indexOf("state[");
                int endstate = msg.indexOf("]");
                //int newstate = UNKNOWN;
                if (startstate>0 && endstate >0) {
                    String val = msg.substring(startstate+8, endstate);
                    int intState = Integer.valueOf(val,16).intValue();
                    EcosSensorState(object, intState);
                }
            }
        } 
    }
    

    public void message(EcosMessage m) {
        // messages are ignored
    }

    static public EcosSensorManager instance() {
        if (_instance == null) _instance = new EcosSensorManager();
        return _instance;
    }
    static EcosSensorManager _instance = null;
    
    private void EcosSensorState(int object, int intState){
        EcosSensor es;
        int k = 1;
        int result;
        String sensorSystemName;
        for(int port =1; port<=_sport.get(object); port++){
            result = intState & k;
            //Little work around to pad single digit address out.
            StringBuffer sb = new StringBuffer();
            sb.append(prefix);
            sb.append(object);
            sb.append(":");
            //Little work around to pad single digit address out.
            if (port<10)
                sb.append("0");
            sb.append(port);
            sensorSystemName = sb.toString();
            es=(EcosSensor)getSensor(sensorSystemName);
            if (result==0)
                es.setOwnState(Sensor.INACTIVE);
            else
                es.setOwnState(Sensor.ACTIVE);
            k=k*2;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosSensorManager.class.getName());
}

/* @(#)EcosSensorManager.java */
