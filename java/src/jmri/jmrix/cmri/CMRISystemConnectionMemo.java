package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import jmri.jmrix.DCCManufacturerList;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 * 
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    public CMRISystemConnectionMemo() {
        super("C", DCCManufacturerList.CMRI);
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CMRIActionListBundle");
    }
    
}
