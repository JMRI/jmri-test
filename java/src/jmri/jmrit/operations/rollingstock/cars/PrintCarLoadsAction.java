// PrintCarLoadsAction.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.Hashtable;
import java.util.List;

/**
 * Action to print a summary of car loads ordered by car type.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in Macintosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision$
 */
public class PrintCarLoadsAction extends AbstractAction {

	CarManager manager = CarManager.instance();

	public PrintCarLoadsAction(String actionName, boolean preview, Component pWho) {
		super(actionName);
		isPreview = preview;
	}

	/**
	 * Frame hosting the printing
	 */

	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;

	public void actionPerformed(ActionEvent e) {
		new CarLoadPrintOption();
	}

	public class CarLoadPrintOption {

		static final String TAB = "\t"; // NOI18N
		static final String NEW_LINE = "\n";	// NOI18N

		// no frame needed for now
		public CarLoadPrintOption() {
			super();
			printCars();
		}

		private void printCars() {

			// obtain a HardcopyWriter to do this
			HardcopyWriter writer = null;
			Frame mFrame = new Frame();
			try {
				writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleCarLoads"), 10, .5, .5,
						.5, .5, isPreview);
			} catch (HardcopyWriter.PrintCanceledException ex) {
				log.debug("Print cancelled");
				return;
			}

			// Loop through the Roster, printing as needed
			CarLoads carLoads = CarLoads.instance();
			String[] carTypes = CarTypes.instance().getNames();
			Hashtable<String, List<CarLoad>> list = carLoads.getList();
			try {
				String s = Bundle.getMessage("Type") + TAB + Bundle.getMessage("Load") + "   "
						+ Bundle.getMessage("BorderLayoutLoadType") + " "
						+ Bundle.getMessage("BorderLayoutPriority") + "   "
						+ Bundle.getMessage("LoadPickupMessage") + "   "
						+ Bundle.getMessage("LoadDropMessage") + NEW_LINE;
				writer.write(s);
				for (int i = 0; i < carTypes.length; i++) {
					List<CarLoad> loads = list.get(carTypes[i]);
					boolean printType = true;
					for (int j = 0; j < loads.size(); j++) {
						StringBuffer buf = new StringBuffer(TAB);
						String load = loads.get(j).getName();
						// don't print out default load or empty
						if ((load.equals(carLoads.getDefaultEmptyName()) || load.equals(carLoads
								.getDefaultLoadName()))
								&& loads.get(j).getPickupComment().equals("")
								&& loads.get(j).getDropComment().equals("")
								&& loads.get(j).getPriority().equals(CarLoad.PRIORITY_LOW))
							continue;
						// print the car type once
						if (printType) {
							writer.write(carTypes[i] + NEW_LINE);
							printType = false;
						}
						buf.append(tabString(load, carLoads.getCurMaxNameLength()+1));
						buf.append(tabString(loads.get(j).getLoadType(), 6));
						buf.append(tabString(loads.get(j).getPriority(), 5));
						buf.append(tabString(loads.get(j).getPickupComment(), 27));
						buf.append(tabString(loads.get(j).getDropComment(), 27));
						writer.write(buf.toString() + NEW_LINE);
					}
				}
				// and force completion of the printing
				writer.close();
			} catch (IOException we) {
				log.error("Error printing car roster");
			}
		}
	}

	private static String tabString(String s, int fieldSize) {
		if (s.length() > fieldSize)
			s = s.substring(0, fieldSize-1);
		StringBuffer buf = new StringBuffer(s + " ");
		while (buf.length() < fieldSize) {
			buf.append(" ");
		}
		return buf.toString();
	}

	static Logger log = LoggerFactory
			.getLogger(PrintCarLoadsAction.class.getName());
}
