// XNetListener.java

package jmri.jmrix.lenz;

/** 
 * XNetListener provides the call-back interface for notification when a 
 * new XNet message arrives from the layout.
 *<P>
 * Note that the XNetListener implementation cannot assume that messages will
 * be returned in any particular thread. We may eventually revisit this, as returning
 * messages in the Swing GUI thread would result in some simplification of client code.
 * We've not done that yet because we're not sure that deadlocks can be avoided in that 
 * case.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version			$Revision: 1.1 $		
 */
public interface XNetListener extends java.util.EventListener{

	/**
	 * Member function that will be invoked by a XNetInterface implementation
	 * to forward a XNet message from the layout.
	 *
	 * @param msg  The received XNet message.  Note that this same object
	 *             may be presented to multiple users. It should not be 
	 *             modified here.
	 */
	public void message(XNetMessage msg);
}


/* @(#)XNetListener.java */
