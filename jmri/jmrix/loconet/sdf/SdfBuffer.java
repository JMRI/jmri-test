// SdfBuffer.java

package jmri.jmrix.loconet.sdf;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide tools for reading, writing and accessing
 * Digitrax SPJ files
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

public class SdfBuffer {

    // jmri.util.StringUtil.hexStringFromBytes
    
    public SdfBuffer(byte[] buffer) {
        this.buffer = buffer;
        loadArray();
    }
    
    public SdfBuffer(String name) throws IOException {
        File file = new File(name);
        int length = (int)file.length();
        
        InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
        
        // Assume we can get all this in memory
        buffer = new byte[length];
        
        for (int i=0; i<length; i++) {
            buffer[i] = (byte)(s.read()&0xFF);
        }
        loadArray();
    }


    byte[] buffer;
    
    
    private int index;
    
    public void resetIndex() { index = 0; }
    public int getAtIndex() { return buffer[index]&0xFF; }
    public int getAtIndexAndInc() { return buffer[index++]&0xFF; }
    public boolean moreData() { return index<buffer.length; }
    
    public String toString() {
        String out ="";
        for (int i = 0; i<ops.size(); i++) {
            SdfMacro m = (SdfMacro)ops.get(i);

            out += m.allInstructionString("    ");
        }
        return out;
    }
    
    public List getArray() { return ops; }
    
    void loadArray() {
        resetIndex();
        ops = new ArrayList();
        while (moreData()) {
            SdfMacro m = SdfMacro.decodeInstruction(this);
            ops.add(m);
        }
    }
    
    ArrayList ops;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SdfBuffer.class.getName());

}

/* @(#)SdfBuffer.java */
