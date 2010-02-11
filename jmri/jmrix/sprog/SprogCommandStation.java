/* SprogSlotManager.java */

package jmri.jmrix.sprog;

import jmri.CommandStation;
import jmri.NmraPacket;

import java.util.LinkedList;
import java.util.Vector;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.sprog.sprogslotmon.*;

/**
 * Controls a collection of slots, acting as a soft command station for SPROG
 * <P>
 * A SlotListener can register to hear changes. By registering here, the SlotListener
 * is saying that it wants to be notified of a change in any slot.  Alternately,
 * the SlotListener can register with some specific slot, done via the SprogSlot
 * object itself.
 * <P>
 * This Programmer implementation is single-user only. It's not clear whether
 * the command stations can have multiple programming requests outstanding
 * (e.g. service mode and ops mode, or two ops mode) at the same time, but this
 * code definitely can't.
 * <P>
 * <P> Updated by Andrew Berridge, January 2010 - state management code now safer,
 * uses enum, etc. Amalgamated with Sprog Slot Manager into a single class - 
 * reduces code duplication </P>
 * @author	Bob Jacobsen  Copyright (C) 2001, 2003
 *              Andrew Crosland         (C) 2006 ported to SPROG
 * @version     $Revision: 1.7 $
 */
public class SprogCommandStation implements CommandStation, SprogListener, Runnable {

	private enum SlotThreadState {IDLE, WAITING_FOR_REPLY, SEND_STATUS_REQUEST, WAITING_FOR_STATUS_REPLY}
	
    private int currentSlot = 0;
    
    private static LinkedList<SprogSlot> slots;
	
    public SprogCommandStation() {
        // error if more than one constructed?
        if (self != null) log.debug("Creating too many SlotManager objects");
        SprogTrafficController.instance().addSprogListener(this);
    }
    
    /**
     * Create a default length queue
     */
    static {
        slots = new LinkedList<SprogSlot>();
        for (int i = 0; i < SprogConstants.MAX_SLOTS; i++) {
      	slots.add(new SprogSlot(i));
        }
      }  
    
	
    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats number of times to repeat the packet
     */
    public void sendPacket(byte[] packet, int repeats) {
        if (packet.length<=1) log.error("Invalid DCC packet length: "+packet.length);
        if (packet.length>=7) log.error("Only 6-byte packets accepted: "+packet.length);
        SprogMessage m = new SprogMessage(packet);
        for (int i = 0; i < repeats; i++) {
        		SprogTrafficController.instance().sendSprogMessage(m, null);
        }
    }

      /**
       * Return contents of Queue slot i
       * @param i int
       * @return SprogSlot
       */
      public SprogSlot slot(int i) {
      	return slots.get(i);
      }


      /**
       * Clear all slots
       */
      @SuppressWarnings("unused")
  	private void clearAllSlots() {
    	  for (SprogSlot s : slots) s.clear();                 
    }

      /**
       * Find a free slot entry. 
       *
       * @return SprogSlot the next free Slot or null if all slots are full
       */
      private SprogSlot findFree() {
        for (SprogSlot s : slots) {
      	  if (s.isFree()) return s;
        }
        return (null);
      }

      /**
       * Find a queue entry matching the address
       * @param a int
       * @return the slot or null if the address is not in the queue
       */
      private SprogSlot findAddress(int a) {
      	for (SprogSlot s : slots) {
      		if (s.getAddr() == a) return s;
      	}
      	return (null);
      }
      
      private SprogSlot findAddressSpeedPacket(int address) {
        	for (SprogSlot s : slots) {
          		if (s.getAddr() == address && s.isSpeedPacket()) return s;
          	}
        	if (getInUseCount() < SprogConstants.MAX_SLOTS) {
        		return findFree();
        	}
          	return (null);
      }
      
      private SprogSlot findF0to4Packet(int address) {
      	for (SprogSlot s : slots) {
        		if (s.getAddr() == address && s.isF0to4Packet()) return s;
        }
      	if (getInUseCount() < SprogConstants.MAX_SLOTS) {
      		return findFree();
      	} 
        return (null);
    }
      
      private SprogSlot findF5to8Packet(int address) {
        	for (SprogSlot s : slots) {
          		if (s.getAddr() == address && s.isF5to8Packet()) return s;
          }
        	if (getInUseCount() < SprogConstants.MAX_SLOTS) {
        		return findFree();
        	} 
          return (null);
      }
      
      private SprogSlot findF9to12Packet(int address) {
      	for (SprogSlot s : slots) {
        		if (s.getAddr() == address && s.isF9to12Packet()) return s;
        }
      	if (getInUseCount() < SprogConstants.MAX_SLOTS) {
      		return findFree();
      	} 
        return (null);
    }

    public void forwardCommandChangeToLayout(int address, boolean closed){
        
        SprogSlot s = this.findFree();
        if (s!= null) { 
        	s.setAccessoryPacket(address, closed, SprogConstants.S_REPEATS);
        	notifySlotListeners(s); }
    
    }
    
