package jmri.jmrix.mrc;

import jmri.jmrix.AbstractMessage;
/**
 *
 * @author kevin
 */
public class MrcPackets {
    
    private static final int setClockRatioLength = 10;
    public static final int setClockAmPmCmd = 50;
    public static final int functionGroup3PacketCmd = 84;
    public static final int readCVCmd = 67;
    private static final int setClockAmPmLength = 10;
    public static final int setClockRatioCmd = 18;
    public static final int readDecoderAddressCmd = 66;
    private static final int writeCVPROGLength = 8;
    private static final int setClockTimeLength = 10;
    public static final int writeCVPOMCmd = 86;
    static final int throttlePacketLength = 10; //length of packet less the header
    public static final int functionGroup1PacketCmd = 52;

    public static final int setClockTimeCmd = 19;
    static final int functionGroupLength = 8;
    static final int[] setClockAmPmHeader = new int[]{setClockAmPmCmd, 0, setClockAmPmCmd, 0};
    public static final int functionGroup6PacketCmd = 164;
    public static final int functionGroup5PacketCmd = 132;
    static final int[] functionGroup6PacketHeader = new int[]{functionGroup6PacketCmd, 0, functionGroup6PacketCmd, 0};
    
    public static final int functionGroup2PacketCmd = 68;
    static final int[] functionGroup2PacketHeader = new int[]{functionGroup2PacketCmd, 0, functionGroup2PacketCmd, 0};
    
    static final int[] functionGroup1PacketHeader = new int[]{functionGroup1PacketCmd, 0, functionGroup1PacketCmd, 0};
    
    public static final int throttlePacketCmd = 37;
    static final int[] throttlePacketHeader = new int[]{throttlePacketCmd, 0, throttlePacketCmd, 0};
    
    static final int[] writeCVPOMHeader = new int[]{writeCVPOMCmd, 0, writeCVPOMCmd, 0};
    
    public static final int functionGroup4PacketCmd = 116;
    static final int[] functionGroup4PacketHeader = new int[]{functionGroup4PacketCmd, 0, functionGroup4PacketCmd, 0};
    
    static final int[] setClockRatioHeader = new int[]{setClockRatioCmd, 0, setClockRatioCmd, 0};
    
    
    static final int[] readCVHeader = new int[]{readCVCmd, 0, readCVCmd, 0};
    static final int[] readDecoderAddress = new int[]{readDecoderAddressCmd, 0, readDecoderAddressCmd, 0, readDecoderAddressCmd, 0};
    
    static final int[] functionGroup5PacketHeader = new int[]{functionGroup5PacketCmd, 0, functionGroup5PacketCmd, 0};

    static final int[] setClockTimeHeader = new int[]{setClockTimeCmd, 0, setClockTimeCmd, 0};
    public static final int writeCVPROGCmd = 36;
    private static final int readCVLength = 6;
    static final int[] writeCVPROGHeader = new int[]{writeCVPROGCmd, 0, writeCVPROGCmd, 0};
    static final int[] functionGroup3PacketHeader = new int[]{functionGroup3PacketCmd, 0, functionGroup3PacketCmd, 0};
    
    private static final int writeCVPOMLength = 12;
    
    public static final int locoSoleControlCode = 34;
    static final int[] locoSoleControl = new int[]{locoSoleControlCode, 0, locoSoleControlCode, 0}; //Reply indicates that we are the sole controller of the loco
    
    public static final int locoDblControlCode = 221;
    static final int[] locoDblControl = new int[]{locoDblControlCode, 0, locoDblControlCode, 0}; //Reply indicates that another throttle also has controll of the loco
    
    public static final int progCmdSentCode = 51;
    static final int[] progCmdSent = new int[]{progCmdSentCode, 0, progCmdSentCode, 0};
    
    public static final int readCVHeaderReplyCode = 102;
    static final int[] readCVHeaderReply = new int[]{readCVHeaderReplyCode, 0, readCVHeaderReplyCode, 0};
    
    public static final int goodCmdRecievedCode = 85;
    static final int readCVPacketLength = 4; //need to double check the length of this packet
    static final int[] goodCmdRecieved = new int[]{goodCmdRecievedCode, 0, goodCmdRecievedCode, 0};
    
