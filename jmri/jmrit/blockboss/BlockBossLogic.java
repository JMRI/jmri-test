// BlockBossLogic.java

package jmri.jmrit.blockboss;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.automat.Siglet;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Represents the "simple signal" logic for one signal; provides
 * some collection services.
 * <P>
 * There are four situations that this logic can handle:
 * <OL>
 * <LI>SIMPLEBLOCK - A simple block, without a turnout.
 * <LI>TRAILINGMAIN - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is closed. It can also be used
 * for the upper head of a two head signal on the facing end of the turnout.
 * <LI>TRAILINGDIVERGING - This signal is protecting a trailing point turnout,
 * which can only be passed when the turnout is thrown. It can also be used
 * for the lower head of a two head signal on the facing end of the turnout.
 * <LI>FACING - This single head signal protects a facing point turnout, which
 * may therefore have two next signals and two sets of next sensors for the
 * closed and thrown states of the turnout.
 * </OL><P>
 * Note that these four possibilities logically require that certain
 * information be configured consistently; e.g. not specifying a turnout
 * in TRAILINGMAIN doesn't make any sense. That's not enforced explicitly, 
 * but violating it can result in confusing behavior.
 *
 * <P>
 * The "hold" unbound parameter can be used to set this 
 * logic to show red, regardless of input.  That's intended for
 * use with CTC logic, etc.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2005
 * @version     $Revision: 1.22 $
 * 
 * Revisions to add facing point sensors, approach lighting, and check box
 * to limit speed. Dick Bronosn (RJB) 2006
 */

public class BlockBossLogic extends Siglet {

    static public final int SINGLEBLOCK = 1;
    static public final int TRAILINGMAIN = 2;
    static public final int TRAILINGDIVERGING = 3;
    static public final int FACING = 4;

    int mode = 0;

    /**
     * Create a default object, without contents.
     */
    public BlockBossLogic() {
    }

    /**
     * Create an object to drive a specific signal.
     * @param name System or user name of the driven signal.
     */
    public BlockBossLogic(String name) {
        super(name+" BlockBossLogic");
        this.name = name;
        driveSignal = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (driveSignal == null) log.warn("Signal "+name+" was not found!");
    }

    /**
     * The "driven signal" is controlled by this
     * element.
     * @return system name of the driven signal
     */
    public String getDrivenSignal() {
        return driveSignal.getSystemName();
    }

