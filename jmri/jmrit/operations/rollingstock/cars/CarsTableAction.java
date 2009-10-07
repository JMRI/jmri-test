// CarsTableAction.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a CarsTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */
public class CarsTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

    public CarsTableAction(String s) {
	super(s);
    }

    public CarsTableAction() {
        this(rb.getString("TitleCarsTable"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a car table frame
        CarsTableFrame f = new CarsTableFrame(true, null, null);
        f.setVisible(true);
    }
}

/* @(#)CarsTableAction.java */
