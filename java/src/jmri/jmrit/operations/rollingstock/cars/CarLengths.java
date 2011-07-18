// CarLengths.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;


/**
 * Represents the lengths that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class CarLengths implements java.beans.PropertyChangeListener {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	private static final String LENGTHS = rb.getString("carLengths");
	public static final String CARLENGTHS_CHANGED_PROPERTY = "CarLengths";
	public static final String CARLENGTHS_NAME_CHANGED_PROPERTY = "CarLengthsName";
	
	private static final int MIN_NAME_LENGTH = 4;
	
    public CarLengths() {
    }
    
	/** record the single instance **/
	private static CarLengths _instance = null;

	public static synchronized CarLengths instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarLengths creating instance");
			// create and load
			_instance = new CarLengths();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarLengths returns instance "+_instance);
		return _instance;
	}

    public synchronized void dispose() {
    	list.clear();    	
    	// remove all listeners
    	for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners() )
    	    pcs.removePropertyChangeListener(p);
    }
    
    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    }

    List<String> list = new ArrayList<String>();
    
    public String[] getNames(){
     	if (list.size() == 0){
     		String[] lengths = LENGTHS.split("%%");
     		for (int i=0; i<lengths.length; i++)
     			list.add(lengths[i]);
    	}
     	String[] lengths = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		lengths[i] = list.get(i);
   		return lengths;
    }
    
    public void setNames(String[] lengths){
    	if (lengths.length == 0) return;
       	try{
    		jmri.util.StringUtil.numberSort(lengths);
    	} catch (NumberFormatException e){
    		log.error("Car lengths not numeric");
    	}
 		for (int i=0; i<lengths.length; i++)
			if (!list.contains(lengths[i]))
				list.add(lengths[i]);
    }
    
    public void addName(String length){
    	// insert at start of list, sort later
    	if (list.contains(length))
    		return;
    	list.add(0,length);
    	maxNameLength = 0;	// reset maximum name length
    	firePropertyChange (CARLENGTHS_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteName(String length){
    	list.remove(length);
    	maxNameLength = 0;	// reset maximum name length
    	firePropertyChange (CARLENGTHS_CHANGED_PROPERTY, list.size()+1, list.size());
    }
     
    public boolean containsName(String length){
    	return list.contains(length);
    }
    
    public void replaceName(String oldName, String newName){
    	addName(newName);
    	firePropertyChange (CARLENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
    	// need to keep old name so location manager can replace properly
       	deleteName(oldName);
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] lengths = getNames();
		for (int i = 0; i < lengths.length; i++)
			box.addItem(lengths[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] lengths = getNames();
		for (int i = 0; i < lengths.length; i++)
			box.addItem(lengths[i]);
    }
      
    private int maxNameLength = 0;
    
    public int getCurMaxNameLength(){
    	if (maxNameLength == 0){
    		String[] lengths = getNames();
    		int length = MIN_NAME_LENGTH;
    		for (int i = 0; i < lengths.length; i++){
    			if (lengths[i].length()>length)
    				length = lengths[i].length();
    		}
    		return length;
    	} else {
    		return maxNameLength;
    	}
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarLengths.class.getName());

}

