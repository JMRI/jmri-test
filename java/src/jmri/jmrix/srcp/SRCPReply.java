// SRCPReply.java

package jmri.jmrix.srcp;

import jmri.jmrix.srcp.parser.SimpleNode;
import jmri.jmrix.srcp.parser.SRCPClientVisitor;
import jmri.jmrix.srcp.parser.Token;

/**
 * Carries the reply to an SRCPMessage.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001, 2004, 2008
 * @version             $Revision$
 */
public class SRCPReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  SRCPReply() {
        super();
    }
    public SRCPReply(String s) {
        super(s);
    }
    public SRCPReply(SRCPReply l) {
        super(l);
    }

    // from a parser message node.
    public SRCPReply(SimpleNode n){
	super();
        String s=new String(n.jjtGetFirstToken().toString());
	for(int i=1;i<n.jjtGetNumChildren();i++) {
	   s= s +" " +((SimpleNode)n.jjtGetChild(i)).jjtGetFirstToken().toString();
        }
        _nDataChars = s.length();
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = s.charAt(i);

    }


    public boolean isResponseOK() {
	return getResponseCode().charAt(0) == '1' || getResponseCode().charAt(0) == '2';
    }

    public String getResponseCode() {
	// split into 3 parts {TIMESTAMP, ResponseCode, Rest}
	// and use the second one (ResponseCode)
	String[] part = toString().split("\\s", 3);
	return part[1];
    }

    protected int skipPrefix(int index) {
		// start at index, passing any whitespace & control characters at the start of the buffer
		while (index < getNumDataElements()-1 &&
			((char)getElement(index) <= ' '))
				index++;
		return index;
    }

    /**
     * Extracts Read-CV returned value from a message.
     * Returns -1 if message can't be parsed. 
     * Expects a message of the form
     * 1264343601.156 100 INFO 1 SM -1 CV 8 99 
     */
    public int value() {
	String s = toString();
	String[] part = s.split("\\s", 10);
	int val = -1;

	try {
	    int tmp = Integer.valueOf(part[8],10).intValue();
	    val = tmp;  // don't do this assign until now in case the conversion throws
	} catch (Exception e) {
	    log.error("Unable to get number from reply: \""+s+"\"");
	}
	return val;
    }

    public boolean isUnsolicited() {
	String s = toString();
	try {
	// Split in 7 is enough for initial handshake 
	String[] part = s.split("\\s",7);
	// Test for initial handshake message with key "SRCP".
	if (part[2].equals("SRCP")) {
	    setUnsolicited();
	    return true;
        } else {
	    // the string wasn't long enough to split.
	    return false;
        }
	} catch(Exception e){ return false;}
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPReply.class.getName());

}


/* @(#)SRCPReply.java */
