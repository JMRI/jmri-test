/**
 * OperationsMenu.java
 */
package jmri.jmrit.operations;

import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Operations" menu
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Daniel Boudreau Copyright 2008
 * @version $Revision$
 */
public class OperationsMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 701227477994394798L;

    public OperationsMenu(String name) {
        this();
        setText(name);
    }

    public OperationsMenu() {
        super();

        setText(Bundle.getMessage("MenuOperations"));

        add(new jmri.jmrit.operations.setup.OperationsSetupAction());
        add(new jmri.jmrit.operations.locations.LocationsTableAction());
        add(new jmri.jmrit.operations.rollingstock.cars.CarsTableAction());
        add(new jmri.jmrit.operations.rollingstock.engines.EnginesTableAction());
        add(new jmri.jmrit.operations.routes.RoutesTableAction());
        add(new jmri.jmrit.operations.trains.TrainsTableAction());

    }

    static Logger log = LoggerFactory.getLogger(OperationsMenu.class.getName());
}
