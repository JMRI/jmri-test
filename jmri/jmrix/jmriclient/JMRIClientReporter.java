// JMRIClientReporter.java

package jmri.jmrix.jmriclient;

import jmri.implementation.AbstractReporter;
import jmri.Reporter;

/**
 * JMRIClient implementation of the Reporter interface.
 * <P>
 *
 * Description:		extend jmri.AbstractReporter for JMRIClient layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author			Paul Bender Copyright (C) 2010
 * @version			$Revision: 1.1 $
 */
public class JMRIClientReporter extends AbstractReporter implements JMRIClientListener {

	// data members
	private int _number;   // reporter number
        private JMRIClientTrafficController tc=null;

	/**
	 * JMRIClient reporters use the reporter  number on the remote host.
	 */
	public JMRIClientReporter(int number,JMRIClientSystemConnectionMemo memo)        {
            super(memo.getSystemPrefix()+"R"+number);
            _number = number;
            tc = memo.getJMRIClientTrafficController();
            // At construction, register for messages
            tc.addJMRIClientListener(this);
            // Then request status.
            requestUpdateFromLayout(); 
	}

	public int getNumber() { return _number; }

	public void requestUpdateFromLayout() {
		// get the message text
        String text = "REPORTER "+ getSystemName() + "\n";
            
        // create and send the message itself
	tc.sendJMRIClientMessage(new JMRIClientMessage(text),this);
	}


	protected void sendMessage(boolean active) {
		// get the message text
        String text;
        if (active) 
            text = "REPORTER "+ getSystemName() + " ACTIVE\n";
        else // thrown
            text = "REPORTER "+ getSystemName() +" INACTIVE\n";
            
        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
	}

       // to listen for status changes from JMRIClient system
        public void reply(JMRIClientReply m) {
               String message=m.toString();
               log.debug("Message Received: " +m );
               log.debug("length "+ message.length() );
               if(!message.contains(getSystemName())) return; // not for us
	       else {
		String text="REPORTER "+ getSystemName() +"\n";
		 if(!message.equals(text)) {
		    setReport(message.substring(text.length()));  // this is an update of the report.
                 } else {
                    setReport(null); // this is an update, but it is just 
                                     // telling us the transient current 
                                     // report is no longer valid.
                 }
               }
        }

        public void message(JMRIClientMessage m) {
        }

        private int state = UNKNOWN;

        public void setState(int s) {
           state = s;
        }

        public int getState() {
           return state;
        }


	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMRIClientReporter.class.getName());

}


/* @(#)JMRIClientReporter.java */
