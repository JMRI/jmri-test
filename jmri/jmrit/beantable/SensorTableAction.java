// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.util.JmriJFrame;


import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.jmrix.DCCManufacturerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;


/**
 * Swing action to create and register a
 * SensorTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision: 1.30 $
 */

public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SensorTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null) {
            setEnabled(false);
        }
    }
    public SensorTableAction() { this("Sensor Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSensorTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;

    JTextField sysName = new JTextField(10);
    JTextField userName = new JTextField(20);
    JComboBox prefixBox = new JComboBox();
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox("Add a range");
    JLabel sysNameLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    jmri.UserPreferencesManager p;
      
    void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        
        if (addFrame==null) {
            addFrame = new JmriJFrame(f.rb.getString("TitleAddSensor"));
            //addFrame.addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
                
            ActionListener rangeListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        canAddRange(e);
                    }
                };
            if (jmri.InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")){
                jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) jmri.InstanceManager.sensorManagerInstance();
                List<Manager> managerList = proxy.getManagerList();
                for(int x = 0; x<managerList.size(); x++){
                    String manuName = provideConnectionNameFromPrefix(managerList.get(x).getSystemPrefix());
                    prefixBox.addItem(manuName);                      
                }
                if(p.getComboBoxLastSelection(systemSelectionCombo)!=null)
                    prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
            else {
                prefixBox.addItem(provideConnectionNameFromPrefix(jmri.InstanceManager.sensorManagerInstance().getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", listener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }
    
    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;

        int numberOfSensors = 1;

        if(range.isSelected()){
            try {
                numberOfSensors = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Number to Sensors to Add must be a number!",""+ex,true, false, org.apache.log4j.Level.ERROR);
                return;
            }
        } 
        if (numberOfSensors>=65){
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 "You are about to add " + numberOfSensors + " Sensors into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        String sensorPrefix = getSensorPrefixFromName()+InstanceManager.sensorManagerInstance().typeLetter();

        String sName = null;
        String curAddress = sysName.getText();

        for (int x = 0; x < numberOfSensors; x++){
            curAddress = InstanceManager.sensorManagerInstance().getNextValidAddress(curAddress, getSensorPrefixFromName());
            if (curAddress==null){
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName=sensorPrefix+curAddress;
            Sensor s = null;
            try {
                s = InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if ((x!=0) && user != null && !user.equals(""))
                user = userName.getText()+":"+x;
            if (user!= null && !user.equals("") && (InstanceManager.sensorManagerInstance().getByUserName(user)==null)) s.setUserName(user);
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }
    
    private String provideConnectionNameFromPrefix(String prefix){
        java.util.List<Object> list 
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (list != null) {
            for (Object memo : list) {
                if (((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix().equals(prefix))
                    return ((jmri.jmrix.SystemConnectionMemo)memo).getUserName();
            }
        }
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return DCCManufacturerList.getDCCSystemFromType(prefix.charAt(0));
    }
    
    private void canAddRange(ActionEvent e){
        range.setEnabled(false);
        range.setSelected(false);
        if (jmri.InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")){
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) jmri.InstanceManager.sensorManagerInstance();
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = getSensorPrefixFromName();
            for(int x = 0; x<managerList.size(); x++){
                jmri.SensorManager mgr = (jmri.SensorManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)){
                    range.setEnabled(true);
                    return;
                }
            }
        }
        else if (jmri.InstanceManager.sensorManagerInstance().allowMultipleAdditions(getSensorPrefixFromName())){
            range.setEnabled(true);
        }
    }
    
    String getSensorPrefixFromName(){
        if (((String) prefixBox.getSelectedItem())==null)
            return null;
        java.util.List<Object> list 
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (list != null) {
            for (Object memo : list) {
                if (((jmri.jmrix.SystemConnectionMemo)memo).getUserName().equals(prefixBox.getSelectedItem())){
                    return ((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix();
                }
            }
        }
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return DCCManufacturerList.getTypeFromDCCSystem((String) prefixBox.getSelectedItem())+"";
    }
    
    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorSensorAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableAction.class.getName());
}


/* @(#)SensorTableAction.java */
