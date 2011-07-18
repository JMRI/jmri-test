// TrainsScheduleTableFrame.java

package jmri.jmrit.operations.trains;
 
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;

import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Frame for adding and editing train schedules for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version             $Revision$
 */
public class TrainsScheduleTableFrame extends OperationsFrame implements PropertyChangeListener {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	public static SwingShutDownTask trainDirtyTask;
	
	public static final String NAME = rb.getString("Name");	// Sort by choices
	public static final String TIME = rb.getString("Time");
		
	TrainManager trainManager = TrainManager.instance();
	TrainScheduleManager scheduleManager = TrainScheduleManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();

	TrainsScheduleTableModel trainsScheduleModel = new TrainsScheduleTableModel();
	javax.swing.JTable trainsScheduleTable = new javax.swing.JTable(trainsScheduleModel);
	JScrollPane trainsPane;
	
	// labels
	JLabel textSort = new JLabel(rb.getString("SortBy"));
	
	// radio buttons
    JRadioButton sortByName = new JRadioButton(NAME);
    JRadioButton sortByTime = new JRadioButton(TIME);
    
    // radio button groups
   	ButtonGroup schGroup;
        
	// major buttons
	JButton applyButton = new JButton(rb.getString("Apply"));
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// check boxes
	
	// panel
	JPanel schedule = new JPanel();
	
	// active schedule id
	private String _activeId = "";
	
    public TrainsScheduleTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle").getString("TitleTimeTableTrains"));

        // set active id
        _activeId = trainManager.getTrainScheduleActiveId();
        
        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	trainsPane = new JScrollPane(trainsScheduleTable);
    	trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trainsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	trainsScheduleModel.initTable(trainsScheduleTable, this);
     	
    	// Set up the control panel	
    	//row 1
    	JPanel cp1 = new JPanel();
    	cp1.setLayout(new BoxLayout(cp1,BoxLayout.X_AXIS));
    	
    	//row 1
    	JPanel sortBy = new JPanel();
    	sortBy.setBorder(BorderFactory.createTitledBorder(rb.getString("SortBy")));
    	sortBy.add(sortByTime);
    	sortBy.add(sortByName);
    	
       	//row 2
    	schedule.setBorder(BorderFactory.createTitledBorder(rb.getString("Active")));
    	updateControlPanel();
    	
    	cp1.add(sortBy);
    	cp1.add(schedule);
			
    	JPanel cp3 = new JPanel();
    	cp3.setBorder(BorderFactory.createTitledBorder(""));
		cp3.add (applyButton);
		cp3.add (saveButton);
		
    	//tool tips
    	applyButton.setToolTipText(rb.getString("ApplyButtonTip"));
    	saveButton.setToolTipText(rb.getString("SaveButtonTip"));
		
		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.Y_AXIS));
		controlPanel.add(cp1);
		controlPanel.add(cp3);
		
	    JScrollPane controlPane = new JScrollPane(controlPanel);
	    // make sure panel doesn't get too short
	    controlPane.setMinimumSize(new Dimension(50,90));
	    controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
    	getContentPane().add(trainsPane);
	   	getContentPane().add(controlPane);
	   	
		// setup buttons
		addButtonAction(applyButton);
		addButtonAction(saveButton);
		
	   	ButtonGroup sortGroup = new ButtonGroup();
	   	sortGroup.add(sortByTime);
    	sortGroup.add(sortByName);
    	sortByName.setSelected(true);
    	
    	addRadioButtonAction(sortByTime);
		addRadioButtonAction(sortByName);	
    	
		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new TrainsScheduleEditAction());
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Timetable", true);
    		
    	pack();
    	
    	/* all JMRI window position and size are now saved
    	setSize(trainManager.getTrainScheduleFrameSize());
    	setLocation(trainManager.getTrainScheduleFramePosition());
    	*/
    	setSortBy(trainManager.getTrainsFrameSortBy());
    	
    	scheduleManager.addPropertyChangeListener(this);
    	addPropertyChangeTrainSchedules();
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYNAME);
		}
		if (ae.getSource() == sortByTime){
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
		}
	}
	
	TrainSwitchListEditFrame tslef;
 
	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("schedule train button activated");
		if (ae.getSource() == applyButton){
			applySchedule();
		}
		if (ae.getSource() == saveButton){
			storeValues();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	private void updateControlPanel(){
		schedule.removeAll();
	   	schGroup = new ButtonGroup();
    	List<String> l = scheduleManager.getSchedulesByIdList();
    	for (int i=0; i<l.size(); i++){
    		TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
    		JRadioButton b = new JRadioButton();
    		b.setText(ts.getName());
    		b.setName(l.get(i));
    		schedule.add(b);
    		schGroup.add(b);
    		if (b.getName().equals(_activeId))
    			b.setSelected(true);
    	}
    	schedule.revalidate();
	}
	
	private void setSortBy(String sortBy){
		if(sortBy.equals(TIME)){
			sortByTime.setSelected(true);
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
		}
	}

	public List<String> getSortByList(){
		return trainsScheduleModel.getSelectedTrainList();
	}

	private void applySchedule(){
		setActiveId();
		TrainSchedule ts = TrainScheduleManager.instance().getScheduleById(_activeId);
		List<String> trains = trainManager.getTrainsByIdList();
		for (int j=0; j<trains.size(); j++){
			log.debug("train id: "+trains.get(j));
			Train train = trainManager.getTrainById(trains.get(j));
			train.setBuildEnabled(ts.containsTrainId(trains.get(j)));
		}
	}
	
	private void setActiveId(){
		AbstractButton b;
		Enumeration<AbstractButton> en = schGroup.getElements();
		for (int i=0; i<schGroup.getButtonCount(); i++){
			b = en.nextElement();
			if (b.isSelected()){
				log.debug("schedule radio button "+b.getText());
				_activeId = b.getName();
			}
		}
	}
	
	protected void storeValues(){
		setActiveId();
		/* all JMRI window position and size are now saved
		trainManager.setTrainScheduleFrame(this);
		*/
		trainManager.setTrainScheduleFrameTableColumnWidths(getCurrentTableColumnWidths()); // save column widths
		trainManager.setTrainSecheduleActiveId(_activeId);
		trainManager.save();
	}
	
	protected int[] getCurrentTableColumnWidths(){
		TableColumnModel tcm = trainsScheduleTable.getColumnModel();
		int[] widths = new int[tcm.getColumnCount()];
		for (int i=0; i<tcm.getColumnCount(); i++)
			widths[i] = tcm.getColumn(i).getWidth();
		return widths;
	}
	
    public void dispose() {
    	trainManager.setTrainScheduleFrameTableColumnWidths(getCurrentTableColumnWidths()); // save column widths
    	scheduleManager.removePropertyChangeListener(this);
    	removePropertyChangeTrainSchedules();
    	trainsScheduleModel.dispose();
        super.dispose();
    }
    
    private void addPropertyChangeTrainSchedules(){
    	List<String> l = scheduleManager.getSchedulesByIdList();
    	for (int i = 0; i < l.size(); i++){
    		TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
    		if (ts != null)
    			ts.addPropertyChangeListener(this);
    	}
    }
    
    private void removePropertyChangeTrainSchedules(){
    	List<String> l = scheduleManager.getSchedulesByIdList();
    	for (int i = 0; i < l.size(); i++){
    		TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
    		if (ts != null)
    			ts.removePropertyChangeListener(this);
    	}
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(TrainScheduleManager.LISTLENGTH_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(TrainSchedule.NAME_CHANGED_PROPERTY)){
    		updateControlPanel();
    	}
    }
      
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainsScheduleTableFrame.class.getName());
}
