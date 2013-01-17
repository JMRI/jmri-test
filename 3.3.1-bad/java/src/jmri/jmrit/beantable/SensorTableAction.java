// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.util.JmriJFrame;
import jmri.util.ConnectionNameFromSystemName;

import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JFrame;

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
 * @version     $Revision$
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
        if (senManager==null) {
            setEnabled(false);
        }
    }
    public SensorTableAction() { this("Sensor Table");}

    protected SensorManager senManager = jmri.InstanceManager.sensorManagerInstance();
    public void setManager(Manager man){
        senManager = (SensorManager) man;
        if (m!=null)
            m.setManager(senManager);
    }
    
    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    protected void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel(senManager);
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleSensorTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
    
    JmriJFrame addFrame = null;

    JTextField sysName = new JTextField(40);
    JTextField userName = new JTextField(40);
    JComboBox prefixBox = new JComboBox();
    JTextField numberToAdd = new JTextField(5);
    JCheckBox range = new JCheckBox("Add a range");
    JLabel sysNameLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    String userNameError = this.getClass().getName()+".DuplicateUserName";
    jmri.UserPreferencesManager p;
      
    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddSensor"));
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
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    //Simple test not to add a system with a duplicate System prefix
                    for (int i = 0; i<prefixBox.getItemCount(); i++){
                        if(((String)prefixBox.getItemAt(i)).equals(manuName))
                            addToPrefix=false;
                    }
                    if (addToPrefix)
                        prefixBox.addItem(manuName);

                }
                if(p.getComboBoxLastSelection(systemSelectionCombo)!=null)
                    prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
            else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(jmri.InstanceManager.sensorManagerInstance().getSystemPrefix()));
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
        /*String user = userName.getText();
        if (user.equals("")) user=null;*/

        int numberOfSensors = 1;

        if(range.isSelected()){
            try {
                numberOfSensors = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Number to Sensors to Add must be a number!",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
        } 
        if (numberOfSensors>=65){
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 "You are about to add " + numberOfSensors + " Sensors into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        String sensorPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());

        String sName = null;
        String curAddress = sysName.getText();

        for (int x = 0; x < numberOfSensors; x++){
            try {
                curAddress = jmri.InstanceManager.sensorManagerInstance().getNextValidAddress(curAddress, sensorPrefix);
            }  catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert '" + curAddress + "' to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
            if (curAddress==null){
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName=sensorPrefix+jmri.InstanceManager.sensorManagerInstance().typeLetter()+curAddress;
            Sensor s = null;
            try {
                s = jmri.InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (s!=null) {
                String user = userName.getText();
                if ((x!=0) && user != null && !user.equals(""))
                    user = userName.getText()+":"+x;
                if (user!= null && !user.equals("") && (jmri.InstanceManager.sensorManagerInstance().getByUserName(user)==null)){
                    s.setUserName(user);
                } else if (jmri.InstanceManager.sensorManagerInstance().getByUserName(user)!=null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage("Duplicate UserName","The username " + user + " specified is already in use and therefore will not be set", getClassName(), "duplicateUserName", false, true, org.apache.log4j.Level.ERROR);
                }
            }
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }
    
    private void canAddRange(ActionEvent e){
        range.setEnabled(false);
        range.setSelected(false);
        if (senManager.getClass().getName().contains("ProxySensorManager")){
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) senManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for(int x = 0; x<managerList.size(); x++){
                jmri.SensorManager mgr = (jmri.SensorManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)){
                    range.setEnabled(true);
                    return;
                }
            }
        }
        else if (senManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))){
            range.setEnabled(true);
        }
    }
    
    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorSensorAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    protected void setDefaultDebounce(JFrame _who){
        JTextField activeField = new JTextField(String.valueOf(senManager.getDefaultSensorDebounceGoingActive()),4);
        JTextField inActiveField = new JTextField(String.valueOf(senManager.getDefaultSensorDebounceGoingInActive()),4);
        
        JPanel active = new JPanel();
        active.add(new JLabel(rb.getString("SensorActiveTimer")));
        active.add(activeField);
        
        JPanel inActive = new JPanel();
        inActive.add(new JLabel(rb.getString("SensorInactiveTimer")));
        inActive.add(inActiveField);
        
        int retval = JOptionPane.showOptionDialog(_who,
                                          rb.getString("SensorGlobalDebounceMessageBox") , rb.getString("SensorGlobalDebounceMessageTitle"),
                                          0, JOptionPane.INFORMATION_MESSAGE, null,
                                          new Object[]{"Cancel", "OK", active, inActive}, null );
        if (retval != 1) {
            return;
        }
        
        //We will allow the turnout manager to handle checking if the values have changed
        try {    
            long goingActive = Long.valueOf(activeField.getText());
            senManager.setDefaultSensorDebounceGoingActive(goingActive);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(_who, rb.getString("SensorDebounceActError")+"\n" + activeField.getText(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        
        try {    
            long goingInActive = Long.valueOf(inActiveField.getText());
            senManager.setDefaultSensorDebounceGoingInActive(goingInActive);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(_who, rb.getString("SensorDebounceActError")+"\n" + inActiveField.getText(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        m.fireTableDataChanged();
    }
    
    public void setMenuBar(BeanTableFrame f){
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();

        JMenu debounceMenu = new JMenu(rb.getString("Debounce"));
        JMenuItem item = new JMenuItem("Defaults...");
        debounceMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    setDefaultDebounce(finalF);
        	}
            });
        menuBar.add(debounceMenu);
    }

    void showDebounceChanged() {
        jmri.jmrit.beantable.sensor.SensorTableDataModel a = (jmri.jmrit.beantable.sensor.SensorTableDataModel)m;
        a.showDebounce(showDebounceBox.isSelected());
    }
    
    JCheckBox showDebounceBox = new JCheckBox(rb.getString("SensorDebounceCheckBox"));

    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(showDebounceBox, this.getClass().getName());
        showDebounceBox.setToolTipText(rb.getString("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
    }
    
    public void addToPanel(AbstractTableTabAction f) {
        String systemPrefix = ConnectionNameFromSystemName.getConnectionName(senManager.getSystemPrefix());
        
        if (senManager.getClass().getName().contains("ProxySensorManager"))
            systemPrefix = "All";
        f.addToBottomBox(showDebounceBox, systemPrefix);
        showDebounceBox.setToolTipText(rb.getString("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
    }
    
    public void setMessagePreferencesDetails(){
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "duplicateUserName",  rb.getString("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }
    
    protected String getClassName() { return SensorTableAction.class.getName(); }
    
    public String getClassDescription() { return rb.getString("TitleSensorTable"); }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableAction.class.getName());
}


/* @(#)SensorTableAction.java */
