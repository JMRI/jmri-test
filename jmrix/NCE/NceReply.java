/** 
 * NceReply.java
 *
 * Description:		Carries the reply to an NceMessage
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;


// Note:  This handles the "binary" form of command in the NCE spec

public class NceReply {
	// is this logically an abstract class?

	// create a new one
	public  NceReply() {
	}

	public void setOpCode(int i) { _dataChars[0]=i;}
	public int getOpCode() {return _dataChars[0];}

	// accessors to the bulk data
	public int getNumDataElements() {return _nDataChars;}
	public int getElement(int n) {return _dataChars[n];}
	public void setElement(int n, int v) { 
		_dataChars[n] = v;
		_nDataChars = Math.max(_nDataChars, n+1);	
	}

	// mode accessors
	boolean _isBinary;
	public boolean isBinary() { return _isBinary; }
	public void setBinary(boolean b) { _isBinary = b; }

	// display format
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataChars; i++) {
			if (_isBinary) {
				if (i!=0) s+=" ";
				if (_dataChars[i] < 16) s+="0";
				s+=Integer.toHexString(_dataChars[i]);
			} else {
				s+=(char)_dataChars[i];
			}
		}
		return s;
	}

	public int value() {  // integer value of 1st three digits
		String s = ""+(char)getElement(0)+(char)getElement(1)+(char)getElement(2);
		return Integer.parseInt(s);
	}
	
	static public int maxSize = 120;
		
	// contents (private)
	private int _nDataChars;
	private int _dataChars[] = new int[maxSize];

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceReply.class.getName());

}


/* @(#)NceReply.java */
