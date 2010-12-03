// PreferencesPane.java

package jmri.jmrix.ecos.swing.preferences;

//import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import jmri.jmrix.ecos.EcosPreferences;
import java.awt.Component;

import javax.swing.*;

/**
 * Pane to show ECoS preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 1.6 $
 */
public class PreferencesPane extends javax.swing.JPanel implements PropertyChangeListener {


    JPanel throttletabpanel = new JPanel();
    JPanel rostertabpanel = new JPanel();
    JPanel turnouttabpanel = new JPanel();
    JComboBox _addTurnoutsEcos;
    JComboBox _removeTurnoutsEcos;
    JComboBox _addTurnoutsJmri;
    JComboBox _removeTurnoutsJmri;
    JComboBox _masterControl;
    JComboBox _addLocoEcos;
    JComboBox _removeLocosEcos;
    JComboBox _addLocoJmri;
    JComboBox _removeLocosJmri;
    JTextField _ecosDescription;
    JRadioButton _adhocLocoEcosAsk;
    JRadioButton _adhocLocoEcosLeave;
    JRadioButton _adhocLocoEcosRemove;
    JRadioButton _forceControlLocoEcosAsk;
    JRadioButton _forceControlLocoEcosNever;
    JRadioButton _forceControlLocoEcosAlways;
    ButtonGroup _adhocLocoEcos;
    ButtonGroup _locoEcosControl;
    JCheckBox _rememberAdhocLocosEcos;
    JComboBox _defaultProtocol;
    EcosPreferences ep;
    
