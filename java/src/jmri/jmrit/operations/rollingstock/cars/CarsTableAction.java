// CarsTableAction.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Swing action to create and register a CarsTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.rollingstock.cars.CarsTableAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false,
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuCars",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4350)
public class CarsTableAction extends AbstractAction {

	public CarsTableAction(String s) {
		super(s);
	}

	public CarsTableAction() {
		this(Bundle.getMessage("MenuCars"));
	}

	public void actionPerformed(ActionEvent e) {
		// create a car table frame
		new CarsTableFrame(true, null, null);
	}
}

/* @(#)CarsTableAction.java */
