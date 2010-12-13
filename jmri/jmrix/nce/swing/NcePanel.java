// LnPanel.java

package jmri.jmrix.nce.swing;

import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation
 * of window title and help reference for Nce panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * Copied from LocoNet.swing
 * @author kcameron 2010
 * @version $Revision: 1.1.2.1 $
 */

abstract public class NcePanel extends jmri.util.swing.JmriPanel implements NcePanelInterface {


    /**
     * make "memo" object available as convenience
     */
    protected NceSystemConnectionMemo memo;
    
    public void initComponents(NceSystemConnectionMemo memo) {
        this.memo = memo;
    }
    
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo ) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }
    
}