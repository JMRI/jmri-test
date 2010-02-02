/* SprogSlotManager.java */

package jmri.jmrix.sprog;

import jmri.CommandStation;
import jmri.NmraPacket;

import java.util.Vector;
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
 * uses enum, etc.</P>
 * @author	Bob Jacobsen  Copyright (C) 2001, 2003
 *              Andrew Crosland         (C) 2006 ported to SPROG
 * @version     $Revision: 1.8 $
 */
public class SprogSlotManager extends SprogCommandStation implements SprogListener, CommandStation, Runnable {

	private enum SlotThreadState {IDLE, WAITING_FOR_REPLY, SEND_STATUS_REQUEST, WAITING_FOR_STATUS_REPLY}
	
    public SprogSlotManager() {
        // error if more than one constructed?
        if (self != null) log.debug("Creating too many SlotManager objects");
        SprogTrafficController.instance().addSprogListener(this);
    }

    /**
     * The SPROG implementation has one queue for both loco refresh packets and
     * temporary accessory and function packets.
     *
     * Information on slot state is stored in a SprogQueue object.
     * This is declared final because we never need to modify the queue
     * itself, just its contents.
     */
    final private SprogQueue _Queue = new SprogQueue();

    /**
     * Send a DCC packet to the rails. This implements the CommandStation interface.
     *
     * @param packet
     */
    public void sendPacket(byte[] packet) {
        if (packet.length<=1) log.error("Invalid DCC packet length: "+packet.length);
        if (packet.length>=7) log.error("Only 6-byte packets accepted: "+packet.length);
        log.debug("Send packet length "+packet.length);

        SprogMessage m = new SprogMessage(packet);
        SprogTrafficController.instance().sendSprogMessage(m, this);
    }

    /**
     * Access the information in a specific slot.
     * @param i  Specific slot, counted starting from zero.
     * @return   The SprogSlot object
     */
    public SprogSlot slot(int i) {return _Queue.slot(i);}

    public void forwardCommandChangeToLayout(int address, boolean closed){
        byte[] payload = NmraPacket.accDecoderPkt(address, closed);
        address=address+10000;
        int s = _Queue.add(address, payload,SprogConstants.S_REPEATS);
        if (s>=0) { notify(slot(s)); }
    
    }
    
    public void function0Through4Packet(int address,
                                        boolean f0, boolean f1, boolean f2,
                                        boolean f3, boolean f4) {
      byte[] payload = jmri.NmraPacket.function0Through4Packet(address,
          (address >= SprogConstants.LONG_START), f0, f1, f2, f3, f4);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void function5Through8Packet(int address,
                                        boolean f5, boolean f6,
                                        boolean f7, boolean f8) {
      byte[] payload = jmri.NmraPacket.function5Through8Packet(address,
          (address >= SprogConstants.LONG_START), f5, f6, f7, f8);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void function9Through12Packet(int address,
                                         boolean f9, boolean f10,
                                         boolean f11, boolean f12) {
      byte[] payload = jmri.NmraPacket.function9Through12Packet(address,
          (address >= SprogConstants.LONG_START), f9, f10, f11, f12);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void speedStep128Packet(int address, int spd, boolean isForward) {
      byte[] payload = jmri.NmraPacket.speedStep128Packet(address,
          (address >= SprogConstants.LONG_START), spd, isForward);
      int s = _Queue.findReplace(address, spd, isForward, payload, -1);
      if (s>=0) { notify(slot(s)); }
    }

    public void opsModepacket(int mAddress, boolean mLongAddr, int cv, int val) {
      byte[] payload = NmraPacket.opsCvWriteByte(mAddress, mLongAddr, cv, val );
      int s = _Queue.add(mAddress, payload, 1000 + SprogConstants.OPS_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void release(int address) {
      int s = _Queue.release(address);
      if (s>=0) {
        notify(slot(s));
      } else {
        log.error("Release for address not in queue"+address);
      }
    }

    /**
     * Send emergency stop to all slots
     */
    public void estopAll() {
    for (int slotNum=0; slotNum<SprogConstants.MAX_SLOTS; slotNum++) {
        SprogSlot s = slot(slotNum);
        if ((s.getRepeat() == -1)
            && s.slotStatus() != SprogConstants.SLOT_FREE
            && s.speed() != 1) {
          estopSlot(slotNum);
        }
      }
    }

    /**
     * Send emergency stop to a slot
     *
     * @param slotNum int
     */
    public void estopSlot(int slotNum) {
      SprogSlot s = slot(slotNum);
      log.debug("Estop slot: "+slotNum+" for address: "+s.locoAddr());
      // Generate a new packet with speed step 1
      byte[] payload = jmri.NmraPacket.speedStep128Packet(s.locoAddr(),
          (s.locoAddr() >= SprogConstants.LONG_START), 1, s.isForward());
      // Replace existing slot
      _Queue.findReplace(s.locoAddr(), 1, s.isForward(), payload, -1);
      notify(s);
    }

    /**
     * method to find the existing SlotManager object, if need be creating one
     */
    static public final SprogSlotManager instance() {
        if (self == null) {
          log.debug("creating a new SprogSlotManager object");
          self = new SprogSlotManager();
        }
        return self;
    }
    static private SprogSlotManager self = null;

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
	protected void notify(SprogSlot s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<SprogSlotListener> v;
        synchronized(this)
            {
                v = (Vector<SprogSlotListener>) slotListeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" SlotListeners about slot for address"
                                            +s.getAddr());
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SprogSlotListener client = v.elementAt(i);
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
              sendPacket(p);

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

    private int currentQ = 0;
    private int oldQ = 0;

    /**
     * Get the next packet to be transmitted. returns length 1 if no packet
     *
     * @return byte[]
     */
    private byte [] getNextPacket() {
      //boolean foundQ = true;
      SprogSlot s;
      byte [] p;
      int rep;
      oldQ = currentQ;
      while (_Queue.slot(currentQ).slotStatus() == SprogConstants.SLOT_FREE) {
        currentQ++;
        currentQ = currentQ%SprogConstants.MAX_SLOTS;
        if (currentQ == oldQ) {
          return null;
        }
      }
      s = _Queue.slot(currentQ);
      p = s.getPayload();
      rep = s.getRepeat();
      if (rep < 1000) {
        // If it's not an ops mode packet step to next slot, otherwise we
        // repeat ops mode packets until exhausted
        currentQ++;
        currentQ = currentQ % SprogConstants.MAX_SLOTS;
      }
      if (s.getRepeat() != -1) {
        // Repeating accessory slot
        if ((s.doRepeat()%1000) == 0) {
          // exhausted
          s.clear();
          notify(s);
        }
      }
      return p;
    }

    /*
     * Needs to listen to replies
     * Need to implement asynch replies for overload & notify power manager
     *
     * How does POM work??? how does programmer send packets??
     */







    public void message(SprogMessage m) {
//        log.error("message received unexpectedly: "+m.toString());
    }

    private SprogReply replyForMe;

    public void reply(SprogReply m) {
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
     * Provide a snapshot of the slots in use
     */
    public int getInUseCount() {
        int result = 0;
        for (int i = 0; i< _Queue.getLength(); i++) {
            if (!_Queue.slot(i).isFree() ) result++;
        }
        return result;
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogSlotManager.class.getName());
}


/* @(#)SprogSlotManager.java */
