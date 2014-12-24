// TrainConductorFrame.java
package jmri.jmrit.operations.trains;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrit.operations.OperationsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conductor Frame. Shows work for a train one location at a time.
 *
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * @version $Revision: 18630 $
 */
public class TrainConductorFrame extends OperationsFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3456559207864002340L;

	public TrainConductorFrame() {
        super(new TrainConductorPanel());
    }

    public void initComponents(Train train) {
        super.initComponents();
        ((TrainConductorPanel) this.getContentPane()).initComponents(train);

        if (train != null) {
            setTitle(Bundle.getMessage("TitleTrainConductor") + " (" + train.getName() + ")");
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        if (train != null) {
            JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
            toolMenu.add(new ShowCarsInTrainAction(Bundle.getMessage("MenuItemShowCarsInTrain"), train));
            menuBar.add(toolMenu);
        }
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N

        pack();
        setVisible(true);
    }

    private static final Logger log = LoggerFactory.getLogger(TrainConductorFrame.class.getName());
}
