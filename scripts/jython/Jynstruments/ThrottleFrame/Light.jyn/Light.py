import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import jmri.jmrit.throttle.FunctionListener as FunctionListener

class Light(Jynstrument, PropertyChangeListener, AddressListener, MouseListener, FunctionListener):
# Jynstrument mandatory part
# Here this JYnstrument like to be in a ThrottleFrame and no anywhere else
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( CardLayout() )
        self.labelOff = ResizableImagePanel(self.getFolder() + "/LightOff.png",100,100 ) 
        self.labelOn = ResizableImagePanel(self.getFolder() + "/LightOn.png",100,100 )
        self.add(self.labelOff, "off")
        self.add(self.labelOn, "on")
        self.addComponentListener(self.labelOff)
        self.addComponentListener(self.labelOn)
        self.addMouseListener(self)
        self.getContext().getAddressPanel().addAddressListener(self)
        self.throttle = self.getContext().getAddressPanel().getThrottle()
        self.updateThrottle()
        self.setIcon()

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        self.cleanThrottle()
        self.getContext().removeThrottleListener(self)
        self.getContext().getAddressPanel().removeAddressListener(self)

# this is a good way to make sure that we're are actaully GCed 
# using memory watcher in development menu, we can force a GC from there
    def __del__(self):  
        print "in destructor"

#Inner workings:
    def updateThrottle(self):    # update throttle informations when a new one is detected
        if self.throttle != None :
            self.throttle.addPropertyChangeListener(self)
        self.getContext().getFunctionPanel().getFunctionButtons()[0].addFunctionListener(self)
        
    def cleanThrottle(self):     # clean up throttle information when it is deconnected
        if self.throttle != None :
            self.throttle.removePropertyChangeListener(self)
        self.getContext().getFunctionPanel().getFunctionButtons()[0].removeFunctionListener(self)
        self.throttle = None

    def switch(self):      # actually do function value change
        if self.throttle != None :
            self.throttle.setF0( not self.throttle.getF0() )   # HERE!
        self.setIcon()

    def setIcon(self):     # update appearance
        cl = self.getLayout()
        if self.throttle == None :
            cl.show(self, "off")
        elif self.throttle.getF0() :
            cl.show(self, "on")
        else :
            cl.show(self, "off")

#FunctionListener: to listen for Function 0 changes from the AddressPanel itself
    def notifyFunctionStateChanged(self, functionNumber, isOn):
        self.setIcon()

    def notifyFunctionLockableChanged(self, functionNumber, isLockable):
        pass

#PropertyChangeListener part:: to listen for Function 0 changes from everywhere else
    def propertyChange(self, event):
        if event.getPropertyName() == "F0" :
            self.setIcon()

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address, isLong):
        pass

    def notifyAddressThrottleFound(self, throttle):
        self.throttle = throttle
        self.updateThrottle()
        self.setIcon()
    
    def notifyAddressReleased(self, address, isLong):
        self.cleanThrottle()
        self.setIcon()

#MouseListener part: to listen for mouse events
    def mouseReleased(self, event):
        self.switch()

    def mousePressed(self, event):
        pass
    def mouseClicked(self, event):
        pass
    def mouseExited(self, event):
        pass
    def mouseEntered(self, event):
        pass

