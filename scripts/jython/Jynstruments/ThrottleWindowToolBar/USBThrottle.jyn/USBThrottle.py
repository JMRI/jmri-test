import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import javax.swing.Timer as Timer
import java.awt.event.ActionListener as ActionListener
import synchronize

# Use a USB device as a throttle
#
# Author: Lionel Jeanson, adapted from Bob Jacobsen, copyright 2008/2009
# Part of the JMRI distribution

# Set the name of the controller you're using
desiredControllerName = "Thrustmaster dual analog 3.2"

componentThrottleFrame = "pov"  # Component for throttle frames browsing
valueNextThrottleFrame = 0.5
valuePreviousThrottleFrame = 1

componentSpeed = "x"  # Analog axis component for curent throttle speed
valueSpeedTrigger = 0.1
valueSpeedDivider = 10
valueSpeedTimerRepeat = 100 # repeat time in ms for speed set task

componentDirection = "rz" # Analog axis component for curent throttle direction
valueDirectionForward = 1
valueDirectionBackward = -1

componentStopSpeed = "0" # Preset speed button
valueStopSpeed = 1
speedStopSpeed = 0

componentSlowSpeed = "1" # Preset speed button
valueSlowSpeed = 1 
speedSlowSpeed = 0.3

componentCruiseSpeed = "2" # Preset speed button
valueCruiseSpeed = 1
speedCruiseSpeed = 0.8

componentMaxSpeed = "3" # Preset speed button
valueMaxSpeed = 1
speedMaxSpeed = 1

componentF0 = "4" # Function button
valueF0 = 1

componentF1 = "5" # Function button
valueF1 = 1 

componentF2 = "6" # Function button
valueF2 = 1

componentF3 = "7" # Function button
valueF3 = 1

class USBThrottle(Jynstrument, PropertyChangeListener, AddressListener):
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleWindow"
    
    def init(self):
        self.getContext().addPropertyChangeListener(self) #ThrottleFrame change
        self.getContext().getCurentThrottleFrame().getAddressPanel().addAddressListener(self) # change of throttle in curent frame
        self.throttle = self.getContext().getCurentThrottleFrame().getAddressPanel().getThrottle() # the throttle
        self.speedAction =  SpeedAction()  #Speed increase thread
        self.speedAction.setThrottle( self.throttle )
        self.speedTimer = Timer(valueSpeedTimerRepeat, self.speedAction )
        self.speedTimer.setRepeats(True)
        self.label = ResizableImagePanel(self.getFolder() + "/USBControl.png",20,20 ) #label
        self.add(self.label)
        self.model = jmri.jmrix.jinput.TreeModel.instance() # USB
        self.model.addPropertyChangeListener(self) 
       
    def quit(self):
        self.speedTimer.stop() 
        self.speedAction = None
        self.speedTimer = None
        self.throttle = None
        self.getContext().removePropertyChangeListener(self)
        self.model.removePropertyChangeListener(self)
        self.getContext().getCurentThrottleFrame().getAddressPanel().removeAddressListener(self)

#Property listener part: USB value
    def propertyChange(self, event):
        if (event.propertyName == "ThrottleFrame") :  # Curent throttle frame changed
            self.speedTimer.stop()
            event.oldValue.getAddressPanel().removeAddressListener(self)
            self.throttle = event.newValue.getAddressPanel().getThrottle()
            self.speedAction.setThrottle( self.throttle )
            event.newValue.getAddressPanel().addAddressListener(self)

        if (event.propertyName == "Value") :  # USB
            if (event.oldValue.getController().getName() == desiredControllerName ) :
                component = event.oldValue.getComponent().toString()
                value = event.newValue
                # ThrottleFrames
                if (component == componentThrottleFrame) :
                    if (value == valueNextThrottleFrame) : #NEXT
                        self.getContext().nextThrottleFrame()
                    if (value == valuePreviousThrottleFrame) : #PREVIOUS
                        self.getContext().previousThrottleFrame()
                # Speed
                if (component == componentSpeed) :
                    self.speedAction.setSpeedIncrement(value / valueSpeedDivider)
                    if (( abs(value) > valueSpeedTrigger) and (self.throttle != None)) :
                        self.speedTimer.start()
                    else :
                        self.speedTimer.stop()
                else :
                    self.speedTimer.stop() # just in case, stop it
                # Direction
                if ((component == componentDirection) and (self.throttle != None)) :
                    if (value == valueDirectionForward) :
                        self.throttle.setIsForward(True)
                    if (value == valueDirectionBackward) :
                        self.throttle.setIsForward(False)
                # Speed presets
                if ((component == componentStopSpeed) and (value == valueStopSpeed) and (self.throttle != None)) :
                    self.throttle.setSpeedSetting(speedStopSpeed)
                if ((component == componentSlowSpeed) and (value == valueSlowSpeed) and (self.throttle != None)) :
                    self.throttle.setSpeedSetting(speedSlowSpeed)
                if ((component == componentCruiseSpeed) and (value == valueCruiseSpeed) and (self.throttle != None)) :
                    self.throttle.setSpeedSetting(speedCruiseSpeed)
                if ((component == componentMaxSpeed) and (value == valueMaxSpeed) and (self.throttle != None)) :
                    self.throttle.setSpeedSetting(speedMaxSpeed)
                # Functions
                if ((component == componentF0) and (value == valueF0) and (self.throttle != None)) :
                    self.throttle.setF0( not self.throttle.getF0() )
                if ((component == componentF1) and (value == valueF1) and (self.throttle != None)) :
                    self.throttle.setF1( not self.throttle.getF1() )
                if ((component == componentF2) and (value == valueF2) and (self.throttle != None)) :
                    self.throttle.setF2( not self.throttle.getF2() )
                if ((component == componentF3) and (value == valueF3) and (self.throttle != None)) :
                    self.throttle.setF3( not self.throttle.getF3() )

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address, isLong):
        pass
        
    def notifyAddressThrottleFound(self, throttle):
        self.speedTimer.stop() 
        self.throttle = throttle
        self.speedAction.setThrottle( self.throttle )
            
    def notifyAddressReleased(self, address, isLong):
        self.speedTimer.stop()
        self.throttle = None
        self.speedAction.setThrottle( self.throttle )
                
# Speed timer class, to increase speed regularly once button pushed
class SpeedAction(ActionListener):
    def SpeedTimer(self):
        self.speedIncrement = 0
        self.throttle = None

    def setSpeedIncrement(self, si):
        self.speedIncrement = si
    setSpeedIncrement = synchronize.make_synchronized(setSpeedIncrement) 

    def getSpeedIncrement(self):
        return self.speedIncrement
    getSpeedIncrement = synchronize.make_synchronized(getSpeedIncrement) 

    def setThrottle(self, throt):
        self.throttle = throt
    setThrottle = synchronize.make_synchronized(setThrottle) 
    
    def getThrottle(self):
        return self.throttle
    getThrottle = synchronize.make_synchronized(getThrottle) 

    def actionPerformed(self, e):
        throttle = self.getThrottle()
        spi = self.getSpeedIncrement()
        if ( throttle != None):
            ns =  throttle.getSpeedSetting() + spi
            if (ns < 0 ) :
                ns = 0
            if (ns > 1 ) :
                ns = 1
            throttle.setSpeedSetting( ns )
