// ChannelStart.java

package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.3 $
 */

class ChannelStart extends SdfMacro {

    public ChannelStart(int number) {
        this.number = number;      
    }
    
    public String name() {
        return "CHANNEL_START";
    }
    
    int number;
        
    public int length() { return 2;}
    
    static SkemeStart dummySkemeStart = new SkemeStart(0,0); // to get name
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0x81) return null;
        buff.getAtIndexAndInc(); // drop opcode
        ChannelStart result = new ChannelStart(buff.getAtIndexAndInc());

        SdfMacro next;
        while (buff.moreData()) {
            // beware of recursion in this part of the code
            int i = buff.getIndex();
            next=decodeInstruction(buff);

            // check for end of channel
            if (result.name().equals(next.name())
                || result.name().equals(dummySkemeStart.name())) {
                // time to start the next one; 
                // decrement index to rescan this, and 
                // return via break
                buff.restoreIndex(i);
                break;
            }
            if (result.children==null) result.children = new ArrayList(); // make sure it's initialized
            result.children.add(next);
        }
        
        return result;
    }
    
    public String toString() {
        linestart = "    "; // shouldn't be here, needs to be stacked later
        String output = linestart+name()+' '+number+'\n';
        linestart = "      ";
        if (children==null) return output;
        for (int i = 0; i<children.size(); i++) {
            output+= ((SdfMacro)children.get(i)).toString();
        }
        return output;
    }
}

/* @(#)ChannelStart.java */
