// z21XNetStreamPortController.java

package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * Override default XPressNet classes to use z21 specific versions.
 * <p>
 *
 * @author			Paul Bender    Copyright (C) 2004,2010,2014
 * @version			$Revision: 25878 $
 */
public class z21XNetStreamPortController extends jmri.jmrix.lenz.XNetStreamPortController {

    public z21XNetStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
    }


    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        jmri.jmrix.lenz.XNetTrafficController packets = new 
              jmri.jmrix.lenz.XNetPacketizer(new 
              jmri.jmrix.lenz.LenzCommandStation());
        packets.connectPort(this);

        ((XNetSystemConnectionMemo)adaptermemo).setXNetTrafficController(packets);

        new z21XNetInitializationManager(
            (XNetSystemConnectionMemo)adaptermemo);

        jmri.jmrix.lenz.ActiveFlag.setActive();
    }


    static Logger log = LoggerFactory.getLogger(z21XNetStreamPortController.class.getName());

}



/* @(#)z21XNetStreamPortController.java */