    public void function0Through4Packet(int address,
                                        boolean f0, boolean f0Momentary,
                                        boolean f1, boolean f1Momentary,
                                        boolean f2, boolean f2Momentary,
                                        boolean f3, boolean f3Momentary, 
                                        boolean f4, boolean f4Momentary) {
      SprogSlot s = this.findF0to4Packet(address);
      s.f0to4packet(address, f0, f0Momentary, 
    		  f1, f1Momentary, 
    		  f2, f2Momentary, 
    		  f3, f3Momentary, 
    		  f4, f4Momentary);
      if (s!=null) { notifySlotListeners(s); }
    }

    public void function5Through8Packet(int address,
                                        boolean f5, boolean f5Momentary,
                                        boolean f6, boolean f6Momentary,
                                        boolean f7, boolean f7Momentary,
                                        boolean f8, boolean f8Momentary) {
        SprogSlot s = this.findF5to8Packet(address);
        s.f5to8packet(address, f5, f5Momentary, f6, f6Momentary, f7, f7Momentary, f8, f8Momentary);
        if (s!=null) { notifySlotListeners(s); }
    }

    public void function9Through12Packet(int address,
                                         boolean f9, boolean f9Momentary,
                                         boolean f10, boolean f10Momentary,
                                         boolean f11, boolean f11Momentary,
                                         boolean f12, boolean f12Momentary) {
        SprogSlot s = this.findF9to12Packet(address);
        s.f9to12packet(address, f9, f9Momentary, f10, f10Momentary, f11, f11Momentary, f12, f12Momentary);
        if (s!=null) { notifySlotListeners(s); }
    }

    public void setSpeed(int address, int spd, boolean isForward) {
    	SprogSlot s = this.findAddressSpeedPacket(address);
    	if (s != null) { // May need an error here - if all slots are full!
    		s.setSpeed(address, spd, isForward);
    		notifySlotListeners(s); 
    	}
    }

    public void opsModepacket(int address, boolean longAddr, int cv, int val) {
    	SprogSlot s = findFree();
    	if (s!= null) {
    		s.setOps(address, longAddr, cv, val);
    		notifySlotListeners(s); 
    	}
    }

    public void release(int address) {
    	SprogSlot s;
    	while ((s = findAddress(address))!= null) {
	        s.clear();
	        notifySlotListeners(s);
    	}
    }

    /**
     * Send emergency stop to all slots
     */
    public void estopAll() {
    	for(SprogSlot s : slots) {
        if ((s.getRepeat() == -1)
                && s.slotStatus() != SprogConstants.SLOT_FREE
                && s.speed() != 1) {
              eStopSlot(s);
            }
    	}
    }

    /**
     * Send emergency stop to a slot
     *
     * @param s SprogSlot to eStop
     */
    private void eStopSlot(SprogSlot s) {
      log.debug("Estop slot: "+s.getSlotNumber()+" for address: "+s.locoAddr());
      s.eStop();
      notifySlotListeners(s);
    }

    /**
     * method to find the existing SlotManager object, if need be creating one
     */
    static public final SprogCommandStation instance() {
        if (self == null) {
          log.debug("creating a new SprogSlotManager object");
          self = new SprogCommandStation();
        }
        return self;
    }
    static private SprogCommandStation self = null;

    // data members to hold contact with the slot listeners
    final private Vector<SprogSlotListener> slotListeners = new Vector<SprogSlotListener>();

    public synchronized void addSlotListener(SprogSlotListener l) {
        // add only if not already registered
        slotListeners.addElement(l);
    }

    public synchronized void removeSlotListener(SprogSlotListener l) {
        slotListeners.removeElement(l);
    }

    /**
     * Trigger the notification of all SlotListeners.
     * @param s The changed slot to notify.
     */
    @SuppressWarnings("unchecked")
	private synchronized void notifySlotListeners(SprogSlot s) {
    	if (log.isDebugEnabled()) log.debug("notify "+slotListeners.size()
                +" SlotListeners about slot for address"
                +s.getAddr());

        // forward to all listeners
        for (SprogSlotListener client : slotListeners) {
        	client.notifyChangedSlot(s);
        }        
    }

