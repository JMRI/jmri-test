// PrintTrainBuildReportAction.java

package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.*;
import java.text.MessageFormat;
import javax.swing.*;

/**
 * Action to print a train's build report
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class PrintTrainBuildReportAction extends AbstractAction {

	public PrintTrainBuildReportAction(String actionName, boolean preview, Frame frame) {
		super(actionName);
		isPreview = preview;
		this.frame = frame;
	}

	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
	Frame frame;

	public void actionPerformed(ActionEvent e) {
		TrainEditFrame f = (TrainEditFrame) frame;
		Train train = f._train;
		if (train == null)
			return;
		if (!train.isBuilt()) {
			String printOrPreview = Bundle.getString("print");
			if (isPreview)
				printOrPreview = Bundle.getString("preview");
			String string = MessageFormat.format(Bundle.getString("DoYouWantToPrintPreviousBuildReport"),
					new Object[] { printOrPreview, train.getName() });
			int results = JOptionPane.showConfirmDialog(null, string, MessageFormat.format(
					Bundle.getString("PrintPreviousBuildReport"), new Object[] { printOrPreview }),
					JOptionPane.YES_NO_OPTION);
			if (results != JOptionPane.YES_OPTION)
				return;
		}
		if (!train.printBuildReport(isPreview)) {
			String string = MessageFormat.format(Bundle.getString("NeedToBuildTrainBeforePrinting"),
					new Object[] { train.getName() });
			JOptionPane.showMessageDialog(null, string, Bundle.getString("CanNotPrintBuildReport"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PrintTrainBuildReportAction.class.getName());
}
