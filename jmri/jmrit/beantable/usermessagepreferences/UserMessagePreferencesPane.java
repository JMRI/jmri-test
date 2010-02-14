// UserMessagePreferencesPane.java

package jmri.jmrit.beantable.usermessagepreferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Pane to show User Message Preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 1.6 $
 */
public class UserMessagePreferencesPane extends jmri.util.swing.JmriPanel {

    jmri.UserPreferencesManager p;
    
    public UserMessagePreferencesPane() {
        super();
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
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
        tab.add(routeTab(), "Routes");
        tab.add(logixTab(), "Logixs");
        tab.add(lRouteTab(), "LRoutes");
        tab.add(applicationTab(), "Application");
        tab.add(tableDeleteTab(), "Deleting Table Entries");
        add(tab);
        add(buttonPanel);
    }

    JCheckBox _routeSaveMsg;
    JComboBox _warnRouteInUse;
    private JPanel routeTab(){
        JPanel routeTabPanel = new JPanel();
        routeTabPanel.setLayout(new BoxLayout(routeTabPanel, BoxLayout.Y_AXIS));
        
        _routeSaveMsg  = new JCheckBox("Always Display Save Message Reminder");
        _routeSaveMsg.setSelected(!p.getPreferenceState("beantable.RouteTableAction.remindRoute"));
        routeTabPanel.add(_routeSaveMsg);
        
        routeTabPanel.add(addQuestionButton(_warnRouteInUse = new JComboBox(), "When Deleting a Route", p.getWarnDeleteRoute()));
        
        return routeTabPanel;
    }
    
    JComboBox _warnLRouteInUse;
    JCheckBox _lRouteSaveMsg;
    private JPanel lRouteTab(){
        JPanel lRouteTabPanel = new JPanel();
        lRouteTabPanel.setLayout(new BoxLayout(lRouteTabPanel, BoxLayout.Y_AXIS));
        
        _lRouteSaveMsg  = new JCheckBox("Always Display Save Message Reminder");
        _lRouteSaveMsg.setSelected(!p.getPreferenceState("beantable.LRouteTableAction.remindRoute"));
        lRouteTabPanel.add(_lRouteSaveMsg);
        
        lRouteTabPanel.add(addQuestionButton(_warnLRouteInUse = new JComboBox(), "When Deleting a LRoute", p.getWarnLRouteInUse()));
        
        return lRouteTabPanel;
    }
    
    JCheckBox _logixSaveMsg;
    JComboBox _warnLogixInUse;
    JComboBox _warnDeleteLogix;
    private JPanel logixTab(){
        JPanel logixTabPanel = new JPanel();
        logixTabPanel.setLayout(new BoxLayout(logixTabPanel, BoxLayout.Y_AXIS));
        
        _logixSaveMsg  = new JCheckBox("Always Display Save Message Reminder");
        _logixSaveMsg.setSelected(!p.getPreferenceState("beantable.LogixTableAction.remindRoute"));
        logixTabPanel.add(_logixSaveMsg);
        
        logixTabPanel.add(addQuestionButton(_warnDeleteLogix = new JComboBox(), "When Deleting a Logix", p.getWarnDeleteLogix()));
        logixTabPanel.add(addQuestionButton(_warnLogixInUse = new JComboBox(), "When Deleting a Logix that is in use", p.getWarnLogixInUse()));
        
        return logixTabPanel;
    }
    
    JComboBox _quitAfterSave;
    
    private JPanel applicationTab(){
        //applicationtabpanel = new JPanel(new BoxLayout());
        JPanel applicationTabPanel = new JPanel();
        applicationTabPanel.setLayout(new BoxLayout(applicationTabPanel, BoxLayout.Y_AXIS));
        
        applicationTabPanel.add(addQuestionButton(_quitAfterSave = new JComboBox(), "Quit after Saving Preferences", p.getQuitAfterSave()));
        
        return applicationTabPanel;
    }
    
    JComboBox _warnTurnoutInUse;
    JComboBox _warnAudioInUse;
    JComboBox _warnBlockInUse;
    JComboBox _warnLightInUse;
    JComboBox _warnSectionInUse;
    JComboBox _warnMemoryInUse;
    JComboBox _warnReporterInUse;
    JComboBox _warnSensorInUse;
    JComboBox _warnSignalMastInUse;
    JComboBox _warnSignalHeadInUse;
    JComboBox _warnTransitInUse;
    