    public void setSensor1(String name) {
        if (name == null || name.equals("")) {
            watchSensor1 = null;
            return;
        }
        watchSensor1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor1 == null) log.warn("Sensor1 "+name+" was not found!");
    }

    public void setSensor2(String name) {
        if (name == null || name.equals("")) {
            watchSensor2 = null;
            return;
        }
        watchSensor2 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor2 == null) log.warn("Sensor2 "+name+" was not found!");
    }

    public void setSensor3(String name) {
        if (name == null || name.equals("")) {
            watchSensor3 = null;
            return;
        }
        watchSensor3 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor3 == null) log.warn("Sensor3 "+name+" was not found!");
    }

    public void setSensor4(String name) {
        if (name == null || name.equals("")) {
            watchSensor4 = null;
            return;
        }
        watchSensor4 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchSensor4 == null) log.warn("Sensor4 "+name+" was not found!");
    }

    /**
     * Return the system name of the sensor being monitored
     * @return system name; null if no sensor configured
     */
    public String getSensor1() {
        if (watchSensor1 == null) return null;
        return watchSensor1.getSystemName();
    }
    public String getSensor2() {
        if (watchSensor2 == null) return null;
        return watchSensor2.getSystemName();
    }
    public String getSensor3() {
        if (watchSensor3 == null) return null;
        return watchSensor3.getSystemName();
    }
    public String getSensor4() {
        if (watchSensor4 == null) return null;
        return watchSensor4.getSystemName();
    }

    public void setTurnout(String name) {
        if (name == null || name.equals("")) {
            watchTurnout = null;
            return;
        }
        watchTurnout = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        if (watchTurnout == null) log.warn("Turnout "+name+" was not found!");
    }

    /**
     * Return the system name of the turnout being monitored
     * @return system name; null if no turnout configured
     */
    public String getTurnout() {
        if (watchTurnout == null) return null;
        return watchTurnout.getSystemName();
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public int getMode() {
        return mode;
    }
    
    public void setWatchedSignal1(String name, boolean useFlash) {
        if (name == null || name.equals("")) {
            watchedSignal1 = null;
            return;
        }
        watchedSignal1 = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal1 == null) log.warn("Signal "+name+" was not found!");
        protectWithFlashing = useFlash;
    }
    /**
     * Return the system name of the signal being monitored for first route
     * @return system name; null if no primary signal configured
     */
    public String getWatchedSignal1() {
        if (watchedSignal1 == null) return null;
        return watchedSignal1.getSystemName();
    }

    public void setWatchedSignal1Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSignal1Alt = null;
            return;
        }
        watchedSignal1Alt = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal1Alt == null) log.warn("Signal "+name+" was not found!");
    }
    /**
     * Return the system name of the alternate signal being monitored for first route
     * @return system name; null if no signal configured
     */
    public String getWatchedSignal1Alt() {
        if (watchedSignal1Alt == null) return null;
        return watchedSignal1Alt.getSystemName();
    }

    public void setWatchedSignal2(String name) {
        if (name == null || name.equals("")) {
            watchedSignal2 = null;
            return;
        }
        watchedSignal2 = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal2 == null) log.warn("Signal "+name+" was not found!");
    }
    /**
     * Return the system name of the signal being monitored for the 2nd route
     * @return system name; null if no signal configured
     */
    public String getWatchedSignal2() {
        if (watchedSignal2 == null) return null;
        return watchedSignal2.getSystemName();
    }

    public void setWatchedSignal2Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSignal2Alt = null;
            return;
        }
        watchedSignal2Alt = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        if (watchedSignal2Alt == null) log.warn("Signal "+name+" was not found!");
    }
    /**
     * Return the system name of the secondary signal being monitored for the 2nd route
     * @return system name; null if no secondary signal configured
     */
    public String getWatchedSignal2Alt() {
        if (watchedSignal2Alt == null) return null;
        return watchedSignal2Alt.getSystemName();
    }

        public void setWatchedSensor1(String name) {
        if (name == null || name.equals("")) {
            watchedSensor1 = null;
            return;
        }
        watchedSensor1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchedSensor1 == null) log.warn("Sensor1 "+name+" was not found!");

    }
        /**
         * Return the system name of the sensor being monitored
         * @return system name; null if no sensor configured
         */
        public String getWatchedSensor1() {
            if (watchedSensor1 == null) return null;
            return watchedSensor1.getSystemName();
        }

        public void setWatchedSensor1Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSensor1Alt = null;
            return;
        }
        watchedSensor1Alt = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchedSensor1Alt == null) log.warn("Sensor1Alt "+name+" was not found!");

    }
        /**
         * Return the system name of the sensor being monitored
         * @return system name; null if no sensor configured
         */
        public String getWatchedSensor1Alt() {
            if (watchedSensor1Alt == null) return null;
            return watchedSensor1Alt.getSystemName();
        }

        public void setWatchedSensor2(String name) {
        if (name == null || name.equals("")) {
            watchedSensor2 = null;
            return;
        }
        watchedSensor2 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchedSensor2 == null) log.warn("Sensor2 "+name+" was not found!");

    }
        /**
         * Return the system name of the sensor being monitored
         * @return system name; null if no sensor configured
         */
        public String getWatchedSensor2() {
            if (watchedSensor2 == null) return null;
            return watchedSensor2.getSystemName();
        }

        public void setWatchedSensor2Alt(String name) {
        if (name == null || name.equals("")) {
            watchedSensor2Alt = null;
            return;
        }
        watchedSensor2Alt = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (watchedSensor2Alt == null) log.warn("Sensor2Alt "+name+" was not found!");

    }
        /**
         * Return the system name of the sensor being monitored
         * @return system name; null if no sensor configured
         */
        public String getWatchedSensor2Alt() {
            if (watchedSensor2Alt == null) return null;
            return watchedSensor2Alt.getSystemName();
        }
       public void setLimitSpeed1(boolean d) { limitSpeed1 = d; }
       public boolean getLimitSpeed1() {
        return limitSpeed1;
    }
       public void setLimitSpeed2(boolean d) { limitSpeed2 = d; }
       public boolean getLimitSpeed2() {
        return limitSpeed2;
    }
    
    public boolean getUseFlash() {
        return protectWithFlashing;
    }

    public void setDistantSignal(boolean d) { distantSignal = d; }
    public boolean getDistantSignal() { return distantSignal; }
    
    boolean mHold = false;
    /**
     * Provide the current value of the "hold" parameter.
     * If true, the output is forced to a RED "stop" aspect.
     * This allows CTC and other higher-level functions to 
     * control permission to enter this section of track.
     */
    public boolean getHold() {return mHold;}
    /*
     * Set the current value of the "hold" parameter.
     * If true, the output is forced to a RED "stop" aspect.
     * This allows CTC and other higher-level functions to 
     * control permission to enter this section of track.
     */
    public void setHold(boolean m) { 
        mHold = m;
        setOutput();  // to invoke the new state
    }
    
    String name;
    SignalHead driveSignal = null;
    Sensor watchSensor1 = null;
    Sensor watchSensor2 = null;
    Sensor watchSensor3 = null;
    Sensor watchSensor4 = null;
    Turnout watchTurnout = null;
    SignalHead watchedSignal1 = null;
    SignalHead watchedSignal1Alt = null;
    SignalHead watchedSignal2 = null;
    SignalHead watchedSignal2Alt = null;
    Sensor watchedSensor1 = null;
    Sensor watchedSensor1Alt = null;
    Sensor watchedSensor2 = null;
    Sensor watchedSensor2Alt = null;
    Sensor approachSensor1 = null;
    
    boolean limitSpeed1 = false;
    boolean limitSpeed2 = false;
    boolean protectWithFlashing = false;
    boolean distantSignal = false;


    
        public void setApproachSensor1(String name) {
        if (name == null || name.equals("")) {
            approachSensor1 = null;
            return;
        }
        approachSensor1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        if (approachSensor1 == null) log.warn("Approach Sensor1 "+name+" was not found!");

    }
        /**
         * Return the system name of the sensor being monitored
         * @return system name; null if no sensor configured
         */
        public String getApproachSensor1() {
            if (approachSensor1 == null) return null;
            return approachSensor1.getSystemName();
        }

    /**
     * Define the siglet's input and output.
     */
    public void defineIO() {
        NamedBean[] tempArray = new NamedBean[10];
        int n = 0;

        if (watchTurnout!=null ) {
            tempArray[n]= watchTurnout;
            n++;
        }
        if (watchSensor1 != null) {
            tempArray[n]= watchSensor1;
            n++;
        }
        if (watchSensor2 != null) {
            tempArray[n]= watchSensor2;
            n++;
        }
        if (watchSensor3 != null) {
            tempArray[n]= watchSensor3;
            n++;
        }
        if (watchSensor4 != null) {
            tempArray[n]= watchSensor4;
            n++;
        }
        if (watchedSignal1 != null) {
            tempArray[n]= watchedSignal1;
            n++;
        }
        if (watchedSignal1Alt != null) {
            tempArray[n]= watchedSignal1Alt;
            n++;
        }
        if (watchedSignal2 != null) {
            tempArray[n]= watchedSignal2;
            n++;
        }
        if (watchedSignal2Alt != null) {
            tempArray[n]= watchedSignal2Alt;
            n++;
        }
        if (watchedSensor1 != null) {
        tempArray[n]= watchedSensor1;
        n++;
        }
        if (watchedSensor1Alt != null) {
            tempArray[n]= watchedSensor1Alt;
            n++;
        }
        if (watchedSensor2 != null) {
            tempArray[n]= watchedSensor2;
            n++;
        }
        if (watchedSensor2Alt != null) {
            tempArray[n]= watchedSensor2Alt;
            n++;
        }
        if (approachSensor1 != null) {
            tempArray[n]= approachSensor1;
            n++;
        }
        
        // copy temp to definitive inputs
        inputs = new NamedBean[n];
        for (int i = 0; i< inputs.length; i++)
            inputs[i] = tempArray[i];

        outputs = new NamedBean[]{driveSignal};
        
        // also need to act if the _signal's_ "held"
        // parameter changes, but we don't want to 
        // act if the signals appearance changes (to 
        // avoid a loop, or avoid somebody changing appearance
        // manually and having it instantly recomputed & changed back
        driveSignal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Held"))
                    setOutput();
            }
        });
    }

    /**
     * Recompute new output state
     * and apply it.
     */
    public void setOutput() {
        // make sure init is complete
        if ( (outputs == null) || (outputs[0]==null) ) return;

        // if "hold" is true, must show red
        if (getHold()) {
            ((SignalHead)outputs[0]).setAppearance(SignalHead.RED);
            return;
        }
        
        // otherwise, process algorithm
        switch (mode) {
        case SINGLEBLOCK:
            doSingleBlock();
            break;
        case TRAILINGMAIN:
            doTrailingMain();
            break;
        case TRAILINGDIVERGING:
            doTrailingDiverging();
            break;
        case FACING:
            doFacing();
            break;
        default:
            log.error("Unexpected mode: "+mode);
        }
    }

    int fastestColor1() {
        int result = SignalHead.RED;
        // special case:  GREEN if no next signal
        if (watchedSignal1==null && watchedSignal1Alt==null ) 
            result = SignalHead.GREEN;
        
        int val = SignalHead.RED;
        if (watchedSignal1!=null) val = watchedSignal1.getAppearance();
        if (watchedSignal1!=null && watchedSignal1.getHeld()) val =  SignalHead.RED;  // if Held, act as if Red

        int valAlt = SignalHead.RED;
        if (watchedSignal1Alt!=null) valAlt = watchedSignal1Alt.getAppearance();
        if (watchedSignal1Alt!=null && watchedSignal1Alt.getHeld()) valAlt =  SignalHead.RED; // if Held, act as if Red
        
        return fasterOf(val, valAlt);
    }
    
    int fastestColor2() {
        int result = SignalHead.RED;
        // special case:  GREEN if no next signal
        if (watchedSignal2==null && watchedSignal2Alt==null ) 
            result = SignalHead.GREEN;

        int val = SignalHead.RED;
        if (watchedSignal2!=null) val = watchedSignal2.getAppearance();
        if (watchedSignal2!=null && watchedSignal2.getHeld()) val =  SignalHead.RED;

        int valAlt = SignalHead.RED;
        if (watchedSignal2Alt!=null) valAlt = watchedSignal2Alt.getAppearance();
        if (watchedSignal2Alt!=null && watchedSignal2Alt.getHeld()) valAlt =  SignalHead.RED;
        
        return fasterOf(val, valAlt);
    }
    
    /**
     * Given two {@link SignalHead} color constants, returns the one corresponding
     * to the slower speed.
     */
    static int slowerOf(int a, int b)
    {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.min(a, b);
    }

    /**
     * Given two {@link SignalHead} color constants, returns the one corresponding
     * to the faster speed.
     */
    static int fasterOf(int a, int b)
    {
        // DARK is smallest, FLASHING GREEN is largest
        return Math.max(a, b);
    }

    void doSingleBlock() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (fastestColor1()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // if distant signal, show exactly what the home signal does
        if (distantSignal)
            appearance = fastestColor1();
            
        // if limited speed and green, reduce to yellow
        if (limitSpeed1)
            appearance = slowerOf(appearance, SignalHead.YELLOW);
      
        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead)outputs[0]).getHeld())
            appearance = SignalHead.RED;

        // handle approach lighting
        doApproach();
        
        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    void doTrailingMain() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (fastestColor1()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // if distant signal, show exactly what the home signal does
        if (distantSignal)
            appearance = fastestColor1();

        // if limited speed and green, reduce to yellow
        if (limitSpeed1)
            appearance = slowerOf(appearance, SignalHead.YELLOW);
      
        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
            
        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.CLOSED)
            appearance = SignalHead.RED;
        if (watchTurnout!=null && watchTurnout.getCommandedState() != Turnout.CLOSED)
            appearance = SignalHead.RED;

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead)outputs[0]).getHeld())
            appearance = SignalHead.RED;
            
        // handle approach lighting
        doApproach();
        
        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }

    void doTrailingDiverging() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && fastestColor1()==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (fastestColor1()==SignalHead.RED)
            appearance = SignalHead.YELLOW;

        // if distant signal, show exactly what the home signal does
        if (distantSignal)
            appearance = fastestColor1();

        // if limited speed and green, reduce to yellow
        if (limitSpeed2)
            appearance = slowerOf(appearance, SignalHead.YELLOW);
      
        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE)
            appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;

        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.THROWN)
            appearance = SignalHead.RED;
        if (watchTurnout!=null && watchTurnout.getCommandedState() != Turnout.THROWN)
            appearance = SignalHead.RED;

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead)outputs[0]).getHeld())
            appearance = SignalHead.RED;
            
        // handle approach lighting
        doApproach();
        
        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }
    
    void doFacing() {
        int appearance = SignalHead.GREEN;
        int oldAppearance = ((SignalHead)outputs[0]).getAppearance();
        
        // find downstream appearance, being pessimistic if we're not sure of the state
        int s = SignalHead.GREEN;
        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.THROWN)
            s = slowerOf(s, fastestColor1());
        if (watchTurnout!=null && watchTurnout.getKnownState() != Turnout.CLOSED)
            s = slowerOf(s, fastestColor2());

        // check for yellow, flashing yellow overriding green
        if (protectWithFlashing && s==SignalHead.YELLOW)
            appearance = SignalHead.FLASHYELLOW;
        if (s==SignalHead.RED)
            appearance = SignalHead.YELLOW;
        // if distant signal, show exactly what the home signal does
        if (distantSignal)
            appearance = s;

        // if limited speed and green or flashing yellow, reduce to yellow
        if (watchTurnout!=null && limitSpeed1 && watchTurnout.getKnownState()!=Turnout.THROWN)
            appearance = slowerOf(appearance, SignalHead.YELLOW);

        if (watchTurnout!=null && limitSpeed2 && watchTurnout.getKnownState()!=Turnout.CLOSED)
            appearance = slowerOf(appearance, SignalHead.YELLOW);
           

        // check for red overriding yellow or green
        if (watchSensor1!=null && watchSensor1.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor2!=null && watchSensor2.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor3!=null && watchSensor3.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;
        if (watchSensor4!=null && watchSensor4.getKnownState() != Sensor.INACTIVE) 
            appearance = SignalHead.RED;

        if ((watchTurnout!=null && watchTurnout.getKnownState() == Turnout.CLOSED) && ((watchedSensor1!=null && watchedSensor1.getKnownState() != Sensor.INACTIVE)))
            appearance = SignalHead.RED;
        if ((watchTurnout!=null && watchTurnout.getKnownState() == Turnout.CLOSED) && ((watchedSensor1Alt!=null && watchedSensor1Alt.getKnownState() != Sensor.INACTIVE)))
            appearance = SignalHead.RED;
        if ((watchTurnout!=null && watchTurnout.getKnownState() == Turnout.THROWN) && ((watchedSensor2!=null && watchedSensor2.getKnownState() != Sensor.INACTIVE)))
            appearance = SignalHead.RED;
        if ((watchTurnout!=null && watchTurnout.getKnownState() == Turnout.THROWN) && ((watchedSensor2Alt!=null && watchedSensor2Alt.getKnownState() != Sensor.INACTIVE)))
            appearance = SignalHead.RED;
        
        // check if turnout in motion, if so force red
        if (watchTurnout!=null && (watchTurnout.getKnownState() != watchTurnout.getCommandedState()) )
            appearance = SignalHead.RED;
        if (watchTurnout!=null && (watchTurnout.getKnownState() != Turnout.THROWN) && (watchTurnout.getKnownState() != Turnout.CLOSED) )  // checking for other states
            appearance = SignalHead.RED;

        // check if signal if held, forcing a red aspect by this calculation
        if (((SignalHead)outputs[0]).getHeld())
            appearance = SignalHead.RED;
            
        // handle approach lighting
        doApproach();
        
        // show result if changed
        if (appearance != oldAppearance)
            ((SignalHead)outputs[0]).setAppearance(appearance);
    }
    
    /**
     * Handle the approach lighting logic for all modes
     */
    void doApproach() {
        if (approachSensor1 != null && approachSensor1.getKnownState() == Sensor.INACTIVE) {
            // should not be lit
            if (driveSignal.getLit()) driveSignal.setLit(false);
        } else {
            // should be lit
            if (!driveSignal.getLit()) driveSignal.setLit(true);
        }            
        return;
    }

    static Hashtable umap = null;
    static Hashtable smap = null;

    public static Enumeration entries() {
        return smap.elements();
    }

    private static void setup() {
        if (smap == null) {
            smap = new Hashtable();
            umap = new Hashtable();
            InstanceManager.configureManagerInstance().registerConfig(new BlockBossLogic());
        }
    }

    /**
     * Ensure that this BlockBossLogic object is available for later retrieval
     */
    public void retain() {
        smap.put(driveSignal.getSystemName(), this);
        if (driveSignal.getUserName()!=null)
            umap.put(driveSignal.getUserName(), this);
    }

    /**
     * Return the BlockBossLogic item governing a specific signal,
     * having removed it from use.
     * @param signal
     * @return never null
     */
    public static BlockBossLogic getStoppedObject(String signal) {
        BlockBossLogic b = null;
        setup(); // ensure we've been registered
        if (smap.containsKey(signal)) {
            b = (BlockBossLogic)smap.get(signal);
        }
        else if (umap.containsKey(signal)) {
            b = (BlockBossLogic)umap.get(signal);
        }
        if (b != null) {
            // found an existing one, remove it from the map and stop its thread
            smap.remove(b.driveSignal.getSystemName());
            if (b.driveSignal.getUserName() != null) {
                umap.remove(b.driveSignal.getUserName());
            }
            b.stop();
            return b;
        }
        else {
            // no existing one, create a new one
            return new BlockBossLogic(signal);
        }
    }
    /**
     * Return the BlockBossLogic item governing a specific signal.
     * <P>
     * Unlike {@link BlockBossLogic#getStoppedObject(String signal)}
     * this does not remove the object from being used.
     * @param signal system name
     * @return never null
     */
    public static BlockBossLogic getExisting(String signal) {
        BlockBossLogic b;
        setup(); // ensure we've been registered
        if (smap.containsKey(signal)) {
            b = (BlockBossLogic)smap.get(signal);
        } else if (umap.containsKey(signal)) {
            b = (BlockBossLogic)umap.get(signal);
        } else {
            b = new BlockBossLogic(signal);
        }
        return b;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());
}

/* @(#)BlockBossLogic.java */
