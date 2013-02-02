// JMRIClientTurnout.java

package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * JMRIClient implementation of the Turnout interface.
 * <P>
 *
 * Description:		extend jmri.AbstractTurnout for JMRIClient layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author			Paul Bender Copyright (C) 2010
 * @version			$Revision$
 */
public class JMRIClientTurnout extends AbstractTurnout implements JMRIClientListener {

	// data members
	private int _number;   // turnout number
        private JMRIClientTrafficController tc=null;
        private String prefix = null;

        /* Static arrays to hold Lenz specific feedback mode information */
        static String[] modeNames = null;
        static int[] modeValues = null;

	/**
	 * JMRIClient turnouts use the turnout number on the remote host.
	 */
	public JMRIClientTurnout(int number,JMRIClientSystemConnectionMemo memo)        {
            super(memo.getSystemPrefix()+"t"+number);
            _number = number;
            tc = memo.getJMRIClientTrafficController();
            prefix=memo.getSystemPrefix();
	     /* allow monitoring mode feedback, which is the same
                as direct for this connection (this aids the 
                transition from directly connected hardware to remotly
                connected hardware.)*/
            _validFeedbackTypes |= MONITORING;

            // Default feedback mode is MONITORING
            _activeFeedbackType = MONITORING;

            setModeInformation(_validFeedbackNames,_validFeedbackModes);

            // set the mode names and values based on the static values.
            _validFeedbackNames = getModeNames();
            _validFeedbackModes = getModeValues();

            // At construction, register for messages
            tc.addJMRIClientListener(this);
            // Then request status.
            requestUpdateFromLayout();
	}

    //Set the mode information for JMRIClient Turnouts.
    synchronized static private void setModeInformation(String[] feedbackNames, int[] feedbackModes){
        // if it hasn't been done already, create static arrays to hold
        // the Lenz specific feedback information.
        if(modeNames == null) {
           if (feedbackNames.length != feedbackModes.length)
               log.error("int and string feedback arrays different length");
           modeNames  = new String[feedbackNames.length+1];
           modeValues = new int[feedbackNames.length+1];
           for (int i = 0; i<feedbackNames.length; i++) {
              modeNames[i] = feedbackNames[i];
              modeValues[i] = feedbackModes[i];
           }
           modeNames[feedbackNames.length] = "MONITORING";
           modeValues[feedbackNames.length] = MONITORING;
        }
    }

    static int[] getModeValues() {return modeValues;}
    static String[] getModeNames() {return modeNames;}


	public int getNumber() { return _number; }

	// Handle a request to change state by sending a formatted packet
        // to the server.
	protected void forwardCommandChangeToLayout(int s) {
		// sort out states
		if ( (s & Turnout.CLOSED) > 0) {
			// first look for the double case, which we can't handle
			if ( (s & Turnout.THROWN) > 0) {
				// this is the disaster case!
				log.error("Cannot command both CLOSED and THROWN "+s);
				return;
			} else {
				// send a CLOSED command
				sendMessage(true^getInverted());
			}
		} else {
			// send a THROWN command
			sendMessage(false^getInverted());
		}
	}
   
    public boolean canInvert() {
              return true;
    }
 
    // request a stuatus update from the layout.
    protected void requestUpdateFromLayout(){
        // create the message
        String text = "TURNOUT "+ getSystemName() + "\n";
        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }
    

    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout){
        if (log.isDebugEnabled()) log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock")+ " Pushbutton "+prefix+_number);
    }


	protected void sendMessage(boolean closed) {
		// get the message text
        String text;
        if (closed) 
            text = "TURNOUT "+ getSystemName() + " CLOSED\n";
        else // thrown
            text = "TURNOUT "+ getSystemName() + " THROWN\n";
            
        // create and send the message itself
	tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
	}

       // to listen for status changes from JMRIClient system
        public void reply(JMRIClientReply m) {
               String message=m.toString();
               if(!message.contains(getSystemName()+ " ")) return; // not for us

               if(m.toString().contains("THROWN"))
                  newKnownState(!getInverted()?jmri.Turnout.THROWN:jmri.Turnout.CLOSED);
               else if(m.toString().contains("CLOSED"))
                  newKnownState(!getInverted()?jmri.Turnout.CLOSED:jmri.Turnout.THROWN);
               else
                  newKnownState(jmri.Turnout.UNKNOWN);
        }

        public void message(JMRIClientMessage m) {
        }





	static Logger log = Logger.getLogger(JMRIClientTurnout.class.getName());

}


/* @(#)JMRIClientTurnout.java */
