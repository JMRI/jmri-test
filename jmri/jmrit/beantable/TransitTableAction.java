// TransitTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.EntryPoint;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.Block;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Container;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.*;

import jmri.util.JmriJFrame;
import java.util.ArrayList;

/**
 * Swing action to create and register a
 * TransitTable GUI.
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author	Dave Duchamp    Copyright (C) 2008
 * @version     $Revision: 1.10 $
 */

public class TransitTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public TransitTableAction(String actionName) {
		super(actionName);
		// set manager - no need to use InstanceManager here
		transitManager = jmri.InstanceManager.transitManagerInstance();
        // disable ourself if there is no Transit manager available
        if (sectionManager==null) {
            setEnabled(false);
        }

    }

    public TransitTableAction() { this("Transit Table");}
	
	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Transit objects
     */
    void createModel() {
        m = new BeanTableDataModel() {

		static public final int EDITCOL = NUMCOLUMN;
		static public final int DUPLICATECOL = EDITCOL+1;	

       	public String getValue(String name) {
        		if (name == null) {
        			super.log.warn("requested getValue(null)");
        			return "(no name)";
        		}
        		Transit z = InstanceManager.transitManagerInstance().getBySystemName(name);
        		if (z == null) {
        			super.log.debug("requested getValue(\""+name+"\"), Transit doesn't exist");
        			return "(no Transit)";
        		}
				return "Transit";
            }
            public Manager getManager() { return InstanceManager.transitManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.transitManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.transitManagerInstance().getByUserName(name);}

            public void clickOn(NamedBean t) {
            }

    		public int getColumnCount(){ 
    		    return DUPLICATECOL+1;
     		}

    		public Object getValueAt(int row, int col) {
				if (col==VALUECOL) {
            		Transit z = (Transit)getBySystemName(sysNameList.get(row));
                    if (z == null) {
						return "";
					}
					else {
						int state = z.getState();
						if (state==Transit.IDLE) return (rbx.getString("TransitIdle"));
						else if (state==Transit.ASSIGNED) return (rbx.getString("TransitAssigned"));
					}
				}
				else if (col==EDITCOL) return rb.getString("ButtonEdit");
				else if (col==DUPLICATECOL) return rbx.getString("ButtonDuplicate"); 
				else return super.getValueAt(row, col);
				return null;
			}    		

    		public void setValueAt(Object value, int row, int col) {
 				if (col == EDITCOL) {
					// set up to edit
					String sName = (String) getValueAt(row, SYSNAMECOL);
					editPressed(sName);
				} 
 				else if (col == DUPLICATECOL) {
					// set up to duplicate
					String sName = (String) getValueAt(row, SYSNAMECOL);
					duplicatePressed(sName);
				} 
				else super.setValueAt(value, row, col);
    		}

	   		public String getColumnName(int col) {
				if (col==EDITCOL) return "";   // no namne on Edit column
				if (col==DUPLICATECOL) return "";   // no namne on Duplicate column
        		return super.getColumnName(col);
        	}

    		public Class<?> getColumnClass(int col) {
				if (col==VALUECOL) return String.class;  // not a button
 				if (col==EDITCOL) return JButton.class;
 				if (col==DUPLICATECOL) return JButton.class;
				else return super.getColumnClass(col);
		    }

 			public boolean isCellEditable(int row, int col) {
				if (col == VALUECOL) return false;
				if (col == EDITCOL) return true;
				if (col == DUPLICATECOL) return true;
				else return super.isCellEditable(row, col);
			}
			
			public int getPreferredWidth(int col) {
 				// override default value for SystemName and UserName columns
				if (col == SYSNAMECOL)return new JTextField(9).getPreferredSize().width;
				if (col == USERNAMECOL)return new JTextField(17).getPreferredSize().width;
				if (col == VALUECOL)return new JTextField(6).getPreferredSize().width;
				// new columns
     			if (col == EDITCOL) return new JTextField(6).getPreferredSize().width;
     			if (col == DUPLICATECOL) return new JTextField(10).getPreferredSize().width;
   			else return super.getPreferredWidth(col);
		    }

    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }

			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}

			public JButton configureButton() {
				super.log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleTransitTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.TransitTable";
    }
	
	// instance variables
	private boolean editMode = false;
	private boolean duplicateMode = false;
	private TransitManager transitManager = null;
	private SectionManager sectionManager = InstanceManager.sectionManagerInstance();
	private Transit curTransit = null;
	private SectionTableModel sectionTableModel = null;
	private ArrayList<Section> sectionList = new ArrayList<Section>();
	private int[] direction = new int[150];
	private int[] sequence = new int[150];
	@SuppressWarnings("raw")
	private ArrayList[] action = new ArrayList[150]; 
	private boolean[] alternate = new boolean[150];
	private int maxSections = 150;  // must be equal to the dimension of the above arrays
	private ArrayList<Section> primarySectionBoxList = new ArrayList<Section>();
	private int[] priSectionDirection = new int[150];
	private ArrayList<Section> alternateSectionBoxList = new ArrayList<Section>();
	private int[] altSectionDirection = new int[150];
	private Section curSection = null;
	private int curSectionDirection = 0;
	private Section prevSection = null;
	private int prevSectionDirection = 0;
	private int curSequenceNum = 0;

	// add/create variables
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JLabel sysNameFixed = new JLabel("");
    JTextField userName = new JTextField(17);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
	JButton create = null;
	JButton update = null;
	JButton deleteSections = null;
	JComboBox primarySectionBox = new JComboBox();
	JButton addNextSection = null;
	JComboBox alternateSectionBox = new JComboBox();
	JButton addAlternateSection = null;

     /**
	 * Responds to the Add... button and the Edit buttons in Transit Table 
	 */
	void addPressed(ActionEvent e) {
		editMode = false;
		duplicateMode = false;
		if ((sectionManager.getSystemNameList().size()) > 0) {
			addEditPressed();
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, rbx
					.getString("Message21"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}
	void editPressed(String sName) {
		curTransit = transitManager.getBySystemName(sName);
		if (curTransit==null) {
			// no transit - should never happen, but protects against a $%^#@ exception
			return;
		}
		sysNameFixed.setText(sName);
		editMode = true;
		duplicateMode = false;
		addEditPressed();
	}
	void duplicatePressed(String sName) {
		curTransit = transitManager.getBySystemName(sName);
		if (curTransit==null) {
			// no transit - should never happen, but protects against a $%^#@ exception
			return;
		}
		duplicateMode = true;
		editMode = false;
		addEditPressed();
	}
	void addEditPressed() {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddTransit"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TransitAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); 
			p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
			p.add(sysNameFixed);
            p.add(sysName);
			sysName.setToolTipText(rbx.getString("TransitSystemNameHint"));
			p.add (new JLabel("     "));
            p.add(userNameLabel);
            p.add(userName);
			userName.setToolTipText(rbx.getString("TransitUserNameHint"));
            addFrame.getContentPane().add(p);
			addFrame.getContentPane().add(new JSeparator());
			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			JPanel p11 = new JPanel();
			p11.setLayout(new FlowLayout());
			p11.add(new JLabel(rbx.getString("SectionTableMessage")));
			p1.add(p11);
			JPanel p12 = new JPanel();
			// initialize table of sections
			sectionTableModel = new SectionTableModel();
			JTable sectionTable = new JTable(sectionTableModel);
			sectionTable.setRowSelectionAllowed(false);
			sectionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(650,150));
			TableColumnModel sectionColumnModel = sectionTable.getColumnModel();
			TableColumn sequenceColumn = sectionColumnModel.getColumn(SectionTableModel.SEQUENCE_COLUMN);
			sequenceColumn.setResizable(true);
			sequenceColumn.setMinWidth(50);
			sequenceColumn.setMaxWidth(70);
			TableColumn sectionColumn = sectionColumnModel.getColumn(SectionTableModel.SECTIONNAME_COLUMN);
			sectionColumn.setResizable(true);
			sectionColumn.setMinWidth(150);
			sectionColumn.setMaxWidth(210);
			TableColumn actionColumn = sectionColumnModel.getColumn(SectionTableModel.ACTION_COLUMN);
			// install button renderer and editor
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			sectionTable.setDefaultRenderer(JButton.class, buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			sectionTable.setDefaultEditor(JButton.class, buttonEditor);
			JButton testButton = new JButton(rbx.getString("AddEditActions"));
			sectionTable.setRowHeight(testButton.getPreferredSize().height);
			actionColumn.setResizable(false);
			actionColumn.setMinWidth(testButton.getPreferredSize().width);
			TableColumn alternateColumn = sectionColumnModel.getColumn(SectionTableModel.ALTERNATE_COLUMN);
			alternateColumn.setResizable(true);
			alternateColumn.setMinWidth(140);
			alternateColumn.setMaxWidth(170);
			JScrollPane sectionTableScrollPane = new JScrollPane(sectionTable);
			p12.add(sectionTableScrollPane, BorderLayout.CENTER);
			p1.add(p12);
			JPanel p13 = new JPanel();
			p13.setLayout(new FlowLayout());
			p13.add (deleteSections = new JButton(rbx.getString("DeleteSectionsButton")));
            deleteSections.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteAllSections(e);
                }
            });
			deleteSections.setToolTipText(rbx.getString("DeleteSectionsButtonHint"));
			p13.add (new JLabel("     "));
			p13.add (primarySectionBox);
			primarySectionBox.setToolTipText(rbx.getString("PrimarySectionBoxHint"));
			p13.add (addNextSection = new JButton(rbx.getString("AddPrimaryButton")));
            addNextSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNextSectionPressed(e);
                }
            });
			addNextSection.setToolTipText(rbx.getString("AddPrimaryButtonHint"));			
			p1.add(p13);
			JPanel p14 = new JPanel();
			p14.setLayout(new FlowLayout());
			p14.add (alternateSectionBox);
			alternateSectionBox.setToolTipText(rbx.getString("AlternateSectionBoxHint"));
			p14.add (addAlternateSection = new JButton(rbx.getString("AddAlternateButton")));
            addAlternateSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addAlternateSectionPressed(e);
                }
            });
			addAlternateSection.setToolTipText(rbx.getString("AddAlternateButtonHint"));			
			p1.add(p14);
			addFrame.getContentPane().add(p1);
			// set up bottom buttons
			addFrame.getContentPane().add(new JSeparator());
			JButton cancel = null;
			JPanel pb = new JPanel();
			pb.setLayout (new FlowLayout());
            pb.add(cancel = new JButton(rb.getString("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
			cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(rb.getString("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
			create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(rb.getString("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
			update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
			addFrame.getContentPane().add(pb);
        }
		if (editMode) {
			// setup for edit window
			create.setVisible(false);
			update.setVisible(true);
			sysName.setVisible(false);
			sysNameFixed.setVisible(true);
			initializeEditInformation();
		}
		else {
			// setup for create window
			create.setVisible(true);
			update.setVisible(false);
			sysName.setVisible(true);
			sysNameFixed.setVisible(false);
			if (duplicateMode) {
				// setup with information from previous Transit
				initializeEditInformation();
				sysName.setText(curTransit.getSystemName());
				curTransit = null;
			}
			else {			
				deleteAllSections(null);
			}
		}
		initializeSectionCombos();
        addFrame.pack();
        addFrame.setVisible(true);
    }
	private void initializeEditInformation() {
		sectionList.clear();
		curSection = null;
		curSectionDirection = 0;
		curSequenceNum = 0;
		prevSection = null;
		prevSectionDirection = 0;
		if (curTransit!=null) {		
			userName.setText(curTransit.getUserName());
			ArrayList<TransitSection> tsList = curTransit.getTransitSectionList();
			for (int i = 0; i<tsList.size(); i++) {
				TransitSection ts = tsList.get(i);
				if (ts!=null) {
					sectionList.add(ts.getSection());
					sequence[i] = ts.getSequenceNumber();
					direction[i] = ts.getDirection();
					action[i] = ts.getTransitSectionActionList();
					alternate[i] = ts.isAlternate();
				}
			}
			int index = sectionList.size()-1;
			while (alternate[index] && (index>0)) index--;
			if (index>=0) {
				curSection = sectionList.get(index);
				curSequenceNum = sequence[index];
				if (index>0) curSectionDirection = direction[index];
				index --;
				while (alternate[index] && (index>=0)) index--;
				if (index>=0) {
					prevSection = sectionList.get(index);
					prevSectionDirection = direction[index];
				}
			}
		}
		sectionTableModel.fireTableDataChanged();
	}
	private void deleteAllSections(ActionEvent e) {
		sectionList.clear();
		for (int i = 0; i<maxSections; i++) {
			direction[i] = Section.FORWARD;
			sequence[i] = 0;
			action[i] = null;
			alternate[i] = false;
		}
		curSection = null;
		curSectionDirection = 0;
		prevSection = null;
		prevSectionDirection = 0;
		curSequenceNum = 0;
		initializeSectionCombos();
		sectionTableModel.fireTableDataChanged();
	}
	void addNextSectionPressed(ActionEvent e) {
		if (sectionList.size()>maxSections) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message23"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (primarySectionBoxList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message25"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);	
			return;		
		}
		int index = primarySectionBox.getSelectedIndex();
		Section s = primarySectionBoxList.get(index);
		if (s!=null) {
			int j = sectionList.size();
			sectionList.add(s);
			direction[j] = priSectionDirection[index];
			curSequenceNum ++;
			sequence[j] = curSequenceNum;
			action[j] = new ArrayList<TransitSectionAction>();
			alternate[j] = false;
			if ( (sectionList.size()==2) && (curSection!=null) ) {
				if (forwardConnected(curSection,s,0)) {
					direction[0] = Section.REVERSE;
				}
				curSectionDirection = direction[0];
			}
			prevSection = curSection;
			prevSectionDirection = curSectionDirection;
			curSection = s;
			if (prevSection!=null) {
				curSectionDirection = direction[j];
			}
			initializeSectionCombos();
		}	
		sectionTableModel.fireTableDataChanged();
	}
	void addAlternateSectionPressed(ActionEvent e) {
		if (sectionList.size()>maxSections) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message23"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (alternateSectionBoxList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message24"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);	
			return;		
		}
		int index = alternateSectionBox.getSelectedIndex();
		Section s = alternateSectionBoxList.get(index);
		if (s!=null) {
			int j = sectionList.size();
			sectionList.add(s);
			direction[j] = altSectionDirection[index];
			sequence[j] = curSequenceNum;
			action[j] = new ArrayList<TransitSectionAction>();
			alternate[j] = true;
			initializeSectionCombos();
		}	
		sectionTableModel.fireTableDataChanged();
	}
    void createPressed(ActionEvent e) {
		if (!checkTransitInformation()) {
			return;
		}
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
        String sName = sysName.getText().toUpperCase();
		// attempt to create the new Transit
        curTransit = transitManager.createNewTransit(sName, uName);
		if (curTransit==null) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
					.getString("Message22"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);			
			return;
		}
		sysName.setText(curTransit.getSystemName());
		setTransitInformation();
		addFrame.setVisible(false);
    }
	void cancelPressed(ActionEvent e) {
		addFrame.setVisible(false);
		addFrame.dispose();  // remove addFrame from Windows menu
		addFrame = null;
	}
	void updatePressed(ActionEvent e) {
		if (!checkTransitInformation()) {
			return;
		}
		// check if user name has been changed
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
		if ( (uName!=null) && (!uName.equals(curTransit.getUserName())) ) {
			// check that new user name is unique
			Transit tTransit = transitManager.getByUserName(uName);
			if (tTransit!=null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message22"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);			
				return;
			}
		}				
		curTransit.setUserName(uName);
		if (setTransitInformation()) {
			// successful update
			addFrame.setVisible(false);
			addFrame.dispose();  // remove addFrame from Windows menu
			addFrame = null;
		}
	}
	private boolean checkTransitInformation() {
		if (sectionList.size()<=0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
					.getString("Message26"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);			
			return false;
		}		
// djd debugging
// add code here as needed
		return true;
	}
	@SuppressWarnings("null")
	private boolean setTransitInformation() {
		if (curTransit==null) return false;
		curTransit.removeAllSections();
		for (int i = 0; i<sectionList.size(); i++) {
			TransitSection ts = new TransitSection(sectionList.get(i),
				sequence[i], direction[i], alternate[i]);
			if (ts==null) {
				log.error("Trouble creating TransitSection");
				return false;
			}
			@SuppressWarnings("raw")
			ArrayList list = action[i];
			for (int j=0; j<list.size(); j++) {
				ts.addAction( (TransitSectionAction)(list.get(j)));
			}
			curTransit.addTransitSection(ts);
		}
		return true;
	}
	private void initializeSectionCombos() {
		ArrayList<String> allSections = (ArrayList<String>)sectionManager.getSystemNameList();
		primarySectionBox.removeAllItems();
		alternateSectionBox.removeAllItems();
		primarySectionBoxList.clear();
		alternateSectionBoxList.clear();
		if (sectionList.size()==0) {
			// no Sections currently in Transit - all Sections and all Directions OK
			for (int i = 0; i<allSections.size(); i++) {
				String sName = allSections.get(i);
				Section s = sectionManager.getBySystemName(sName);
				if (s!=null) {
					if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
						sName = sName+"( "+s.getUserName()+" )";
					primarySectionBox.addItem(sName);
				    primarySectionBoxList.add(s);
					priSectionDirection[primarySectionBoxList.size()-1] = Section.FORWARD;
				}
			}
		}
		else {
			// limit to Sections that connect to the current Section and are not the previous Section
			for (int i = 0; i<allSections.size(); i++) {
				String sName = allSections.get(i);
				Section s = sectionManager.getBySystemName(sName);
				if (s!=null) {
					if ( (s!=prevSection) && (forwardConnected(s,curSection,curSectionDirection)) ) {
						if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
							sName = sName+"( "+s.getUserName()+" )";
						primarySectionBox.addItem(sName);
						primarySectionBoxList.add(s);
						priSectionDirection[primarySectionBoxList.size()-1] = Section.FORWARD;
					}
					else if ( (s!=prevSection) && (reverseConnected(s,curSection,curSectionDirection)) ) {
						if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
							sName = sName+"( "+s.getUserName()+" )";
						primarySectionBox.addItem(sName);
						primarySectionBoxList.add(s);
						priSectionDirection[primarySectionBoxList.size()-1] = Section.REVERSE;
					}
				}
			}
			// check if there are any alternate Section choices
			if ( prevSection!=null ) {
				for (int i = 0; i<allSections.size(); i++) {
					String sName = allSections.get(i);
					Section s = sectionManager.getBySystemName(sName);
					if (s!=null) {
						if ( (notIncludedWithSeq(s,curSequenceNum)) && 
											forwardConnected(s,prevSection,prevSectionDirection) ) {
							if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
								sName = sName+"( "+s.getUserName()+" )";
							alternateSectionBox.addItem(sName);
							alternateSectionBoxList.add(s);							
							altSectionDirection[alternateSectionBoxList.size()-1] = Section.FORWARD;
						}
						else if ( notIncludedWithSeq(s,curSequenceNum) && 
											reverseConnected(s,prevSection,prevSectionDirection) ) {
							if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
								sName = sName+"( "+s.getUserName()+" )";
							alternateSectionBox.addItem(sName);
							alternateSectionBoxList.add(s);							
							altSectionDirection[alternateSectionBoxList.size()-1] = Section.REVERSE;
						}
					}
				}
			}							
		}
	}
	@SuppressWarnings("unused")
	private boolean connected(Section s1, Section s2) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList<EntryPoint> s1Entries = (ArrayList<EntryPoint>)s1.getEntryPointList();
			ArrayList<EntryPoint> s2Entries = (ArrayList<EntryPoint>)s2.getEntryPointList();
			for (int i = 0; i<s1Entries.size(); i++) {
				Block b = s1Entries.get(i).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					if (b == s2Entries.get(j).getBlock()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean forwardConnected(Section s1, Section s2, int restrictedDirection) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList<EntryPoint> s1ForwardEntries = (ArrayList<EntryPoint>)s1.getForwardEntryPointList();
			ArrayList<EntryPoint> s2Entries = new ArrayList<EntryPoint>();
			if ( restrictedDirection == Section.FORWARD ) {
				s2Entries = (ArrayList<EntryPoint>)s2.getReverseEntryPointList();
			}
			else if ( restrictedDirection == Section.REVERSE ) {
				s2Entries = (ArrayList<EntryPoint>)s2.getForwardEntryPointList();
			}
			else {
				s2Entries = (ArrayList<EntryPoint>)s2.getEntryPointList();
			}
			for (int i = 0; i<s1ForwardEntries.size(); i++) {
				Block b1 = s1ForwardEntries.get(i).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					Block b2 = s2Entries.get(j).getFromBlock();
					if ( (b1 == s2Entries.get(j).getBlock()) &&
							(b2 == s1ForwardEntries.get(i).getBlock()) ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean reverseConnected(Section s1, Section s2, int restrictedDirection) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList<EntryPoint> s1ReverseEntries = (ArrayList<EntryPoint>)s1.getReverseEntryPointList();
			ArrayList<EntryPoint> s2Entries = new ArrayList<EntryPoint>();
			if ( restrictedDirection == Section.FORWARD ) {
				s2Entries = (ArrayList<EntryPoint>)s2.getReverseEntryPointList();
			}
			else if ( restrictedDirection == Section.REVERSE ) {
				s2Entries = (ArrayList<EntryPoint>)s2.getForwardEntryPointList();
			}
			else {
				s2Entries = (ArrayList<EntryPoint>)s2.getEntryPointList();
			}
			for (int i = 0; i<s1ReverseEntries.size(); i++) {
				Block b1 = s1ReverseEntries.get(i).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					Block b2 = s2Entries.get(j).getFromBlock();
					if ( (b1 == s2Entries.get(j).getBlock()) &&
							(b2 == s1ReverseEntries.get(i).getBlock()) ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean notIncludedWithSeq(Section s, int seq) {
		for (int i = 0; i<sectionList.size(); i++) {
			if ( (sectionList.get(i)==s) && (seq==sequence[i]) ) return false;
		}
		return true;
	}
	
	// variables for view actions window
	private int activeRow = 0;
	private SpecialActionTableModel actionTableModel = null;
	private JmriJFrame actionTableFrame = null;
	private JLabel fixedSectionLabel = new JLabel("X");
	
	private void addEditActionsPressed(int r) {
		activeRow = r;
		if (actionTableModel != null) {
			actionTableModel.fireTableStructureChanged();
		}
		if (actionTableFrame == null) {
			actionTableFrame = new JmriJFrame(rbx.getString("TitleViewActions"));
			actionTableFrame.addHelpMenu(
					"package.jmri.jmrit.beantable.ViewSpecialActions", true);
			actionTableFrame.setLocation(50, 60);
			Container contentPane = actionTableFrame.getContentPane();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new FlowLayout());
			JLabel sectionNameLabel = new JLabel(rbx
					.getString("SectionName")+": ");
			panel1.add(sectionNameLabel);
			panel1.add(fixedSectionLabel);
			contentPane.add(panel1);
			// add table of Actions
			JPanel pctSpace = new JPanel();
			pctSpace.setLayout(new FlowLayout());
			pctSpace.add(new JLabel("   "));
			contentPane.add(pctSpace);
			JPanel pct = new JPanel();
			// initialize table of actions
			actionTableModel = new SpecialActionTableModel();
			JTable actionTable = new JTable(actionTableModel);
			actionTable.setRowSelectionAllowed(false);
			actionTable.setPreferredScrollableViewportSize(
							new java.awt.Dimension(750, 200));
			TableColumnModel actionColumnModel = actionTable
					.getColumnModel();
			TableColumn whenColumn = actionColumnModel
					.getColumn(SpecialActionTableModel.WHEN_COLUMN);
			whenColumn.setResizable(true);
			whenColumn.setMinWidth(270);
			whenColumn.setMaxWidth(300);
			TableColumn whatColumn = actionColumnModel
					.getColumn(SpecialActionTableModel.WHAT_COLUMN);
			whatColumn.setResizable(true);
			whatColumn.setMinWidth(290);
			whatColumn.setMaxWidth(350);
			TableColumn editColumn = actionColumnModel
					.getColumn(SpecialActionTableModel.EDIT_COLUMN);
			// install button renderer and editor
			ButtonRenderer buttonRenderer = new ButtonRenderer();
			actionTable.setDefaultRenderer(JButton.class, buttonRenderer);
			TableCellEditor buttonEditor = new ButtonEditor(new JButton());
			actionTable.setDefaultEditor(JButton.class, buttonEditor);
			JButton testButton = new JButton(rbx.getString("ButtonDelete"));
			actionTable.setRowHeight(testButton.getPreferredSize().height);
			editColumn.setResizable(false);
			editColumn.setMinWidth(testButton.getPreferredSize().width);
			TableColumn removeColumn = actionColumnModel
					.getColumn(SpecialActionTableModel.REMOVE_COLUMN);
			removeColumn.setMinWidth(testButton.getPreferredSize().width);
			removeColumn.setResizable(false);
			JScrollPane actionTableScrollPane = new JScrollPane(
					actionTable);
			pct.add(actionTableScrollPane, BorderLayout.CENTER);
			contentPane.add(pct);
			pct.setVisible(true);
			// add view action panel buttons
			JPanel but = new JPanel();
			but.setLayout(new BoxLayout(but, BoxLayout.Y_AXIS));
			JPanel panel4 = new JPanel();
			panel4.setLayout(new FlowLayout());
            JButton newActionButton = new JButton(rbx.getString("ButtonAddNewAction"));
			panel4.add(newActionButton);
			newActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newActionPressed(e);
				}
			});
			newActionButton.setToolTipText(rbx.getString("NewActionButtonHint"));
            JButton doneButton = new JButton(rbx.getString("ButtonDone"));
			panel4.add(doneButton);
			doneButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doneWithActionsPressed(e);
				}
			});
			doneButton.setToolTipText(rbx.getString("DoneButtonHint"));
			but.add(panel4);
			contentPane.add(but);
		}
		fixedSectionLabel.setText(getSectionNameByRow(r)+"    "+
					rbx.getString("SequenceAbbrev")+": "+sequence[r]);
		actionTableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
						actionTableFrame.setVisible(false);
						actionTableFrame.dispose();
						actionTableFrame = null;
						if (addEditActionFrame!=null) {
							addEditActionFrame.setVisible(false);
							addEditActionFrame.dispose();
							addEditActionFrame = null;
						}
					}
				});
		actionTableFrame.pack();
		actionTableFrame.setVisible(true);	
	}
	private void doneWithActionsPressed(ActionEvent e) {
		actionTableFrame.setVisible(false);
		actionTableFrame.dispose();
		actionTableFrame = null;
		if (addEditActionFrame!=null) {
			addEditActionFrame.setVisible(false);
			addEditActionFrame.dispose();
			addEditActionFrame = null;
		}
	}	
	private void newActionPressed(ActionEvent e) {
		editActionMode = false;
		curTSA = null;
		addEditActionWindow();
	}

	// variables for add/edit actions window
	private boolean editActionMode = false;
	private JmriJFrame addEditActionFrame = null;
	private TransitSectionAction curTSA = null;
	private JComboBox whenBox = new JComboBox();
	private JTextField whenDataField = new JTextField(7);
	private JTextField whenStringField = new JTextField(17);
	private JComboBox whatBox = new JComboBox();
	private JTextField whatData1Field = new JTextField(7);
	private JTextField whatData2Field = new JTextField(7);
	private JTextField whatStringField = new JTextField(17);
	private JButton updateActionButton = null;
	private JButton createActionButton = null;
	private JButton cancelAddEditActionButton = null;
	private JComboBox blockBox = new JComboBox();
	private ArrayList<Block> blockList = new ArrayList<Block>();
	
	private void addEditActionWindow() {
		if (addEditActionFrame == null) {
			// set up add/edit action window
			addEditActionFrame = new JmriJFrame(rbx.getString("TitleAddEditAction"));
			addEditActionFrame.addHelpMenu(
					"package.jmri.jmrit.beantable.TransitSectionAddEditAction", true);
			addEditActionFrame.setLocation(120, 80);
			Container contentPane = addEditActionFrame.getContentPane();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			JPanel panelx = new JPanel();
			panelx.setLayout(new BoxLayout(panelx, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new FlowLayout());	
			panel1.add(new JLabel(rbx.getString("WhenText")));		
			initializeWhenBox();
			panel1.add(whenBox);
			whenBox.setToolTipText(rbx.getString("WhenBoxTip"));
			whenBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setWhen(whenBox.getSelectedIndex()+1);
				}
			});
			panel1.add(whenStringField);
			initializeBlockBox();
			panel1.add(blockBox);
			panelx.add(panel1);
			JPanel panel11 = new JPanel();
			panel11.setLayout(new FlowLayout());	
			panel11.add(new JLabel("    "+rbx.getString("OptionalDelay")+": "));
			panel11.add(whenDataField);
			whenDataField.setToolTipText(rbx.getString("HintDelayData"));
			panel11.add(new JLabel(rbx.getString("Milliseconds")));
			panelx.add(panel11);
			JPanel sp = new JPanel();
			sp.setLayout(new FlowLayout());
			sp.add(new JLabel("     "));
			panelx.add(sp);				
			JPanel panel2 = new JPanel();
			panel2.setLayout(new FlowLayout());	
			panel2.add(new JLabel(rbx.getString("WhatText")));		
			initializeWhatBox();
			panel2.add(whatBox);
			whatBox.setToolTipText(rbx.getString("WhatBoxTip"));
			whatBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setWhat(whatBox.getSelectedIndex()+1);
				}
			});
			panel2.add(whatStringField);
			panelx.add(panel2);
			JPanel panel21 = new JPanel();
			panel21.setLayout(new FlowLayout());				
			panel21.add(whatData1Field);
			panel21.add(whatData2Field);
			panelx.add(panel21);
			contentPane.add(panelx);
			contentPane.add(new JSeparator());
			// add buttons
			JPanel but = new JPanel();
			but.setLayout(new FlowLayout());			
            createActionButton = new JButton(rbx.getString("CreateActionButton"));
			but.add(createActionButton);
			createActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createActionPressed(e);
				}
			});
			createActionButton.setToolTipText(rbx.getString("CreateActionButtonHint"));
            updateActionButton = new JButton(rbx.getString("UpdateActionButton"));
			but.add(updateActionButton);
			updateActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateActionPressed(e);
				}
			});
			updateActionButton.setToolTipText(rbx.getString("UpdateActionButtonHint"));
            but.add(cancelAddEditActionButton = new JButton(rb.getString("ButtonCancel")));
            cancelAddEditActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAddEditActionPressed(e);
                }
            });
			cancelAddEditActionButton.setToolTipText(rbx.getString("CancelButtonHint"));
			contentPane.add(but);
		}
		if (editActionMode) {
			// initialize window for the action being edited
			updateActionButton.setVisible(true);
			createActionButton.setVisible(false);
			whenDataField.setText(""+curTSA.getDataWhen());
			whenStringField.setText(curTSA.getStringWhen());
			whatData1Field.setText(""+curTSA.getDataWhat1());
			whatData2Field.setText(""+curTSA.getDataWhat2());
			whatStringField.setText(curTSA.getStringWhat());
			setWhen(curTSA.getWhenCode());
			setWhat(curTSA.getWhatCode());
			setBlockBox();
		}
		else {
			// initialize for add new action
			whenDataField.setText("");
			whenStringField.setText("");
			whatData1Field.setText("");
			whatData2Field.setText("");
			whatStringField.setText("");
			setWhen(1);
			setWhat(1);
			updateActionButton.setVisible(false);
			createActionButton.setVisible(true);
			setBlockBox();			
		}
		addEditActionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
						addEditActionFrame.setVisible(false);
					}
				});
		addEditActionFrame.pack();
		addEditActionFrame.setVisible(true);	
	}
	private void setWhen(int code) {
		whenBox.setSelectedIndex(code-1);
		whenStringField.setVisible(false);
		blockBox.setVisible(false);
		switch (code) {
			case TransitSectionAction.ENTRY:
			case TransitSectionAction.EXIT:
			case TransitSectionAction.TRAINSTOP:
			case TransitSectionAction.TRAINSTART:
			case TransitSectionAction.CONTAINED:
				break;
			case TransitSectionAction.BLOCKENTRY:
			case TransitSectionAction.BLOCKEXIT:
				blockBox.setVisible(true);
				blockBox.setToolTipText(rbx.getString("HintBlockEntry"));
				break;
			case TransitSectionAction.SENSORACTIVE:
			case TransitSectionAction.SENSORINACTIVE:
				whenStringField.setVisible(true);
				whenStringField.setToolTipText(rbx.getString("HintSensorEntry"));
				break;
		}
		addEditActionFrame.pack();
		addEditActionFrame.setVisible(true);			
	}
	private void setWhat(int code) {
		whatBox.setSelectedIndex(code-1);
		whatStringField.setVisible(false);
		whatData1Field.setVisible(false);
		whatData2Field.setVisible(false);
		switch (code) {
			case TransitSectionAction.PAUSE:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintPauseData"));
				break;
			case TransitSectionAction.SETMAXSPEED:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
				break;
			case TransitSectionAction.SETCURRENTSPEED:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
				break;
			case TransitSectionAction.RAMPTRAINSPEED:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
				whatData2Field.setVisible(true);
				whatData2Field.setToolTipText(rbx.getString("HintSetTrainSpeedData2"));
				break;
			case TransitSectionAction.TOMANUALMODE:
				break;
			case TransitSectionAction.RESUMEAUTO:
				break;
			case TransitSectionAction.STARTBELL:
				break;
			case TransitSectionAction.STOPBELL:
				break;
			case TransitSectionAction.SOUNDHORN:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintSoundHornData1"));
				break;
			case TransitSectionAction.SOUNDHORNPATTERN:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintSoundHornPatternData1"));
				whatData2Field.setVisible(true);
				whatData2Field.setToolTipText(rbx.getString("HintSoundHornPatternData2"));
				whatStringField.setVisible(true);
				whatStringField.setToolTipText(rbx.getString("HintSoundHornPatternString"));
				break;
			case TransitSectionAction.LOCOFUNCTION:
				whatData1Field.setVisible(true);
				whatData1Field.setToolTipText(rbx.getString("HintLocoFunctionData1"));
				break;
			case TransitSectionAction.SETSENSORACTIVE:
			case TransitSectionAction.SETSENSORINACTIVE:
				whatStringField.setVisible(true);
				whatStringField.setToolTipText(rbx.getString("HintSensorEntry"));
				break;	
		}
		addEditActionFrame.pack();
		addEditActionFrame.setVisible(true);				
	}
	// temporary action variables
	private int tWhen = 0;
	private int tWhenData = 0;
	private String tWhenString = "";
	private int tWhat = 0;
	private int tWhatData1 = 0;
	private int tWhatData2 = 0;
	private String tWhatString = "";
	// handle button presses in add/edit action window
	private void createActionPressed(ActionEvent e) {
		if ( (!validateWhenData()) || (!validateWhatData()) ) return;
		// entered data is OK, create a special action
		curTSA = new TransitSectionAction(tWhen,tWhat,tWhenData,tWhatData1,tWhatData2,tWhenString,tWhatString);
		if (curTSA==null) {
			log.error("Failure when creating new TransitSectionAction");
		}
// djd - the warning issued for the statement below is extraneous - all works fine - don't change
		@SuppressWarnings({"unchecked"})
		ArrayList<TransitSectionAction> list = action[activeRow];
		list.add(curTSA);
		actionTableModel.fireTableDataChanged();
		addEditActionFrame.setVisible(false);
		addEditActionFrame.dispose();
		addEditActionFrame = null;
	}
	private void updateActionPressed(ActionEvent e) {
		if ( (!validateWhenData()) || (!validateWhatData()) ) return;
		// entered data is OK, update the current special action
		curTSA.setWhenCode(tWhen);
		curTSA.setWhatCode(tWhat);
		curTSA.setDataWhen(tWhenData);
		curTSA.setDataWhat1(tWhatData1);
		curTSA.setDataWhat2(tWhatData2);
		curTSA.setStringWhen(tWhenString);
		curTSA.setStringWhat(tWhatString);
		actionTableModel.fireTableDataChanged();
		addEditActionFrame.setVisible(false);
		addEditActionFrame.dispose();
		addEditActionFrame = null;
	}
	private void cancelAddEditActionPressed(ActionEvent e) {
		addEditActionFrame.setVisible(false);
		addEditActionFrame.dispose();  // remove from Window menu
		addEditActionFrame = null;
	}
	private boolean validateWhenData() {
		tWhen = whenBox.getSelectedIndex()+1;
		String s = whenDataField.getText();
		tWhenData = 0;
		if ( (s!=null) && (!s.equals("")) ) {
			try {
				tWhenData = Integer.parseInt(s);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("DelayError")+"\n"+e),
						rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				log.error("Exception when parsing Field: "+e);
				return false;
			}
			if ((tWhenData<0) || (tWhenData>65500)) {
				JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("DelayRangeError")),
						rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
				return false;
			}				
        }
		tWhenString = "";
		if ( (tWhen == TransitSectionAction.SENSORACTIVE) || (tWhen == TransitSectionAction.SENSORINACTIVE) ) {
			tWhenString = whenStringField.getText();
			if (!validateSensor(tWhenString, true)) return false;
		}
		if ( (tWhen == TransitSectionAction.BLOCKENTRY) || (tWhen == TransitSectionAction.BLOCKEXIT) ) {
			tWhenString = blockList.get(blockBox.getSelectedIndex()).getSystemName();
		}		
		return true;
	}
	private boolean validateSensor(String sName, boolean when) {
		// check if anything entered	
		if (sName.length()<1) {
			// no sensor entered
			JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("NoSensorError")),
						rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// get the sensor corresponding to this name
		Sensor s = InstanceManager.sensorManagerInstance().getSensor(sName);
		if (s==null) {
			// There is no sensor corresponding to this name
			JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("SensorEntryError")),
						rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ( !sName.equals(s.getUserName()) ) {
			if (when) tWhenString = sName.toUpperCase();
			else tWhatString = sName.toUpperCase();
		}
		return true;
	}
	private boolean validateWhatData() {
		tWhat = whatBox.getSelectedIndex()+1;
		tWhatData1 = 0;
		tWhatData2 = 0;
		tWhatString = "";
		switch (tWhat) {
			case TransitSectionAction.PAUSE:
				if (!readWhatData1(rbx.getString("PauseTime"),1,65500)) return false;
				break;
			case TransitSectionAction.SETMAXSPEED:
			case TransitSectionAction.SETCURRENTSPEED:
				if (!readWhatData1(rbx.getString("SpeedPercentage"),1,99)) return false;
				break;
			case TransitSectionAction.RAMPTRAINSPEED:
				if (!readWhatData1(rbx.getString("SpeedPercentage"),1,99)) return false;
				if (!readWhatData2(rbx.getString("RampLengthTime"),100,20000)) return false;
				break;
			case TransitSectionAction.TOMANUALMODE:
			case TransitSectionAction.RESUMEAUTO:
			case TransitSectionAction.STARTBELL:
			case TransitSectionAction.STOPBELL:
				break;
			case TransitSectionAction.SOUNDHORN:
				if (!readWhatData1(rbx.getString("HornBlastLength"),100,65500)) return false;
				break;
			case TransitSectionAction.SOUNDHORNPATTERN:
				if (!readWhatData1(rbx.getString("ShortBlastLength"),100,65500)) return false;
				if (!readWhatData2(rbx.getString("LongBlastLength"),100,65500)) return false;
				tWhatString = whatStringField.getText();
				if ( (tWhatString==null) || tWhatString=="" || (tWhatString.length()<1) ) {
					JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("MissingPattern")),
								rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				tWhatString = tWhatString.toLowerCase();
				for (int i = 0; i<tWhatString.length(); i++) {
					char c = tWhatString.charAt(i);
					if ( (c!='s') && (c!='l') ) {
						JOptionPane.showMessageDialog(addEditActionFrame,(rbx.getString("ErrorPattern")),
								rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				whatStringField.setText(tWhatString);
				break;
			case TransitSectionAction.LOCOFUNCTION:
				if (!readWhatData1(rbx.getString("FunctionNumber"),0,28)) return false;
				break;
			case TransitSectionAction.SETSENSORACTIVE:
			case TransitSectionAction.SETSENSORINACTIVE:
				tWhatString = whatStringField.getText();
				if (!validateSensor(tWhatString, false)) return false;
				break;	
		}
		return true;
	}
	private boolean readWhatData1 (String err, int min, int max) {
		String s = whatData1Field.getText();
		if ((s==null) || (s.equals(""))) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("MissingEntryError"),
						new Object[]{err}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			tWhatData1 = Integer.parseInt(s);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("EntryError")+e,
						new Object[]{err}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
					log.error("Exception when parsing "+err+" Field: "+e);
			return false;
		}
		if ( (tWhatData1<min) || (tWhatData1>max) ) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("EntryRangeError"),
						new Object[]{err, ""+min, ""+max}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}		
		return true;
	}
	private boolean readWhatData2 (String err, int min, int max) {
		String s = whatData2Field.getText();
		if ((s==null) || (s.equals(""))) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("MissingEntryError"),
						new Object[]{err}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			tWhatData2 = Integer.parseInt(s);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("EntryError")+e,
						new Object[]{err}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
					log.error("Exception when parsing "+err+" Field: "+e);
			return false;
		}
		if ( (tWhatData2<min) || (tWhatData2>max) ) {
			JOptionPane.showMessageDialog(addEditActionFrame,
					java.text.MessageFormat.format(rbx.getString("EntryRangeError"),
						new Object[]{err, ""+min, ""+max}), 
							rbx.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
			return false;
		}		
		return true;
	}
	
	// initialize combos for add/edit action window	
	private void initializeWhenBox() {
		whenBox.removeAllItems();
		for (int i = 1; i<=TransitSectionAction.NUM_WHENS; i++) {
			whenBox.addItem(getWhenMenuText(i));
		}
	}
	private String getWhenMenuText(int i) {
		switch (i) {
			case TransitSectionAction.ENTRY:
				return rbx.getString("OnEntry");
			case TransitSectionAction.EXIT:
				return rbx.getString("OnExit");
			case TransitSectionAction.BLOCKENTRY:
				return rbx.getString("OnBlockEntry");
			case TransitSectionAction.BLOCKEXIT:
				return rbx.getString("OnBlockExit");
			case TransitSectionAction.TRAINSTOP:
				return rbx.getString("TrainStop");
			case TransitSectionAction.TRAINSTART:
				return rbx.getString("TrainStart");
			case TransitSectionAction.SENSORACTIVE:
				return rbx.getString("OnSensorActive");
			case TransitSectionAction.SENSORINACTIVE:
				return rbx.getString("OnSensorInactive");
			case TransitSectionAction.CONTAINED:
				return rbx.getString("WithinSection");
		}		
		return "WHEN";
	}
	private void initializeWhatBox() {
		whatBox.removeAllItems();
		for (int i = 1; i<=TransitSectionAction.NUM_WHATS; i++) {
			whatBox.addItem(getWhatMenuText(i));
		}	
	}
	private String getWhatMenuText(int i) {
		switch (i) {
			case TransitSectionAction.PAUSE:
				return rbx.getString("Pause");
			case TransitSectionAction.SETMAXSPEED:
				return rbx.getString("SetMaxSpeed");
			case TransitSectionAction.SETCURRENTSPEED:
				return rbx.getString("SetTrainSpeed");
			case TransitSectionAction.RAMPTRAINSPEED:
				return rbx.getString("RampTrainSpeed");
			case TransitSectionAction.TOMANUALMODE:
				return rbx.getString("ToManualMode");
			case TransitSectionAction.RESUMEAUTO:
				return rbx.getString("ResumeAuto");
			case TransitSectionAction.STARTBELL:
				return rbx.getString("StartBell");	
			case TransitSectionAction.STOPBELL:
				return rbx.getString("StopBell");	
			case TransitSectionAction.SOUNDHORN:
				return rbx.getString("SoundHorn");	
			case TransitSectionAction.SOUNDHORNPATTERN:
				return rbx.getString("SoundHornPattern");	
			case TransitSectionAction.LOCOFUNCTION:
				return rbx.getString("LocoFunction");	
			case TransitSectionAction.SETSENSORACTIVE:
				return rbx.getString("SetSensorActive");	
			case TransitSectionAction.SETSENSORINACTIVE:
				return rbx.getString("SetSensorInactive");	
		}
		return "WHAT";
	}
	private void initializeBlockBox() {
		blockList = sectionList.get(activeRow).getBlockList();
		blockBox.removeAllItems();
		for (int i = 0; i<blockList.size(); i++) {
			String s = blockList.get(i).getSystemName();
			if ( (blockList.get(i).getUserName()!=null) && (!blockList.get(i).getUserName().equals("")) )
				s = s+"("+blockList.get(i).getUserName()+")";
			blockBox.addItem(s);
		}	
	}
	private void setBlockBox() {
		if (editActionMode) {
			if ( (curTSA.getWhenCode()==TransitSectionAction.BLOCKENTRY) ||
							(curTSA.getWhenCode()==TransitSectionAction.BLOCKEXIT) ) {
				// assumes that initializeBlockBox has been called prior to this call
				for (int i = 0; i<blockList.size(); i++) {
					if (curTSA.getStringWhen().equals(blockList.get(i).getSystemName())) {
						blockBox.setSelectedIndex(i);
						return;
					}
				}
			}
		}
		blockBox.setSelectedIndex(0);	
	}
	private void editAction(int r) {
		curTSA = (TransitSectionAction)(action[activeRow].get(r));
		editActionMode = true;
		addEditActionWindow();
	}
	private void deleteAction(int r) {
		TransitSectionAction tsa = (TransitSectionAction)(action[activeRow].get(r));
		action[activeRow].remove(r);
		tsa.dispose();
		actionTableModel.fireTableDataChanged();
	}
	/* 
	 * Notes: For the following, r = row in the Special Actions table.
	 *        A TransitSectionAction must be available for this row. 
	 */ 
	private String getWhenText(int r) {
		TransitSectionAction tsa = (TransitSectionAction)(action[activeRow].get(r));
		switch (tsa.getWhenCode()) {
			case TransitSectionAction.ENTRY:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnEntryDelayedFull"),  
							new Object[] {""+tsa.getDataWhen()});
				return rbx.getString("OnEntryFull");
			case TransitSectionAction.EXIT:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnExitDelayedFull"),  
							new Object[] {""+tsa.getDataWhen()});
				return rbx.getString("OnExitFull");
			case TransitSectionAction.BLOCKENTRY:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnBlockEntryDelayedFull"),  
							new Object[] {""+tsa.getDataWhen(),tsa.getStringWhen()});
				return java.text.MessageFormat.format(rbx.getString("OnBlockEntryFull"),  
							new Object[] {tsa.getStringWhen()});
			case TransitSectionAction.BLOCKEXIT:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnBlockExitDelayedFull"),  
							new Object[] {""+tsa.getDataWhen(),tsa.getStringWhen()});
				return java.text.MessageFormat.format(rbx.getString("OnBlockExitFull"),  
							new Object[] {tsa.getStringWhen()});
			case TransitSectionAction.TRAINSTOP:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("TrainStopDelayedFull"),  
							new Object[] {""+tsa.getDataWhen()});
				return rbx.getString("TrainStopFull");
			case TransitSectionAction.TRAINSTART:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("TrainStartDelayedFull"),  
							new Object[] {""+tsa.getDataWhen()});
				return rbx.getString("TrainStartFull");
			case TransitSectionAction.SENSORACTIVE:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnSensorActiveDelayedFull"),  
							new Object[] {""+tsa.getDataWhen(),tsa.getStringWhen()});
				return java.text.MessageFormat.format(rbx.getString("OnSensorActiveFull"),  
							new Object[] {tsa.getStringWhen()});
			case TransitSectionAction.SENSORINACTIVE:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("OnSensorInactiveDelayedFull"),  
							new Object[] {""+tsa.getDataWhen(),tsa.getStringWhen()});
				return java.text.MessageFormat.format(rbx.getString("OnSensorInactiveFull"),  
							new Object[] {tsa.getStringWhen()});
			case TransitSectionAction.CONTAINED:
				if (tsa.getDataWhen()>0) 
					return java.text.MessageFormat.format(rbx.getString("WithinSectionDelayedFull"),  
							new Object[] {""+tsa.getDataWhen()});
				return rbx.getString("WithinSectionFull");
		}		
		return "WHEN";
	}
	/* 
	 * Notes: For the following, r = row in the Special Actions table.
	 *        A TransitSectionAction must be available for this row.
	 */ 
	private String getWhatText(int r) {
		TransitSectionAction tsa = (TransitSectionAction)(action[activeRow].get(r));
		switch (tsa.getWhatCode()) {
			case TransitSectionAction.PAUSE:
				return java.text.MessageFormat.format(rbx.getString("PauseFull"),  
							new Object[] {tsa.getDataWhat1()});
			case TransitSectionAction.SETMAXSPEED:
				return java.text.MessageFormat.format(rbx.getString("SetMaxSpeedFull"),  
							new Object[] {tsa.getDataWhat1()});	
			case TransitSectionAction.SETCURRENTSPEED:
				return java.text.MessageFormat.format(rbx.getString("SetTrainSpeedFull"),  
							new Object[] {tsa.getDataWhat1()});	
			case TransitSectionAction.RAMPTRAINSPEED:
				return java.text.MessageFormat.format(rbx.getString("RampTrainSpeedFull"),  
							new Object[] {""+tsa.getDataWhat1(),""+tsa.getDataWhat2()});
			case TransitSectionAction.TOMANUALMODE:
				return rbx.getString("ToManualModeFull");
			case TransitSectionAction.RESUMEAUTO:
				return rbx.getString("ResumeAutoFull");
			case TransitSectionAction.STARTBELL:
				return rbx.getString("StartBellFull");	
			case TransitSectionAction.STOPBELL:
				return rbx.getString("StopBellFull");	
			case TransitSectionAction.SOUNDHORN:
				return java.text.MessageFormat.format(rbx.getString("SoundHornFull"),  
							new Object[] {tsa.getDataWhat1()});	
			case TransitSectionAction.SOUNDHORNPATTERN:
				return java.text.MessageFormat.format(rbx.getString("SoundHornPatternFull"),  
						new Object[] {tsa.getStringWhat(),""+tsa.getDataWhat1(),""+tsa.getDataWhat2()});
			case TransitSectionAction.LOCOFUNCTION:
				return java.text.MessageFormat.format(rbx.getString("LocoFunctionFull"),  
							new Object[] {tsa.getDataWhat1()});
			case TransitSectionAction.SETSENSORACTIVE:
				return java.text.MessageFormat.format(rbx.getString("SetSensorActiveFull"),  
							new Object[] {tsa.getStringWhat()});	
			case TransitSectionAction.SETSENSORINACTIVE:
				return java.text.MessageFormat.format(rbx.getString("SetSensorInactiveFull"),  
							new Object[] {tsa.getStringWhat()});			
		}
		return "WHAT";
	}
	private String getSectionNameByRow(int r) {
		String s = sectionList.get(r).getSystemName();
		String u = sectionList.get(r).getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	
    //private boolean noWarn = false;
	
	/**
	 * Table model for Sections in Create/Edit Transit window
	 */
	public class SectionTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int SEQUENCE_COLUMN = 0;
		public static final int SECTIONNAME_COLUMN = 1;
		public static final int ACTION_COLUMN = 2;
		public static final int SEC_DIRECTION_COLUMN = 3;
		public static final int ALTERNATE_COLUMN = 4;

		public SectionTableModel() {
			super();
			sectionManager.addPropertyChangeListener(this);
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				fireTableDataChanged();
			}
		}

		public Class<?> getColumnClass(int c) {
			if ( c==ACTION_COLUMN ) 
				return JButton.class;
			return String.class;
		}

		public int getColumnCount() {
			return ALTERNATE_COLUMN+1;
		}

		public int getRowCount() {
			return (sectionList.size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==ACTION_COLUMN ) 
				return (true);
//			if ( ( c==DATA_COLUMN ) && ( action[r]==TransitSection.PAUSE ) )
//				return (true);
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case SEQUENCE_COLUMN:
				return rbx.getString("SequenceColName");
			case SECTIONNAME_COLUMN:
				return rbx.getString("SectionName");
			case ACTION_COLUMN:
				return rbx.getString("ActionColName");
			case SEC_DIRECTION_COLUMN:
				return rbx.getString("DirectionColName");
			case ALTERNATE_COLUMN:
				return rbx.getString("AlternateColName");
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case SEQUENCE_COLUMN:
				return new JTextField(8).getPreferredSize().width;				
			case SECTIONNAME_COLUMN:
				return new JTextField(17).getPreferredSize().width;
			case ACTION_COLUMN:
				return new JTextField(12).getPreferredSize().width;				
			case SEC_DIRECTION_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			case ALTERNATE_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			}
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > sectionList.size()) {
				return null;
			}
			switch (c) {
				case SEQUENCE_COLUMN:
					return (""+sequence[rx]);
				case SECTIONNAME_COLUMN:
					return (getSectionNameByRow(rx));
				case ACTION_COLUMN:
					return rbx.getString("AddEditActions");
				case SEC_DIRECTION_COLUMN:
					if ( direction[rx]==Section.FORWARD )
						return rbx.getString("SectionForward");
					else if (direction[rx]==Section.REVERSE)
						return rbx.getString("SectionReverse");
					return rbx.getString("Unknown");
				case ALTERNATE_COLUMN:
					if ( alternate[rx] )
						return rbx.getString("Alternate");
					return rbx.getString("Primary");						
				default:
					return rbx.getString("Unknown");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==ACTION_COLUMN) {
				addEditActionsPressed(row);
			}
			return;
		}
	}
	
	/**
	 * Table model for Actions in Special Actions window
	 */
	public class SpecialActionTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int WHEN_COLUMN = 0;
		public static final int WHAT_COLUMN = 1;
		public static final int EDIT_COLUMN = 2;
		public static final int REMOVE_COLUMN = 3;		

		public SpecialActionTableModel() {
			super();
			sectionManager.addPropertyChangeListener(this);
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				fireTableDataChanged();
			}
		}

		public Class<?> getColumnClass(int c) {
			if ( c==WHEN_COLUMN ) return String.class;
			if ( c==WHAT_COLUMN ) return String.class;
			if ( c==EDIT_COLUMN )return JButton.class;
			if ( c==REMOVE_COLUMN )return JButton.class;
			return String.class;
		}

		public int getColumnCount() {
			return REMOVE_COLUMN+1;
		}

		public int getRowCount() {			
			return (action[activeRow].size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==WHEN_COLUMN ) return (false);
			if ( c==WHAT_COLUMN ) return (false);
			if ( c==EDIT_COLUMN )return (true);
			if ( c==REMOVE_COLUMN )return (true);
			return (false);
		}

		public String getColumnName(int col) {
			if (col == WHEN_COLUMN)
				return rbx.getString("WhenColName");
			else if (col == WHAT_COLUMN)
				return rbx.getString("WhatColName");
			return "";
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case WHEN_COLUMN:
				return new JTextField(50).getPreferredSize().width;				
			case WHAT_COLUMN:
				return new JTextField(50).getPreferredSize().width;				
			case EDIT_COLUMN:
				return new JTextField(8).getPreferredSize().width;	
			case REMOVE_COLUMN:
				return new JTextField(8).getPreferredSize().width;	
			}
			return new JTextField(8).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > sectionList.size()) {
				return null;
			}
			switch (c) {
				case WHEN_COLUMN:
					return (getWhenText(rx));
				case WHAT_COLUMN:
					return (getWhatText(rx));
				case EDIT_COLUMN:
					return rb.getString("ButtonEdit");
				case REMOVE_COLUMN:
					return rb.getString("ButtonDelete");
				default:
					return rbx.getString("Unknown");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==EDIT_COLUMN) {
				// set up to edit
				editAction(row);
			}
			if (col==REMOVE_COLUMN) {
				deleteAction(row);
			}
			return;
		}
	}

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TransitTableAction.class.getName());
}

/* @(#)TransitTableAction.java */
