/**
 * DccConsistManager.java
 *
 * Description:           The Default Consist Manager which uses the 
 *                        DccConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2003
 * @version               $ version 1.00 $
 */


package jmri;

import java.util.Enumeration;

import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.ArrayList;

import jmri.DccConsist;
import jmri.ConsistListener;

public class DccConsistManager implements ConsistManager{

	private Hashtable ConsistTable = null;

	private ArrayList ConsistList = null;

	public DccConsistManager(){
	      ConsistTable = new Hashtable();
	      ConsistList = new ArrayList();
	}

	// Clean up Local Storage
	public void dispose() {
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
	public Consist getConsist(int address){
		String Address=Integer.toString(address);
		if(ConsistTable.containsKey(Address)) {
			return((Consist)ConsistTable.get(Address));
		} else {
			DccConsist consist;
			consist = new DccConsist(address);
			ConsistTable.put(Address,consist);
		 	ConsistList.add(Address);
			return(consist);
		}
	   }
	
	// remove the old Consist
	public void delConsist(int address){
		String Address=Integer.toString(address);
		((DccConsist)ConsistTable.get(Address)).dispose();
		ConsistTable.remove(Address);
		ConsistList.remove(Address);
	}

	/**
         *    This implementation does NOT support Command Station 
	 *    consists, so return false.
         **/
        public boolean isCommandStationConsistPossible() { return false; }

        /**
         *    Does a CS consist require a seperate consist address?
	 *    This implemenation does not support Command Station 
	 *    consists, so return false
         **/
        public boolean csConsistNeedsSeperateAddress() { return false; }

	/**
  	 *  Return the list of consists we know about.
	 **/
	public ArrayList getConsistList() { return ConsistList; }

	public String decodeErrorCode(int ErrorCode){
		StringBuffer buffer = new StringBuffer("");
		if ((ErrorCode & ConsistListener.NotImplemented) != 0)
					buffer.append("Not Implemented ");
		if ((ErrorCode & ConsistListener.OPERATION_SUCCESS) != 0)
					buffer.append("Operation Completed Successfully ");
		if ((ErrorCode & ConsistListener.CONSIST_ERROR) != 0)
					buffer.append("Consist Error ");
		if ((ErrorCode & ConsistListener.LOCO_NOT_OPERATED) != 0)
					buffer.append("Address not controled by this device.");
		if ((ErrorCode & ConsistListener.ALREADY_CONSISTED) != 0)
					buffer.append("Locomotive already consisted");
		if ((ErrorCode & ConsistListener.NOT_CONSISTED) != 0)
					buffer.append("Locomotive Not Consisted ");
		if ((ErrorCode & ConsistListener.NONZERO_SPEED) != 0)
					buffer.append("Speed Not Zero ");
		if ((ErrorCode & ConsistListener.NOT_CONSIST_ADDR) != 0)
					buffer.append("Address Not Conist Address ");
		if ((ErrorCode & ConsistListener.DELETE_ERROR) != 0)
					buffer.append("Delete Error ");
		if ((ErrorCode & ConsistListener.STACK_FULL) != 0)
					buffer.append("Stack Full ");

		String  retval = buffer.toString();
		if (retval.equals(""))
		   return "Unknown Status Code: " + ErrorCode;
		else return retval;
	}


}
