// AlignTableAction.java

package jmri.jmrix.rps.aligntable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			AlignTableFrame object.
 * <p>
 * Only one frame is used (e.g. it's reused) because notification is
 * not yet completely in place.
 *<p>
 * Note that we only allow one of these right now.
 *
 * @author	Bob Jacobsen    Copyright (C) 2006, 2008
 * @version	$Revision$
 */
public class AlignTableAction extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1629850954178461422L;
	static final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");
	public AlignTableAction(String s) { super(s);}

    public AlignTableAction() {
        this(rb.getString("WindowTitle"));
    }

    AlignTableFrame f;
    
    public void actionPerformed(ActionEvent e) {
        if (f== null) {
            f = new AlignTableFrame();
            try {
                f.initComponents();
                }
            catch (Exception ex) {
                log.error("Exception: "+ex.toString());
                }
            f.setLocation(100,30);
        }
        f.setVisible(true);
    }
   static Logger log = LoggerFactory.getLogger(AlignTableAction.class.getName());
}


/* @(#)AlignTableAction.java */
