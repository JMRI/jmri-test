/**
 *  XNetConsist.java
 *
 * This is the Consist definition for a consist on an XPresNet system.
 * it uses the XPressNet specific commands to build a consist.
 *
 * @author                      Paul Bender Copyright (C) 2004
 * @version                     $Revision: 2.15 $
 */

package jmri.jmrix.lenz;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

public class XNetConsist extends jmri.DccConsist implements XNetListener {

	// We need to wait for replies before completing consist 
	// operations
	private final int IDLESTATE = 0;
	private final int ADDREQUESTSENTSTATE = 1;
	private final int REMOVEREQUESTSENTSTATE = 2;

	private int _state = IDLESTATE;

	private DccLocoAddress _locoAddress = null; // address for the last request
	private boolean _directionNormal = false; // direction of the last request

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public XNetConsist(int address) {
		super(address);
	 	// At construction, register for messages
        	XNetTrafficController.instance().addXNetListener(
						XNetInterface.COMMINFO|
						XNetInterface.CONSIST, 
						this); 
	}

	// Initialize a consist for the specific address
        // the Default consist type is an advanced consist 
	public XNetConsist(DccLocoAddress address) {
		super(address);
	 	// At construction, register for messages
        	XNetTrafficController.instance().addXNetListener(
						XNetInterface.COMMINFO|
						XNetInterface.CONSIST, 
						this); 
	}

	// Clean Up local storage, and remove the XNetListener
	public void dispose() {
		super.dispose();
		XNetTrafficController.instance().removeXNetListener(
						XNetInterface.COMMINFO|
						XNetInterface.CONSIST, 
						this);
	}

	// Set the Consist Type
	public void setConsistType(int consist_type){ 
	      if(consist_type==Consist.ADVANCED_CONSIST) {
		ConsistType = consist_type;
		return;
	      } else if(consist_type==Consist.CS_CONSIST) {
		ConsistType = consist_type;
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(new DccLocoAddress(0,false),ConsistListener.NotImplemented);
	      }
	}

	/* is this address allowed?
	 * On Lenz systems, All addresses but 0 can be used in a consist 
	 * (Either and Advanced Consist or a Double Header).
	 */
	public boolean isAddressAllowed(DccLocoAddress address) {
		if(address.getNumber()!=0) return(true);
		else return(false);
	}

	/* is there a size limit for this consist?
	 * For Lenz double headers, returns 2
	 * For Decoder Assisted Consists, returns -1 (no limit)
	 * return 0 for any other consist type.
   	 */
	public int sizeLimit(){
	   if(ConsistType==ADVANCED_CONSIST) {
		return -1;
	   } else if(ConsistType==CS_CONSIST) {
		return 2;
	   } else return 0;
	}

	// does the consist contain the specified address?
	public boolean contains(DccLocoAddress address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		return( (boolean) ConsistList.contains(address));
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	// get the relative direction setting for a specific
	// locomotive in the consist
	public boolean getLocoDirection(DccLocoAddress address) {
	   if(ConsistType==ADVANCED_CONSIST || ConsistType == CS_CONSIST) {
		Boolean Direction=(Boolean) ConsistDir.get(address);
		return( Direction.booleanValue());
	   } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(address,ConsistListener.NotImplemented);
	      }
	   return false;
	}

	/*
     	 * Method for adding an Address to the internal consist list object.
	 */
	private synchronized void addToConsistList(DccLocoAddress LocoAddress, boolean directionNormal) {
	        Boolean Direction = Boolean.valueOf(directionNormal);
		if(!(ConsistList.contains(LocoAddress))) 
					ConsistList.add(LocoAddress);
		ConsistDir.put(LocoAddress,Direction);
		if(ConsistType==CS_CONSIST && ConsistList.size()==2) {
		    notifyConsistListeners(LocoAddress,
				ConsistListener.OPERATION_SUCCESS | 
				ConsistListener.CONSIST_FULL);
	 	} else {
		    notifyConsistListeners(LocoAddress,
				ConsistListener.OPERATION_SUCCESS);
		}
	}	

	/*
     	 * Method for removing an address from the internal consist list object.
	 */
	private synchronized void removeFromConsistList(DccLocoAddress LocoAddress){
		ConsistDir.remove(LocoAddress);
		ConsistList.remove(LocoAddress);
		notifyConsistListeners(LocoAddress,ConsistListener.OPERATION_SUCCESS);	   	
	}

