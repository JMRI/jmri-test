// EcosTurnoutManager.java

package jmri.jmrix.ecos;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import jmri.Turnout;
import jmri.jmrix.ecos.utilities.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.ResourceBundle;

/**
 * Implement turnout manager for Ecos systems.
 * <P>
 * System names are "UTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision: 1.16 $
 */
public class EcosTurnoutManager extends jmri.managers.AbstractTurnoutManager
                                implements EcosListener {

    private EcosTurnoutManager() {
        //_instance = this;
        
        // listen for turnout creation
        // connect to the TrafficManager
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
                
        // ask to be notified about newly created turnouts on the layout.
        EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);
        
        // get initial state
        m = new EcosMessage("queryObjects(11, addrext)");
        tc.sendEcosMessage(m, this);
        this.addPropertyChangeListener(this);
    }

    EcosTrafficController tc;
    
    //The hash table simply holds the object number against the EcosTurnout ref.
    private static Hashtable <Integer, EcosTurnout> _tecos = new Hashtable<Integer, EcosTurnout>();   // stores known Ecos Object ids to DCC
    
    public String getSystemPrefix() { return "U"; }
    final String prefix = getSystemPrefix()+typeLetter();

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(2)).intValue();
        } catch (java.lang.NumberFormatException e){
            log.error("failed to convert systemName " + systemName + " to a turnout address");
            return null;
        }
        Turnout t = new EcosTurnout(addr);
        t.setUserName(userName);
        return t;
    }
    
    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // is this a list of turnouts?
        EcosTurnout et;
        int start;
        int end;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            if (lines[0].startsWith("<REPLY queryObjects(11)>")) {
                checkTurnoutList(lines);
            }
            else if (lines[0].startsWith("<REPLY queryObjects(11, addr)>")) {
                // yes, make sure TOs exist
                log.debug("found "+(lines.length-2)+" turnout objects");
                for (int i = 1; i<lines.length-1; i++) {
                    if (lines[i].contains("addr[")) { // skip odd lines
                        int object = GetEcosObjectNumber.getEcosObjectNumber(lines[i], null, " ");
                        if ( (20000<=object) && (object<30000)) { // only physical turnouts
                            int addr = GetEcosObjectNumber.getEcosObjectNumber(lines[i], "[", "]");
                            log.debug("Found turnout object "+object+" addr "+addr);
                            
                            if ( addr > 0 ) {
                                Turnout t = getTurnout(prefix+addr);
                                if (t == null) {
                                    et = (EcosTurnout)provideTurnout(prefix+addr);
                                    et.setObjectNumber(object);
                                    _tecos.put(object, et);
                                    // listen for changes

                                    EcosMessage em = new EcosMessage("request("+object+",view)");
                                    tc.sendEcosMessage(em, null);
                                    
                                    // get initial state
                                    em = new EcosMessage("get("+object+",state)");
                                    tc.sendEcosMessage(em, null);
                                    
                                }
                            }
                        } else if (( 30000<=object) && (object<40000)){  //This is a ecos route
                            log.debug("Found route object " + object);

                            Turnout t = getTurnout(prefix+object);
                            if (t==null) {
                                    et = (EcosTurnout)provideTurnout(prefix+object);
                                    et.setObjectNumber(object);
                                    _tecos.put(object, et);

                                    // get initial state
                                    EcosMessage em = new EcosMessage("get("+object+",state)");
                                    tc.sendEcosMessage(em, null);
                            }
                        }
                    }
                }
            } 
            else if (lines[0].startsWith("<REPLY get(") ) {
                /*
                Potentially we could have received a message that is for a Loco or sensor
                rather than for a turnout or route
                We therefore need to extract the object number to check.
                 */
                
                int object = GetEcosObjectNumber.getEcosObjectNumber(lines[0], "(", ",");
                if ((20000<=object) && (object<40000)){
                    //et = _tecos.get(object);
                    et = (EcosTurnout) getByEcosObject(object);
                    if(lines[0].contains("state")){
                        //As this is in response to a change in state we shall forward
                        //it straight on to the ecos turnout to deal with.
                        et.reply(m);
                        //As the event will come from one object, we shall check to see if it is an extended address,
                        // if it is we also forward the message onto the slaved address.
                        if(et.getExtended()!=0){
                            EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                            etx.reply(m);
                        }

                    } else if (lines[0].contains("symbol")){
                    //Extract symbol number and set on turnout.
                        int symbol = GetEcosObjectNumber.getEcosObjectNumber(lines[1], "[", "]");
                        et.setExtended(symbol);
                        et.setTurnoutOperation(jmri.TurnoutOperationManager.getInstance().getOperation("NoFeedback"));
                        if((symbol==2)||(symbol==4)){
                            
                            EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                            etx.setExtended(symbol);
                            etx.setTurnoutOperation(jmri.TurnoutOperationManager.getInstance().getOperation("NoFeedback"));
                            switch(symbol) {
                                case 2 : et.setComment("Three Way Point with " + et.getSlaveAddress());
                                        break;
                                case 4 : et.setComment("Double Slip with " + et.getSlaveAddress());
                                        break;
                            }
                        }
                        // get initial state
                        EcosMessage em = new EcosMessage("get("+object+",state)");
                        tc.sendEcosMessage(em, null);
                    
                    } else if (lines[0].contains("addrext")){
                        turnoutAddressDetails(lines[1]);
                    }
                    else {
                        String name = null;
                        for(int i = 1; i<lines.length-1;i++){
                            if (lines[i].contains("name")){
                                start=lines[i].indexOf("[")+2;
                                end=lines[i].indexOf("]")-1;
                                if ((name!=null) && (start!=end))
                                    name = name + " " + lines[i].substring(start, end);
                                else if (name==null)
                                    name = lines[i].substring(start, end);
                                //name = name + " " +
                                //et.setUserName(name);
                            }
                        }
                        if (name!=null)
                            et.setUserName(name);
                    }
                }
            } 
            else if (lines[0].startsWith("<EVENT 11>")){
                //Creation or removal of a turnout from the Ecos.
                if (lines[1].contains("msg[LIST_CHANGED]")){
                    log.debug("We have received notification of a change in the Turnout list");
                    EcosMessage mout = new EcosMessage("queryObjects(11)");
                    tc.sendEcosMessage(mout, this);
                }
                //Creation or removal of a turnout from the Ecos.
            }
            else if (lines[0].startsWith("<EVENT")){
                //So long as the event information is for a turnout we will determine
                //which turnout it is for and let that deal with the message.
                int object = GetEcosObjectNumber.getEcosObjectNumber(lines[0], " ", ">");
                if ((20000<=object) && (object<40000)){
                    log.debug("Forwarding on State change for " + object);
                    et = _tecos.get(object);
                    if (et!=null){
                        et.reply(m);
                        //As the event will come from one object, we shall check to see if it is an extended address,
                        // if it is we also forward the message onto the slaved address.
                        if(et.getExtended()!=0){
                            log.debug("This is also an extended turnout so forwarding on change to " + et.getSlaveAddress());
                            EcosTurnout etx = (EcosTurnout)provideTurnout(et.getSlaveAddress());
                            etx.reply(m);
                        }
                    }
                }
            } 
            else if (lines[0].startsWith("<REPLY queryObjects(11, addrext)>")){
                for (int i = 1; i<lines.length-1; i++) {
                    if (lines[i].contains("addrext[")) { // skip odd lines
                        turnoutAddressDetails(lines[i]);
                    }
                }
            }
        }
        else
            log.debug("Message received from Ecos is in error");
    }

    protected boolean addingTurnouts = false;
    
    private void turnoutAddressDetails(String lines){
        addingTurnouts = true;
        EcosTurnout et;
        int start;
        int end;
        int object = GetEcosObjectNumber.getEcosObjectNumber(lines, null, " ");
        if ( (20000<=object) && (object<30000)) {
            start = lines.indexOf('[')+1;
            end = lines.indexOf(']');
            String turnoutadd=stripChar(lines.substring(start, end));
            String[] straddr = turnoutadd.split(",");
            log.debug("Number of Address for this device is " + straddr.length);
            if(straddr.length<=2){
                if (straddr.length==2) {
                    if (!straddr[0].equals(straddr[1])) log.debug("Addresses are not the same, we shall use the first address listed.");
                }
                int addr=Integer.parseInt(straddr[0]);
                if ( addr > 0 ) {
                    Turnout t = getTurnout(prefix+addr);
                    if (t == null) {
                        et = (EcosTurnout)provideTurnout(prefix+addr);
                        et.setObjectNumber(object);
                        _tecos.put(object, et);
                        // listen for changes
                        EcosMessage em = new EcosMessage("request("+object+",view)");
                        tc.sendEcosMessage(em, null);
                        
                        // get initial state
                        em = new EcosMessage("get("+object+",state)");
                        tc.sendEcosMessage(em, null);

                        em = new EcosMessage("get("+object+", name1, name2, name3)");
                        tc.sendEcosMessage(em, null);
                    }
                }
                
            }else if (straddr.length ==4){
                log.debug("We have a two address object.");
                //The first two addresses should be the same
                if(!straddr[0].equals(straddr[1])) log.debug("First Pair of Addresses are not the same, we shall use the first address");
                if(!straddr[2].equals(straddr[3])) log.debug("Second Pair of Addresses are not the same, we shall use the first address");
                int addr=Integer.parseInt(straddr[0]);
                int addr2=Integer.parseInt(straddr[2]);
                if ( addr > 0 ) {
                    //addr = straddr[0];
                    Turnout t = getTurnout(prefix+addr);
                    if (t == null) {
                        et = (EcosTurnout)provideTurnout(prefix+addr);
                        et.setObjectNumber(object);
                        et.setSlaveAddress(addr2);
                        _tecos.put(object, et);
                        
                        //Get the type of accessory...
                        EcosMessage em = new EcosMessage("get("+object+",symbol)");
                        tc.sendEcosMessage(em, this);
                        
                        // listen for changes
                        em = new EcosMessage("request("+object+",view)");
                        tc.sendEcosMessage(em, this);

                        em = new EcosMessage("get("+object+", name1, name2, name3)");
                        tc.sendEcosMessage(em, this);
                    }
                }
                
                if (addr2 > 0){
                    Turnout t = getTurnout(prefix+addr2);
                    if (t == null) {
                        et = (EcosTurnout)provideTurnout(prefix+addr2);
                        et.setMasterObjectNumber(false);
                        et.setObjectNumber(object);
                        et.setComment("Extended address linked with turnout " + getSystemPrefix()+"T"+straddr[0]);
                    }
                }
            }
            
        } else if (( 30000<=object) && (object<40000)){  //This is a ecos route

            log.debug("Found route object " + object);

            Turnout t = getTurnout(prefix+object);
            if (t==null) {
                et = (EcosTurnout)provideTurnout(prefix+object);
                et.setObjectNumber(object);
                _tecos.put(object, et);

                // get initial state
                EcosMessage em = new EcosMessage("get("+object+",state)");
                tc.sendEcosMessage(em, null);
                //Need to do some more work on routes on the ecos.

                // listen for changes
               // em = new EcosMessage("request("+object+",view)");
               // tc.sendEcosMessage(em, null);

                // get the name from the ecos to set as Username
                em = new EcosMessage("get("+object+", name1, name2, name3)");
                tc.sendEcosMessage(em, null);
            }
        }
        addingTurnouts = false;
    }

    
     /* This is used after an event update form the ecos informing us of a change in the 
     * turnout list, we have to determine if it is an addition or delete.
     * We should only ever do either a remove or an add in one go.
     */
    void checkTurnoutList(String[] ecoslines){
        final EcosPreferences p = EcosPreferences.instance();
        
        String[] jmrilist = getEcosObjectArray();
        boolean nomatch = true;
        int intTurnout = 0;
        String strECOSTurnout = null;
        for(int i=0; i<jmrilist.length; i++){
            nomatch=true;
            String strJMRITurnout=jmrilist[i];
            intTurnout = Integer.parseInt(strJMRITurnout);
            for(int k=1; k<ecoslines.length-1;k++){
                strECOSTurnout = ecoslines[k].replaceAll("[\\n\\r]","");
                if (strECOSTurnout.equals(strJMRITurnout)){
                    nomatch=false;
                    break;
                }
            }

            if (nomatch){
                final EcosTurnout et = (EcosTurnout) getByEcosObject(intTurnout);
                _tecos.remove(intTurnout);
                if (p.getRemoveTurnoutsFromJMRI()==0x02) {
                    //Remove turnout
                    _tecos.remove(et.getObject());
                    deregister(et);
                } else if (p.getRemoveTurnoutsFromJMRI()==0x00){
                    final JDialog dialog = new JDialog();
                    dialog.setTitle("Delete Turnout");
                    dialog.setLocationRelativeTo(null);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                    JLabel question = new JLabel("A Turnout " + et.getDisplayName() + " has been deleted on the ECOS");
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    question = new JLabel("Do you want to remove this turnout from JMRI");
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JButton yesButton = new JButton("Yes");
                    JButton noButton = new JButton("No");
                    JPanel button = new JPanel();
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.add(yesButton);
                    button.add(noButton);
                    container.add(button);

                    noButton.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()){
                                p.setRemoveTurnoutsFromJMRI(0x01);
                            }

                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener(){
                        final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()) {
                                p.setRemoveTurnoutsFromJMRI(0x02);
                            }
                            int count = et.getNumPropertyChangeListeners()-1; // one is this table
                            if (log.isDebugEnabled()) log.debug("Delete with "+count);
                            if ((!noWarnDelete) && (count >0)) {
                                String msg = java.text.MessageFormat.format(
                                            rb.getString("DeletePrompt")+"\n"
                                            +rb.getString("ReminderInUse"),
                                            new Object[]{et.getSystemName(),""+count});
                                 // verify deletion
                                int val = javax.swing.JOptionPane.showOptionDialog(null,
                                        msg, rb.getString("WarningTitle"),
                                        javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null,
                                        new Object[]{rb.getString("ButtonYes"),
                                                     rb.getString("ButtonYesPlus"),
                                                     rb.getString("ButtonNo")},
                                        rb.getString("ButtonNo"));
                                if (val == 2) {
                                    _tecos.remove(et.getObject());
                                    deregister(et);
                                    dialog.dispose();
                                    return;  // return without deleting
                                }
                                if (val == 1) { // suppress future warnings
                                    noWarnDelete = true;
                                }
                            }
                            // finally OK, do the actual delete
                            deleteEcosTurnout(et);
                            dialog.dispose();
                        }
                    });
                    container.add(remember);
                    container.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.setAlignmentY(Component.CENTER_ALIGNMENT);
                    dialog.getContentPane().add(container);
                    dialog.pack();
                    dialog.setModal(true);
                    dialog.setVisible(true);
                } else {
                    //We will need to remove the turnout from our list as it no longer exists on the ecos.
                    _tecos.remove(et.getObject());
                }
            }
        }
        int turnout;
        for(int i=1; i<ecoslines.length-1; i++){
            String tmpturn = ecoslines[i].replaceAll("[\\n\\r]","");
            turnout = Integer.parseInt(tmpturn);
            if(getByEcosObject(turnout)==null){
                EcosMessage mout = new EcosMessage("get(" + turnout + ", addrext)");
                tc.sendEcosMessage(mout, this);
            }
        }
        /*mout = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(mout, this);*/
    }
    
    boolean noWarnDelete = false;
    
    public String stripChar(String s) {  
        String allowed =
          ",0123456789";
        StringBuffer result = new StringBuffer();
        for ( int i = 0; i < s.length(); i++ ) {
            if ( allowed.indexOf(s.charAt(i)) >= 0 )
               result.append(s.charAt(i));
        }
        
        return result.toString();
    }

    public void message(EcosMessage m) {
        // messages are ignored
    } 
    
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals("length")) && (!addingTurnouts)) {
            final EcosPreferences p = EcosPreferences.instance();
            EcosTurnout et;
            String[] ecoslist = this.getEcosObjectArray();
            String[] jmrilist = getSystemNameArray();
            for (int i = 0; i<jmrilist.length; i++){
                if (jmrilist[i].startsWith(prefix)) {
                    et = (EcosTurnout) getBySystemName(jmrilist[i]);
                    if(et.getObject()==0){
                        //We do not support this yet at there are many parameters
                        // when creating a turnout on the ecos.
                    }
                }
            }
            
            for(int i = 0; i<ecoslist.length; i++){
                et = (EcosTurnout) getByEcosObject(Integer.parseInt(ecoslist[i]));
                int address = et.getNumber();
                if (getBySystemName(prefix+address)==null) {
                    if (p.getRemoveTurnoutsFromEcos()==0x02){
                        RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                        removeObjectFromEcos.removeObjectFromEcos(""+et.getObject());
                        deleteEcosTurnout(et);
                    } else {
                        final EcosTurnout etd = et;
                        final JDialog dialog = new JDialog();
                        dialog.setTitle("Remove Turnout From ECoS?");
                        dialog.setLocation(300,200);
                        dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                        JPanel container = new JPanel();
                        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                        container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                        JLabel question = new JLabel("Do you also want to remove turnout " + etd.getSystemName() + " from the Ecos");
                        question.setAlignmentX(Component.CENTER_ALIGNMENT);
                        container.add(question);
                        final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                        remember.setFont(remember.getFont().deriveFont(10f));
                        remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                        remember.setVisible(true);
                        JButton yesButton = new JButton("Yes");
                        JButton noButton = new JButton("No");
                        JPanel button = new JPanel();
                        button.setAlignmentX(Component.CENTER_ALIGNMENT);
                        button.add(yesButton);
                        button.add(noButton);
                        container.add(button);

                        noButton.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(remember.isSelected()){
                                    p.setRemoveTurnoutsFromEcos(0x01);
                                }
                                dialog.dispose();
                            }
                        });

                        yesButton.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(remember.isSelected()) {
                                    p.setRemoveTurnoutsFromEcos(0x02);
                                }
                                RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                                removeObjectFromEcos.removeObjectFromEcos(""+etd.getObject());
                                deleteEcosTurnout(etd);
                                dialog.dispose();
                            }
                        });
                        container.add(remember);
                        container.setAlignmentX(Component.CENTER_ALIGNMENT);
                        container.setAlignmentY(Component.CENTER_ALIGNMENT);
                        dialog.getContentPane().add(container);
                        dialog.pack();
                        dialog.setModal(true);
                        dialog.setVisible(true);
                    }
                }
            }
        }
        super.propertyChange(e);
    }

    public void deleteEcosTurnout(EcosTurnout et){
        addingTurnouts = true;
        deregister(et);
        et.dispose();
        EcosMessage em = new EcosMessage("release("+et.getObject()+",view)");
        tc.sendEcosMessage(em, this);
        _tecos.remove(et.getObject());
        addingTurnouts = false;
    }

    public void dispose(){
        Enumeration<Integer> en = _tecos.keys();
        EcosMessage em;
        while (en.hasMoreElements()) {
            int ecosObject = en.nextElement();
            em = new EcosMessage("release("+ecosObject+",view)");
            tc.sendEcosMessage(em, this);
        }
        
        if (jmri.InstanceManager.configureManagerInstance()!= null)
            jmri.InstanceManager.configureManagerInstance().deregister(this);
        _tecos.clear();
    }
    
    public List<String> getEcosObjectList() {
        String[] arr = new String[_tecos.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<Integer> en = _tecos.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = ""+en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public String[] getEcosObjectArray() {
        String[] arr = new String[_tecos.size()];
        Enumeration<Integer> en = _tecos.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = ""+en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }
    
    public Turnout getByEcosObject(int ecosObject) { 
        return _tecos.get(ecosObject);
    }
    
    static class EcosTurnoutManagerHolder {
        static EcosTurnoutManager
            instance = new EcosTurnoutManager();
    }

    public static EcosTurnoutManager instance() {
        return EcosTurnoutManagerHolder.instance;
    }
    
    /*static public EcosTurnoutManager instance() {
        if (_instance == null) _instance = new EcosTurnoutManager();
        return _instance;
    }
    static EcosTurnoutManager _instance = null;*/

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosTurnoutManager.class.getName());
}

/* @(#)EcosTurnoutManager.java */
