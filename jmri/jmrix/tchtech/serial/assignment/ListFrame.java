/*
 * ListFrame.java
 *
 * Created on August 17, 2007, 8:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.assignment;

/**
 *
 * @author tim
 */
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrix.tchtech.serial.SerialTrafficController;
import jmri.jmrix.tchtech.serial.SerialNode;
import jmri.jmrix.tchtech.serial.SerialAddress;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;

import java.util.ResourceBundle;

import javax.swing.border.Border;
import javax.swing.*;
import javax.swing.table.*;

import java.lang.Integer;


public class ListFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.tchtech.serial.assignment.ListBundle");

	// configured node information
	protected int numConfigNodes = 0;
	protected SerialNode[] configNodes = new SerialNode[256];
	protected int[] configNodeAddresses = new int[256];
	protected boolean inputSelected = false;  // true if displaying input assignments, false for output
	protected SerialNode selNode = null;
	protected String selNodeID = "x"; // text address of selected Node
	public int selNodeNum = 0;  // Address (NA) of selected Node
	public int numBits = 96;  // number of bits in assignment table
	public int numInputBits = 48;  // number of input bits for selected node
	public int numOutputBits = 96; // number of output bits for selected node
	
	// node select pane items
	JLabel nodeLabel = new JLabel(rb.getString("NodeBoxLabel")+" ");
	JComboBox nodeSelBox = new JComboBox();
	ButtonGroup bitTypeGroup = new ButtonGroup();
	JRadioButton inputBits = new JRadioButton(rb.getString("ShowInputButton")+"   ",false);
    JRadioButton outputBits = new JRadioButton(rb.getString("ShowOutputButton"),true);
	JLabel nodeInfoText = new JLabel("Node Information Text");
	
	// assignment pane items
	protected JPanel assignmentPanel = null;
	protected Border inputBorder = BorderFactory.createEtchedBorder();
	protected Border inputBorderTitled = BorderFactory.createTitledBorder(inputBorder,
									rb.getString("AssignmentPanelInputName") );
	protected Border outputBorder = BorderFactory.createEtchedBorder();
	protected Border outputBorderTitled = BorderFactory.createTitledBorder(outputBorder,
									rb.getString("AssignmentPanelOutputName") );
	protected JTable assignmentTable = null;
	protected TableModel assignmentListModel = null;
							
	// button pane items
    JButton printButton = new JButton(rb.getString("PrintButtonText") );
    
    ListFrame curFrame;
    
    public ListFrame() {
        curFrame = this;
    }

    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle(rb.getString("WindowTitle"));
        setSize(1000,2500);
        Container contentPane = getContentPane();        
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up the node selection panel
		initializeNodes();
		nodeSelBox.setEditable(false);
		if (numConfigNodes>0) {
			nodeSelBox.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent event)
				{
					displayNodeInfo( (String)nodeSelBox.getSelectedItem() ); 
				}
			} );
			inputBits.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent event)
				{
					if (inputSelected == false) {
						inputSelected = true;
						displayNodeInfo(selNodeID);
					}
				}
			} );
			outputBits.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent event)
				{
					if (inputSelected == true) {
						inputSelected = false;
						displayNodeInfo(selNodeID);
					}
				}
			} );
		}
		else {
			nodeInfoText.setText(rb.getString("NoNodesError"));
		}
		nodeSelBox.setToolTipText(rb.getString("NodeBoxTip"));
		inputBits.setToolTipText(rb.getString("ShowInputTip"));
		outputBits.setToolTipText(rb.getString("ShowOutputTip"));
		
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
		panel11.add(nodeLabel);
		panel11.add(nodeSelBox);
        bitTypeGroup.add(outputBits);
        bitTypeGroup.add(inputBits);
        panel11.add(inputBits);
        panel11.add(outputBits);
        JPanel panel12 = new JPanel();
		panel12.add(nodeInfoText);
        panel1.add(panel11);
        panel1.add(panel12);
        Border panel1Border = BorderFactory.createEtchedBorder();
        Border panel1Titled = BorderFactory.createTitledBorder(panel1Border,
									rb.getString("NodePanelName") );
        panel1.setBorder(panel1Titled);                
        contentPane.add(panel1);
        
        if (numConfigNodes>0) {
			// Set up the assignment panel
			assignmentPanel = new JPanel();
			assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.Y_AXIS));
			assignmentListModel = new AssignmentTableModel();        
			assignmentTable = new JTable(assignmentListModel);
			assignmentTable.setRowSelectionAllowed(false);
			assignmentTable.setPreferredScrollableViewportSize(new java.awt.Dimension(400,350));
			TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
			TableColumn bitColumn = assignmentColumnModel.getColumn(AssignmentTableModel.BIT_COLUMN);
			bitColumn.setMinWidth(20);
			bitColumn.setMaxWidth(40);
			bitColumn.setResizable(true);
			TableColumn addressColumn = assignmentColumnModel.getColumn(AssignmentTableModel.ADDRESS_COLUMN);
			addressColumn.setMinWidth(40);
			addressColumn.setMaxWidth(85);
			addressColumn.setResizable(true);
			TableColumn sysColumn = assignmentColumnModel.getColumn(AssignmentTableModel.SYSNAME_COLUMN);
			sysColumn.setMinWidth(75);
			sysColumn.setMaxWidth(100);
			sysColumn.setResizable(true);
			TableColumn userColumn = assignmentColumnModel.getColumn(AssignmentTableModel.USERNAME_COLUMN);
			userColumn.setMinWidth(90);
			userColumn.setMaxWidth(450);			
			userColumn.setResizable(true);
			JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
			assignmentPanel.add(assignmentScrollPane,BorderLayout.CENTER);
			if (inputSelected) {
				assignmentPanel.setBorder(inputBorderTitled);
			}
			else {
				assignmentPanel.setBorder(outputBorderTitled);
			}
			contentPane.add(assignmentPanel);
		}
        
        // Set up Print button
        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout());
        printButton.setVisible(true);
        printButton.setToolTipText(rb.getString("PrintButtonTip") );
		if (numConfigNodes>0) {
			printButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					printButtonActionPerformed(e);
				}
			});
		}
		panel3.add(printButton);
        contentPane.add(panel3);

		if (numConfigNodes>0) {
			// initialize for the first time
			displayNodeInfo( (String)nodeSelBox.getSelectedItem() );
		}

        addHelpMenu("package.jmri.jmrix.tchtech.serial.assignment.ListFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to initialize configured nodes and set up the node select combo box
     */        
    public void initializeNodes() {
		String str = "";
		// clear the arrays
		for (int i=0;i<256;i++) {
			configNodeAddresses[i] = -1;
			configNodes[i] = null;
		}
		// get all configured nodes
		SerialNode node = SerialTrafficController.instance().getSerialNode(0);
        int index = 1;
        while (node != null) {
			configNodes[numConfigNodes] = node;
			configNodeAddresses[numConfigNodes] = node.getNodeAddress();
			str = Integer.toString(configNodeAddresses[numConfigNodes]);
			nodeSelBox.addItem(str);
			if (index == 1) {
				selNode = node;
				selNodeNum = configNodeAddresses[numConfigNodes];
				selNodeID = "y";  // to force first time initialization
			}
			numConfigNodes ++;		
			// go to next node
			node = SerialTrafficController.instance().getSerialNode(index);
			index ++;
		}
    }

    /**
     * Method to handle selection of a Node for info display
     */ 
	 public void displayNodeInfo( String nodeID ) {
		if (nodeID != selNodeID) {
			// The selected node is changing - initialize it
			int nAdd = Integer.parseInt(nodeID);
			SerialNode s = null;
			for (int k = 0;k < numConfigNodes;k++) {
				if (nAdd == configNodeAddresses[k]) {
					s = configNodes[k];
				}
			}
			if (s == null) {
				// serious trouble, log error and ignore
				log.error("Cannot find Node "+nodeID+" in list of configured Nodes.");
				return;
			}
			// have node, initialize for new node
			selNodeID = nodeID;
			selNode = s;
			selNodeNum = nAdd;
			// prepare the information line
			int type = selNode.getNodeType();
			if (type == SerialNode.MICRO) {
				nodeInfoText.setText("Micro - 16 "+rb.getString("InputBitsAnd")+" 32 "+
													rb.getString("OutputBits"));
				numInputBits = 16;
				numOutputBits = 32;
			}

			if (type == SerialNode.TERA) {

			
				int bitsPerCard = selNode.getNumBitsPerCard();
				int numInputCards = selNode.numInputCards();
				int numOutputCards = selNode.numOutputCards();
				numInputBits = bitsPerCard*numInputCards;
				numOutputBits = bitsPerCard*numOutputCards;
				nodeInfoText.setText("Tera -  " +bitsPerCard+rb.getString("BitsPerCard")+
						", "+numInputBits+" "+rb.getString("InputBitsAnd")+" "+
							numOutputBits+" "+rb.getString("OutputBits"));
			}
                        if (type == SerialNode.PICO) {
				nodeInfoText.setText("Pico - 8 "+rb.getString("InputBitsAnd")+" 16 "+
													rb.getString("OutputBits"));
				numInputBits = 8;
				numOutputBits = 16;
			
		}
                        else if (type == SerialNode.MEGA) {
				nodeInfoText.setText("Mega - 48 "+rb.getString("InputBitsAnd")+" 96 "+
													rb.getString("OutputBits"));
				numInputBits = 48;
				numOutputBits = 96;
			}
                }
// here insert code for new types of TCH Technology nodes
		
		// initialize for input or output assignments
		if (inputSelected) {
			numBits = numInputBits;
			assignmentPanel.setBorder(inputBorderTitled);
		}
		else {
			numBits = numOutputBits;
			assignmentPanel.setBorder(outputBorderTitled);
		}
		((AssignmentTableModel)assignmentListModel).fireTableDataChanged();
	}

    /**
     * Method to handle print button in List Frame
     */        
    public void printButtonActionPerformed(java.awt.event.ActionEvent e) {
		int[] colWidth = new int[4];
		// initialize column widths
		TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
		colWidth[0] = assignmentColumnModel.getColumn(AssignmentTableModel.BIT_COLUMN).getWidth();
		colWidth[1] = assignmentColumnModel.getColumn(AssignmentTableModel.ADDRESS_COLUMN).getWidth();
		colWidth[2] = assignmentColumnModel.getColumn(AssignmentTableModel.SYSNAME_COLUMN).getWidth();
		colWidth[3] = assignmentColumnModel.getColumn(AssignmentTableModel.USERNAME_COLUMN).getWidth();
		// set up a page title
		String head;
		if (inputSelected) {
			head = "TCH Technology "+rb.getString("AssignmentPanelInputName")+" - "+
													rb.getString("NodeBoxLabel")+" "+selNodeID;
		}
		else {
			head = "TCH Technology "+rb.getString("AssignmentPanelOutputName")+" - "+
													rb.getString("NodeBoxLabel")+" "+selNodeID;
		}
		// initialize a printer writer
		HardcopyWriter writer = null;
		try {
			writer = new HardcopyWriter(curFrame, head, 10, .8, .5, .5, .5, false);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			//log.debug("Print cancelled");
			return;
		}
		writer.increaseLineSpacing(20);
		// print the assignments
		((AssignmentTableModel)assignmentListModel).printTable(writer,colWidth);
    }

    /**
     * Set up table for displaying bit assignments
     */
    public class AssignmentTableModel extends AbstractTableModel
    {
		private String free = rb.getString("AssignmentFree");
		private int curRow = -1;
		private String curRowSysName = "";
		
        public String getColumnName(int c) {return assignmentTableColumnNames[c];}
        public Class getColumnClass(int c) {return String.class;}
		public boolean isCellEditable(int r,int c) {return false;}
        public int getColumnCount () {return 4;}
        public int getRowCount () {return numBits;}
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r+1);
            }
            else if (c==1) {
                return Integer.toString((selNodeNum*1000)+r+1);
            }
            else if (c==2) {
				String  sName = null;
				if (curRow!=r) {
					if (inputSelected) {
						sName = SerialAddress.isInputBitFree(selNodeNum,(r+1));
					}
					else {
						sName = SerialAddress.isOutputBitFree(selNodeNum,(r+1));
					}
					curRow = r;
					curRowSysName = sName;
				}
				else {
					sName = curRowSysName;
				}
				if (sName == null) {
					return (free);
				}
				else {
					return sName;
				}
            }
            else if (c==3) {
				String  sName = null;
				if (curRow!=r) {
					if (inputSelected) {
						sName = SerialAddress.isInputBitFree(selNodeNum,(r+1));
					}
					else {
						sName = SerialAddress.isOutputBitFree(selNodeNum,(r+1));
					}
					curRow = r;
					curRowSysName = sName;
				}
				else {
					sName = curRowSysName;
				}
				if (sName == null) {
					return ("");
				}
				else {
					return (SerialAddress.getUserNameFromSystemName(sName));
				}
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
			// nothing is stored here
        }
		
        public static final int BIT_COLUMN = 0;
        public static final int ADDRESS_COLUMN = 1;
        public static final int SYSNAME_COLUMN = 2;
        public static final int USERNAME_COLUMN = 3;
    
		/**
		* Method to print or print preview the assignment table.
		* Printed in proportionately sized columns across the page with headings and
		* vertical lines between each column. Data is word wrapped within a column.
		* Can only handle 4 columns of data as strings.
		* Adapted from routines in BeanTableDataModel.java by Bob Jacobsen and Dennis Miller
		*/
		public void printTable(HardcopyWriter w,int colWidth[]) {
			// determine the column sizes - proportionately sized, with space between for lines
			int[] columnSize = new int[4];
			int charPerLine = w.getCharactersPerLine();
			int tableLineWidth = 0;  // table line width in characters
			int totalColWidth = 0;
			for (int j = 0; j < 4; j++) {
				totalColWidth += colWidth[j];
			}
			float ratio = ((float)charPerLine)/((float)totalColWidth);
			for (int j = 0; j < 4; j++) {
				columnSize[j] = ((int)(((float)colWidth[j])*ratio)) - 1;
				tableLineWidth += (columnSize[j] + 1);
			}
        
			// Draw horizontal dividing line
			w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
				tableLineWidth);
        
			// print the column header labels
			String[] columnStrings = new String[4];
			// Put each column header in the array
			for (int i = 0; i < 4; i++){
				columnStrings[i] = this.getColumnName(i);
			}
			w.setFontStyle(Font.BOLD);
			printColumns(w, columnStrings, columnSize);
			w.setFontStyle(0);
			// draw horizontal line
			w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
						tableLineWidth);
  
			// now print each row of data
			String[] spaces = new String[4];
			// create base strings the width of each of the columns
			for (int k = 0; k < 4; k++) {
				spaces[k] = "";
				for (int i = 0; i < columnSize[k]; i++) {
					spaces[k] = spaces[k] + " ";
				}
			}
			for (int i = 0; i < this.getRowCount(); i++) {
				for (int j = 0; j < 4; j++) {
					//check for special, null contents
					if (this.getValueAt(i, j) == null) {
						columnStrings[j] = spaces[j];
					} 
					else {
						columnStrings[j] = (String) this.getValueAt(i, j);
					}
				}
				printColumns(w, columnStrings, columnSize);
				// draw horizontal line
				w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
						tableLineWidth);
			}            
			w.close();
		}
    
		protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize[]) 
		{
			String columnString = "";
			String lineString = "";
			String[] spaces = new String[4];
			// create base strings the width of each of the columns
			for (int k = 0; k < 4; k++) {
				spaces[k] = "";
				for (int i = 0; i < columnSize[k]; i++) {
					spaces[k] = spaces[k] + " ";
				}
			}
			// loop through each column
			boolean complete = false;
			while (!complete){
				complete = true;
				for (int i = 0; i < 4; i++) {
					// if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
					// use the initial part of the text,pad it with spaces and place the remainder back in the array
					// for further processing on next line
					// if column string isn't too wide, pad it to column width with spaces if needed
					if (columnStrings[i].length() > columnSize[i]) {
						// this column string will not fit on one line
						boolean noWord = true;
						for (int k = columnSize[i]; k >= 1 ; k--) {
							if (columnStrings[i].substring(k-1,k).equals(" ") 
									|| columnStrings[i].substring(k-1,k).equals("-")
									|| columnStrings[i].substring(k-1,k).equals("_")) {
								columnString = columnStrings[i].substring(0,k) 
										+ spaces[i].substring(columnStrings[i].substring(0,k).length());
								columnStrings[i] = columnStrings[i].substring(k);
								noWord = false;
								complete = false;
								break;
							}
						}
						if (noWord) {
							columnString = columnStrings[i].substring(0,columnSize[i]);
							columnStrings[i] = columnStrings[i].substring(columnSize[i]);
							complete = false;
						}                    
					}	
					else {
						// this column string will fit on one line
						columnString = columnStrings[i] + spaces[i].substring(columnStrings[i].length());
						columnStrings[i] = "";
					}
					lineString = lineString + columnString + " ";
				}
				try {
					w.write(lineString);
					//write vertical dividing lines
					int iLine = w.getCurrentLineNumber();
					for (int i = 0, k = 0; i < w.getCharactersPerLine(); k++) {
						w.write( iLine, i, iLine + 1, i);
						if (k<4) {
							i = i+columnSize[k]+1;
						}
						else {
							i = w.getCharactersPerLine();
						}
					}
					lineString = "\n";
					w.write(lineString);
					lineString = "";
				} 
				catch (IOException e) { 
					log.warn("error during printing: "+e);
				}
			}
		}
    }
    private String[] assignmentTableColumnNames = {rb.getString("HeadingBit"),
												rb.getString("HeadingAddress"),
												rb.getString("HeadingSystemName"),
												rb.getString("HeadingUserName")};

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ListFrame.class.getName());
	
}

/* @(#)ListFrame.java */