    public PreferencesPane(EcosPreferences epref) {
        super();
        ep=epref;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update");
        
        updateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                updateButtonPressed();
            }
        });
        buttonPanel.add(updateButton);
        
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTabbedPane tab = new JTabbedPane();
        tab.add(rosterTab(), "Roster");
        tab.add(throttleTab(), "Throttle");
        tab.add(turnoutTab(), "Turnouts");
        
        add(tab);
        add(buttonPanel);
        
        ep.addPropertyChangeListener(this);
        
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("update")) {
            updateValues();
        }
    }
    
    void updateValues(){
        setChoiceType(_removeTurnoutsEcos, ep.getRemoveTurnoutsFromEcos());
        setChoiceType(_addTurnoutsJmri, ep.getAddTurnoutsToJMRI());
        setChoiceType(_removeTurnoutsJmri, ep.getRemoveTurnoutsFromJMRI());
        setChoiceType(_addLocoEcos, ep.getAddLocoToEcos());
        setChoiceType(_removeLocosEcos, ep.getRemoveLocoFromEcos());
        setChoiceType(_addLocoJmri, ep.getAddLocoToJMRI());
        setChoiceType(_removeLocosJmri, ep.getRemoveLocoFromJMRI());
        switch (ep.getAdhocLocoFromEcos()){
            case 0  :   _adhocLocoEcosAsk.setSelected(true);
                        break;
            case 1  :   _adhocLocoEcosLeave.setSelected(true);
                        break;
            case 2  :   _adhocLocoEcosRemove.setSelected(true);
                        break;
            default :   _adhocLocoEcosAsk.setSelected(true);
                        break;
        }
        
        switch (ep.getForceControlFromEcos()){
            case 0x00  :   _forceControlLocoEcosAsk.setSelected(true);
                        break;
            case 0x01  :   _forceControlLocoEcosNever.setSelected(true);
                        break;
            case 0x02  :   _forceControlLocoEcosAlways.setSelected(true);
                        break;
            default :   _forceControlLocoEcosAsk.setSelected(true);
                        break;
        }
        setEcosProtocolType(_defaultProtocol, ep.getDefaultEcosProtocol());
    }
    
    private JPanel turnoutTab(){
        turnouttabpanel.setLayout(new BoxLayout(turnouttabpanel, BoxLayout.Y_AXIS));
        
        JPanel _removeTurnoutsEcosPanel = new JPanel();
        JLabel _removeTurnoutsEcosLabel = new JLabel("Remove Turnouts From the ECoS");
        _removeTurnoutsEcos = new JComboBox();
        _removeTurnoutsEcosPanel.add(_removeTurnoutsEcosLabel);
        initializeChoiceCombo(_removeTurnoutsEcos);
        if (ep.getRemoveTurnoutsFromEcos()!=0x00)
            setChoiceType(_removeTurnoutsEcos, ep.getRemoveTurnoutsFromEcos());
        _removeTurnoutsEcosPanel.add(_removeTurnoutsEcos);
        turnouttabpanel.add(_removeTurnoutsEcosPanel);
        
        JPanel _addTurnoutsJMRIPanel = new JPanel();
        JLabel _addTurnoutsJMRILabel = new JLabel("Add Turnouts to JMRI");
        _addTurnoutsJmri = new JComboBox();
        _addTurnoutsJMRIPanel.add(_addTurnoutsJMRILabel);
        initializeChoiceCombo(_addTurnoutsJmri);
        if (ep.getAddTurnoutsToJMRI()!=0x00)
            setChoiceType(_addTurnoutsJmri, ep.getAddTurnoutsToJMRI());
        _addTurnoutsJMRIPanel.add(_addTurnoutsJmri);
        turnouttabpanel.add(_addTurnoutsJMRIPanel);
        
        JPanel _removeTurnoutsJMRIPanel = new JPanel();
        JLabel _removeTurnoutsJMRILabel = new JLabel("Remove Turnouts from JMRI");
        _removeTurnoutsJmri = new JComboBox();
        _removeTurnoutsJMRIPanel.add(_removeTurnoutsJMRILabel);
        initializeChoiceCombo(_removeTurnoutsJmri);
        if (ep.getRemoveTurnoutsFromJMRI()!=0x00)
            setChoiceType(_removeTurnoutsJmri, ep.getRemoveTurnoutsFromJMRI());
        _removeTurnoutsJMRIPanel.add(_removeTurnoutsJmri);
        turnouttabpanel.add(_removeTurnoutsJMRIPanel);
        
        return turnouttabpanel;
    }
    
    private JPanel rosterTab(){
        
        rostertabpanel.setLayout(new BoxLayout(rostertabpanel, BoxLayout.Y_AXIS));
        
        JLabel _rosterLabel = new JLabel("These option control the Syncronisation of the JMRI Roster Database and the ECOS Database");
        _rosterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rostertabpanel.add(_rosterLabel);
        
        JPanel _locomaster = new JPanel();
        JLabel _masterLocoLabel = new JLabel("Resolve conflicts between JMRI and the ECOS");
        _masterLocoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _locomaster.add(_masterLocoLabel);
        _masterControl = new JComboBox();
        initializeMasterControlCombo(_masterControl);
        if (ep.getLocoMaster()!=0x00)
            setMasterControlType(_masterControl, ep.getLocoMaster());
        //_masterControl.setEnabled(false);
        _locomaster.add(_masterControl);
        rostertabpanel.add(_locomaster);
        
        JPanel _addlocoecospanel = new JPanel();
        JLabel _addLocosEcosLabel = new JLabel("Add Locos to the ECoS");
        _addLocoEcos = new JComboBox();
        _addlocoecospanel.add(_addLocosEcosLabel);
        initializeChoiceCombo(_addLocoEcos);
        if (ep.getAddLocoToEcos()!=0x00)
            setChoiceType(_addLocoEcos, ep.getAddLocoToEcos());
        _addlocoecospanel.add(_addLocoEcos);
        rostertabpanel.add(_addlocoecospanel);
        //_addLocosEcos  = new JCheckBox("Add Locos to the ECoS");
        //_addLocosEcos.setSelected(ep.getAddLocoToEcos());
        //_addLocosEcos.setEnabled(false);
        //rostertabpanel.add(_addLocosEcos);
        
        /*_removeLocosEcos  = new JCheckBox("Remove Locos from the ECoS");
        _removeLocosEcos.setSelected(ep.getRemoveLocoFromEcos());
        //_removeLocosEcos.setEnabled(false);
        rostertabpanel.add(_removeLocosEcos);*/

        JPanel _removelocosecospanel = new JPanel();
        JLabel _removeLocosEcosLabel = new JLabel("Remove Locos from the ECoS");
        _removeLocosEcos = new JComboBox();
        _removelocosecospanel.add(_removeLocosEcosLabel);
        initializeChoiceCombo(_removeLocosEcos);
        if (ep.getRemoveLocoFromEcos()!=0x00)
            setChoiceType(_removeLocosEcos, ep.getRemoveLocoFromEcos());
        _removelocosecospanel.add(_removeLocosEcos);
        rostertabpanel.add(_removelocosecospanel);
        
        
        /*_addLocosJmri  = new JCheckBox("Add Locos to JMRI Roster");
        _addLocosJmri.setSelected(ep.getAddLocoToJMRI());
        //_addLocosJmri.setEnabled(false);
        rostertabpanel.add(_addLocosJmri);*/
        
        JPanel _addlocosjmripanel = new JPanel();
        JLabel _addLocoJmriLabel = new JLabel("Add Locos to JMRI Roster");
        _addLocoJmri = new JComboBox();
        _addlocosjmripanel.add(_addLocoJmriLabel);
        initializeChoiceCombo(_addLocoJmri);
        if (ep.getAddLocoToJMRI()!=0x00)
            setChoiceType(_addLocoJmri, ep.getAddLocoToJMRI());
        _addlocosjmripanel.add(_addLocoJmri);
        rostertabpanel.add(_addlocosjmripanel);
        
        /*_removeLocosJmri  = new JCheckBox("Remove Locos from JMRI Roster");
        _removeLocosJmri.setSelected(ep.getRemoveLocoFromJMRI());
        _removeLocosJmri.setEnabled(false);
        rostertabpanel.add(_removeLocosJmri);*/
        
        JPanel _removelocosjmripanel = new JPanel();
        JLabel _removeLocosJmriLabel = new JLabel("Remove Locos from JMRI Roster");
        _removeLocosJmri = new JComboBox();
        _removelocosjmripanel.add(_removeLocosJmriLabel);
        initializeChoiceCombo(_removeLocosJmri);
        if (ep.getRemoveLocoFromJMRI()!=0x00)
            setChoiceType(_removeLocosJmri, ep.getRemoveLocoFromJMRI());
        _removelocosjmripanel.add(_removeLocosJmri);
        rostertabpanel.add(_removelocosjmripanel);
        
        JPanel ecosDescriptionPanel = new JPanel();
        
        JLabel _ecosDesLabel = new JLabel("Ecos Loco Description Format");
        ecosDescriptionPanel.add(_ecosDesLabel);
        
        _ecosDescription = new JTextField(20);
        _ecosDescription.setText(ep.getEcosLocoDescription());
        ecosDescriptionPanel.add(_ecosDescription);
        
        rostertabpanel.add(ecosDescriptionPanel);
        
        JLabel _descriptionformat = new JLabel("%i - Roster Id, %r - Road Name, %n - Road Number, %m - Manufacturer");
        rostertabpanel.add(_descriptionformat);
        JLabel _descriptionformat2 = new JLabel("%o - Owner, %l - Model, %c - Comment");
        rostertabpanel.add(_descriptionformat2);
    
        return rostertabpanel;
    }
    
    private JPanel throttleTab(){
    
        throttletabpanel.setLayout(new BoxLayout(throttletabpanel, BoxLayout.Y_AXIS));
        
        JLabel _throttleLabel = new JLabel("This option control what happens to a loco on the ECoS Database that has been specifically created to enable a throttle to be used");
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(_throttleLabel);
        /*_throttleLabel = new JLabel("specifically created to enable a throttle to be used");
        throttletabpanel.add(_throttleLabel);*/
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _adhocLocoEcosAsk = new JRadioButton("Always ask when quiting JMRI");
        _adhocLocoEcosLeave = new JRadioButton("Always leave the Loco in the Ecos Database");
        _adhocLocoEcosRemove = new JRadioButton("Always remove the Loco from the Ecos Database");
        switch (ep.getAdhocLocoFromEcos()){
            case 0  :   _adhocLocoEcosAsk.setSelected(true);
                        break;
            case 1  :   _adhocLocoEcosLeave.setSelected(true);
                        break;
            case 2  :   _adhocLocoEcosRemove.setSelected(true);
                        break;
            default :   _adhocLocoEcosAsk.setSelected(true);
                        break;
        }
        _adhocLocoEcos = new ButtonGroup();
        _adhocLocoEcos.add(_adhocLocoEcosAsk);
        _adhocLocoEcos.add(_adhocLocoEcosLeave);
        _adhocLocoEcos.add(_adhocLocoEcosRemove);

        JPanel adhocEcosGroup = new JPanel();
        adhocEcosGroup.setLayout(new BoxLayout(adhocEcosGroup, BoxLayout.Y_AXIS));
        adhocEcosGroup.add(_adhocLocoEcosAsk);
        adhocEcosGroup.add(_adhocLocoEcosLeave);
        adhocEcosGroup.add(_adhocLocoEcosRemove);
        adhocEcosGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(adhocEcosGroup);
        
        
        /*throttletabpanel.add(_adhocLocoEcosAsk);
        throttletabpanel.add(_adhocLocoEcosLeave);
        throttletabpanel.add(_adhocLocoEcosRemove);*/
        
        _throttleLabel = new JLabel("If JMRI can not get control of a loco, this sets how JMRI should react.");
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(_throttleLabel);
        _forceControlLocoEcosAsk = new JRadioButton("Always ask when attempting to take control of a loco");
        _forceControlLocoEcosNever = new JRadioButton("Never force control of a Loco");
        _forceControlLocoEcosAlways = new JRadioButton("Always force control of a Loco");
        switch (ep.getForceControlFromEcos()){
            case 0x00  :   _forceControlLocoEcosAsk.setSelected(true);
                        break;
            case 0x01  :   _forceControlLocoEcosNever.setSelected(true);
                        break;
            case 0x02  :   _forceControlLocoEcosAlways.setSelected(true);
                        break;
            default :   _forceControlLocoEcosAsk.setSelected(true);
                        break;
        }
        _locoEcosControl = new ButtonGroup();
        JPanel locoEcosControlGroup = new JPanel();
        locoEcosControlGroup.setLayout(new BoxLayout(locoEcosControlGroup, BoxLayout.Y_AXIS));
        locoEcosControlGroup.add(_forceControlLocoEcosAsk);
        locoEcosControlGroup.add(_forceControlLocoEcosNever);
        locoEcosControlGroup.add(_forceControlLocoEcosAlways);
        locoEcosControlGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(locoEcosControlGroup);
        
        JPanel _defaultprotocolpanel = new JPanel();

        JLabel _defaultprotocolLabel = new JLabel("Sets the Default protocol to use for an Adhoc Loco");
        _defaultprotocolpanel.add(_defaultprotocolLabel);
        _defaultProtocol = new JComboBox();
        initializeEcosProtocolCombo(_defaultProtocol);
        //if (ep.getLocoMaster()!=0x00)
        setEcosProtocolType(_defaultProtocol, ep.getDefaultEcosProtocol());
        _defaultprotocolpanel.add(_defaultProtocol);
        throttletabpanel.add(_defaultprotocolpanel);

        return throttletabpanel;
    
    }
    
    private void updateButtonPressed(){
        //EcosPreferences ep = jmri.InstanceManager.getDefault(jmri.jmrix.ecos.EcosPreferences.class);
        ep.setRemoveLocoFromJMRI(getChoiceType(_removeLocosJmri));
        ep.setAddLocoToJMRI(getChoiceType(_addLocoJmri));
        ep.setRemoveLocoFromEcos(getChoiceType(_removeLocosEcos));
        ep.setAddLocoToEcos(getChoiceType(_addLocoEcos));
        ep.setRemoveTurnoutsFromJMRI(getChoiceType(_removeTurnoutsJmri));
        ep.setAddTurnoutsToJMRI(getChoiceType(_addTurnoutsJmri));
        ep.setRemoveTurnoutsFromEcos(getChoiceType(_removeTurnoutsEcos));
        //ep.setAddTurnoutsToEcos(getChoiceType(_addTurnoutsEcos));
        ep.setLocoMaster(getMasterControlType(_masterControl));
        ep.setDefaultEcosProtocol(getEcosProtocol(_defaultProtocol));
        ep.setEcosLocoDescription(_ecosDescription.getText());
        if (_adhocLocoEcosAsk.isSelected()) ep.setAdhocLocoFromEcos(0);
        else if (_adhocLocoEcosLeave.isSelected()) ep.setAdhocLocoFromEcos(1);
        else if (_adhocLocoEcosRemove.isSelected()) ep.setAdhocLocoFromEcos(2);
        else ep.setAdhocLocoFromEcos(0);
        
        jmri.InstanceManager.configureManagerInstance().storePrefs();
    }
    
    String[] masterControlTypes = {"NOSYNC","WARNING","JMRI","ECoS"};
    int[] masterControlCode = {0x00,0x01,0x02,0x03};
    int numTypes = 4;  // number of entries in the above arrays
    
    private void initializeMasterControlCombo(JComboBox masterCombo) {
		masterCombo.removeAllItems();
		for (int i = 0;i<numTypes;i++) {
			masterCombo.addItem(masterControlTypes[i]);
		}
	}
    private void setMasterControlType(JComboBox masterBox, int master){
        for (int i = 0;i<numTypes;i++) {
			if (master==masterControlCode[i]) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }
    
    private int getMasterControlType(JComboBox masterBox){
        return masterControlCode[masterBox.getSelectedIndex()];
    
    }
    
    String[] choiceTypes = {"Always Ask","No","Yes"};
    int[] masterChoiceCode = {0x00,0x01,0x02};
    int numChoiceTypes = 3;  // number of entries in the above arrays
    
    private void initializeChoiceCombo(JComboBox masterCombo) {
		masterCombo.removeAllItems();
		for (int i = 0;i<numChoiceTypes;i++) {
			masterCombo.addItem(choiceTypes[i]);
		}
	}
    private void setChoiceType(JComboBox masterBox, int master){
        for (int i = 0;i<numChoiceTypes;i++) {
			if (master==masterChoiceCode[i]) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }
    
    private int getChoiceType(JComboBox masterBox){
        return masterChoiceCode[masterBox.getSelectedIndex()];
    
    }


    String[] ecosProtocolTypes = {"DCC14","DCC28", "DCC128", "MM14", "MM27", "MM28", "SX32", "MMFKT"};
    int numProtocolTypes = 8;  // number of entries in the above arrays

    private void initializeEcosProtocolCombo(JComboBox protocolCombo) {
		protocolCombo.removeAllItems();
		for (int i = 0;i<numProtocolTypes;i++) {
			protocolCombo.addItem(ecosProtocolTypes[i]);
		}
	}
    private void setEcosProtocolType(JComboBox masterBox, String protocol){
        for (int i = 0;i<numProtocolTypes;i++) {
			if (protocol.equals(ecosProtocolTypes[i])) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }

    private String getEcosProtocol(JComboBox masterBox){
        return ecosProtocolTypes[masterBox.getSelectedIndex()];

    }
}


/* @(#)PreferencesPane.java */