    public static final int badCmdRecievedCode = 238;
    static final int[] badCmdRecieved = new int[]{badCmdRecievedCode, 0, badCmdRecievedCode, 0};
    
    

    public static int getWriteCVPROGPacketLength() {
        return writeCVPROGHeader.length + writeCVPROGLength;
    }

    public static int getWriteCVPOMPacketLength() {
        return writeCVPOMHeader.length + writeCVPOMLength;
    }

    public static int getSetClockRatioPacketLength() {
        return setClockRatioLength;
    }

    public static int getSetClockAmPmPacketLength() {
        return setClockAmPmLength;
    }

    public static int getFunctionPacketLength() {
        return functionGroup1PacketHeader.length + functionGroupLength;
    }

    public static int getReadDecoderAddressLength() {
        return readDecoderAddress.length;
    }

    public static int getSetClockTimePacketLength() {
        return setClockTimeLength;
    }

    public static int getThrottlePacketLength() {
        return throttlePacketHeader.length + throttlePacketLength;
    }

    public static int getReadCVPacketLength() {
        return readCVHeader.length + readCVLength;
    }

    public static boolean startsWith(jmri.jmrix.AbstractMRReply source, int[] match) {
        if (match.length > (source.getNumDataElements())) {
            return false;
        }
        for (int i = 0; i < match.length; i++) {
            if ((source.getElement(i) & 255) != (match[i] & 255)) {
                return false;
            }
        }
        return true;
    }

    public static int getReadCVPacketReplyLength() {
        return readCVHeaderReply.length + readCVPacketLength;
    }

    static protected int provideLocoId(int hi, int lo) {
        if (hi == 0) {
            return lo;
        } else {
            hi = (((hi & 255) - 192) << 8);
            hi = hi + (lo & 255);
            return hi;
        }
    }
    
