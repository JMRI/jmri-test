// NceCommand.java

package jmri.jmrix.nce;

import jmri.NmraPacket;

/**
 * 
 * From NCE System notes for version March 1, 2007
 * 
 * New 0xAD command sends accessory or signal packets.
 * This command can also issue NCE macros
 * Command Format: 0xAD <addr_h> <addr_l> <op_1> <data_1>
 * Addr_h and Addr_l are the accessory/signal address as a
 * normal binary number (NOT in DCC format).
 * Ex: Accessory Address 1 = 0x00 0x01 (hi byte first)
 * Ex: Accessory Address 2 = 0x00 0x02 (hi byte first)
 * Ex: Accessory Address 513 = 0x02 0x01 (hi byte first)
 * NOTE: accy/signal address 0 is not a valid address
 *
 * Op_1 Data_1 		Operation description
 *	01 	0-255 		NCE macro number 0-255
 *	02 	0-255 		Duplicate of Op_1 command 01
 *	03 	0 			Accessory Normal direction (ON)
 *	04 	0 			Accessory Reverse direction (OFF)
 *	05 	0-1f 		Signal Aspect 0-31
 *	06-7f 			reserved reserved
 *
 *	Returns: ! = success
 *	1 = bad accy/signal address
 *
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.2 $
 */

public class NceCommand {
	
		
	public static byte[] accDecoderPkt(int number, boolean closed) {
		
		if (number < 1 || number>2044) {
			log.error("invalid accessory address "+number);
			return null;	
		}	
		
		byte op_1;
		if (closed) op_1 = 0x03; else op_1 = 0x04;
		
		int addr_h = number/256;
		int addr_l = number & 0xFF;
		
		byte [] retVal = new byte[5];
		retVal[0] = (byte) (0xAD); 		//acc_cmd
		retVal[1] = (byte) (addr_h);	//high address
		retVal[2] = (byte) (addr_l);	//low address
		retVal[3] = (byte) (op_1);		//command
		retVal[4] = (byte) 0; 			//zero out last byte for acc

	return retVal;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NmraPacket.class.getName());
}
/* @(#)NceCommand.java */

