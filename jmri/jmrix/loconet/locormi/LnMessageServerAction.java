package jmri.jmrix.loconet.locormi;

import javax.swing.AbstractAction;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import jmri.util.zeroconf.ZeroConfUtil;

/**
 * Start a LnMessageServer that will listen for clients wanting to
 * use the LocoNet connection on this machine.
 * Copyright:    Copyright (c) 2002
 * @author      Alex Shepherd
 * @version $Revision: 1.8 $
 */
public class LnMessageServerAction extends AbstractAction {

    public LnMessageServerAction( String s ) {
        super( s ) ;
    }

    public LnMessageServerAction() {
        super( "Start LocoNet server" ) ;
    }

    public void actionPerformed( ActionEvent e) {
        try {
            // start server
            LnMessageServer server = LnMessageServer.getInstance() ;
            server.enable();
            // advertise under zeroconf
            try {
               ZeroConfUtil.advertiseService(ZeroConfUtil.getServerName("JMRI locormi server"), "_jmri-locormi._tcp.local.", 1099, ZeroConfUtil.jmdnsInstance());
            } catch (java.io.IOException e2) {
                    Logger.getLogger(LnMessageServerAction.class.getName()).error("can't advertise via ZeroConf: "+e2);
            }
            // disable action, as already run
            setEnabled(false);
        } catch( RemoteException ex ) {
            Logger.getLogger(LnMessageServerAction.class.getName()).warn( "LnMessageServerAction Exception: " + ex );
        }
    }

}
