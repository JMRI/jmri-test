/** 
 * LocoNetSlot.java
 *
 * Description:		<describe the LocoNetSlot class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package loconet;

import loconet.LocoNetMessage;
import loconet.LocoNetException;

public class LocoNetSlot {

// accessors to specific information
	public int getSlot() { return slot;}  // cannot modify the slot number once created
	
// status bits
	// decoder mode
	// possible values are  DEC_MODE_128A, DEC_MODE_28A, DEC_MODE_128, 
	//						DEC_MODE_14, DEC_MODE_28TRI, DEC_MODE_28  
	public int decoderType() 	{ return stat&LnConstants.DEC_MODE_MASK;}
	
	// slot status
	// possible values are LOCO_IN_USE, LOCO_IDLE, LOCO_COMMON, LOCO_FREE 
	public int slotStatus() 	{ return stat&LnConstants.LOCOSTAT_MASK; }	

	// consist status
	// possible values are CONSIST_MID, CONSIST_TOP, CONSIST_SUB, CONSIST_NO
	public int consistStatus() 	{ return stat&LnConstants.CONSIST_MASK; }	
	
	// direction and functions
	public boolean isForward()	{ return 0!=(dirf&LnConstants.DIRF_DIR); }
	public boolean isF0()		{ return 0!=(dirf&LnConstants.DIRF_F0); }
	public boolean isF1()		{ return 0!=(dirf&LnConstants.DIRF_F1); }
	public boolean isF2()		{ return 0!=(dirf&LnConstants.DIRF_F2); }
	public boolean isF3()		{ return 0!=(dirf&LnConstants.DIRF_F3); }
	public boolean isF4()		{ return 0!=(dirf&LnConstants.DIRF_F4); }
	public boolean isF5()		{ return 0!=(snd&LnConstants.SND_F5); }
	public boolean isF6()		{ return 0!=(snd&LnConstants.SND_F6); }
	public boolean isF7()		{ return 0!=(snd&LnConstants.SND_F7); }
	public boolean isF8()		{ return 0!=(snd&LnConstants.SND_F8); }
	
	// loco address, speed
	public int locoAddr()   { return addr; }
	public int speed()      { return spd; }
	
	// global track status should be reference through SlotManager
	
// create a specific slot
	public void LocoNetSlot(int slotNum)  { slot = slotNum;}
	public void LocoNetSlot(LocoNetMessage l) throws LocoNetException { 
		slot = l.getElement(1);
		setSlot(l);
	}

// methods to interact with LocoNet 
	public void setSlot(LocoNetMessage l) throws LocoNetException { // exception if message can't be parsed 
		// check valid
		if ( l.getOpCode() != LnConstants.OPC_SL_RD_DATA
			 && l.getElement(1) != 0x0E )
					throw new LocoNetException();

		// valid, so fill contents
		slot = l.getElement(2);
		stat = l.getElement(3);
		addr = l.getElement(4)+128*l.getElement(9);
		spd =  l.getElement(5);
		dirf = l.getElement(6);
		trk =  l.getElement(7);
		ss2 =  l.getElement(8);
		// item 9 is add2
		snd =  l.getElement(10);
		id =   l.getElement(11)+128*l.getElement(12);
		}
		
	public LocoNetMessage writeSlot() { 
		LocoNetMessage l = new LocoNetMessage(13); 
		l.setOpCode( LnConstants.OPC_WR_SL_DATA );
		l.setElement(1, 0x0E);
		l.setElement(2, slot);
		l.setElement(3, stat);
		l.setElement(4, addr & 0x7F); l.setElement(9, (addr/128)&0x7F);
		l.setElement(5, spd);
		l.setElement(6, dirf);
		l.setElement(7, trk);
		l.setElement(8, ss2);
		// item 9 is add2
		l.setElement(10, snd);
		l.setElement(11, id&0x7F); l.setElement(12, (id/128)&0x7F );
		return l;
		}

// data values to echo slot contents
	private int slot;   // <SLOT#> is the number of the slot that was read.
	private int stat;	// <STAT> is the status of the slot
	private int addr;	// full address of the loco, made from
						//    <ADDR> is the low 7 (0-6) bits of the Loco address
						//    <ADD2> is the high 7 bits (7-13) of the 14-bit loco address
	private int spd;	// <SPD> is the current speed (0-127)
	private int dirf;	// <DIRF> is the current Direction and the setting for functions F0-F4
	private int trk;	// <TRK> is the global track status
	private int ss2;	// <SS2> is the an additional slot status
	private int snd; 	// <SND> is the settings for functions F5-F8
	private int id;		// throttle id, made from 
						//     <ID1> and <ID2> normally identify the throttle controlling the loco
}


/* @(#)LocoNetSlot.java */
