// EngineLengths.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;


/**
 * Represents the lengths that engines can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class EngineLengths {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	private static final String LENGTHS = rb.getString("engineDefaultLengths");
	public static final String ENGINELENGTHS_CHANGED_PROPERTY = "EngineLengths";
	public static final String ENGINELENGTHS_NAME_CHANGED_PROPERTY = "EngineLengthsName";
	
    public EngineLengths() {
    }
    
	/** record the single instance **/
	private static EngineLengths _instance = null;

	public static synchronized EngineLengths instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineLengths creating instance");
			// create and load
			_instance = new EngineLengths();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineLengths returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
    	list.clear();
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
    		log.error("Engine lengths not numeric");
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
    	firePropertyChange (ENGINELENGTHS_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteName(String length){
    	list.remove(length);
    	firePropertyChange (ENGINELENGTHS_CHANGED_PROPERTY, list.size()+1, list.size());
    }
     
    public boolean containsName(String length){
    	return list.contains(length);
    }
    
    public void replaceName(String oldName, String newName){
    	addName(newName);
    	firePropertyChange (ENGINELENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
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
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineLengths.class.getName());

}

