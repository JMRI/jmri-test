/** 
 * CvTableModel.java
 *
 * Description:		Table data model for display of CvValues in symbolic programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import java.util.Vector;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import com.sun.java.util.collections.List;
import org.jdom.Element;
import org.jdom.Namespace;


public class CvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

	private int _numRows = 0;                // must be zero until Vectors are initialized
	private Vector _cvDisplayVector = new Vector();  // vector of CvValue objects, in display order
	private Vector _cvAllVector = new Vector(512);  // vector of all 512 possible CV objects
	
	
	// Defines the columns
	private static final int NUMCOLUMN   = 0;
	private static final int VALCOLUMN   = 1;
	private static final int STATECOLUMN = 2;
	private static final int READCOLUMN  = 3;
	private static final int WRITECOLUMN = 4;
	private static final int HIGHESTCOLUMN = WRITECOLUMN+1;
	
	public CvTableModel() {  
		super();
		// initialize the 512-length _cvAllVector;
		for (int i=0; i<512; i++) _cvAllVector.addElement(null);
		
		// define just most common CVs at start
		addCV("1");
		addCV("18");
		addCV("19");
		addCV("29");
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { return _numRows; }
	
	public int getColumnCount( ){ return HIGHESTCOLUMN;}

	public String getColumnName(int col) { 
		switch (col) {
			case NUMCOLUMN: return "Number";
			case VALCOLUMN: return "Value";
			case STATECOLUMN: return "State";
			case READCOLUMN: return "Read";
			case WRITECOLUMN: return "Write";
			default: return "unknown";
		}
	}
	
	public Class getColumnClass(int col) { 
		switch (col) {
			case NUMCOLUMN: return String.class;
			case VALCOLUMN: return String.class;
			case STATECOLUMN: return String.class;
			case READCOLUMN: return JButton.class;
			case WRITECOLUMN: return JButton.class;
			default: return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
			case NUMCOLUMN: return false;
			case VALCOLUMN: return true;
			case STATECOLUMN: return false;
			case READCOLUMN: return true;
			case WRITECOLUMN: return true;
			default: return false;
		}
	}
	
	
	public Object getValueAt(int row, int col) { 
		switch (col) {
			case NUMCOLUMN: 
				return ""+((CvValue)_cvDisplayVector.elementAt(row)).number();
			case VALCOLUMN:
				return ""+((CvValue)_cvDisplayVector.elementAt(row)).getValue();
			case STATECOLUMN:
				int state = ((CvValue)_cvDisplayVector.elementAt(row)).getState();
				switch (state) {
					case CvValue.UNKNOWN:  	return "Unknown";
					case CvValue.READ:  	return "Read";
					case CvValue.EDITTED:  	return "Editted";
					case CvValue.STORED:  	return "Stored";
					default: return "inconsistent";
				}
			case READCOLUMN: 
				JButton br = new JButton("Read");
				br.setActionCommand("R"+row);
				br.addActionListener(this);
				return br;
			case WRITECOLUMN:
				JButton bw = new JButton("Write");
				bw.setActionCommand("W"+row);
				bw.addActionListener(this);
				return bw;
			default: return "unknown";
		}
	}	

	public void setValueAt(Object value, int row, int col) { 
		switch (col) {
			case VALCOLUMN:
				((CvValue)_cvDisplayVector.elementAt(row)).setValue( Integer.valueOf((String)value).intValue() );
				break;
			default:
				break;
		}
	}	
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("action command: "+e.getActionCommand());
		char b = e.getActionCommand().charAt(0);
		int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
		System.out.println("event "+b+" col "+row);
		if (b=='R') {
			// read command
			((CvValue)_cvDisplayVector.elementAt(row)).read();
		} else {
			// write command
			((CvValue)_cvDisplayVector.elementAt(row)).write();
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		fireTableDataChanged();	
	}
	
	public void addCV(String s) {
		int num = Integer.valueOf(s).intValue();
		if (_cvAllVector.elementAt(num) == null) {
			_numRows++;
			CvValue cv = new CvValue(num);
			_cvAllVector.setElementAt(cv, num);
			_cvDisplayVector.addElement(cv);
			// connect to this CV to ensure the table display updates
			cv.addPropertyChangeListener(this);
			fireTableDataChanged();
		}
	}
}
