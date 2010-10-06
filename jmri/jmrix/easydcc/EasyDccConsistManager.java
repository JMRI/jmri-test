/**
 * EasyDccConsistManager.java
 *
 * Description:           Consist Manager for use with the
 *                        EasyDccConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2006
 * @version               $Revision: 1.9 $
 */


package jmri.jmrix.easydcc;

import jmri.Consist;
import jmri.DccLocoAddress;

public class EasyDccConsistManager extends jmri.jmrix.AbstractConsistManager implements jmri.ConsistManager {

	//private Thread initThread = null;

	/**
	 *  Constructor - call the constructor for the superclass, and 
	 *  initilize the consist reader thread, which retrieves consist 
	 *  information from the command station
	 **/
	public EasyDccConsistManager(){
		super();
		// Initilize the consist reader thread.
		new EasyDccConsistReader();
	}


	/**
         *    This implementation does support andvanced consists, so 
	 *    return true.
         **/
        public boolean isCommandStationConsistPossible() { return true; }

        /**
         *    Does a CS consist require a seperate consist address?
	 *    CS consist addresses are assigned by the user, so return true.
         **/
        public boolean csConsistNeedsSeperateAddress() { return true; }

	/**
	 *    Add a new EasyDccConsist with the given address to ConsistTable/ConsistList
	 */
	public Consist addConsist(DccLocoAddress address){ 
		        EasyDccConsist consist;
                        consist = new EasyDccConsist(address);
                        ConsistTable.put(address,consist);
                        ConsistList.add(address);
                        return consist;
	}


        // Internal class to read consists from the command station
	private class EasyDccConsistReader implements Runnable,EasyDccListener {

           // Storage for addresses
	   int _lastAddress=0;

           // Possible States
           final static int IDLE=0;
           final static int SEARCHREQUESTSENT=1;

           // Current State
	   int CurrentState=IDLE;

	   EasyDccConsistReader(){
              searchNext();
           }
 
           public void run() {
           }

           private void searchNext() {
              if(log.isDebugEnabled()) log.debug("Sending request for next consist, _lastAddress is: " + _lastAddress);
              CurrentState=SEARCHREQUESTSENT;
	      EasyDccMessage msg=EasyDccMessage.getDisplayConsist(++_lastAddress);
              EasyDccTrafficController.instance().sendEasyDccMessage(msg,this);
           }

           // Listener for messages from the command station
           public void reply(EasyDccReply r){
		if(CurrentState==SEARCHREQUESTSENT){
                      // We sent a request for a consist address.
                      // We need to find out what type of message 
                      // was recived as a response.  If the message 
                      // has an opcode of 'G', then it is a response 
                      // to the Display Consist instruction we sent 
                      // previously.  If the message has any other
                      // opcode, we can ignore the message.
                      if(log.isDebugEnabled()) log.debug("Message Recieved in SEARCHREQUESTSENT state.  Message is: " + r.toString());
                      if(r.getOpCode() == 'G'){
                         // This is the response we're looking for
                         // The bytes 2 and 3 are the

                         int consistAddr= -1;
                         EasyDccConsist currentConsist = null;
                         String sa = "" + (char)r.getElement(1) + 
                                     (char)r.getElement(2);
                         consistAddr=Integer.valueOf(sa,16).intValue();

                         // The rest of the message consists of 4 hex digits
                         // for each of up to 8 locomotives.
                         for(int i=3;i<r.getNumDataElements();i+=4)
                         {
                            DccLocoAddress locoAddress;
                            int tempAddr;
                            boolean directionNormal=true;
                            //String sb = "" + (char)r.getElement(i) + 
                            //         (char)r.getElement(i+1) +
                            //         (char)r.getElement(i+2) +
                            //         (char)r.getElement(i+3);
                            tempAddr=Integer.valueOf(sa,16).intValue();
                            directionNormal=((tempAddr&0x8000)==0);
                            if(tempAddr!=0) {
                               if(i==3){
                                  // This is the first address, add the 
                                  // consist
                                  currentConsist=(EasyDccConsist)addConsist(
                                                        new DccLocoAddress(consistAddr,false));
                               }
                               locoAddress=new DccLocoAddress(
                                           tempAddr&0x7fff,(tempAddr&0x7fff)>99);
                               if (currentConsist != null)
                            	   currentConsist.restore(locoAddress,directionNormal);
                               else
                            	   //should never happen since currentCOnsist get set in the first pass
                            	   log.error("currentConsist is null!");
                            }
                         }
                         if(_lastAddress<255)
                                  searchNext();
                         else CurrentState=IDLE;
                      } else {
                      if(log.isDebugEnabled()) log.debug("Message Recieved in IDLE state.  Message is: " + r.toString());
                      }
                  }
           }

           // Listener for messages to the command station
           public void message(EasyDccMessage m){
           }
     }

        static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EasyDccConsistManager.class.getName());

}
