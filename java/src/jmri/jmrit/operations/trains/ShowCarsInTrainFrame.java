// ShowCarsInTrainFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;


/**
 * Show Cars In Train Frame.
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 18630 $
 */

public class ShowCarsInTrainFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	Train _train = null;
	CarManager carManager = CarManager.instance();
	TrainCommon trainCommon = new TrainCommon();
	
	JScrollPane carPane;

	// labels
	JLabel textTrainName = new JLabel();
	JLabel textLocationName = new JLabel();
	JLabel textNextLocationName = new JLabel();
	JLabel textStatus = new JLabel();

	// major buttons

	// radio buttons
	
	// text field
	
	// combo boxes
	
	// panels
	JPanel pCars = new JPanel();
	
	// check boxes

	public ShowCarsInTrainFrame() {
		super();
	}

	public void initComponents(Train train) {
		_train = train;

	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
       	carPane = new JScrollPane(pCars);
       	carPane.setBorder(BorderFactory.createTitledBorder(rb.getString("Cars")));
       	carPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
       	//carPane.setPreferredSize(new Dimension(200, 300));
       	
	    //      Set up the panels
				
		// Layout the panel by rows
		
       	// row 2
       	JPanel pRow2 = new JPanel();
       	pRow2.setLayout(new BoxLayout(pRow2,BoxLayout.X_AXIS));
       	
		// row 2a (train name)
       	JPanel pTrainName = new JPanel();
       	pTrainName.setBorder(BorderFactory.createTitledBorder(rb.getString("Train")));
       	pTrainName.add(textTrainName);
       	
       	pRow2.add(pTrainName);
       	
       	// row 6
       	JPanel pRow6 = new JPanel();
       	pRow6.setLayout(new BoxLayout(pRow6,BoxLayout.X_AXIS));
       	
       	// row 10
       	JPanel pRow10 = new JPanel();
       	pRow10.setLayout(new BoxLayout(pRow10,BoxLayout.X_AXIS));
       	
       	// row 10a (location name)
       	JPanel pLocationName = new JPanel();
       	pLocationName.setBorder(BorderFactory.createTitledBorder("Location"));
       	pLocationName.add(textLocationName);
       	
      	// row 10c (next location name)
       	JPanel pNextLocationName = new JPanel();
       	pNextLocationName.setBorder(BorderFactory.createTitledBorder(rb.getString("NextLocation")));
       	pNextLocationName.add(textNextLocationName);
       	
       	pRow10.add(pLocationName);
       	pRow10.add(pNextLocationName);
       	
       	// row 12
       	JPanel pRow12 = new JPanel();
       	pRow12.setLayout(new BoxLayout(pRow12,BoxLayout.X_AXIS));

       	pCars.setLayout(new GridBagLayout());
       	pRow12.add(carPane);
       	
       	// row 13
      	JPanel pStatus = new JPanel();
      	pStatus.setLayout(new GridBagLayout());
      	pStatus.setBorder(BorderFactory.createTitledBorder(""));
       	addItem(pStatus, textStatus, 0, 0);
		
		getContentPane().add(pRow2);
		getContentPane().add(pRow6);
		getContentPane().add(pRow10);
		getContentPane().add(pRow12);
		getContentPane().add(pStatus);
		
       	update();
		
		if (_train != null){
			textTrainName.setText(_train.getIconName());			
			setTitle(rb.getString("TitleShowCarsInTrain") + " ("+_train.getName()+")");

			// listen for train changes
			_train.addPropertyChangeListener(this);
		} 
		

//		//	build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu(rb.getString("Tools"));			
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
//		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
		
		packFrame();
    	setVisible(true);
		
	}

	
	private void update(){
		log.debug("update");
		if (_train != null && _train.getRoute() != null){
			pCars.removeAll();
			RouteLocation rl = _train.getCurrentLocation();
			if (rl != null){
				textLocationName.setText(rl.getLocation().getName());
				textNextLocationName.setText(_train.getNextLocationName());
				
				// now update the car pick ups and set outs
				List<String> carList = carManager.getByTrainDestinationList(_train);
				List<String> routeList = _train.getRoute().getLocationsBySequenceList();
				// block cars by destination
				int i = 0;
				for (int j = 0; j < routeList.size(); j++) {
					RouteLocation rld = _train.getRoute().getLocationById(routeList.get(j));
					for (int k = 0; k < carList.size(); k++) {
						Car car = carManager.getById(carList.get(k));
						if ((car.getTrack() == null || car.getRouteLocation() == rl) 
								&& car.getRouteDestination() == rld){
							JCheckBox checkBox = new JCheckBox(car.toString());
							if (car.getRouteDestination() == rl)
								addItemLeft(pCars, checkBox, 2, i++);	// set out
							else if (car.getRouteLocation() == rl && car.getTrack() != null)
								addItemLeft(pCars, checkBox, 0, i++);	// pick up			
							else
								addItemLeft(pCars, checkBox, 1, i++);	// in train
						}
					}
				}

				textStatus.setText(getStatus(rl));
			} else {
				textStatus.setText(rb.getString("TrainTerminatesIn")+ " " + _train.getTrainTerminatesName());
			}
			pCars.repaint();
		}
	}
	
	private String getStatus(RouteLocation rl){
		StringBuffer buf = new StringBuffer(rb.getString("TrainDeparts")+ " " + rl.getName() +" "+ rl.getTrainDirectionString()
				+ rb.getString("boundWith") +" ");
		/*
		if (Setup.isPrintLoadsAndEmptiesEnabled())
			buf.append((_train.getNumberCarsInTrain()-emptyCars)+" "+rb.getString("Loads")+", "+emptyCars+" "+rb.getString("Empties")+", ");
		else
		*/
			buf.append(_train.getNumberCarsInTrain() +" "+rb.getString("cars")+", ");
		String s = _train.getTrainLength()+" "+rb.getString("feet")+", "+_train.getTrainWeight()+" "+rb.getString("tons");
		buf.append(s);
		return buf.toString();
	}
    
    private void packFrame(){
    	setVisible(false);
 		pack();
		if (getWidth()<300)
			setSize(300, getHeight());
		if (getHeight()<Control.panelHeight)
			setSize(getWidth(), Control.panelHeight);
		setMinimumSize(new Dimension(300, Control.panelHeight));
		setVisible(true);
    }
	
	public void dispose() {
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e){
		//if (Control.showProperty && log.isDebugEnabled()) 
		log.debug("Property change " +e.getPropertyName() + " for: "+e.getSource().toString()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY) 
				|| e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY))
			update();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ShowCarsInTrainFrame.class.getName());
}
