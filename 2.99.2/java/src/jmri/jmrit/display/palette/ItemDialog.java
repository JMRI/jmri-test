// ItemDialog.java
package jmri.jmrit.display.palette;

import javax.swing.JDialog;

/**
 * @author Pete Cressman  Copyright (c) 2010
 */

public class ItemDialog extends JDialog {

    protected ItemPanel _parent;
    protected String    _type;
    protected String    _family;

    /**
    */
    public ItemDialog(String type, String family, String title, ItemPanel parent, boolean mode) {
        super(parent._paletteFrame, title, mode);
        _type = type;
        _family = family;
        _parent = parent;
    }

    protected void sizeLocate() {
        setSize(_parent.getSize().width, this.getPreferredSize().height);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
    }

    protected String getDialogType() {
        return _type;
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemDialog.class.getName());
}