        /*
	 * Add a Locomotive to a Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
	         addToAdvancedConsist(LocoAddress, directionNormal);
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
		 _directionNormal = directionNormal;
	      } else if(ConsistType==CS_CONSIST) {
		if(ConsistList.size()<2) {
		    // Lenz Double Headers require exactly 2 locomotives, so 
		    // wait for the second locomotive to be added to start
		    if(ConsistList.size()==1) {
	         	addToCSConsist(LocoAddress, directionNormal);		
		        // save the address for the check after we get a response 
		        // from the command station
		        _locoAddress = LocoAddress;
		        _directionNormal = directionNormal;
		    }  else if(ConsistList.size()<1) {
		       // we're going to just add this directly, since we 
		       // can't form the consist yet.
		       addToConsistList(LocoAddress,directionNormal);
		    }
		 } else {
                      // The only way it is valid for us to do something
                      // here is if the locomotive we're adding is
                      // already in the consist and we want to change
                      // it's direction
                      if(ConsistList.size()==2 &&   
                         ConsistList.contains(LocoAddress)) {
                      addToCSConsist(LocoAddress, directionNormal);    
                      // save the address for the check after we get aresponse
                      // from the command station
                      _locoAddress = LocoAddress;
                      _directionNormal = directionNormal;
                      } else {
   			notifyConsistListeners(LocoAddress,
				ConsistListener.CONSIST_ERROR | 
				ConsistListener.CONSIST_FULL);
			}
		 }
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
         * Restore a Locomotive to an Advanced Consist, but don't write to
         * the command station.  This is used for restoring the consist
         * from a file or adding a consist read from the command station.
         *  @param address is the Locomotive address to add to the locomotive
         *  @param directionNormal is True if the locomotive is traveling
         *        the same direction as the consist, or false otherwise.
         */
 	public synchronized void restore(DccLocoAddress LocoAddress, boolean directionNormal) {
	      if(ConsistType==ADVANCED_CONSIST) {
		 addToConsistList(LocoAddress,directionNormal);
	      } else if(ConsistType==CS_CONSIST) {
		 addToConsistList(LocoAddress,directionNormal);
	      } else {
		log.error("Consist Type Not Supported");
		notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
	 *  Remove a Locomotive from this Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void remove(DccLocoAddress LocoAddress) {
	      if(ConsistType==ADVANCED_CONSIST) {
	         removeFromAdvancedConsist(LocoAddress);		
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
	      }
	      else if(ConsistType==CS_CONSIST) {
		 // Lenz Double Headers must be formed with EXACTLY 2 
		 // addresses, so if there are two addresses in the list, 
	         // we'll actually send the commands to remove the consist
		 if(ConsistList.size()==2) {
	         removeFromCSConsist(LocoAddress);
		 // save the address for the check after we get a response 
		 // from the command station
		 _locoAddress = LocoAddress;
		 } else {
		    // we just want to remove this from the list.
		    removeFromConsistList(LocoAddress);
		 }
	      }
	      else {
		 log.error("Consist Type Not Supported");
		 notifyConsistListeners(LocoAddress,ConsistListener.NotImplemented);
	      }
	}

        /*
	 *  Add a Locomotive to an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	private synchronized void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
		if(log.isDebugEnabled()) log.debug("Adding locomotive " +LocoAddress.getNumber() + " to consist " + ConsistAddress.getNumber());
		// First, check to see if the locomotive is in the consist already
		if(this.contains(LocoAddress)) {
			// we want to remove the locomotive from the consist 
			// before we re-add it. (we might just be switching
			// the direction of the locomotive in the consist)
			removeFromAdvancedConsist(LocoAddress);
			/*while(_state!=IDLESTATE) {
			try {
				   wait(1000);
			    	}
			    } catch (java.lang.InterruptedException e) {
			        Thread.currentThread().interrupt(); // retain if needed later
			    }
			}*/
		}
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getAddLocoToConsistMsg(ConsistAddress.getNumber(),LocoAddress.getNumber(),directionNormal);
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		_state=ADDREQUESTSENTSTATE;
	}

