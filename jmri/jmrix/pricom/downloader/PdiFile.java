// PdiFile.java

package jmri.jmrix.pricom.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

/**
 * Support for reading PRICOM ".pdi" files
 * <P>
 * The PRICOM format documentation is Copyright 2003, 2005, PRICOM Corp. 
 * They have kindly given permission for this use.
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version             $Revision: 1.2 $
 */
public class PdiFile {

    public PdiFile(File file) {
        this.file = file;       
    }

    File file;
    private InputStream buffIn;

    String comment = "";
    int commentLength;
    
    int lastAddress;
    int address;
    
    int fileLength;
        
    public void open() throws IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(file));
        open(stream);
    }
    
    public void open(InputStream stream) throws IOException {
        buffIn = stream;
        
        // get comment length, comment
        int high= (buffIn.read()&0xFF);
        int low = (buffIn.read()&0xFF);
        commentLength = high*256+low;
        
        StringBuffer buffer = new StringBuffer();
        
        // Note the count is decremented by two in the following.
        // Apparently, the comment length field includes it's own
        // two bytes in the count
        for (int i = 0; i< (commentLength-2); i++) {
            int next = buffIn.read();
            if (next == 0x0d) buffer.append("\n");
            else if (next != 0x0a) buffer.append((char)next);
        }
        
        comment = new String(buffer);
    
        // get data base address
        high= (buffIn.read()&0xFF);
        low = (buffIn.read()&0xFF);
        address = high*256+low;
        System.out.println("address "+high+" "+low);
        
        // get last address to write
        high= (buffIn.read()&0xFF);
        low = (buffIn.read()&0xFF);
        lastAddress = high*256+low;
        System.out.println("length "+high+" "+low);

        fileLength = (int)file.length()-6-commentLength;
        
        System.out.println("lengths: file "+(int)file.length()
                        +", comment "+commentLength
                        +", data "+lastAddress);
    }
    
    /**
     * Return the comment embedded at the front of the file
     */
    public String getComment() {
        return comment;
    }
    
    int length() {
        return fileLength;
    }
    
    /**
     * Get the next n bytes for transmission to the device
     * @param n number of data bytes to include
     * @returns byte buffer, starting with address info and containing data, but not CRC
     */
    public byte[] getNext(int n) {
        byte[] buffer = new byte[n+3+2];
        
        // load header
        if (n == 128) buffer[0] = 60;
        else buffer[0] = 59;
        
        buffer[1] = (byte) ((address>>8)&0xFF);
        buffer[2] = (byte) (address & 0xFF);
        address = address+n;
        
        for (int i = 0; i<n; i++) buffer[2+i] = 0;  // clear data section
        
        try {
            // fill data
            for (int i = 0; i<n; i++) {
                buffer[2+i] = (byte) (buffIn.read()&0xFF);
            }
        } catch (IOException e) {
            log.error("IO exception reading file: "+e);
        }
        return buffer;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PdiFile.class.getName());
}
