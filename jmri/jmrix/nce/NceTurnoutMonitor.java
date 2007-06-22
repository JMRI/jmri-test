//NceTournoutMonitor.java

package jmri.jmrix.nce;

import jmri.InstanceManager;
import jmri.Turnout;

/**
 * 
 * Polls NCE Command Station for turnout discrepancies
 * 
 * This implementation reads the NCE Command Station (CS) memory
 * that stores the state of all accessories thrown by cabs or though
 * the comm port using the new binary switch command.  The accessory
 * states are storied in 256 byte array starting at address 0xEC00.
 * 
 * byte 0,   bit 0 = ACCY 1,    bit 1 = ACCY 2
 * byte 1,   bit 0 = ACCY 9,    bit 1 = ACCY 10
 * 
 * byte 255, bit 0 = ACCY 2041, bit 3 = ACCY 2044 (last valid addr)
 * 
 * ACCY bit = 0 turnout thrown, 1 = turnout closed
 * 
 * Block reads (16 bytes) of the NCE CS memory are performed to 
 * minimize impact to the NCE CS.  Data from the CS is then compared 
 * to the JMRI turnout (accessory) state and if a discrepancy is discovered,
 * the JMRI turnout state is modified to match the CS.       
 * 
 *  
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.15 $
 */

public class NceTurnoutMonitor implements NceListener{

    // scope constants
    private static final int CS_ACCY_MEMORY = 0xEC00; 	// Address of start of CS accessory memory 
    private static final int NUM_BLOCK = 16;            // maximum number of memory blocks
    private static final int BLOCK_LEN = 16;            // number of bytes in a block
    private static final int REPLY_LEN = BLOCK_LEN;		// number of bytes read
    private static final int NCE_ACCY_THROWN = 0;		// NCE internal accessory "REV"
    private static final int NCE_ACCY_CLOSE = 1;		// NCE internal accessory "NORM"
    static final int POLL_TIME = 100;					// Poll NCE memory every 100 msec plus xmt time (~70 msec)
 
    // object state
    private int currentBlock;							// used as state in scan over active blocks
    private int numTurnouts = 0;						// number of NT turnouts known by NceTurnoutMonitor 
    private int numActiveBlocks = 0;
    private int savedFeedbackChanges = 0;				// number of feedback changes known by NceTurnoutMonitor
         
    // cached work fields
    boolean [] newTurnouts = new boolean [NUM_BLOCK];	// used to sync poll turnout memory
    boolean [] activeBlock = new boolean [NUM_BLOCK];	// When true there are active turnouts in the memory block
    boolean [] validBlock = new boolean [NUM_BLOCK];	// When true received block from CS
    byte [] csAccMemCopy = new byte [NUM_BLOCK*BLOCK_LEN];	// Copy of NCE CS accessory memory
    byte [] dataBuffer = new byte [NUM_BLOCK*BLOCK_LEN];	// place to store reply messages
    
    private boolean recData = false;					// when true, valid recieve data
    
    Thread NceTurnoutMonitorThread;
    boolean turnoutUpdateValid = true;					// keep the thread running
    
    // debug final
    static final boolean debugTurnoutMonitor = false;	// Control verbose debug
        
