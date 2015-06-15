// LocationTrackBlockingOrderFrame.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of location
 *
 * @author Dan Boudreau Copyright (C) 2015
 * @version $Revision: 29365 $
 */
public class LocationTrackBlockingOrderFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -820196357214001064L;
    LocationTrackBlockingOrderTableModel trackModel = new LocationTrackBlockingOrderTableModel();
    JTable trackTable = new JTable(trackModel);
    JScrollPane trackPane = new JScrollPane(trackTable);

    LocationManager locationManager = LocationManager.instance();

    Location _location = null;
    
    JLabel locationName = new JLabel();

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("Save"));
    JButton resetButton = new JButton(Bundle.getMessage("Reset"));

    public LocationTrackBlockingOrderFrame() {
        super(Bundle.getMessage("TitleTrackBlockingOrder"));
    }

    public void initComponents(Location location) {
        _location = location;
        
        trackPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trackPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ServiceOrderMessage")));

        if (_location != null) {
            trackModel.initTable(trackTable, location);
            locationName.setText(_location.getName());
            enableButtons(true);
        } else {
            enableButtons(false);
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows       
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));

        addItem(pName, locationName, 0, 0);
        
        // row buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, resetButton, 0, 0);
        addItem(pB, saveButton, 1, 0);

        getContentPane().add(pName);
        getContentPane().add(trackPane);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(resetButton);
        addButtonAction(saveButton);

        // add tool tips
        resetButton.setToolTipText(Bundle.getMessage("TipResetButton"));

        // build menu
//        JMenuBar menuBar = new JMenuBar();
//        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
//        menuBar.add(toolMenu);
//        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrackBlockingOrder", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight500));

    }

    // Reset and Save
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == resetButton && _location != null) {
            _location.resetTracksByBlockingOrder();
        }
        if (ae.getSource() == saveButton) {
            if (trackTable.isEditing()) {
                log.debug("track table edit true");
                trackTable.getCellEditor().stopCellEditing();
            }
            _location.resequnceTracksByBlockingOrder();
            // recreate all train manifests
            TrainManager.instance().setTrainsModified();
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }


    private void enableButtons(boolean enabled) {
        resetButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    public void dispose() {
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        trackModel.dispose();
        super.dispose();
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
    }

    static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderFrame.class.getName());
}
