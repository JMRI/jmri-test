// LocationsTableAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;


/**
 * Swing action to create and register a LocationTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
@ActionID(id = "jmri.jmrit.operations.locations.LocationsTableAction",
        category = "Operations")
@ActionRegistration(iconInMenu = false, 
        displayName = "jmri.jmrit.operations.JmritOperationsBundle#MenuLocations",
        iconBase = "org/jmri/core/ui/toolbar/generic.gif")
@ActionReference(path = "Menu/Operations",
        position = 4340)
public class LocationsTableAction extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8215433161940132587L;
	public LocationsTableAction(String s) {
    	super(s);
    }

    public LocationsTableAction() {
    	this(Bundle.getMessage("MenuLocations"));	// NOI18N
    }

    static LocationsTableFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a location table frame
    	if (f == null || !f.isVisible()){
    		f = new LocationsTableFrame();
     	}
    	f.setExtendedState(Frame.NORMAL);
    	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)LocationsTableAction.java */