    /**
     * Loop here sending packets to the rails
     */
    private volatile boolean replyReceived;
    private volatile boolean awaitingReply;
    private int statusDue = 0;
    public void run() {
      log.debug("Slot thread starts");
      byte [] p;
      int [] statusA = new int [4];
      //int statusIdx = 0;
      //AJB slot state now uses enums
      SlotThreadState state =  SlotThreadState.IDLE;
      SlotThreadState prevState = state;
      //Keep track of how many times we've been doing the same thing
      //in case we need to give up (to avoid being stuck in a state with 
      //no escape!
      int numLoopsSameState = 0;
      //count of no. of times idle
      int idleCount = 0;
      while (true) { // loop permanently but sleep
         if (log.isDebugEnabled()) {
        	 log.debug("SPROG SlotManager in state: " + state.toString());
         }
    	/*
    	 * Check:
    	 * Are we stuck in certain (non idle) state?
    	 */
    	if (state != SlotThreadState.IDLE) { 
			idleCount = 0;
			if (state == prevState) {
				if(++ numLoopsSameState > 100) {
					//We're probably stuck in a state... Just go back to idle and
				//carry on!
					log.error("Stuck in state: " + state.toString());
					numLoopsSameState = 0;
					state = SlotThreadState.IDLE;
				}
			} else {
				numLoopsSameState = 0;
			}
		}
    	prevState = state;
        try {
        	//Slow down loop repeat rate if we've been idle for a while,
        	//otherwise repeat frequently for responsiveness
        	if (idleCount > 10) {
        		log.debug("sleeping 800ms");
        		Thread.sleep(800);
        	} else {
        		Thread.sleep(10);
        	}
        } catch (InterruptedException i) {
            Thread.currentThread().interrupt(); // retain if needed later
          log.error("Sprog slot thread interrupted\n"+i);
        }
        switch(state) {
          case IDLE: {
            idleCount ++;
            // Get next packet to send
            p = getNextPacket();
            if (p != null) {
            	/* AJB: Moved flags to before sending packet - with improved
            	 * performance, we were sometimes setting the flags AFTER
            	 * a reply was received elsewhere!
            	 */
	            synchronized(this) {
	                replyReceived = false; //should be false!
	                awaitingReply = true;  //should be true!
	            }
              sendPacket(p, SprogConstants.S_REPEATS);

              state = SlotThreadState.WAITING_FOR_REPLY; 
            }
            break;
          }
          case WAITING_FOR_REPLY: {
            // Wait for reply
            if (replyReceived) {
              if (++statusDue > 20) {
                state = SlotThreadState.SEND_STATUS_REQUEST;
              } else {
                state = SlotThreadState.IDLE;
              }
            } 
            break;
          }
          case SEND_STATUS_REQUEST: {
            // Send status request
        	/* AJB: Moved flags to before sending packet - with improved
        	 * performance, we were sometimes setting the flags AFTER
        	 * a reply was received elsewhere!
        	 */
            synchronized(this) {
                replyReceived = false;
                awaitingReply = true;
            }
            SprogTrafficController.instance().
            	sendSprogMessage(SprogMessage.getStatus(), this);
            
            statusDue = 0;
            state = SlotThreadState.WAITING_FOR_STATUS_REPLY;
            break;
          }
          case WAITING_FOR_STATUS_REPLY: {
            // Waiting for status reply
            if (replyReceived) {
              if (SprogSlotMonFrame.instance() != null) {
                String s = replyForMe.toString();
                log.debug("Reply received whilst waiting for status");
                int i = s.indexOf('h');
                //Check that we got a status message before acting on it
                //by checking that "h" was found in the reply
                if (i > -1) { 
	//                float volts = Integer.decode("0x"+s.substring(i+1, i+5)).intValue();
	                int milliAmps = ((Integer.decode("0x"+s.substring(i+7, i+11)).intValue())*488)/47;
	//                statusA[statusIdx] = milliAmps;
	//                statusIdx = (statusIdx+1)%4;
	//                String voltString, ampString;
	//                ampString = Float.toString((float)((statusA[0] + statusA[1] + statusA[2] + statusA[3])/4)/1000);
	                statusA[0] = milliAmps;
	                String ampString;
	                ampString = Float.toString((float)statusA[0]/1000);
	                SprogSlotMonFrame.instance().updateStatus(ampString);
	              }
              }
              state = SlotThreadState.IDLE;
              break;
            }
          }
        }
      }
    }

    /**
     * Get the next packet to be transmitted. returns null if no packet
     *
     * @return byte[]
     */
    private byte [] getNextPacket() {
      SprogSlot s;
      
      if (getInUseCount() == 0) return null;
      while (slots.get(currentSlot).isFree()) {
        currentSlot++;
        currentSlot = currentSlot%SprogConstants.MAX_SLOTS;
      }
      s = slots.get(currentSlot);

	  currentSlot = currentSlot % SprogConstants.MAX_SLOTS;
	  currentSlot ++;
      if (s.isFinished()) {
    	  notifySlotListeners(s);
    	  return null;
      } 

      return s.getPayload();
    }

    /*
     * Needs to listen to replies
     * Need to implement asynch replies for overload & notify power manager
     *
     * How does POM work??? how does programmer send packets??
     */

    public void notifyMessage(SprogMessage m) {
//        log.error("message received unexpectedly: "+m.toString());
    }

    private SprogReply replyForMe;

    public void notifyReply(SprogReply m) {
      replyForMe = m;
      log.debug("reply received: "+m.toString());
      if (m.isUnsolicited() && m.isOverload()){
        log.error("Overload");

        // *** turn power off

      }
      if (awaitingReply) {
        synchronized(this) {
          replyReceived = true;
          awaitingReply = false;
        }
      }
    }

    /**
     * Provide a count of the slots in use
     */
    public int getInUseCount() {
        int result = 0;
        for (SprogSlot s : slots ) {
        	if (!s.isFree()) result ++;
        }
        return result;
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogCommandStation.class.getName());
}


/* @(#)SprogSlotManager.java */
