package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.ThrottleListener;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class SpeedProfilePanel extends jmri.util.swing.JmriPanel implements ThrottleListener{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7592615396122420235L;
	JButton profileButton = new JButton(Bundle.getMessage("ButtonProfile"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton testButton = new JButton(Bundle.getMessage("ButtonTest"));
    JTextField lengthField = new JTextField(10);
    JTextField speedStepFrom = new JTextField(10);
    BeanSelectCreatePanel sensorAPanel = new  BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);
    BeanSelectCreatePanel sensorBPanel = new  BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);
    BeanSelectCreatePanel blockCPanel = new  BeanSelectCreatePanel(InstanceManager.blockManagerInstance(), null);
    BeanSelectCreatePanel sensorCPanel = new  BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);
    RosterEntryComboBox reBox = new RosterEntryComboBox();
    boolean profile = false;
    boolean test = false;
    
    JLabel sourceLabel = new JLabel();
    
    public SpeedProfilePanel(){
        setLayout(new BorderLayout());
        JPanel main = new JPanel();
        
        main.setLayout(new GridLayout(0,2));
        
        main.add(new JLabel(Bundle.getMessage("LabelLengthOfBlock")));
        main.add(lengthField);
        lengthField.setText("0");
        main.add(new JLabel(Bundle.getMessage("LabelStartSensor")));
        main.add(sensorAPanel);
        main.add(new JLabel(Bundle.getMessage("LabelBlockSensor"))); //was optional
        main.add(sensorCPanel);
        //main.add(blockCPanel);
        main.add(new JLabel(Bundle.getMessage("LabelFinishSensor")));
        main.add(sensorBPanel);
        
        //main.add(new JLabel("Speed Steps"));
        //speedStepsCombo = new JComboBox(InstanceManager.throttleManagerInstance()
        
        main.add(new JLabel(Bundle.getMessage("LabelSelectRoster")));
        main.add(reBox);
        main.add(new JLabel(""));
        main.add(new JLabel(""));
        main.add(cancelButton);
        
        main.add(profileButton);
        
        main.add(speedStepFrom);
        main.add(testButton);
        
        add(main, BorderLayout.CENTER);
        
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        sourceLabel = new JLabel("   ");
        panel1.add(sourceLabel, BorderLayout.SOUTH);
        
        add(panel1, BorderLayout.SOUTH);
            
        profileButton.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent e){
                profile = true;
                setupProfile();
            }
        });
        cancelButton.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent e){
                cancelButton();
            }
        });
        testButton.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent e){
                test = true;
                testButton();
            }
        });
        setButtonStates(true);
    }
    
    SensorDetails sensorA;
    SensorDetails sensorB;
    RosterEntry re;
    DccThrottle t;
    int finishSpeedStep;
    
    protected int profileStep = 1;
    protected float profileSpeed;
    protected float profileIncrement;
    RosterSpeedProfile rosterSpeedProfile;
    
    void setupProfile(){
        try {
            Integer.parseInt(lengthField.getText());
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorLengthInvalid"));
            return;
        }
        setButtonStates(false);
        if(sensorA==null){
            try{
                sensorA = new SensorDetails((Sensor)sensorAPanel.getNamedBean());
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Start"));
                setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor)sensorAPanel.getNamedBean();
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Start"));
                setButtonStates(true);
                return;
            }
            if(tmpSen!=sensorA.getSensor()){
                sensorA.resetDetails();
                sensorA = new SensorDetails(tmpSen);
            }
        }
        if(sensorB==null){
            try{
                sensorB = new SensorDetails((Sensor)sensorBPanel.getNamedBean());
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Finish"));
                setButtonStates(true);
                return;
            }
        
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor)sensorBPanel.getNamedBean();
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Finish"));
                setButtonStates(true);
                return;
            }
            if(tmpSen!=sensorB.getSensor()){
                sensorB.resetDetails();
                sensorB = new SensorDetails(tmpSen);
            }
        }
        if(middleBlockSensor==null){
            try {
                middleBlockSensor = new SensorDetails((Sensor)sensorCPanel.getNamedBean());
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Block"));
                 setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor)sensorCPanel.getNamedBean();
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Block"));
                setButtonStates(true);
                return;
            }
            if(tmpSen!=middleBlockSensor.getSensor()){
                middleBlockSensor.resetDetails();
                middleBlockSensor = new SensorDetails(tmpSen);
            }
        }
        if(reBox.getSelectedRosterEntries().length==0){
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoRosterSelected"));
			log.warn("No roster Entry selected.");
            setButtonStates(true);
            return;
        }
        re = reBox.getSelectedRosterEntries()[0];
        boolean ok = InstanceManager.throttleManagerInstance().requestThrottle(re,this);
        if (!ok) {
            log.warn("Throttle for locomotive "+re.getId()+" could not be setup.");
            setButtonStates(true);
            return;
        }
    }
    
    javax.swing.Timer overRunTimer = null;
    
    public void notifyThrottleFound(DccThrottle _throttle) {
		t = _throttle;
		if (t==null) {
			JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorThrottleNotFound"));
			log.warn("null throttle returned for train  "+re.getId()+" during automatic initialization.");
            setButtonStates(true);
			return;
		}
        if (log.isDebugEnabled()) log.debug("throttle address= " +t.getLocoAddress().toString());
        
        int speedStepMode = t.getSpeedStepMode();
        profileIncrement = t.getSpeedIncrement();
        if(speedStepMode==DccThrottle.SpeedStepMode14){
            finishSpeedStep = 14;
        } else if(speedStepMode==DccThrottle.SpeedStepMode27) {
            finishSpeedStep = 27;
    	} else if(speedStepMode==DccThrottle.SpeedStepMode28) {
            finishSpeedStep = 28;
    	} else {// default to 128 speed step mode
            finishSpeedStep = 126;
        }
        
        log.debug("Speed step mode " + speedStepMode);
        profileStep = 1;
        profileSpeed = profileIncrement;
        
        if(profile){
            startSensor = middleBlockSensor.getSensor();
            finishSensor = sensorB.getSensor();
            startListener = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent e) {
                    if(e.getPropertyName().equals("KnownState")){
                        if(((Integer)e.getNewValue())==Sensor.ACTIVE){
                            startTiming();
                        }
                        if(((Integer)e.getNewValue())==Sensor.INACTIVE){
                            stopLoco();
                        }
                    }
                }
            };
            finishListener = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent e) {
                    if(e.getPropertyName().equals("KnownState")){
                        if(((Integer)e.getNewValue())==Sensor.ACTIVE){
                            stopCurrentSpeedStep();
                        }
                    }
                }
            };
            
            
            isForward = true;
            startProfile();
        } else {
            if(re.getSpeedProfile()==null){
                log.error("Loco has no speed profile");
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSpeedProfile"));
                setButtonStates(true);
                return;
            }
            startSensor = middleBlockSensor.getSensor();
            startListener = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent e) {
                    if(e.getPropertyName().equals("KnownState")){
                        if(((Integer)e.getNewValue())==Sensor.ACTIVE){
                            stopTrainTest();
                        }
                    }
                }
            };
            startSensor.addPropertyChangeListener(startListener);
            int startstep = Integer.parseInt(speedStepFrom.getText());
            isForward = true;
            t.setIsForward(isForward);
            profileSpeed = profileIncrement*startstep;
            t.setSpeedSetting(profileSpeed);
        }
	}
    
    void setButtonStates(boolean state){
        cancelButton.setEnabled(!state);
        profileButton.setEnabled(state);
        testButton.setEnabled(state);
        if(state){
            sourceLabel.setText("   ");
            profile = false;
            test = false;
        }
        if(sensorA!=null)
            sensorA.resetDetails();
        if(sensorB!=null)
            sensorB.resetDetails();
        if(middleBlockSensor!=null)
            middleBlockSensor.resetDetails();
    }

	public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason) {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorFailThrottleRequest"));
		log.error ("Throttle request failed for "+address+" because "+reason);
        setButtonStates(true);
	}
    
    PropertyChangeListener startListener = null;
    PropertyChangeListener finishListener = null;
    PropertyChangeListener middleListener = null;
    
    Sensor startSensor;
    Sensor finishSensor;
    SensorDetails middleBlockSensor;
    
    void startProfile(){
        stepCalculated = false;
        sourceLabel.setText(Bundle.getMessage("StatusLabelNextRun"));
        if(isForward){
            finishSensor = sensorB.getSensor();
        } else {
            finishSensor = sensorA.getSensor();
        }
        startSensor = middleBlockSensor.getSensor();
        startSensor.addPropertyChangeListener(startListener);
        finishSensor.addPropertyChangeListener(finishListener);
        t.setIsForward(isForward);
        log.debug("Set speed to " + profileSpeed + " isForward " + isForward);
        t.setSpeedSetting(profileSpeed);
        sourceLabel.setText(Bundle.getMessage("StatusLabelBlockToGoActive"));
    }
    
    boolean isForward = true;
    
    void startTiming(){
        startTime = System.nanoTime();
        sourceLabel.setText(Bundle.getMessage("StatusLabelCurrentRun", (isForward?"(forward) ":"(reverse) "), profileStep, finishSpeedStep));
    }
    boolean stepCalculated = false;
    void stopCurrentSpeedStep(){
        finishTime = System.nanoTime();
        stepCalculated = true;
        finishSensor.removePropertyChangeListener(finishListener);
        sourceLabel.setText(Bundle.getMessage("StatusLabelCalculating"));
        if(profileStep>=4)
            t.setSpeedSetting(profileSpeed/2);
        calculateSpeed();
        sourceLabel.setText(Bundle.getMessage("StatusLabelWaitingToClear"));
    }
    

    void stopLoco() {
        
        if(!stepCalculated){
            return;
        }
        
        startSensor.removePropertyChangeListener(startListener);
        finishSensor.removePropertyChangeListener(finishListener);
        
        isForward = !isForward;
        if(isForward){
            profileSpeed =profileIncrement+profileSpeed;
            profileStep ++;
        }
        
        if(profileStep>finishSpeedStep){
            updateSpeedProfileWithResults();
            setButtonStates(true);
            re.updateFile();
            Roster.writeRosterFile();
            t.setSpeedSetting(0.0f);
            return;
        }
        //Don't bring the loco to an abrupt halt, but bring down to half speed then stop.
        javax.swing.Timer stopTimer = new javax.swing.Timer(2500, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                
                t.setSpeedSetting(0.0f);
                
                startProfile();
            }
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
    }
    
    void calculateSpeed(){
        float duration = (((float)(finishTime-startTime))/1000000000); //Now in seconds
        duration = duration - 2f;  // Abratory time to add in feedback delay
        float length = (Integer.parseInt(lengthField.getText())); //left as mm
        float speed = length/duration;
        if (log.isDebugEnabled()) log.debug("Step:" + profileStep + " duration:" + duration + " length:" + length + " speed:" + speed);

        int iSpeedStep = Math.round(profileSpeed*1000);
        if(!speeds.containsKey(iSpeedStep)){
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);
        
        if(isForward){
            ss.setForwardSpeed(speed);
        } else {
            ss.setReverseSpeed(speed);
        }
    }
    
    void updateSpeedProfileWithResults(){
        RosterSpeedProfile rosterSpeedProfile = re.getSpeedProfile();
        if(rosterSpeedProfile==null){
            rosterSpeedProfile = new RosterSpeedProfile(re);
            re.setSpeedProfile(rosterSpeedProfile);
        } else {
            rosterSpeedProfile.clearCurrentProfile();
        }
        for(Integer i : speeds.keySet()){
            rosterSpeedProfile.setSpeed(i, speeds.get(i).getForwardSpeed(), speeds.get(i).getReverseSpeed());
        }
    }
    
    void cancelButton(){
        if(t!=null)
            t.setSpeedSetting(0.0f);
        if(startSensor!=null)
            startSensor.removePropertyChangeListener(startListener);
        if(finishSensor!=null)
            finishSensor.removePropertyChangeListener(finishListener);
        if(middleListener!=null){
            middleBlockSensor.getSensor().removePropertyChangeListener(middleListener);
        }
        setButtonStates(true);
    }
    
    void testButton(){
        //Should also test that the step is no greater than those avaialble on the throttle.
        try{ 
            Integer.parseInt(speedStepFrom.getText());
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep"));
            return;
        }
        setupProfile();
        
    }
    
    void stopTrainTest(){
        //int locolength = Integer.parseInt(locoLengthField.getText());
        int sectionlength = Integer.parseInt(lengthField.getText());
        re.getSpeedProfile().changeLocoSpeed(t, sectionlength, 0.0f);
        //rosterSpeedProfile.stopLoco(t, Integer.parseInt(lengthField.getText())/1000);
        setButtonStates(true);
        startSensor.removePropertyChangeListener(startListener);
    }
    
    long startTime;
    long finishTime;
    
    ArrayList<Double> forwardOverRuns = new ArrayList<Double>();
    ArrayList<Double> reverseOverRuns = new ArrayList<Double>();
    
    JPanel update;
    
    static class SensorDetails {
        
        Sensor sensor = null;
        long inactiveDelay = 0;
        long activeDelay = 0;
        boolean usingGlobal = false;
        
        SensorDetails(Sensor sen){
            sensor = sen;
            usingGlobal = sen.useDefaultTimerSettings();
            activeDelay = sen.getSensorDebounceGoingActiveTimer();
            inactiveDelay = sen.getSensorDebounceGoingInActiveTimer();
        }
        
        void setupSensor(){
            sensor.useDefaultTimerSettings(false);
            sensor.setSensorDebounceGoingActiveTimer(0);
            sensor.setSensorDebounceGoingInActiveTimer(0);
        }
        
        void resetDetails(){
            sensor.useDefaultTimerSettings(usingGlobal);
            sensor.setSensorDebounceGoingActiveTimer(activeDelay);
            sensor.setSensorDebounceGoingInActiveTimer(inactiveDelay);
        }
        
        Sensor getSensor(){
            return sensor;
        }
        
    }
    
    TreeMap<Integer, SpeedStep>  speeds= new TreeMap<Integer, SpeedStep>();
    
    static class SpeedStep {
        
        float forward = 0.0f;
        float reverse = 0.0f;
        
        SpeedStep(){
        }
        
        void setForwardSpeed(float speed){
            forward = speed;
        }
        
        void setReverseSpeed(float speed){
            reverse = speed;
        }
        
        float getForwardSpeed(){
            return forward;
        }
        
        float getReverseSpeed(){
            return reverse;
        }
    }
    
    static Logger log = LoggerFactory.getLogger(SpeedProfilePanel.class);
    
}