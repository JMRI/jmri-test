// CarOwners.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

/**
 * Represents the owner names that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.6 $
 */
public class CarOwners implements java.beans.PropertyChangeListener {
	
	public static final String CAROWNERS_CHANGED_PROPERTY = "CarOwners";
	private static final String LENGTH = "Length";
	
    public CarOwners() {
    }
    
	/** record the single instance **/
	private static CarOwners _instance = null;

	public static synchronized CarOwners instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarOwners creating instance");
			// create and load
			_instance = new CarOwners();
		}
		if (log.isDebugEnabled()) log.debug("CarOwners returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
    	list.clear();
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
     	String[] owners = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		owners[i] = list.get(i);
   		return owners;
    }
    
    public void setNames(String[] owners){
    	if (owners.length == 0) return;
    	jmri.util.StringUtil.sort(owners);
 		for (int i=0; i<owners.length; i++)
 			if (!list.contains(owners[i]))
 				list.add(owners[i]);
    }
    
    public void addName(String owner){
    	// insert at start of list, sort later
    	if (list.contains(owner))
    		return;
    	list.add(0,owner);
    	firePropertyChange (CAROWNERS_CHANGED_PROPERTY, null, LENGTH);
    }
    
    public void deleteName(String owner){
    	list.remove(owner);
    	firePropertyChange (CAROWNERS_CHANGED_PROPERTY, null, LENGTH);
    }
    
    public boolean containsName(String owner){
    	return list.contains(owner);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] owners = getNames();
		for (int i = 0; i < owners.length; i++)
			box.addItem(owners[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] owners = getNames();
		for (int i = 0; i < owners.length; i++)
			box.addItem(owners[i]);
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CarOwners.class.getName());

}

