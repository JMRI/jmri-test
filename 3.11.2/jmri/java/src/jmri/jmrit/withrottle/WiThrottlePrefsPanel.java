package jmri.jmrit.withrottle;

/**
 * @author Brett Hoffman Copyright (C) 2010
 * @version $Revision$
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;

public class WiThrottlePrefsPanel extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = -5008747256799742063L;
    JCheckBox eStopCB;
    JSpinner delaySpinner;

    JCheckBox momF2CB;

    JCheckBox portCB;
    JTextField port;

    JCheckBox powerCB;
    JCheckBox turnoutCB;
    JCheckBox routeCB;
    JCheckBox consistCB;
    JRadioButton wifiRB;
    JRadioButton dccRB;

    WiThrottlePreferences localPrefs;
    JFrame parentFrame = null;

    public WiThrottlePrefsPanel() {
        if (InstanceManager.getDefault(WiThrottlePreferences.class) == null) {
            InstanceManager.store(new WiThrottlePreferences(FileUtil.getUserFilesPath() + "throttle" + File.separator + "WiThrottlePreferences.xml"), WiThrottlePreferences.class);
        }
        localPrefs = InstanceManager.getDefault(WiThrottlePreferences.class);
        initGUI();
        setGUI();
    }

    public WiThrottlePrefsPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    public void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(eStopDelayPanel());
        add(functionsPanel());
        add(socketPortPanel());
        add(allowedControllers());
    }

    private void setGUI() {
        eStopCB.setSelected(localPrefs.isUseEStop());
        delaySpinner.setValue(localPrefs.getEStopDelay());

        momF2CB.setSelected(localPrefs.isUseMomF2());

        portCB.setSelected(localPrefs.isUseFixedPort());
        updatePortField();

        powerCB.setSelected(localPrefs.isAllowTrackPower());
        turnoutCB.setSelected(localPrefs.isAllowTurnout());
        routeCB.setSelected(localPrefs.isAllowRoute());
        consistCB.setSelected(localPrefs.isAllowConsist());
        wifiRB.setSelected(localPrefs.isUseWiFiConsist());
        dccRB.setSelected(!localPrefs.isUseWiFiConsist());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independant from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        localPrefs.setUseEStop(eStopCB.isSelected());
        localPrefs.setEStopDelay((Integer) delaySpinner.getValue());

        localPrefs.setUseMomF2(momF2CB.isSelected());

        localPrefs.setUseFixedPort(portCB.isSelected());
        if (portCB.isSelected()) {
            int portNum;
            try {
                portNum = Integer.parseInt(port.getText());
            } catch (NumberFormatException NFE) { //  Not a number
                portNum = 0;
            }
            if ((portNum < 1024) || (portNum > 65535)) { //  Invalid port value
                javax.swing.JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("WarningInvalidPort"),
                        Bundle.getMessage("TitlePortWarningDialog"),
                        JOptionPane.WARNING_MESSAGE);
                didSet = false;
            } else {
                localPrefs.setPort(port.getText());
            }
        }

        localPrefs.setAllowTrackPower(powerCB.isSelected());
        localPrefs.setAllowTurnout(turnoutCB.isSelected());
        localPrefs.setAllowRoute(routeCB.isSelected());
        localPrefs.setAllowConsist(consistCB.isSelected());
        localPrefs.setUseWiFiConsist(wifiRB.isSelected());

        return didSet;
    }

    public void storeValues() {
        if (setValues()) {
            this.localPrefs.save();

            if (parentFrame != null) {
                parentFrame.dispose();
            }
        }

    }

    protected void cancelValues() {
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    private JPanel eStopDelayPanel() {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("TitleDelayPanel")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        eStopCB = new JCheckBox(Bundle.getMessage("LabelUseEStop"));
        eStopCB.setToolTipText(Bundle.getMessage("ToolTipUseEStop"));
        SpinnerNumberModel spinMod = new SpinnerNumberModel(10, 4, 60, 2);
        delaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) delaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(eStopCB);
        panel.add(delaySpinner);
        panel.add(new JLabel(Bundle.getMessage("LabelEStopDelay")));
        return panel;
    }

    private JPanel functionsPanel() {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("TitleFunctionsPanel")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        momF2CB = new JCheckBox(Bundle.getMessage("LabelMomF2"));
        momF2CB.setToolTipText(Bundle.getMessage("ToolTipMomF2"));
        panel.add(momF2CB);
        return panel;
    }

    private JPanel socketPortPanel() {
        JPanel SPPanel = new JPanel();

        SPPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("TitleNetworkPanel")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        portCB = new JCheckBox(Bundle.getMessage("LabelUseFixedPortNumber"));
        portCB.setToolTipText(Bundle.getMessage("ToolTipUseFixedPortNumber"));
        portCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updatePortField();
            }
        });
        port = new JTextField();
        port.setText(Bundle.getMessage("LabelNotFixed"));
        port.setPreferredSize(port.getPreferredSize());
        SPPanel.add(portCB);
        SPPanel.add(port);
        return SPPanel;
    }

    private JPanel allowedControllers() {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("TitleControllersPanel")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        powerCB = new JCheckBox(Bundle.getMessage("LabelTrackPower"));
        powerCB.setToolTipText(Bundle.getMessage("ToolTipTrackPower"));
        panel.add(powerCB);

        turnoutCB = new JCheckBox(Bundle.getMessage("LabelTurnout"));
        turnoutCB.setToolTipText(Bundle.getMessage("ToolTipTurnout"));
        panel.add(turnoutCB);

        routeCB = new JCheckBox(Bundle.getMessage("LabelRoute"));
        routeCB.setToolTipText(Bundle.getMessage("ToolTipRoute"));
        panel.add(routeCB);

        consistCB = new JCheckBox(Bundle.getMessage("LabelConsist"));
        consistCB.setToolTipText(Bundle.getMessage("ToolTipConsist"));
        panel.add(consistCB);

        JPanel conPanel = new JPanel();
        conPanel.setLayout(new BoxLayout(conPanel, BoxLayout.Y_AXIS));
        wifiRB = new JRadioButton(Bundle.getMessage("LabelWiFiConsist"));
        wifiRB.setToolTipText(Bundle.getMessage("ToolTipWiFiConsist"));
        dccRB = new JRadioButton(Bundle.getMessage("LabelDCCConsist"));
        dccRB.setToolTipText(Bundle.getMessage("ToolTipDCCConsist"));

        ButtonGroup group = new ButtonGroup();
        group.add(wifiRB);
        group.add(dccRB);
        conPanel.add(wifiRB);
        conPanel.add(dccRB);
        panel.add(conPanel);

        return panel;
    }

    private void updatePortField() {
        if (portCB.isSelected()) {
            port.setText(localPrefs.getPort());

        } else {
            port.setText(Bundle.getMessage("LabelNotFixed"));
        }

    }

    //private static Logger log = LoggerFactory.getLogger(WiThrottlePrefsPanel.class.getName());
    @Override
    public String getPreferencesItem() {
        return "WITHROTTLE"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        // TODO: migrate to local resource bundle
        return ResourceBundle.getBundle("apps.AppsConfigBundle").getString("MenuWiThrottle"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        this.storeValues();
    }

    @Override
    public boolean isDirty() {
        return this.localPrefs.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return this.localPrefs.isRestartRequired();
    }
}
