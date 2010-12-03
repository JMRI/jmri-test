package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.*;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.decoderdefn.*;

public class RosterToEcos implements EcosListener{

    private EcosLocoAddress objEcosLoco=null;
    private EcosLocoAddressManager objEcosLocoManager;
    protected RosterEntry _re = null;
    private EcosPreferences ep;
    //private String _rosterid;
    EcosTrafficController tc;
    private boolean createloco;
    
    DecoderIndexFile decoderind = DecoderIndexFile.instance();
    
    public RosterToEcos() { }

    public void createEcosLoco(RosterEntry re, EcosTrafficController etc) {
        if (createloco==true)
            return;
        createloco = true;
        etc = tc;
        ep = jmri.InstanceManager.getDefault(jmri.jmrix.ecos.EcosPreferences.class);
        _re = re;
		objEcosLocoManager = jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);

        String message = "create(10, addr[" + _re.getDccAddress() + "], name[\""+ description() +"\"], protocol["+ ep.getDefaultEcosProtocol()+"], append)";

        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    
    }
    
    private String description(){
    
        String result ="";
        String str = ep.getEcosLocoDescription();
        if ((str==null) || (str.equals(""))){
            return _re.getId();
        }
        char comp = '%';
        for(int i=0; i<str.length(); i++){
            if (str.charAt(i)==comp){
                if (str.charAt(i+1)=='i') result = result + _re.getId();
                else if (str.charAt(i+1)=='r') result = result + _re.getRoadName();
                else if (str.charAt(i+1)=='n') result = result + _re.getRoadNumber();
                else if (str.charAt(i+1)=='m') result = result + _re.getMfg();
                else if (str.charAt(i+1)=='o') result = result + _re.getOwner();
                else if (str.charAt(i+1)=='l') result = result + _re.getModel();
                else if (str.charAt(i+1)=='c') result = result + _re.getComment();
                i++;
            } else {
                result = result + str.charAt(i);
            }
        }
        return result;
    
    }
    
    //Need to deal with the loco not being created somehow.
    //If we get the error, then we could simply delete the loco from our loco list.
    public void reply(EcosReply m) {
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            if (lines[0].startsWith("<REPLY create(10, addr")){
                //System.out.println(msg);
                for(int i =1; i<lines.length-1; i++) {
                    if(lines[i].contains("10 id[")){

                        start = lines[i].indexOf("[")+1;
                        end = lines[i].indexOf("]");
                        String EcosAddr = lines[i].substring(start, end);
                        objEcosLoco = objEcosLocoManager.provideByEcosObject(EcosAddr);
                        objEcosLoco.setEcosTempEntry(false);
                        _re.putAttribute("EcosObject", EcosAddr);
                        objEcosLoco.setRosterId(_re.getId());
                        objEcosLoco.setEcosDescription(description());
                        objEcosLoco.setEcosLocoAddress(Integer.parseInt(_re.getDccAddress()));
                        objEcosLoco.setProtocol(ep.getDefaultEcosProtocol());
                        _re.writeFile(null, null, null);
                        jmri.jmrit.roster.Roster.writeRosterFile();
                        objEcosLocoManager.register(objEcosLoco);
                        createloco = false;
                        dispose();
                    }
                }
            }
        }
    }
    
    public void message(EcosMessage m){
        
    }
    void dispose(){
        objEcosLoco = null;
        objEcosLocoManager = null;
        _re = null;
        createloco = false;        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterToEcos.class.getName());
}