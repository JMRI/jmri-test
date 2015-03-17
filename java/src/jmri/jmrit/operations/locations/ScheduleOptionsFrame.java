//ScheduleOptionsAction.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to launch schedule options.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */
class ScheduleOptionsFrame extends OperationsFrame {

    /**
     *
     */
    private static final long serialVersionUID = -4111002798586670354L;

    // text field
    JTextField factorTextField = new JTextField(5);

    // combo boxes
    JComboBox<Track> trackBox = new JComboBox<>();

    // radio buttons
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("Save"));

    Track _track;

    public ScheduleOptionsFrame(ScheduleEditFrame sef) {
        super();

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track = sef._track;

        // load the panel
        // row 1
        JPanel pFactor = new JPanel();
        pFactor.setLayout(new GridBagLayout());
        pFactor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScheduleFactor")));
        addItem(pFactor, factorTextField, 0, 0);

        factorTextField.setToolTipText(Bundle.getMessage("TipScheduleFactor"));
        factorTextField.setText(Integer.toString(_track.getReservationFactor()));

        // row 2
        JPanel pAlternate = new JPanel();
        pAlternate.setLayout(new GridBagLayout());
        pAlternate.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AlternateTrack")));
        addItem(pAlternate, trackBox, 0, 0);

        _track.getLocation().updateComboBox(trackBox);
        trackBox.removeItem(_track); // remove this track from consideration
        trackBox.setSelectedItem(_track.getAlternateTrack());

        JPanel pControls = new JPanel();
        pControls.add(saveButton);

        // button action
        addButtonAction(saveButton);

        getContentPane().add(pFactor);
        getContentPane().add(pAlternate);
        getContentPane().add(pControls);

        setTitle(Bundle.getMessage("MenuItemScheduleOptions"));
        pack();
        setMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight200));
        setVisible(true);
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // confirm that factor is between 0 and 1000
            try {
                int factor = Integer.parseInt(factorTextField.getText());
                if (factor < 0 || factor > 1000) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("FactorMustBeNumber"),
                            Bundle.getMessage("ErrorFactor"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("FactorMustBeNumber"),
                        Bundle.getMessage("ErrorFactor"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            _track.setReservationFactor(Integer.parseInt(factorTextField.getText()));
            if (trackBox.getSelectedItem() != null && !trackBox.getSelectedItem().equals(Location.NONE)) {
                _track.setAlternateTrack((Track) trackBox.getSelectedItem());
            } else {
                _track.setAlternateTrack(null);
            }
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(TrackEditFrame.class
            .getName());
}
