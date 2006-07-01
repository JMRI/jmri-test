package jmri.jmrix.loconet.locormi;

/**
 * Opens a connection with a LnMessageServer on a remote machine.
 * The remote machine IP address is specified via a dialog box.
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author  Bob Jacobsen
 * @version $Revision: 1.3 $
 */

import javax.swing.*;
import jmri.jmrix.loconet.*;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

public class LnMessageClientAction extends AbstractAction
{
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageClientAction.class.getName());

  public LnMessageClientAction( String s )
  {
    super( s ) ;
  }

  public void actionPerformed( ActionEvent e)
  {
    try{
    // get the portname and timeout
        String remoteHostName = JOptionPane.showInputDialog("Remote host name?");

        int timeoutMSec = 500;      // this is temporarily fixed, until
                                    // I understand how best to present it
                                    // to the user.

    // create the LnMessageClient
        LnMessageClient client = new LnMessageClient();

    // start the connection
        client.configureRemoteConnection(remoteHostName, timeoutMSec);

    // configure the other instance objects
        client.configureLocalServices();
    }
    catch( LocoNetException ex ){
        log.warn( "Exception: " + ex );
    }
  }
}