        /*
	 *  Remove a Locomotive from an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getRemoveLocoFromConsistMsg(ConsistAddress.getNumber(),LocoAddress.getNumber());
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		 _state=REMOVEREQUESTSENTSTATE;
	}

        /*
	 *  Add a Locomotive to a Lenz Double Header
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	private synchronized void addToCSConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
	   // If the consist already contains the locomotive in
           // question, we need to disolve the consist
           if(ConsistList.size()==2 &&
                     ConsistList.contains(LocoAddress)) {
              XNetMessage msg=XNetMessage.getDisolveDoubleHeaderMsg(
                           ((DccLocoAddress)ConsistList.get(0)).getNumber());  
              XNetTrafficController.instance().sendXNetMessage(msg,this);
           }

	   // We need to make sure the directions are set correctly
           // In order to do this, we have to pull up both throttles,
           // and check that the direction of the trailing locomotive
           // is correct relative to the lead locomotive.
           DccLocoAddress address = (DccLocoAddress)ConsistList.get(0);
           XNetThrottle lead= new XNetThrottle(address);
		
	   XNetThrottle trail = new XNetThrottle(LocoAddress);

           if(directionNormal) {
              if(log.isDebugEnabled()) log.debug("DOUBLE HEADER: Set direction of trailing locomotive same as lead locomotive");
              trail.setIsForward(lead.getIsForward());
	      sendDirection(lead,lead.getIsForward());
	      sendDirection(trail,lead.getIsForward());
           } else {
              if(log.isDebugEnabled()) log.debug("DOUBLE HEADER: Set direction of trailing locomotive opposite lead locomotive");
              trail.setIsForward(!lead.getIsForward());
	      sendDirection(lead,lead.getIsForward());
	      sendDirection(trail,!lead.getIsForward());
           }

	   // All we have to do here is create an apropriate XNetMessage, 
	   // and send it.
	   XNetMessage msg=XNetMessage.getBuildDoubleHeaderMsg(address.getNumber(),LocoAddress.getNumber());
	   XNetTrafficController.instance().sendXNetMessage(msg,this);
	   _state=ADDREQUESTSENTSTATE;
	}

        /*
	 *  Remove a Locomotive from a Lenz Double Header
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
		// All we have to do here is create an apropriate XNetMessage, 
		// and send it.
		XNetMessage msg=XNetMessage.getDisolveDoubleHeaderMsg(((DccLocoAddress)ConsistList.get(0)).getNumber());
		XNetTrafficController.instance().sendXNetMessage(msg,this);
		_state=REMOVEREQUESTSENTSTATE; 
	}

	// Listeners for messages from the command station
	public synchronized void message(XNetReply l){
	   if(_state!=IDLESTATE) {
	   // we're waiting for a reply, so examine what we recieved
	   String text;
	   if (l.isOkMessage()) {
		if(_state==ADDREQUESTSENTSTATE) {
		   addToConsistList(_locoAddress,_directionNormal);
		} else if(_state==REMOVEREQUESTSENTSTATE) {
		   removeFromConsistList(_locoAddress);
		}
		_state=IDLESTATE;
	   } else if (l.getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
                text = "XpressNet MU+DH error: ";
                switch(l.getElement(1)) {
                   case 0x81: text = text+ "Selected Locomotive has not been operated by this XPressNet device or address 0 selected";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.LOCO_NOT_OPERATED);
                        break;
                   case 0x82: text = text+ "Selected Locomotive is being operated by another XPressNet device";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR | 
					ConsistListener.LOCO_NOT_OPERATED);
                        break;
                   case 0x83: text = text+ "Selected Locomotive already in MU or DH";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.ALREADY_CONSISTED);
                        break;
                   case 0x84: text = text+ "Unit selected for MU or DH has speed setting other than 0";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NONZERO_SPEED);
                        break;
                   case 0x85: text = text+ "Locomotive not in a MU";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NOT_CONSISTED);
			log.error(text);
                        break;
                   case 0x86: text = text+ "Locomotive address not a multi-unit base address";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.NOT_CONSIST_ADDR);

			log.error(text);
                        break;
                   case 0x87: text = text+ "It is not possible to delete the locomotive";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.DELETE_ERROR);
                        break;
                   case 0x88: text = text+ "The Command Station Stack is Full";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR |
					ConsistListener.STACK_FULL );
			log.error(text);
                        break;
		   default: text = text+ "Unknown";
			log.error(text);
			_state=IDLESTATE;
			notifyConsistListeners(_locoAddress,
					ConsistListener.CONSIST_ERROR);
		   }
		}
	     }
	}

	public void message(XNetMessage l){
	}

	/* 
	 * <P>
	 * Set the speed and direction of a locomotive; bypassing the 
	 * commands in the throttle, since they don't work for this 
	 * application
	 * <P> 
	 * For this application, we also set the speed setting to 0, which 
	 * also establishes control over the locomotive in the consist.
	 * @param t is an XPressNett throttle
 	 * @param isForward is the boolean value representing the desired 
	 * direction
	 */
	private void sendDirection(XNetThrottle t,boolean isForward){
	 XNetMessage msg=new XNetMessage(6);
         msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
         int element4value=0;   /* this is for holding the speed and
                                 direction setting */
         if(t.getSpeedIncrement()==XNetConstants.SPEED_STEP_128_INCREMENT) {
                 // We're in 128 speed step mode
                 msg.setElement(1,XNetConstants.LOCO_SPEED_128);
         } else if(t.getSpeedIncrement()==XNetConstants.SPEED_STEP_28_INCREMENT) {
                 // We're in 28 speed step mode
                 msg.setElement(1,XNetConstants.LOCO_SPEED_28);
         } else if(t.getSpeedIncrement()==XNetConstants.SPEED_STEP_27_INCREMENT) {
                 // We're in 27 speed step mode
                 msg.setElement(1,XNetConstants.LOCO_SPEED_27);
         } else {
                 // We're in 14 speed step mode
                 msg.setElement(1,XNetConstants.LOCO_SPEED_14);
	 }

         msg.setElement(2,
		LenzCommandStation.getDCCAddressHigh(t.getDccAddress()));
						    // set to the upper
                                                    // byte of the  DCC address
         msg.setElement(3,
		LenzCommandStation.getDCCAddressLow(t.getDccAddress())); 
						    // set to the lower byte
                                                    // of the DCC address
         if(isForward)
         {
            /* the direction bit is always the most significant bit */
            element4value+=128;
         }
        msg.setElement(4,element4value);
        msg.setParity(); // Set the parity bit

        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
    }

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetConsist.class.getName());

}