    private JPanel tableDeleteTab(){
        //applicationtabpanel = new JPanel(new BoxLayout());
        JPanel tableDeleteTabPanel = new JPanel();
        tableDeleteTabPanel.setLayout(new BoxLayout(tableDeleteTabPanel, BoxLayout.Y_AXIS));

        JLabel tableDeleteInfoLabel = new JLabel("These options determine whether you are prompted before deleting an item,", JLabel.CENTER);
        tableDeleteInfoLabel.setAlignmentX(0.5f);
        tableDeleteTabPanel.add(tableDeleteInfoLabel);

        tableDeleteInfoLabel = new JLabel("or if not, what action should be taken.", JLabel.CENTER);
        tableDeleteInfoLabel.setAlignmentX(0.5f);
        tableDeleteTabPanel.add(tableDeleteInfoLabel);

        tableDeleteInfoLabel = new JLabel("Note: if No is selected in any of these items then the item will never be deleted!");
        tableDeleteInfoLabel.setFont(tableDeleteInfoLabel.getFont().deriveFont(10f));
        tableDeleteInfoLabel.setAlignmentX(0.5f);
        tableDeleteTabPanel.add(tableDeleteInfoLabel);

        JPanel inside = new JPanel();
        inside.setLayout(new jmri.util.javaworld.GridLayout2(6,2*2));
        
        addQuestionComboBoxToGrid(inside, _warnAudioInUse = new JComboBox(), "Audio", p.getWarnAudioInUse());
        addQuestionComboBoxToGrid(inside, _warnBlockInUse = new JComboBox(), "Block", p.getWarnBlockInUse());
        //addQuestionComboBoxToGrid(inside, _warnLRouteInUse = new JComboBox(), "LRoute", p.getWarnLRouteInUse());
        addQuestionComboBoxToGrid(inside, _warnLightInUse = new JComboBox(), "Light", p.getWarnLightInUse());    
        addQuestionComboBoxToGrid(inside, _warnSectionInUse = new JComboBox(), "Section", p.getWarnSectionInUse());
        addQuestionComboBoxToGrid(inside, _warnMemoryInUse = new JComboBox(), "Memory", p.getWarnMemoryInUse());
        addQuestionComboBoxToGrid(inside, _warnReporterInUse = new JComboBox(), "Reporter", p.getWarnReporterInUse());
        addQuestionComboBoxToGrid(inside, _warnSensorInUse = new JComboBox(), "Sensor", p.getWarnSensorInUse());
        addQuestionComboBoxToGrid(inside, _warnSignalMastInUse = new JComboBox(), "SignalMast", p.getWarnSignalMastInUse());
        addQuestionComboBoxToGrid(inside, _warnSignalHeadInUse = new JComboBox(), "SignalHead", p.getWarnSignalHeadInUse());
        addQuestionComboBoxToGrid(inside, _warnTransitInUse = new JComboBox(), "Transit", p.getWarnTransitInUse());
        addQuestionComboBoxToGrid(inside, _warnTurnoutInUse = new JComboBox(), "Turnout", p.getWarnTurnoutInUse());
        
        tableDeleteTabPanel.add(inside);
        
        return tableDeleteTabPanel;
    }
    
    
    private void updateButtonPressed(){
        p.setLoading();
        
        //From Route Tab
        p.setPreferenceState("beantable.RouteTableAction.remindRoute", !_routeSaveMsg.isSelected());
        p.setWarnDeleteRoute(getChoiceType(_warnRouteInUse));
        
        //From logix Tab
        p.setPreferenceState("beantable.LogixTableAction.remindLogix", !_logixSaveMsg.isSelected());
        p.setWarnDeleteLogix(getChoiceType(_warnDeleteLogix));
        p.setWarnLogixInUse(getChoiceType(_warnLogixInUse));
        
        //From Application Tab
        p.setQuitAfterSave(getChoiceType(_quitAfterSave));
        
        //From LRoute Tab
        p.setWarnLRouteInUse(getChoiceType(_warnLRouteInUse));
        p.setPreferenceState("beantable.LogixTableAction.remindLogix", !_lRouteSaveMsg.isSelected());
        
        //From table DeleteTab
        p.setWarnTurnoutInUse(getChoiceType(_warnTurnoutInUse));
        p.setWarnAudioInUse(getChoiceType(_warnAudioInUse));
        p.setWarnBlockInUse(getChoiceType(_warnBlockInUse));
        p.setWarnLightInUse(getChoiceType(_warnLightInUse));
        p.setWarnSectionInUse(getChoiceType(_warnSectionInUse));
        p.setWarnMemoryInUse(getChoiceType(_warnMemoryInUse));
        p.setWarnReporterInUse(getChoiceType(_warnReporterInUse));
        p.setWarnSensorInUse(getChoiceType(_warnSensorInUse));
        p.setWarnSignalMastInUse(getChoiceType(_warnSignalMastInUse));
        p.setWarnSignalHeadInUse(getChoiceType(_warnSignalHeadInUse));
        p.setWarnTransitInUse(getChoiceType(_warnTransitInUse));

        jmri.InstanceManager.configureManagerInstance().storePrefs();
        p.finishLoading();
    }
    
    private void addQuestionComboBoxToGrid(JPanel p, JComboBox _combo, String label, int value){
        JLabel _comboLabel = new JLabel(label, JLabel.RIGHT);
        p.add(_comboLabel);
        
        initializeChoiceCombo(_combo);
        if (value!=0x00)
            setChoiceType(_combo, value);
        p.add(_combo);
    }

    private JPanel addQuestionButton(JComboBox _combo, String label, int value){
        JPanel _comboPanel = new JPanel();
        JLabel _comboLabel = new JLabel(label);
        
        _comboPanel.add(_comboLabel);
        _comboPanel.add(_combo);
        initializeChoiceCombo(_combo);
        if (value!=0x00)
            setChoiceType(_combo, value);
        
        return _comboPanel;
    
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
}


/* @(#)UserMessagePreferencesPane.java */
