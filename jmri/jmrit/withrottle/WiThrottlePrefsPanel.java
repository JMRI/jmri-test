package jmri.jmrit.withrottle;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.5 $
 */

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class WiThrottlePrefsPanel extends JPanel{
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");
    
    Border lineBorder;

    JCheckBox eStopCB;
    JSpinner delaySpinner;

    JCheckBox portCB;
    JTextField port;

    JCheckBox powerCB;
    JCheckBox turnoutCB;
    JCheckBox routeCB;
    JCheckBox consistCB;

    JButton saveB;
    JButton cancelB;

    WiThrottlePreferences localPrefs = new WiThrottlePreferences();
    JFrame parentFrame = null;
    boolean enableSave;

    public WiThrottlePrefsPanel(){
        //  set local prefs to match instance prefs
        localPrefs.apply(WiThrottleManager.withrottlePreferencesInstance());
        initGUI();
        setGUI();
    }

    public WiThrottlePrefsPanel(JFrame f){
        this();
        parentFrame = f;
    }

    public void initGUI(){
        lineBorder = BorderFactory.createLineBorder(Color.black);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(eStopDelayPanel());
        add(socketPortPanel());
        add(allowedControllers());
        add(cancelApplySave());

    }

    private void setGUI(){
        eStopCB.setSelected(localPrefs.isUseEStop());
        delaySpinner.setValue(localPrefs.getEStopDelay());

        portCB.setSelected(localPrefs.isUseFixedPort());
        updatePortField();

        powerCB.setSelected(localPrefs.isAllowTrackPower());
        turnoutCB.setSelected(localPrefs.isAllowTurnout());
        routeCB.setSelected(localPrefs.isAllowRoute());
        consistCB.setSelected(localPrefs.isAllowConsist());
    }

/**
 * Show the save and cancel buttons if displayed in its own frame.
 */
    public void enableSave(){
        saveB.setVisible(true);
        cancelB.setVisible(true);
    }
/**
 * set the local prefs to match the GUI
 * Local prefs are independant from the singleton instance prefs.
 * @return true if set, false if values are unacceptable.
 */
    private boolean setValues(){
        boolean didSet = true;
        localPrefs.setUseEStop(eStopCB.isSelected());
        localPrefs.setEStopDelay((Integer)delaySpinner.getValue());

        localPrefs.setUseFixedPort(portCB.isSelected());
        if (portCB.isSelected()){
            int portNum;
            try{
                portNum = Integer.parseInt(port.getText());
            }catch (NumberFormatException NFE){ //  Not a number
                portNum = 0;
            }
            if ((portNum < 1024) || (portNum > 65535)){ //  Invalid port value
                javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("WarningInvalidPort"),
                    rb.getString("TitlePortWarningDialog"),
                    JOptionPane.WARNING_MESSAGE);
                didSet = false;
            }else {
                localPrefs.setPort(port.getText());
            }
        }
        

        localPrefs.setAllowTrackPower(powerCB.isSelected());
        localPrefs.setAllowTurnout(turnoutCB.isSelected());
        localPrefs.setAllowRoute(routeCB.isSelected());
        localPrefs.setAllowConsist(consistCB.isSelected());

        return didSet;
    }

    public void storeValues() {
        if (setValues()){
            WiThrottleManager.withrottlePreferencesInstance().apply(localPrefs);
            WiThrottleManager.withrottlePreferencesInstance().save();
            
            if (parentFrame != null){
                parentFrame.dispose();
            }
        }

        
    }
/**
 * Update the singleton instance of prefs,
 * then mark (isDirty) that the
 * values have changed and needs to save to xml file.
 */
    protected void applyValues(){
        if (setValues()){
            WiThrottleManager.withrottlePreferencesInstance().apply(localPrefs);
            WiThrottleManager.withrottlePreferencesInstance().setIsDirty(true); //  mark to save later
        }
    }

    protected void cancelValues(){
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }


    private JPanel eStopDelayPanel(){
        JPanel panel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitleDelayPanel"), TitledBorder.CENTER, TitledBorder.TOP);

        panel.setBorder(border);
        eStopCB = new JCheckBox(rb.getString("LabelUseEStop"));
        eStopCB.setToolTipText(rb.getString("ToolTipUseEStop"));
        SpinnerNumberModel spinMod = new SpinnerNumberModel(10,4,30,2);
        delaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor)delaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(eStopCB);
        panel.add(delaySpinner);
        panel.add(new JLabel(rb.getString("LabelEStopDelay")));
        return panel;
    }

    private JPanel socketPortPanel(){
        JPanel SPPanel = new JPanel();
        TitledBorder networkBorder = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitleNetworkPanel"), TitledBorder.CENTER, TitledBorder.TOP);

        SPPanel.setBorder(networkBorder);
        portCB = new JCheckBox(rb.getString("LabelUseFixedPortNumber"));
        portCB.setToolTipText(rb.getString("ToolTipUseFixedPortNumber"));
        portCB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                updatePortField();
            }
        });
        port = new JTextField();
        port.setText(rb.getString("LabelNotFixed"));
        port.setPreferredSize(port.getPreferredSize());
        SPPanel.add(portCB);
        SPPanel.add(port);
        return SPPanel;
    }

    private JPanel allowedControllers(){
        JPanel panel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitleControllersPanel"), TitledBorder.CENTER, TitledBorder.TOP);

        panel.setBorder(border);
        powerCB = new JCheckBox(rb.getString("LabelTrackPower"));
        powerCB.setToolTipText(rb.getString("ToolTipTrackPower"));
        panel.add(powerCB);

        turnoutCB = new JCheckBox(rb.getString("LabelTurnout"));
        turnoutCB.setToolTipText(rb.getString("ToolTipTurnout"));
        panel.add(turnoutCB);

        routeCB = new JCheckBox(rb.getString("LabelRoute"));
        routeCB.setToolTipText(rb.getString("ToolTipRoute"));
        panel.add(routeCB);

        consistCB = new JCheckBox(rb.getString("LabelConsist"));
        consistCB.setToolTipText(rb.getString("ToolTipConsist"));
        panel.add(consistCB);

        return panel;
    }
    
    private JPanel cancelApplySave(){
        JPanel panel = new JPanel();
        cancelB = new JButton(rb.getString("ButtonCancel"));
        cancelB.setVisible(false);
        cancelB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                cancelValues();
            }
        });
        JButton applyB = new JButton(rb.getString("ButtonApply"));
        applyB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                applyValues();
            }
        });
        saveB = new JButton(rb.getString("ButtonSave"));
        saveB.setVisible(false);
        saveB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                storeValues();
            }
        });
        panel.add(cancelB);
        panel.add(saveB);
        panel.add(new JLabel(rb.getString("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
    }

    private void updatePortField(){
        if (portCB.isSelected()){
            port.setText(localPrefs.getPort());

        }else {
            port.setText(rb.getString("LabelNotFixed"));
        }

    }

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottlePrefsPanel.class.getName());

}