        //Need to test toString() for POM
    static public String toString(AbstractMessage m){
        StringBuilder txt = new StringBuilder();
        if((m.getNumDataElements() <4) || (m.getNumDataElements()>=4 && m.getElement(0)!=m.getElement(2) && m.getElement(1)!=0x01)){
            //byte 0 and byte 2 should always be the same except for a clock update packet.
        	if (m.getNumDataElements() < 4) {
        		txt.append("Short Packet");
        	} else {
        		txt.append("Error in Packet");
        	}
            for (int i=0;i<m.getNumDataElements(); i++) {
                txt.append(" ");
                txt.append(jmri.util.StringUtil.twoHexFromInt(m.getElement(i)&0xFF));
            }
        } else {
	        switch (m.getElement(0)&0xFF) {
	        case setClockRatioCmd:
	        	txt.append("Set Clock Ratio: " + m.getElement(4));
	        	break;
	        case setClockTimeCmd:
	        	txt.append("Set Clock Time: " + m.getElement(4) + ":" + m.getElement(6));
	        	break;
	        case setClockAmPmCmd:
	        	txt.append("Set Clock AM/PM");
	        	break;
	        case MrcPackets.throttlePacketCmd:
	        	if (m.getElement(4) != 0) {
	            	txt.append("Loco (L)");
	        	} else {
	            	txt.append("Loco (S)");
	        	}
	        	txt.append(Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))));
	        	if ((m.getElement(8) & 0x80) == 0x80) {
	        		txt.append(" Fwd ");
	        	} else {
	        		txt.append(" Rev ");
	        	}
	        	txt.append(" Speed: " + Integer.toString((m.getElement(8) & 0x80)));
	    		break;
	        case functionGroup1PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 1");
	        	txt.append(" F1 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F2 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F3 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F4 " + Integer.toString(m.getElement(8) & 0x08));
	        	txt.append(" F0 " + Integer.toString(m.getElement(8) & 0x10));
	    		break;
	        case functionGroup2PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 2");
	        	txt.append(" F5 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F6 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F7 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F8 " + Integer.toString(m.getElement(8) & 0x08));
	    		break;
	        case functionGroup3PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 3");
	        	txt.append(" F9 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F10 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F11 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F12 " + Integer.toString(m.getElement(8) & 0x08));
	    		break;
	        case functionGroup4PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 4");
	        	txt.append(" F13 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F14 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F15 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F16" + Integer.toString(m.getElement(8) & 0x08));
	    		break;
	        case functionGroup5PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 5");
	        	txt.append(" F17 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F18 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F19 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F20 " + Integer.toString(m.getElement(8) & 0x08));
	    		break;
	        case functionGroup6PacketCmd:
	        	txt.append("Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))) + " Group 6");
	        	txt.append(" F21 " + Integer.toString(m.getElement(8) & 0x01));
	        	txt.append(" F22 " + Integer.toString(m.getElement(8) & 0x02));
	        	txt.append(" F23 " + Integer.toString(m.getElement(8) & 0x04));
	        	txt.append(" F24 " + Integer.toString(m.getElement(8) & 0x08));
	        	txt.append(" F25 " + Integer.toString(m.getElement(8) & 0x10));
	        	txt.append(" F26 " + Integer.toString(m.getElement(8) & 0x20));
	        	txt.append(" F27 " + Integer.toString(m.getElement(8) & 0x40));
	        	txt.append(" F28 " + Integer.toString(m.getElement(8) & 0x80));
	    		break;
	        case readCVCmd:
	        	int cv = ((m.getElement(4)&0xff) << 8) + (m.getElement(6)&0xff);
	        	txt.append("Read CV " + Integer.toString(cv));
	    		break;
	        case readDecoderAddressCmd:
	        	txt.append("Read Decoder Address ");
	    		break;
	        case writeCVPROGCmd:
	        	txt.append("Write PROG CV Loco " + Integer.toString(provideLocoId(m.getElement(4), m.getElement(6))));
	        	txt.append(Integer.toString(m.getElement(10)&0xff));
	        	txt.append("=");
	        	txt.append(Integer.toString(m.getElement(8)&0xff));
	    		break;
	        case writeCVPOMCmd:
	        	txt.append("Write POM CV ");
	        	txt.append(Integer.toString(m.getElement(4) << 8) + m.getElement(6));
	        	txt.append("=");
	        	txt.append(Integer.toString(m.getElement(8)&0xff));
	    		break;
            case readCVHeaderReplyCode:
                txt.append("Read CV");
                break;
            case badCmdRecievedCode:
                txt.append("Bad Cmd Ack");
                break;
            case goodCmdRecievedCode:
                txt.append("Good Cmd Ack");
                break;
            case progCmdSentCode:
                txt.append("Pgm Cmd Sent");
                break;
            case locoSoleControlCode:
                txt.append("Single Throttle");
                break;
            case locoDblControlCode:
                txt.append("Multiple Throttle");
                break;

	        default:
                if(m.getNumDataElements()==6){
                    if(m.getElement(0)==m.getElement(2)&&m.getElement(0)==m.getElement(4)){
                        txt.append("Poll to Cab " + m.getElement(0));
                    } else if (m.getElement(0)==0 && m.getElement(1)==0x01){
                        txt.append("Clock Update");
                    }
                } else if(m.getNumDataElements()==4 && m.getElement(0)==0x00 && m.getElement(1)==0x00){
                    txt.append("No Data From Last Cab");
                } else {
                    txt.append("Unk Cmd Code:");
                    for (int i=0;i<m.getNumDataElements(); i++) {
                        txt.append(" ");
                        txt.append(jmri.util.StringUtil.twoHexFromInt(m.getElement(i)&0xFF));
                    }
                }
	        	break;
	        }
        }
        return txt.toString();
    }

    //Principle last two ar ehte checksum, the first four indicate the packet type and ignored.
    //the rest should be XOR'd.
    static public boolean validCheckSum(MrcReply mrcReply) {
        if (mrcReply.getNumDataElements() > 6) {
            int result = 0;
            for (int i = 4; i < mrcReply.getNumDataElements() - 2; i++) {
                result = (mrcReply.getElement(i) & 255) ^ result;
            }
            if (result == (mrcReply.getElement(mrcReply.getNumDataElements() - 2) & 255)) {
                return true;
            }
        }
        return false;
    }
    
}