    public NceMessage pollMessage() {
    	
    	if (NceMessage.getCommandOptions() < NceMessage.OPTION_2006 )return null;	//Only 2007 CS EPROMs support polling
    	if (NceEpromChecker.nceUSBdetected)return null;								//Can't poll USB!
    	if (NceTurnout.getNumNtTurnouts() == 0)return null;							//No work!
    	
    	// User can change a turnout's feedback to MONITORING, therefore we need to rescan
    	// This doesn't occur very often, so we'll assume the change was to MONITORING
		if (savedFeedbackChanges != NceTurnout.getNumFeedbackChanges()) {
			savedFeedbackChanges = NceTurnout.getNumFeedbackChanges();
			numTurnouts = -1; // force rescan
		}
 
    	// See if the number of turnouts now differs from the last scan
        if (numTurnouts != NceTurnout.getNumNtTurnouts()) {
            numTurnouts = NceTurnout.getNumNtTurnouts();	
            
            // Determine what turnouts have been defined and what blocks have active turnouts
            for (int block = 0; block < NUM_BLOCK; block++){

            	newTurnouts[block] = true;			// Block may be active, but new turnouts may have been loaded	
            	if (activeBlock[block] == false) {  // no need to scan once known to be active

            		for (int i = 0; i < 128; i++) { // Check 128 turnouts per block 
            			int NTnum = 1 + i + (block*128);
            			Turnout mControlTurnout = InstanceManager.turnoutManagerInstance().getBySystemName("NT"+ NTnum);
            			if (mControlTurnout != null){
            				int tFeedBack = mControlTurnout.getFeedbackMode();
            				if (tFeedBack==Turnout.MONITORING) {
            					activeBlock[block] = true;	// turnout found, block is active forever
            					numActiveBlocks++;
            					break; 						// don't check rest of block
            				}
            			}
            		}
            	}

            }
        }     
               
        // See if there's any poll messages needed
        if (numActiveBlocks<=0) {
            return null; // to avoid immediate infinite loop
        }
         	
    	// Set up a separate thread to notify state changes in turnouts
		// This protects pollMessage (xmt) and reply threads if there's lockup!
		if (NceTurnoutMonitorThread == null) {
			NceTurnoutMonitorThread = new Thread(new Runnable() {
				public void run() {
					turnoutUpdate();
				}
			});
			NceTurnoutMonitorThread.setName("NCE Turnout Monitor");
			NceTurnoutMonitorThread.setPriority(Thread.MIN_PRIORITY);
			NceTurnoutMonitorThread.start();
		}
        
        // now try to build a poll message if there are any defined turnouts to scan
        while (true) { // will break out when next block to poll is found
			currentBlock++;
			if (currentBlock >= NUM_BLOCK)
				currentBlock = 0;

			if (activeBlock[currentBlock]) {
				if (debugTurnoutMonitor && log.isDebugEnabled())
					log.debug("found turnouts block " + currentBlock);

				// Read NCE CS memory
				int nceAccAddress = CS_ACCY_MEMORY + currentBlock * BLOCK_LEN;
				byte[] bl = NceBinaryCommand.accMemoryRead(nceAccAddress);
				NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_LEN);
				return m;
			}
		}
	}
    
    public void message(NceMessage m){
        if (log.isDebugEnabled()) {
            log.debug("unexpected message" );
        }	
    }
    
    public void reply(NceReply r) {
		if (r.getNumDataElements() == REPLY_LEN) {

			if (log.isDebugEnabled() & debugTurnoutMonitor == true) {
				log.debug("memory poll reply received for memory block "
						+ currentBlock + ": " + r.toString());
			}
			// Copy recieve data into buffer and process later
			for (int i = 0; i < REPLY_LEN; i++){
				dataBuffer[i + currentBlock * BLOCK_LEN] = (byte)r.getElement(i);
			}
			validBlock [currentBlock] = true;
			recData = true;
			//wake up turnout monitor thread
			synchronized (this) {
				notify();
			}
		}else{
				log.warn("wrong number of read bytes for memory poll");
		}
        
    } 
    
    // Thread to process turnout changes, protects receive and xmt threads
    // from hanging by the method setKnownStateFromCS
    private void turnoutUpdate() {
		while (turnoutUpdateValid) {
			// if nothing to do, sleep
			if (!recData) {
				synchronized (this) {
					try {
						wait(POLL_TIME*5);
					} catch (InterruptedException e) {};
				}
				// process rcv buffer and update turnouts
			} else {
				recData = false;
				// scan all valid replys from CS
				for (int block = 0; block < NUM_BLOCK; block++) {
					if (validBlock[block]) {
						// Compare NCE CS memory to local copy, change state if
						// necessary
						// 128 turnouts checked per NCE CS memory read (block)
						for (int byteIndex = 0; byteIndex < REPLY_LEN; byteIndex++) { 
							// CS memory byte
							byte recMemByte = dataBuffer[byteIndex + block * BLOCK_LEN]; 
							if (recMemByte != csAccMemCopy[byteIndex + block * BLOCK_LEN] || newTurnouts[block] == true) {

								// load copy into local memory
								csAccMemCopy[byteIndex + block * BLOCK_LEN] = recMemByte;

								// search this byte for active turnouts
								for (int i = 0; i < 8; i++) {
									int NTnum = 1 + i + byteIndex * 8 + (block * 128);
									
									// Nasty bug in March 2007 EPROM, accessory
									// bit 3 is shared by two accessories and 7
									// MSB isn't used and the bit map is skewed by
									// one bit, ie accy num 2 is in bit 0, should
									// have been in bit 1.
									if (NceEpromChecker.nceEpromMarch2007) {
										// bit 3 is shared by two accessories!!!!
										if (i == 3)
											monitorAction(NTnum - 3, recMemByte,	i);
										
										NTnum++; // skew fix 
										if (i == 7)
											break; // bit 7 is not used!!!
									}
									monitorAction(NTnum, recMemByte, i);
								}
							}
						}
						newTurnouts[block] = false;
					}
				}
			}
		}
	}

    
    private void monitorAction(int NTnum, int recMemByte, int bit) {

		NceTurnout rControlTurnout = (NceTurnout) InstanceManager
				.turnoutManagerInstance().getBySystemName("NT" + NTnum);
		if (rControlTurnout == null)
			return;

		int tState = rControlTurnout.getKnownState();

		if (debugTurnoutMonitor && log.isDebugEnabled()) {
			log.debug("turnout exists NT" + NTnum + " state: " + tState
					+ " Feed back mode: " + rControlTurnout.getFeedbackMode());
		}

		// Show the byte read from NCE CS
		if (debugTurnoutMonitor && log.isDebugEnabled()) {
			log.debug("memory byte: " + Integer.toHexString(recMemByte & 0xFF));
		}

		// test for closed or thrown, 0 = closed, 1 = thrown
		int NceAccState = (recMemByte >> bit) & 0x01;
		if (NceAccState == NCE_ACCY_THROWN && tState != Turnout.THROWN) {
			if (log.isDebugEnabled()) {
				log.debug("turnout discrepancy, need to THROW turnout NT"
						+ NTnum);
			}
			// change JMRI's knowledge of the turnout state to match observed
			rControlTurnout.setKnownStateFromCS(Turnout.THROWN);
		}

		if (NceAccState == NCE_ACCY_CLOSE && tState != Turnout.CLOSED) {
			if (log.isDebugEnabled()) {
				log.debug("turnout discrepancy, need to CLOSE turnout NT"
						+ NTnum);
			}
			// change JMRI's knowledge of the turnout state to match observed
			rControlTurnout.setKnownStateFromCS(Turnout.CLOSED);
		}
	}
 
        
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutMonitor.class.getName());	
    
}
/* @(#)NceTurnoutMonitor.java */




