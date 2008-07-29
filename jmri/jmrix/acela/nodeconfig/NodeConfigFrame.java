// NodeConfigFrame.java

package jmri.jmrix.acela.nodeconfig;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.jmrix.acela.ActiveFlag;
import jmri.jmrix.acela.AcelaTrafficController;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaSensorManager;

/**
 * Frame for user configuration of Acela nodes
 * @author	Bob Jacobsen   Copyright (C) 2004, 2007, 2008
 * @author	Dave Duchamp   Copyright (C) 2004, 2006
 * @version	$Revision: 1.4 $
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.acela.nodeconfig.NodeConfigBundle");
    protected Container contentPane;
    protected NodeConfigModel d8outputConfigModel;
    protected NodeConfigModel swswitchConfigModel;
    protected NodeConfigModel ymswitchConfigModel;
    protected NodeConfigModel TBoutputConfigModel;
    protected NodeConfigModel TBsensorConfigModel;
    protected NodeConfigModel smswitchConfigModel;
    protected NodeConfigModel wmsensorConfigModel;
    protected NodeConfigModel sysensorConfigModel;

    protected javax.swing.JLabel thenodesStaticH = new javax.swing.JLabel("  00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19");
    protected javax.swing.JLabel thenodesStaticC = new javax.swing.JLabel("");
    protected javax.swing.JLabel thenodesStaticP = new javax.swing.JLabel("              Hardware Polling Double Check Not Supported Yet");
    
    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected javax.swing.JLabel nodeAddrStatic = new javax.swing.JLabel("000");
    protected javax.swing.JLabel nodeTypeStatic = new javax.swing.JLabel("Acela");
    protected javax.swing.JComboBox nodeAddrBox; 
    protected javax.swing.JComboBox nodeTypeBox; 
    
    protected javax.swing.JButton addButton = new javax.swing.JButton(rb.getString("ButtonAdd"));
    protected javax.swing.JButton editButton = new javax.swing.JButton(rb.getString("ButtonEdit"));
    protected javax.swing.JButton deleteButton = new javax.swing.JButton(rb.getString("ButtonDelete"));
    protected javax.swing.JButton doneButton = new javax.swing.JButton(rb.getString("ButtonDone"));
    protected javax.swing.JButton updateButton = new javax.swing.JButton(rb.getString("ButtonUpdate"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(rb.getString("ButtonCancel"));
		
    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();		

    protected javax.swing.JLabel statusTextAcela1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela4 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela5 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela6 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela7 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela8 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela9 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela10 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextAcela11 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextTBrain1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextTBrain2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextTBrain3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextDash81 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextDash82 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextDash83 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextWatchman1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextWatchman2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextWatchman3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSignalman1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSignalman2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSignalman3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSwitchman1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSwitchman2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSwitchman3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextYardMaster1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextYardMaster2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextYardMaster3 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSentry1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSentry2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusTextSentry3 = new javax.swing.JLabel();

    
    
    protected javax.swing.JPanel panelAcela = new JPanel();
    protected javax.swing.JPanel panelTBrain = new JPanel();
    protected javax.swing.JPanel panelDash8 = new JPanel();
    protected javax.swing.JPanel panelWatchman = new JPanel();
    protected javax.swing.JPanel panelSignalman = new JPanel();
    protected javax.swing.JPanel panelSwitchman = new JPanel();
    protected javax.swing.JPanel panelYardMaster = new JPanel();
    protected javax.swing.JPanel panelSentry = new JPanel();
    
    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode
    
    protected AcelaNode curNode = null;    // Acela Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = AcelaNode.UN; // Node type

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = rb.getString("NotesStd1");
    protected String stdStatus2 = rb.getString("NotesStd2");
    protected String stdStatus3 = rb.getString("NotesStd3");
    protected String stdStatusAcela1 = rb.getString("NotesStdAcela1");
    protected String stdStatusAcela2 = rb.getString("NotesStdAcela2");
    protected String stdStatusAcela3 = rb.getString("NotesStdAcela3");
    protected String stdStatusAcela4 = rb.getString("NotesStdAcela4");
    protected String stdStatusAcela5 = rb.getString("NotesStdAcela5");
    protected String stdStatusAcela6 = rb.getString("NotesStdAcela6");
    protected String stdStatusAcela7 = rb.getString("NotesStdAcela7");
    protected String stdStatusAcela8 = rb.getString("NotesStdAcela8");
    protected String stdStatusAcela9 = rb.getString("NotesStdAcela9");
    protected String stdStatusAcela10 = rb.getString("NotesStdAcela10");
    protected String stdStatusAcela11 = rb.getString("NotesStdAcela11");
    protected String stdStatusTBrain1 = rb.getString("NotesStdTBrain1");
    protected String stdStatusTBrain2 = rb.getString("NotesStdTBrain2");
    protected String stdStatusTBrain3 = rb.getString("NotesStdTBrain3");
    protected String stdStatusDash81 = rb.getString("NotesStdDash81");
    protected String stdStatusDash82 = rb.getString("NotesStdDash82");
    protected String stdStatusDash83 = rb.getString("NotesStdDash83");
    protected String stdStatusWatchman1 = rb.getString("NotesStdWatchman1");
    protected String stdStatusWatchman2 = rb.getString("NotesStdWatchman2");
    protected String stdStatusWatchman3 = rb.getString("NotesStdWatchman3");
    protected String stdStatusSignalman1 = rb.getString("NotesStdSignalman1");
    protected String stdStatusSignalman2 = rb.getString("NotesStdSignalman2");
    protected String stdStatusSignalman3 = rb.getString("NotesStdSignalman3");
    protected String stdStatusSwitchman1 = rb.getString("NotesStdSwitchman1");
    protected String stdStatusSwitchman2 = rb.getString("NotesStdSwitchman2");
    protected String stdStatusSwitchman3 = rb.getString("NotesStdSwitchman3");
    protected String stdStatusYardMaster1 = rb.getString("NotesStdYardMaster1");
    protected String stdStatusYardMaster2 = rb.getString("NotesStdYardMaster2");
    protected String stdStatusYardMaster3 = rb.getString("NotesStdYardMaster3");
    protected String stdStatusSentry1 = rb.getString("NotesStdSentry1");
    protected String stdStatusSentry2 = rb.getString("NotesStdSentry2");
    protected String stdStatusSentry3 = rb.getString("NotesStdSentry3");
    protected String editStatus1 = rb.getString("NotesEdit1");
    protected String editStatus2 = rb.getString("NotesEdit2");
    protected String editStatus3 = rb.getString("NotesEdit3");
    protected String infoStatus1 = rb.getString("NotesStd1");
    protected String infoStatus2 = rb.getString("NotesStd2");
    protected String infoStatus3 = rb.getString("NotesStd3");

    protected javax.swing.JTextField receiveDelayField = new javax.swing.JTextField(3);

    /**
     * Constructor method
     */
    public NodeConfigFrame() {
    	super();
    }

    /** 
     *  Initialize the config window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));
			
//        Container contentPane = getContentPane();
        contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        
        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

        // Copy and pasted from the info button
        String nodesstring = ""; 
        int tempnumnodes = AcelaTrafficController.instance().getNumNodes();
        for (int i=0;i<tempnumnodes;i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(i);
            nodesstring = nodesstring + " " + tempnode.getNodeTypeString();
        }
        thenodesStaticC.setText(nodesstring);
        
        
        // panelthenodes displays the current node configuration and polling result
        JPanel panelthenodes = new JPanel();
        panelthenodes.setLayout(new BoxLayout(panelthenodes, BoxLayout.Y_AXIS));

        JPanel panelthenodes1 = new JPanel();
        panelthenodes1.setLayout(new FlowLayout());
        panelthenodes1.add(new JLabel("    The nodes: "));
        panelthenodes1.add(thenodesStaticH);
        panelthenodes.add(panelthenodes1);
        
        JPanel panelthenodes2 = new JPanel();
        panelthenodes2.setLayout(new FlowLayout());
        panelthenodes2.add(new JLabel("As Configured: "));
        panelthenodes2.add(thenodesStaticC);
        panelthenodes.add(panelthenodes2);

        JPanel panelthenodes3 = new JPanel();
        panelthenodes3.setLayout(new FlowLayout());
        panelthenodes3.add(new JLabel("    As Polled: "));
        panelthenodes3.add(thenodesStaticP);
        panelthenodes.add(panelthenodes3);

        Border panelthenodesBorder = BorderFactory.createEtchedBorder();
        Border panelthenodesTitled = BorderFactory.createTitledBorder(panelthenodesBorder,
                                                rb.getString("BoxLabelNodes"));
        panelthenodes.setBorder(panelthenodesTitled);                
        
        contentPane.add(panelthenodes);

        
        // panel11 is the node address
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        
        panel11.add(new JLabel(rb.getString("LabelNodeAddress")+" "));
        nodeAddrBox = new JComboBox(AcelaNode.nodeNames);
        nodeAddrBox.addActionListener(new java.awt.event.ActionListener() 
            {
                public void actionPerformed(java.awt.event.ActionEvent event)
                {
                    infoButtonActionPerformed();
                }
            });
        panel11.add(nodeAddrBox);
        panel11.add(nodeAddrField);
//        nodeAddrField.setToolTipText(rb.getString("TipNodeAddress"));
        nodeAddrField.setText("0");
        panel11.add(nodeAddrStatic);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);
        contentPane.add(panel11);

        // panelNodeInfo is the node type
        JPanel panelNodeInfo = new JPanel();

        panelNodeInfo.add(new JLabel("   "+rb.getString("LabelNodeType")+" "));
        nodeTypeBox = new JComboBox(AcelaNode.moduleNames);
        nodeTypeBox.addActionListener(new java.awt.event.ActionListener() 
            {
                public void actionPerformed(java.awt.event.ActionEvent event)
                {
                    String s = (String)nodeTypeBox.getSelectedItem();
                    if (s.equals("Acela")) {
                        panelAcela.setVisible(true);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("TrainBrain")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(true);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("Dash-8")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(true);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("Watchman")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(true);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("SignalMan")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(true);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("SwitchMan")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(true);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("YardMaster")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(true);
                        panelSentry.setVisible(false);
                    }
                    else if (s.equals("Sentry")) {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(true);
                    }
                    // Add code here for other types of nodes
                    else {
                        panelAcela.setVisible(false);
                        panelTBrain.setVisible(false);
                        panelDash8.setVisible(false);
                        panelWatchman.setVisible(false);
                        panelSignalman.setVisible(false);
                        panelSwitchman.setVisible(false);
                        panelYardMaster.setVisible(false);
                        panelSentry.setVisible(false);
                    }
                }
            });
        panelNodeInfo.add(nodeTypeBox);
        nodeTypeBox.setToolTipText(rb.getString("TipNodeType"));
        panelNodeInfo.add(nodeTypeStatic);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setVisible(true);
        contentPane.add(panelNodeInfo);

        // Set up the Acela nodes
        panelAcela.setLayout(new BoxLayout(panelAcela, BoxLayout.Y_AXIS));
        JPanel panelAcela1 = new JPanel();
        panelAcela1.setLayout(new FlowLayout());
        statusTextAcela1.setText(stdStatusAcela1);
        statusTextAcela1.setVisible(true);
        panelAcela1.add(statusTextAcela1);
        panelAcela.add(panelAcela1);
        JPanel panelAcela2 = new JPanel();
        panelAcela2.setLayout(new FlowLayout());
        statusTextAcela2.setText(stdStatusAcela2);
        statusTextAcela2.setVisible(true);
        panelAcela2.add(statusTextAcela2);
        panelAcela.add(panelAcela2);
        JPanel panelAcela3 = new JPanel();
        panelAcela3.setLayout(new FlowLayout());
        statusTextAcela3.setText(stdStatusAcela3);
        statusTextAcela3.setVisible(true);
        panelAcela3.add(statusTextAcela3);
        panelAcela.add(panelAcela3);
        JPanel panelAcela4 = new JPanel();
        panelAcela4.setLayout(new FlowLayout());
        statusTextAcela4.setText(stdStatusAcela4);
        statusTextAcela4.setVisible(true);
        panelAcela4.add(statusTextAcela4);
        panelAcela.add(panelAcela4);
        JPanel panelAcela5 = new JPanel();
        panelAcela5.setLayout(new FlowLayout());
        statusTextAcela5.setText(stdStatusAcela5);
        statusTextAcela5.setVisible(true);
        panelAcela5.add(statusTextAcela5);
        panelAcela.add(panelAcela5);
        JPanel panelAcela6 = new JPanel();
        panelAcela6.setLayout(new FlowLayout());
        statusTextAcela6.setText(stdStatusAcela6);
        statusTextAcela6.setVisible(true);
        panelAcela6.add(statusTextAcela6);
        panelAcela.add(panelAcela6);
        JPanel panelAcela7 = new JPanel();
        panelAcela7.setLayout(new FlowLayout());
        statusTextAcela7.setText(stdStatusAcela7);
        statusTextAcela7.setVisible(true);
        panelAcela7.add(statusTextAcela7);
        panelAcela.add(panelAcela7);
        JPanel panelAcela8 = new JPanel();
        panelAcela8.setLayout(new FlowLayout());
        statusTextAcela8.setText(stdStatusAcela8);
        statusTextAcela8.setVisible(true);
        panelAcela8.add(statusTextAcela8);
        panelAcela.add(panelAcela8);
        JPanel panelAcela9 = new JPanel();
        panelAcela9.setLayout(new FlowLayout());
        statusTextAcela9.setText(stdStatusAcela9);
        statusTextAcela9.setVisible(true);
        panelAcela9.add(statusTextAcela9);
        panelAcela.add(panelAcela9);
        JPanel panelAcela10 = new JPanel();
        panelAcela10.setLayout(new FlowLayout());
        statusTextAcela10.setText(stdStatusAcela10);
        statusTextAcela10.setVisible(true);
        panelAcela10.add(statusTextAcela10);
        panelAcela.add(panelAcela10);
        JPanel panelAcela11 = new JPanel();
        panelAcela11.setLayout(new FlowLayout());
        statusTextAcela11.setText(stdStatusAcela11);
        statusTextAcela11.setVisible(true);
        panelAcela11.add(statusTextAcela11);
        panelAcela.add(panelAcela11);

        Border panelAcelaBorder = BorderFactory.createEtchedBorder();
        Border panelAcelaTitled = BorderFactory.createTitledBorder(panelAcelaBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelAcela.setBorder(panelAcelaTitled);                
        
        contentPane.add(panelAcela);

        // Set up the Dash8 nodes
        panelDash8.setLayout(new BoxLayout(panelDash8, BoxLayout.Y_AXIS));
        JPanel panelDash81 = new JPanel();
        panelDash81.setLayout(new FlowLayout());
        statusTextDash81.setText(stdStatusDash81);
        statusTextDash81.setVisible(true);
        panelDash81.add(statusTextDash81);
        panelDash8.add(panelDash81);

        JPanel panelDash82 = new JPanel();
        panelDash82.setLayout(new FlowLayout());
        statusTextDash82.setText(stdStatusDash82);
        statusTextDash82.setVisible(true);
        panelDash82.add(statusTextDash82);
        panelDash8.add(panelDash82);

        JPanel panelDash83 = new JPanel();
        panelDash83.setLayout(new FlowLayout());
        statusTextDash83.setText(stdStatusDash83);
        statusTextDash83.setVisible(true);
        panelDash83.add(statusTextDash83);
        panelDash8.add(panelDash83);

        d8outputConfigModel = new OutputConfigModel();
        d8outputConfigModel.setNumRows(8);
        d8outputConfigModel.setEditMode(false);
        JTable d8outputConfigTable = new JTable(d8outputConfigModel);
        d8outputConfigTable.setRowSelectionAllowed(false);
        d8outputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));
			
        JComboBox d8outputTypeCombo = new JComboBox();
        d8outputTypeCombo.addItem(rb.getString("OutputTypeNC"));
        d8outputTypeCombo.addItem(rb.getString("OutputTypeNO"));
			
        JComboBox d8initialStateCombo = new JComboBox();
        d8initialStateCombo.addItem(rb.getString("InitialStateOn"));
        d8initialStateCombo.addItem(rb.getString("InitialStateOff"));
			

        TableColumnModel d8outputColumnModel = d8outputConfigTable.getColumnModel();
        TableColumn d8outputCircuitAddressColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        d8outputCircuitAddressColumn.setMinWidth(70);
        d8outputCircuitAddressColumn.setMaxWidth(80);
        TableColumn d8outputTypeColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        d8outputTypeColumn.setCellEditor(new DefaultCellEditor(d8outputTypeCombo));
        d8outputTypeColumn.setResizable(false);
        d8outputTypeColumn.setMinWidth(90);
        d8outputTypeColumn.setMaxWidth(100);
        TableColumn d8initialStateColumn = d8outputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        d8initialStateColumn.setCellEditor(new DefaultCellEditor(d8initialStateCombo));
        d8initialStateColumn.setResizable(false);
        d8initialStateColumn.setMinWidth(90);
        d8initialStateColumn.setMaxWidth(100);
        TableColumn d8outputaddressColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        d8outputaddressColumn.setMinWidth(110);
        d8outputaddressColumn.setMaxWidth(120);


        // Finish Set up the Dash8 nodes
        JScrollPane d8outputScrollPane = new JScrollPane(d8outputConfigTable);

        JPanel panelDash8Table = new JPanel();
        panelDash8Table.setLayout(new BoxLayout(panelDash8Table, BoxLayout.Y_AXIS));
        
        panelDash8Table.add(d8outputScrollPane,BorderLayout.CENTER);
        panelDash8.add(panelDash8Table,BoxLayout.Y_AXIS);

        Border panelDash8Border = BorderFactory.createEtchedBorder();
        Border panelDash8Titled = BorderFactory.createTitledBorder(panelDash8Border,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelDash8.setBorder(panelDash8Titled);                
        
        panelDash8.setVisible(false);
        contentPane.add(panelDash8);

        
        // Set up the TBrain nodes
        panelTBrain.setLayout(new BoxLayout(panelTBrain, BoxLayout.Y_AXIS));
        JPanel panelTBrain1 = new JPanel();
        statusTextTBrain1.setText(stdStatusTBrain1);
        statusTextTBrain1.setVisible(true);
        panelTBrain1.add(statusTextTBrain1);
        panelTBrain.add(panelTBrain1);

        JPanel panelTBrain2 = new JPanel();
        statusTextTBrain2.setText(stdStatusTBrain2);
        statusTextTBrain2.setVisible(true);
        panelTBrain2.add(statusTextTBrain2);
        panelTBrain.add(panelTBrain2);

        JPanel panelTBrain3 = new JPanel();
        statusTextTBrain3.setText(stdStatusTBrain3);
        statusTextTBrain3.setVisible(true);
        panelTBrain3.add(statusTextTBrain3);
        panelTBrain.add(panelTBrain3);

        TBoutputConfigModel = new OutputConfigModel();
        TBoutputConfigModel.setNumRows(4);
        TBoutputConfigModel.setEditMode(false);
        JTable TBoutputConfigTable = new JTable(TBoutputConfigModel);
        TBoutputConfigTable.setRowSelectionAllowed(false);
        TBoutputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,62));
			
        JComboBox TBoutputTypeCombo = new JComboBox();
        TBoutputTypeCombo.addItem(rb.getString("OutputTypeNC"));
        TBoutputTypeCombo.addItem(rb.getString("OutputTypeNO"));
			
        JComboBox TBinitialStateCombo = new JComboBox();
        TBinitialStateCombo.addItem(rb.getString("InitialStateOn"));
        TBinitialStateCombo.addItem(rb.getString("InitialStateOff"));

        TableColumnModel TBoutputColumnModel = TBoutputConfigTable.getColumnModel();
        TableColumn TBoutputCircuitAddressColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        TBoutputCircuitAddressColumn.setMinWidth(70);
        TBoutputCircuitAddressColumn.setMaxWidth(80);
        TableColumn TBoutputTypeColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        TBoutputTypeColumn.setCellEditor(new DefaultCellEditor(TBoutputTypeCombo));
        TBoutputTypeColumn.setResizable(false);
        TBoutputTypeColumn.setMinWidth(90);
        TBoutputTypeColumn.setMaxWidth(100);
        TableColumn TBinitialStateColumn = TBoutputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        TBinitialStateColumn.setCellEditor(new DefaultCellEditor(TBinitialStateCombo));
        TBinitialStateColumn.setResizable(false);
        TBinitialStateColumn.setMinWidth(90);
        TBinitialStateColumn.setMaxWidth(100);
        TableColumn TBoutputaddressColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        TBoutputaddressColumn.setMinWidth(110);
        TBoutputaddressColumn.setMaxWidth(120);

        JScrollPane TBoutputScrollPane = new JScrollPane(TBoutputConfigTable);

        JPanel panelTrainBrainTable = new JPanel();
        panelTrainBrainTable.setLayout(new BoxLayout(panelTrainBrainTable, BoxLayout.Y_AXIS));
        
        panelTrainBrainTable.add(TBoutputScrollPane,BorderLayout.CENTER);
        panelTBrain.add(panelTrainBrainTable,BoxLayout.Y_AXIS);
        
        TBsensorConfigModel = new SensorConfigModel();
        TBsensorConfigModel.setNumRows(4);
        TBsensorConfigModel.setEditMode(false);

        JTable TBsensorConfigTable = new JTable(TBsensorConfigModel);
        TBsensorConfigTable.setRowSelectionAllowed(false);
        TBsensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,62));
			
        JComboBox TBfilterTypeCombo = new JComboBox();
        TBfilterTypeCombo.addItem(rb.getString("FilterTypeNoise"));
        TBfilterTypeCombo.addItem(rb.getString("FilterTypeDebounce"));
        TBfilterTypeCombo.addItem(rb.getString("FilterTypeCarGap"));
        TBfilterTypeCombo.addItem(rb.getString("FilterTypeDirtyTrack"));
			
        JComboBox TBfilterPolarityCombo = new JComboBox();
        TBfilterPolarityCombo.addItem(rb.getString("FilterNormalPolarity"));
        TBfilterPolarityCombo.addItem(rb.getString("FilterInversePolarity"));
			
        JComboBox TBfilterThresholdCombo = new JComboBox();
        for (int t=0;t<32;t++) {
            TBfilterThresholdCombo.addItem(String.valueOf(t));
        }
/*
        TBfilterThresholdCombo.addItem(rb.getString("FilterNormalThreshold"));
        TBfilterThresholdCombo.addItem(rb.getString("FilterMinThreshold"));
        TBfilterThresholdCombo.addItem(rb.getString("FilterLowThreshold"));
        TBfilterThresholdCombo.addItem(rb.getString("FilterHighThreshold"));
        TBfilterThresholdCombo.addItem(rb.getString("FilterMaxThreshold"));
*/
        TableColumnModel TBtypeColumnModel = TBsensorConfigTable.getColumnModel();
        TableColumn TBcircuitAddressColumn = TBtypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        TBcircuitAddressColumn.setMinWidth(70);
        TBcircuitAddressColumn.setMaxWidth(80);
        TableColumn TBcardTypeColumn = TBtypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        TBcardTypeColumn.setCellEditor(new DefaultCellEditor(TBfilterTypeCombo));
        TBcardTypeColumn.setResizable(false);
        TBcardTypeColumn.setMinWidth(90);
        TBcardTypeColumn.setMaxWidth(100);
        TableColumn TBcardPolarityColumn = TBtypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        TBcardPolarityColumn.setCellEditor(new DefaultCellEditor(TBfilterPolarityCombo));
        TBcardPolarityColumn.setResizable(false);
        TBcardPolarityColumn.setMinWidth(90);
        TBcardPolarityColumn.setMaxWidth(100);
        TableColumn TBcardThresholdColumn = TBtypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        TBcardThresholdColumn.setCellEditor(new DefaultCellEditor(TBfilterThresholdCombo));
        TBcardThresholdColumn.setResizable(false);
        TBcardThresholdColumn.setMinWidth(90);
        TBcardThresholdColumn.setMaxWidth(100);
        TableColumn TBsensorAddressColumn = TBtypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        TBsensorAddressColumn.setMinWidth(110);
        TBsensorAddressColumn.setMaxWidth(1200);
			
        JScrollPane TBsensorScrollPane = new JScrollPane(TBsensorConfigTable);

        JPanel panelTBsensortable = new JPanel();
        panelTBsensortable.setLayout(new BoxLayout(panelTBsensortable, BoxLayout.Y_AXIS));
        
        panelTBsensortable.add(TBsensorScrollPane,BorderLayout.CENTER);
        panelTBrain.add(panelTBsensortable,BoxLayout.Y_AXIS);

        // Finish Set up the TrainBrain nodes
        Border panelTBrainBorder = BorderFactory.createEtchedBorder();
        Border panelTBrainTitled = BorderFactory.createTitledBorder(panelTBrainBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelTBrain.setBorder(panelTBrainTitled);                
        
        contentPane.add(panelTBrain);
        panelTBrain.setVisible(false);

        
        // Set up the Watchman nodes
        panelWatchman.setLayout(new BoxLayout(panelWatchman, BoxLayout.Y_AXIS));
        JPanel panelWatchman1 = new JPanel();
        panelWatchman1.setLayout(new FlowLayout());
        statusTextWatchman1.setText(stdStatusWatchman1);
        statusTextWatchman1.setVisible(true);
        panelWatchman1.add(statusTextWatchman1);
        panelWatchman.add(panelWatchman1);

        JPanel panelWatchman2 = new JPanel();
        panelWatchman2.setLayout(new FlowLayout());
        statusTextWatchman2.setText(stdStatusWatchman2);
        statusTextWatchman2.setVisible(true);
        panelWatchman2.add(statusTextWatchman2);
        panelWatchman.add(panelWatchman2);

        JPanel panelWatchman3 = new JPanel();
        panelWatchman3.setLayout(new FlowLayout());
        statusTextWatchman3.setText(stdStatusWatchman3);
        statusTextWatchman3.setVisible(true);
        panelWatchman3.add(statusTextWatchman3);
        panelWatchman.add(panelWatchman3);
        
        wmsensorConfigModel = new SensorConfigModel();
        wmsensorConfigModel.setNumRows(8);
        wmsensorConfigModel.setEditMode(false);

        JTable wmsensorConfigTable = new JTable(wmsensorConfigModel);
        wmsensorConfigTable.setRowSelectionAllowed(false);
        wmsensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));
			
        JComboBox wmfilterTypeCombo = new JComboBox();
        wmfilterTypeCombo.addItem(rb.getString("FilterTypeNoise"));
        wmfilterTypeCombo.addItem(rb.getString("FilterTypeDebounce"));
        wmfilterTypeCombo.addItem(rb.getString("FilterTypeCarGap"));
        wmfilterTypeCombo.addItem(rb.getString("FilterTypeDirtyTrack"));
			
        JComboBox wmfilterPolarityCombo = new JComboBox();
        wmfilterPolarityCombo.addItem(rb.getString("FilterNormalPolarity"));
        wmfilterPolarityCombo.addItem(rb.getString("FilterInversePolarity"));
			
        JComboBox wmfilterThresholdCombo = new JComboBox();
        for (int t=0;t<32;t++) {
            wmfilterThresholdCombo.addItem(String.valueOf(t));
        }
/*
        wmfilterThresholdCombo.addItem(rb.getString("FilterNormalThreshold"));
        wmfilterThresholdCombo.addItem(rb.getString("FilterMinThreshold"));
        wmfilterThresholdCombo.addItem(rb.getString("FilterLowThreshold"));
        wmfilterThresholdCombo.addItem(rb.getString("FilterHighThreshold"));
        wmfilterThresholdCombo.addItem(rb.getString("FilterMaxThreshold"));
*/
        TableColumnModel wmtypeColumnModel = wmsensorConfigTable.getColumnModel();
        TableColumn wmcircuitAddressColumn = wmtypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        wmcircuitAddressColumn.setMinWidth(70);
        wmcircuitAddressColumn.setMaxWidth(80);
        TableColumn wmcardTypeColumn = wmtypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        wmcardTypeColumn.setCellEditor(new DefaultCellEditor(wmfilterTypeCombo));
        wmcardTypeColumn.setResizable(false);
        wmcardTypeColumn.setMinWidth(90);
        wmcardTypeColumn.setMaxWidth(100);
        TableColumn wmcardPolarityColumn = wmtypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        wmcardPolarityColumn.setCellEditor(new DefaultCellEditor(wmfilterPolarityCombo));
        wmcardPolarityColumn.setResizable(false);
        wmcardPolarityColumn.setMinWidth(90);
        wmcardPolarityColumn.setMaxWidth(100);
        TableColumn wmcardThresholdColumn = wmtypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        wmcardThresholdColumn.setCellEditor(new DefaultCellEditor(wmfilterThresholdCombo));
        wmcardThresholdColumn.setResizable(false);
        wmcardThresholdColumn.setMinWidth(90);
        wmcardThresholdColumn.setMaxWidth(100);
        TableColumn wmsensorAddressColumn = wmtypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        wmsensorAddressColumn.setMinWidth(110);
        wmsensorAddressColumn.setMaxWidth(1200);

        // Finish Set up the Watchman nodes
        JScrollPane wmsensorScrollPane = new JScrollPane(wmsensorConfigTable);

        JPanel panelWatchmantable = new JPanel();
        panelWatchmantable.setLayout(new BoxLayout(panelWatchmantable, BoxLayout.Y_AXIS));
        
        panelWatchmantable.add(wmsensorScrollPane,BorderLayout.CENTER);
        panelWatchman.add(panelWatchmantable,BoxLayout.Y_AXIS);

        Border panelWatchmanBorder = BorderFactory.createEtchedBorder();
        Border panelWatchmanTitled = BorderFactory.createTitledBorder(panelWatchmanBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelWatchman.setBorder(panelWatchmanTitled);                
        
        contentPane.add(panelWatchman);
        panelWatchman.setVisible(false);

        
        // Set up the Signalman nodes
        panelSignalman.setLayout(new BoxLayout(panelSignalman, BoxLayout.Y_AXIS));
        JPanel panelSignalman1 = new JPanel();
        panelSignalman1.setLayout(new FlowLayout());
        statusTextSignalman1.setText(stdStatusSignalman1);
        statusTextSignalman1.setVisible(true);
        panelSignalman1.add(statusTextSignalman1);
        panelSignalman.add(panelSignalman1);

        JPanel panelSignalman2 = new JPanel();
        panelSignalman2.setLayout(new FlowLayout());
        statusTextSignalman2.setText(stdStatusSignalman2);
        statusTextSignalman2.setVisible(true);
        panelSignalman2.add(statusTextSignalman2);
        panelSignalman.add(panelSignalman2);

        JPanel panelSignalman3 = new JPanel();
        panelSignalman3.setLayout(new FlowLayout());
        statusTextSignalman3.setText(stdStatusSignalman3);
        statusTextSignalman3.setVisible(true);
        panelSignalman3.add(statusTextSignalman3);
        panelSignalman.add(panelSignalman3);

        smswitchConfigModel = new SwitchConfigModel();
        smswitchConfigModel.setNumRows(16);
        smswitchConfigModel.setEditMode(false);
        JTable smswitchConfigTable = new JTable(smswitchConfigModel);
        smswitchConfigTable.setRowSelectionAllowed(false);
        smswitchConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));

        JComboBox sminitialStateCombo = new JComboBox();
        sminitialStateCombo.addItem(rb.getString("InitialStateOn"));
        sminitialStateCombo.addItem(rb.getString("InitialStateOff"));
			
        TableColumnModel smswitchColumnModel = smswitchConfigTable.getColumnModel();
        TableColumn smswitchCircuitAddressColumn = smswitchColumnModel.getColumn(SwitchConfigModel.SWITCHCIRCUITADDRESS_COLUMN);
        smswitchCircuitAddressColumn.setMinWidth(70);
        smswitchCircuitAddressColumn.setMaxWidth(80);

        TableColumn sminitialStateColumn = smswitchColumnModel.getColumn(SwitchConfigModel.SWITCHINITIALSTATE_COLUMN);
        sminitialStateColumn.setCellEditor(new DefaultCellEditor(sminitialStateCombo));
        sminitialStateColumn.setResizable(false);
        sminitialStateColumn.setMinWidth(90);
        sminitialStateColumn.setMaxWidth(100);
        TableColumn smswitchaddressColumn = smswitchColumnModel.getColumn(SwitchConfigModel.SWITCHADDRESS_COLUMN);
        smswitchaddressColumn.setMinWidth(110);
        smswitchaddressColumn.setMaxWidth(120);

        // Finish Set up the Signalman nodes
        JScrollPane smswitchScrollPane = new JScrollPane(smswitchConfigTable);

        JPanel panelSignalmanTable = new JPanel();
        panelSignalmanTable.setLayout(new BoxLayout(panelSignalmanTable, BoxLayout.Y_AXIS));
        
        panelSignalmanTable.add(smswitchScrollPane,BorderLayout.CENTER);
        panelSignalman.add(panelSignalmanTable,BoxLayout.Y_AXIS);

        Border panelSignalmanBorder = BorderFactory.createEtchedBorder();
        Border panelSignalmanTitled = BorderFactory.createTitledBorder(panelSignalmanBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelSignalman.setBorder(panelSignalmanTitled);                
        
        panelSignalman.setVisible(false);
        contentPane.add(panelSignalman);

        
        // Set up the YardMaster nodes
        panelYardMaster.setLayout(new BoxLayout(panelYardMaster, BoxLayout.Y_AXIS));
        JPanel panelYardMaster1 = new JPanel();
        panelYardMaster1.setLayout(new FlowLayout());
        statusTextYardMaster1.setText(stdStatusYardMaster1);
        statusTextYardMaster1.setVisible(true);
        panelYardMaster1.add(statusTextYardMaster1);
        panelYardMaster.add(panelYardMaster1);

        JPanel panelYardMaster2 = new JPanel();
        panelYardMaster2.setLayout(new FlowLayout());
        statusTextYardMaster2.setText(stdStatusYardMaster2);
        statusTextYardMaster2.setVisible(true);
        panelYardMaster2.add(statusTextYardMaster2);
        panelYardMaster.add(panelYardMaster2);

        JPanel panelYardMaster3 = new JPanel();
        panelYardMaster3.setLayout(new FlowLayout());
        statusTextYardMaster3.setText(stdStatusYardMaster3);
        statusTextYardMaster3.setVisible(true);
        panelYardMaster3.add(statusTextYardMaster3);
        panelYardMaster.add(panelYardMaster3);

        ymswitchConfigModel = new SwitchConfigModel();
        ymswitchConfigModel.setNumRows(16);
        ymswitchConfigModel.setEditMode(false);
        JTable ymswitchConfigTable = new JTable(ymswitchConfigModel);
        ymswitchConfigTable.setRowSelectionAllowed(false);
        ymswitchConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));

        JComboBox yminitialStateCombo = new JComboBox();
        yminitialStateCombo.addItem(rb.getString("InitialStateOn"));
        yminitialStateCombo.addItem(rb.getString("InitialStateOff"));
			
        TableColumnModel ymswitchColumnModel = ymswitchConfigTable.getColumnModel();
        TableColumn ymswitchCircuitAddressColumn = ymswitchColumnModel.getColumn(SwitchConfigModel.SWITCHCIRCUITADDRESS_COLUMN);
        ymswitchCircuitAddressColumn.setMinWidth(70);
        ymswitchCircuitAddressColumn.setMaxWidth(80);

        TableColumn yminitialStateColumn = ymswitchColumnModel.getColumn(SwitchConfigModel.SWITCHINITIALSTATE_COLUMN);
        yminitialStateColumn.setCellEditor(new DefaultCellEditor(yminitialStateCombo));
        yminitialStateColumn.setResizable(false);
        yminitialStateColumn.setMinWidth(90);
        yminitialStateColumn.setMaxWidth(100);
        TableColumn ymswitchaddressColumn = ymswitchColumnModel.getColumn(SwitchConfigModel.SWITCHADDRESS_COLUMN);
        ymswitchaddressColumn.setMinWidth(110);
        ymswitchaddressColumn.setMaxWidth(120);

        // Finish Set up the YardMaster nodes
        JScrollPane ymswitchScrollPane = new JScrollPane(ymswitchConfigTable);

        JPanel panelYardMasterTable = new JPanel();
        panelYardMasterTable.setLayout(new BoxLayout(panelYardMasterTable, BoxLayout.Y_AXIS));
        
        panelYardMasterTable.add(ymswitchScrollPane,BorderLayout.CENTER);
        panelYardMaster.add(panelYardMasterTable,BoxLayout.Y_AXIS);

        Border panelYardMasterBorder = BorderFactory.createEtchedBorder();
        Border panelYardMasterTitled = BorderFactory.createTitledBorder(panelYardMasterBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelYardMaster.setBorder(panelYardMasterTitled);                
        
        panelYardMaster.setVisible(false);
        contentPane.add(panelYardMaster);

        
        // Set up the SwitchMan nodes
        panelSwitchman.setLayout(new BoxLayout(panelSwitchman, BoxLayout.Y_AXIS));
        JPanel panelSwitchman1 = new JPanel();
        panelSwitchman1.setLayout(new FlowLayout());
        statusTextSwitchman1.setText(stdStatusSwitchman1);
        statusTextSwitchman1.setVisible(true);
        panelSwitchman1.add(statusTextSwitchman1);
        panelSwitchman.add(panelSwitchman1);

        JPanel panelSwitchman2 = new JPanel();
        panelSwitchman2.setLayout(new FlowLayout());
        statusTextSwitchman2.setText(stdStatusSwitchman2);
        statusTextSwitchman2.setVisible(true);
        panelSwitchman2.add(statusTextSwitchman2);
        panelSwitchman.add(panelSwitchman2);

        JPanel panelSwitchman3 = new JPanel();
        panelSwitchman3.setLayout(new FlowLayout());
        statusTextSwitchman3.setText(stdStatusSwitchman3);
        statusTextSwitchman3.setVisible(true);
        panelSwitchman3.add(statusTextSwitchman3);
        panelSwitchman.add(panelSwitchman3);

        swswitchConfigModel = new SwitchConfigModel();
        swswitchConfigModel.setNumRows(16);
        swswitchConfigModel.setEditMode(false);
        JTable swswitchConfigTable = new JTable(swswitchConfigModel);
        swswitchConfigTable.setRowSelectionAllowed(false);
        swswitchConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));

        JComboBox swinitialStateCombo = new JComboBox();
        swinitialStateCombo.addItem(rb.getString("InitialStateOn"));
        swinitialStateCombo.addItem(rb.getString("InitialStateOff"));
			
        TableColumnModel swswitchColumnModel = swswitchConfigTable.getColumnModel();
        TableColumn swswitchCircuitAddressColumn = swswitchColumnModel.getColumn(SwitchConfigModel.SWITCHCIRCUITADDRESS_COLUMN);
        swswitchCircuitAddressColumn.setMinWidth(70);
        swswitchCircuitAddressColumn.setMaxWidth(80);

        TableColumn swinitialStateColumn = swswitchColumnModel.getColumn(SwitchConfigModel.SWITCHINITIALSTATE_COLUMN);
        swinitialStateColumn.setCellEditor(new DefaultCellEditor(swinitialStateCombo));
        swinitialStateColumn.setResizable(false);
        swinitialStateColumn.setMinWidth(90);
        swinitialStateColumn.setMaxWidth(100);
        TableColumn swswitchaddressColumn = swswitchColumnModel.getColumn(SwitchConfigModel.SWITCHADDRESS_COLUMN);
        swswitchaddressColumn.setMinWidth(110);
        swswitchaddressColumn.setMaxWidth(120);

        // Finish Set up the Switchman nodes
        JScrollPane swswitchScrollPane = new JScrollPane(swswitchConfigTable);

        JPanel panelSwitchmanTable = new JPanel();
        panelSwitchmanTable.setLayout(new BoxLayout(panelSwitchmanTable, BoxLayout.Y_AXIS));
        
        panelSwitchmanTable.add(swswitchScrollPane,BorderLayout.CENTER);
        panelSwitchman.add(panelSwitchmanTable,BoxLayout.Y_AXIS);

        Border panelSwitchmanBorder = BorderFactory.createEtchedBorder();
        Border panelSwitchmanTitled = BorderFactory.createTitledBorder(panelSwitchmanBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelSwitchman.setBorder(panelSwitchmanTitled);                
        
        panelSwitchman.setVisible(false);
        contentPane.add(panelSwitchman);

        
        // Set up the Sentry nodes
        panelSentry.setLayout(new BoxLayout(panelSentry, BoxLayout.Y_AXIS));
        JPanel panelSentry1 = new JPanel();
        panelSentry1.setLayout(new FlowLayout());
        statusTextSentry1.setText(stdStatusSentry1);
        statusTextSentry1.setVisible(true);
        panelSentry1.add(statusTextSentry1);
        panelSentry.add(panelSentry1);

        JPanel panelSentry2 = new JPanel();
        panelSentry2.setLayout(new FlowLayout());
        statusTextSentry2.setText(stdStatusSentry2);
        statusTextSentry2.setVisible(true);
        panelSentry2.add(statusTextSentry2);
        panelSentry.add(panelSentry2);

        JPanel panelSentry3 = new JPanel();
        panelSentry3.setLayout(new FlowLayout());
        statusTextSentry3.setText(stdStatusSentry3);
        statusTextSentry3.setVisible(true);
        panelSentry3.add(statusTextSentry3);
        panelSentry.add(panelSentry3);
        
        sysensorConfigModel = new SensorConfigModel();
        sysensorConfigModel.setNumRows(16);
        sysensorConfigModel.setEditMode(false);

        JTable sysensorConfigTable = new JTable(sysensorConfigModel);
        sysensorConfigTable.setRowSelectionAllowed(false);
        sysensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,125));
			
        JComboBox syfilterTypeCombo = new JComboBox();
        syfilterTypeCombo.addItem(rb.getString("FilterTypeNoise"));
        syfilterTypeCombo.addItem(rb.getString("FilterTypeDebounce"));
        syfilterTypeCombo.addItem(rb.getString("FilterTypeCarGap"));
        syfilterTypeCombo.addItem(rb.getString("FilterTypeDirtyTrack"));
			
        JComboBox syfilterPolarityCombo = new JComboBox();
        syfilterPolarityCombo.addItem(rb.getString("FilterNormalPolarity"));
        syfilterPolarityCombo.addItem(rb.getString("FilterInversePolarity"));
			
        JComboBox syfilterThresholdCombo = new JComboBox();
        for (int t=0;t<32;t++) {
            syfilterThresholdCombo.addItem(String.valueOf(t));
        }
/*
        syfilterThresholdCombo.addItem(rb.getString("FilterNormalThreshold"));
        syfilterThresholdCombo.addItem(rb.getString("FilterMinThreshold"));
        syfilterThresholdCombo.addItem(rb.getString("FilterLowThreshold"));
        syfilterThresholdCombo.addItem(rb.getString("FilterHighThreshold"));
        syfilterThresholdCombo.addItem(rb.getString("FilterMaxThreshold"));
*/
        TableColumnModel sytypeColumnModel = sysensorConfigTable.getColumnModel();
        TableColumn sycircuitAddressColumn = sytypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        sycircuitAddressColumn.setMinWidth(70);
        sycircuitAddressColumn.setMaxWidth(80);
        TableColumn sycardTypeColumn = sytypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        sycardTypeColumn.setCellEditor(new DefaultCellEditor(syfilterTypeCombo));
        sycardTypeColumn.setResizable(false);
        sycardTypeColumn.setMinWidth(90);
        sycardTypeColumn.setMaxWidth(100);
        TableColumn sycardPolarityColumn = sytypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        sycardPolarityColumn.setCellEditor(new DefaultCellEditor(syfilterPolarityCombo));
        sycardPolarityColumn.setResizable(false);
        sycardPolarityColumn.setMinWidth(90);
        sycardPolarityColumn.setMaxWidth(100);
        TableColumn sycardThresholdColumn = sytypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        sycardThresholdColumn.setCellEditor(new DefaultCellEditor(syfilterThresholdCombo));
        sycardThresholdColumn.setResizable(false);
        sycardThresholdColumn.setMinWidth(90);
        sycardThresholdColumn.setMaxWidth(100);
        TableColumn sysensorAddressColumn = sytypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        sysensorAddressColumn.setMinWidth(110);
        sysensorAddressColumn.setMaxWidth(1200);

        // Finish Set up the Sentry nodes
        JScrollPane sysensorScrollPane = new JScrollPane(sysensorConfigTable);

        JPanel panelSentrytable = new JPanel();
        panelSentrytable.setLayout(new BoxLayout(panelSentrytable, BoxLayout.Y_AXIS));
        
        panelSentrytable.add(sysensorScrollPane,BorderLayout.CENTER);
        panelSentry.add(panelSentrytable,BoxLayout.Y_AXIS);

        Border panelSentryBorder = BorderFactory.createEtchedBorder();
        Border panelSentryTitled = BorderFactory.createTitledBorder(panelSentryBorder,
                                                rb.getString("BoxLabelNodeSpecific"));
        panelSentry.setBorder(panelSentryTitled);                
        
        contentPane.add(panelSentry);
        panelSentry.setVisible(false);


        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText(stdStatus1);
        statusText1.setVisible(true);
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText(stdStatus2);
        statusText2.setVisible(true);
        panel32.add(statusText2);
        JPanel panel33 = new JPanel();
        panel33.setLayout(new FlowLayout());
        statusText3.setText(stdStatus3);
        statusText3.setVisible(true);
        panel33.add(statusText3);
        panel3.add(panel31);
        panel3.add(panel32);
        panel3.add(panel33);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                                                rb.getString("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);                
        contentPane.add(panel3);
        
        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(rb.getString("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(rb.getString("TipAddButton"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addButtonActionPerformed();
                }
            });
        panel4.add(addButton);
        editButton.setText(rb.getString("ButtonEdit"));
        editButton.setVisible(true);
        editButton.setToolTipText(rb.getString("TipEditButton"));
        panel4.add(editButton);
        editButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    editButtonActionPerformed();
                }
            });
        panel4.add(deleteButton);
        deleteButton.setText(rb.getString("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(rb.getString("TipDeleteButton"));
        panel4.add(deleteButton);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    deleteButtonActionPerformed();
                }
            });
        panel4.add(doneButton);
        doneButton.setText(rb.getString("ButtonDone"));
        doneButton.setVisible(true);
        doneButton.setToolTipText(rb.getString("TipDoneButton"));
        panel4.add(doneButton);
        doneButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doneButtonActionPerformed();
                }
            });
        panel4.add(updateButton);
        updateButton.setText(rb.getString("ButtonUpdate"));
        updateButton.setVisible(true);
        updateButton.setToolTipText(rb.getString("TipUpdateButton"));
        panel4.add(updateButton);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateButtonActionPerformed();
                }
            });
        updateButton.setVisible(false);			
        panel4.add(cancelButton);
        cancelButton.setText(rb.getString("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(rb.getString("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cancelButtonActionPerformed();
                }
            });
        cancelButton.setVisible(false);			

        contentPane.add(panel4);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.acela.nodeconfig.NodeConfigFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to handle add button 
     */        
    public void addButtonActionPerformed() {
       javax.swing.JOptionPane.showMessageDialog(this,
           rb.getString("NotSupported1")+"\n"+rb.getString("NotSupported2"),
                        rb.getString("NotSupportedTitle"),
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
       resetNotes();
       return;
/*
        // Check that a node with this address does not exist
        int addnodeAddress = readNodeAddress();
        if (addnodeAddress < 0) return;

        // get a AcelaNode corresponding to this node address if one exists
        curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(addnodeAddress);
        if (curNode != null) {
            statusText1.setText(rb.getString("Error1")+Integer.toString(addnodeAddress)+
                        rb.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        nodeType = nodeTypeBox.getSelectedIndex();
        // Node 0 and only Node 0 must be an Acela node.
        if ((nodeType == AcelaNode.AC) && (addnodeAddress != 0)) {
            statusText1.setText(rb.getString("Error7"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        
        // all ready, create the new node
        curNode = new AcelaNode(addnodeAddress,nodeType);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error3"));
            statusText1.setVisible(true);
            log.error("Error creating Acela Node, constructor returned null");
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o=0;o<numoutputbits;o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = rb.getString("InitialStateOff");
                }
                else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = rb.getString("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputType[o] = rb.getString("OutputTypeNO");
                }
                else { // if (curNode.getOutputWired(o) == 1) {
                    outputType[o] = rb.getString("OutputTypeNC");
                }
            }
        }
        
        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i=0;i<numsensorbits;i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = rb.getString("FilterTypeNoise");
                }
                else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = rb.getString("FilterTypeDebounce");
                }
                else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = rb.getString("FilterTypeCarGap");
                }
                else {
                    filterType[i] = rb.getString("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = rb.getString("FilterNormalPolarity");
                }
                else {
                    filterPolarity[i] = rb.getString("FilterInversePolarity");
                }

                if (curNode.getSensorThreshold(i) == 0) {
                    filterThreshold[i] = rb.getString("FilterNormalThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 1) {
                    filterThreshold[i] = rb.getString("FilterMinThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 2) {
                    filterThreshold[i] = rb.getString("FilterLowThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 3) {
                    filterThreshold[i] = rb.getString("FilterHighThreshold");
                }
                else {
                    filterThreshold[i] = rb.getString("FilterMaxThreshold");
                }
            }
        }
        
        // configure the new node
        setNodeParameters();
        // register any orphan sensors that this node may have
        AcelaSensorManager.instance().registerSensorsForNode(curNode);
        // reset after succefully adding node
        resetNotes();
        changedNode = true;
        // provide user feedback
        statusText1.setText(rb.getString("FeedBackAdd")+" "+
                                    Integer.toString(addnodeAddress));
        errorInStatus1 = true;
*/
 }

    /**
     * Method to handle info state 
     */        
    public void infoButtonActionPerformed() {

        // lookup the nodes
        String nodesstring = ""; 
        int tempnumnodes = AcelaTrafficController.instance().getNumNodes();
        for (int i=0;i<tempnumnodes;i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(i);
            nodesstring = nodesstring + " " + tempnode.getNodeTypeString();
        }
        thenodesStaticC.setText(nodesstring);
        
        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);     
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setText(AcelaNode.moduleNames[nodeType]);
        nodeTypeStatic.setVisible(true);     

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o=0;o<numoutputbits;o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = rb.getString("InitialStateOff");
                }
                else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = rb.getString("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputType[o] = rb.getString("OutputTypeNO");
                }
                else { // if (curNode.getOutputWired(o) == 1) {
                    outputType[o] = rb.getString("OutputTypeNC");
                }
            }
        }
        
        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i=0;i<numsensorbits;i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = rb.getString("FilterTypeNoise");
                }
                else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = rb.getString("FilterTypeDebounce");
                }
                else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = rb.getString("FilterTypeCarGap");
                }
                else {
                    filterType[i] = rb.getString("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = rb.getString("FilterNormalPolarity");
                }
                else {
                    filterPolarity[i] = rb.getString("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
/*
                if (curNode.getSensorThreshold(i) == 0) {
                    filterThreshold[i] = rb.getString("FilterNormalThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 1) {
                    filterThreshold[i] = rb.getString("FilterMinThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 2) {
                    filterThreshold[i] = rb.getString("FilterLowThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 3) {
                    filterThreshold[i] = rb.getString("FilterHighThreshold");
                }
                else {
                    filterThreshold[i] = rb.getString("FilterMaxThreshold");
                }
*/
            }
        }
        
        // Switch buttons
        editMode = false;
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false); 
        // Switch to edit notes
        statusText1.setText(infoStatus1);
        statusText2.setText(infoStatus2);
        statusText3.setText(infoStatus3);
        
        d8outputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        smswitchConfigModel.setEditMode(false);
        contentPane.repaint();
    }

    /**
     * Method to handle edit button 
     */        
    public void editButtonActionPerformed() {
        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(false);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);     
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(true);
        nodeTypeStatic.setText(AcelaNode.moduleNames[nodeType]);
        nodeTypeStatic.setVisible(false);     

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o=0;o<numoutputbits;o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = rb.getString("InitialStateOff");
                }
                else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = rb.getString("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputType[o] = rb.getString("OutputTypeNO");
                }
                else { // if (curNode.getOutputWired(o) == 1) {
                    outputType[o] = rb.getString("OutputTypeNC");
                }
            }
        }
        
        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i=0;i<numsensorbits;i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = rb.getString("FilterTypeNoise");
                }
                else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = rb.getString("FilterTypeDebounce");
                }
                else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = rb.getString("FilterTypeCarGap");
                }
                else {
                    filterType[i] = rb.getString("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = rb.getString("FilterNormalPolarity");
                }
                else {
                    filterPolarity[i] = rb.getString("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
/*
                if (curNode.getSensorThreshold(i) == 0) {
                    filterThreshold[i] = rb.getString("FilterNormalThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 1) {
                    filterThreshold[i] = rb.getString("FilterMinThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 2) {
                    filterThreshold[i] = rb.getString("FilterLowThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 3) {
                    filterThreshold[i] = rb.getString("FilterHighThreshold");
                }
                else {
                    filterThreshold[i] = rb.getString("FilterMaxThreshold");
                }
*/
            }
        }
        
        // Switch buttons
        editMode = true;
        addButton.setVisible(false);
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        doneButton.setVisible(false);
        updateButton.setVisible(true);
        cancelButton.setVisible(true); 
        // Switch to edit notes
        statusText1.setText(editStatus1);
        statusText2.setText(editStatus2);
        statusText3.setText(editStatus3);

        d8outputConfigModel.setEditMode(true);
        TBoutputConfigModel.setEditMode(true);
        TBsensorConfigModel.setEditMode(true);
        wmsensorConfigModel.setEditMode(true);
        smswitchConfigModel.setEditMode(true);
        contentPane.repaint();

    }

    /**
     * Method to handle delete button 
     */        
    public void deleteButtonActionPerformed() {

       javax.swing.JOptionPane.showMessageDialog(this,
           rb.getString("NotSupported1")+"\n"+rb.getString("NotSupported2"),
                        rb.getString("NotSupportedTitle"),
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
       resetNotes();
       return;
/*
        
        
        // Find Acela Node address
        int delnodeAddress = readNodeAddress();
        if (delnodeAddress < 0) return;
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(delnodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if ( javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                this,rb.getString("ConfirmDelete1")+"\n"+
                    rb.getString("ConfirmDelete2"),rb.getString("ConfirmDeleteTitle"),
                        javax.swing.JOptionPane.OK_CANCEL_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE) ) {
            // delete this node
            AcelaTrafficController.instance().deleteNode(delnodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(rb.getString("FeedBackDelete")+" "+
                                    Integer.toString(delnodeAddress));
            errorInStatus1 = true;
            changedNode = true;
	}
        else {
            // reset as needed
            resetNotes();
        }
*/
 }

    /**
     * Method to handle done button 
     */        
    public void doneButtonActionPerformed() {
        if (editMode) {
            // Reset 
            editMode = false;
            curNode = null;
            // Switch buttons
            addButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            doneButton.setVisible(true);
            updateButton.setVisible(false);
            cancelButton.setVisible(false);
            nodeAddrBox.setVisible(true);
//            nodeAddrField.setVisible(true);
            nodeAddrStatic.setVisible(false);     
            nodeTypeStatic.setVisible(true);     
            nodeTypeBox.setVisible(false);     
        }
        if (changedNode) {
            // Remind user to Save new configuration
            javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("Reminder1")+"\n"+rb.getString("Reminder2"),
                        rb.getString("ReminderTitle"),
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Method to handle update button 
     */        
    public void updateButtonActionPerformed() {
        // update node information
        nodeType = nodeTypeBox.getSelectedIndex();
        log.debug("update performed: was "+curNode.getNodeType()+" request "+nodeType);
        if (curNode.getNodeType() != nodeType) {
            // node type has changed
            curNode.setNodeType(nodeType);
        }
        setNodeParameters();
        changedNode = true;
        // Reset Edit Mode
        editMode = false;
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // make node address editable again	
        nodeAddrBox.setVisible(true);
//        nodeAddrField.setVisible(true);
        nodeAddrStatic.setVisible(false);             
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setVisible(true);             
        // refresh notes panel
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
        // provide user feedback
        statusText1.setText(rb.getString("FeedBackUpdate")+" "+
                                    Integer.toString(nodeAddress));
        errorInStatus1 = true;

            
        d8outputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        smswitchConfigModel.setEditMode(false);
        contentPane.repaint();

    }

    /**
     * Method to handle cancel button 
     */        
    public void cancelButtonActionPerformed() {
        // Reset 
        editMode = false;
        curNode = null;

        // lookup the nodes
        String nodesstring = ""; 
        int tempnumnodes = AcelaTrafficController.instance().getNumNodes();
        for (int i=0;i<tempnumnodes;i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(i);
            nodesstring = nodesstring + " " + tempnode.getNodeTypeString();
        }
        thenodesStaticC.setText(nodesstring);
        
        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);     
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setText(AcelaNode.moduleNames[nodeType]);
        nodeTypeStatic.setVisible(true);     

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o=0;o<numoutputbits;o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = rb.getString("InitialStateOff");
                }
                else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = rb.getString("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputType[o] = rb.getString("OutputTypeNO");
                }
                else { // if (curNode.getOutputWired(o) == 1) {
                    outputType[o] = rb.getString("OutputTypeNC");
                }
            }
        }
        
        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i=0;i<numsensorbits;i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = rb.getString("FilterTypeNoise");
                }
                else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = rb.getString("FilterTypeDebounce");
                }
                else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = rb.getString("FilterTypeCarGap");
                }
                else {
                    filterType[i] = rb.getString("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = rb.getString("FilterNormalPolarity");
                }
                else {
                    filterPolarity[i] = rb.getString("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
/*
                if (curNode.getSensorThreshold(i) == 0) {
                    filterThreshold[i] = rb.getString("FilterNormalThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 1) {
                    filterThreshold[i] = rb.getString("FilterMinThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 2) {
                    filterThreshold[i] = rb.getString("FilterLowThreshold");
                }
                else if (curNode.getSensorThreshold(i) == 3) {
                    filterThreshold[i] = rb.getString("FilterHighThreshold");
                }
                else {
                    filterThreshold[i] = rb.getString("FilterMaxThreshold");
                }
*/
            }
        }
        
        // Switch buttons
        editMode = false;
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false); 
        // Switch to edit notes
        statusText1.setText(infoStatus1);
        statusText2.setText(infoStatus2);
        statusText3.setText(infoStatus3);
        
        d8outputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        smswitchConfigModel.setEditMode(false);
        contentPane.repaint();
    }

    /**
     * Do the done action if the window is closed early.
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
    }    

    /**
     * Method to set node parameters
     *    The node must exist, and be in 'curNode'
     *    Also, the node type must be set and in 'nodeType'
     */
    void setNodeParameters() {
        // set curNode type
        curNode.setNodeType(nodeType);

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o=0;o<numoutputbits;o++) {
                if (initialState[o].contentEquals(rb.getString("InitialStateOff"))) {
                    curNode.setOutputInit(o, 0);
                }
                else { // if (initialState[o].contentEquals(rb.getString("InitialStateOn"))) {
                    curNode.setOutputInit(o, 1);
                }
                if (outputType[o].contentEquals(rb.getString("OutputTypeNO"))) {
                    curNode.setOutputWired(o, 0);
                }
                else { // if (outputType[o].contentEquals(rb.getString("OutputTypeNC"))) {
                    curNode.setOutputWired(o, 1);
                }
            }
        }
        
        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
        
        // set up sensor types
            for (int i=0;i<numsensorbits;i++) {
                if (filterType[i].contentEquals(rb.getString("FilterTypeNoise"))) {
                    curNode.setSensorType(i, 0);
                }
                else if (filterType[i].contentEquals(rb.getString("FilterTypeDebounce"))) {
                    curNode.setSensorType(i, 1);
                }
                else if (filterType[i].contentEquals(rb.getString("FilterTypeCarGap"))) {
                    curNode.setSensorType(i, 2);
                }
                else { // filterType[i].contentEquals(rb.getString("FilterTypeDirtyTrack"))
                    curNode.setSensorType(i, 3);
                }

                if (filterPolarity[i].contentEquals(rb.getString("FilterNormalPolarity"))) {
                    curNode.setSensorPolarity(i, 0);
                }
                else { // filterPolarity[i].contentEquals(rb.getString("FilterInversePolarity"))
                    curNode.setSensorPolarity(i, 1);
                }

                    curNode.setSensorThreshold(i, Integer.parseInt(filterThreshold[i]));
/*
                if (filterThreshold[i].contentEquals(rb.getString("FilterNormalThreshold"))) {
                    curNode.setSensorThreshold(i, 0);
                }
                else if (filterThreshold[i].contentEquals(rb.getString("FilterMinThreshold"))) {
                    curNode.setSensorThreshold(i, 1);
                }
                else if (filterThreshold[i].contentEquals(rb.getString("FilterLowThreshold"))) {
                    curNode.setSensorThreshold(i, 2);
                }
                else if (filterThreshold[i].contentEquals(rb.getString("FilterHighThreshold"))) {
                    curNode.setSensorThreshold(i, 3);
                }
                else { // filterThreshold[i].contentEquals(rb.getString("FilterMaxThreshold"))
                    curNode.setSensorThreshold(i, 4);
                }
*/
            }
        }
        
        
        
        // Cause reinitialization of this Node to reflect these parameters
        AcelaTrafficController.instance().initializeAcelaNode(curNode);
    }
    
    /**
     * Method to reset the notes error after error display
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
                statusText1.setText(editStatus1);
            }
            else {
                statusText1.setText(stdStatus1);
            }
            errorInStatus1 = false;
        }
        resetNotes2();
    }
    /**
     * Reset the second line of Notes area
     */
    private void resetNotes2() {
        if (errorInStatus2) {
            if (editMode) {
                statusText1.setText(editStatus2);
            }
            else {
                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }
    
    /**
     * Read node address and check for legal range
     *     If successful, a node address in the range 0-255 is returned.
     *     If not successful, -1 is returned and an appropriate error
     *          message is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr = -1;
        try 
        {
            addr = nodeAddrBox.getSelectedIndex();
//            addr = Integer.parseInt(nodeAddrField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rb.getString("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        if ( (addr < 0) || (addr > 255) ) {
            statusText1.setText(rb.getString("Error6"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        return (addr);
    }

    /**
     * Set up table for selecting sensor default parameters for Sentry or TBrain nodes
     */
//    public class SensorConfigModel extends AbstractTableModel
    public class SensorConfigModel extends NodeConfigModel
    {
        public String getColumnName(int c) {return sensorConfigColumnNames[c];}
        public Class getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 5;}
        public int getRowCount () {return numrows;}
        public void setNumRows(int r) {
            numrows = r;
        }
        public void setEditMode(boolean b) {
            editmode = b;
        }
        public boolean getEditMode() {
            return editmode;
        }
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r);
            }
            else if (c==1) {
                return filterType[r];
            }
            else if (c==2) {
                return filterPolarity[r];
            }
            else if (c==3) {
                return filterThreshold[r];
            }
            else if (c==4) {
                // Find Acela Node address
                nodeAddress = readNodeAddress();
                if (nodeAddress < 0) return Integer.toString(0);
                // get the AcelaNode corresponding to this node address
                curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
                if (curNode == null) {
                    statusText1.setText(rb.getString("Error4"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return Integer.toString(0);
                }
                return Integer.toString(curNode.getStartingSensorAddress()+r);
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==1) {
                filterType[r] = (String)type;
            }
            if (c==2) {
                filterPolarity[r] = (String)type;
            }
            if (c==3) {
                filterThreshold[r] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {
            if ((c==1) && editmode) {
                return (true);
            }
            if ((c==2) && editmode) {
                return (true);
            }
            if ((c==3) && editmode) {
                return (true);
            }
            return (false);
        }
		
        public static final int SENSORCIRCUITADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
        public static final int POLARITY_COLUMN = 2;
        public static final int THRESHOLD_COLUMN = 3;
        public static final int SENSORADDRESS_COLUMN = 0;
    }
    private String[] sensorConfigColumnNames = {rb.getString("HeadingSensorCircuitAddress"),
                                        rb.getString("HeadingFilterType"),
                                        rb.getString("HeadingFilterPolarity"),
                                        rb.getString("HeadingFilterThreshold"),
                                        rb.getString("HeadingSensorAddress")};
    private String[] filterType = new String[16];
    private String[] filterPolarity = new String[16];
    private String[] filterThreshold = new String[16];

    

    /**
     * Set up table for selecting output default parameters for Dash-8 or TBrain nodes
     */
    public class OutputConfigModel extends NodeConfigModel
    {
        public String getColumnName(int c) {return outputConfigColumnNames[c];}
        public Class getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 4;}
        public int getRowCount () {return numrows;}
        public void setNumRows(int r) {
            numrows = r;
        }
        public void setEditMode(boolean b) {
            editmode = b;
        }
        public boolean getEditMode() {
            return editmode;
        }
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r);
            }
            else if (c==1) {
                return outputType[r];
            }
            else if (c==2) {
                return initialState[r];
            }
            else if (c==3) {
                // Find Acela Node address
                nodeAddress = readNodeAddress();
                if (nodeAddress < 0) return Integer.toString(0);
                // get the AcelaNode corresponding to this node address
                curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
                if (curNode == null) {
                    statusText1.setText(rb.getString("Error4"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return Integer.toString(0);
                }
                return Integer.toString(curNode.getStartingOutputAddress()+r);
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==1) {
                outputType[r] = (String)type;
            }
            if (c==2) {
                initialState[r] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {
            if ((c==1) && editmode) {
                return (true);
            }
            if ((c==2) && editmode) {
                return (true);
            }
            return (false);
        }
		
        public static final int OUTPUTCIRCUITADDRESS_COLUMN = 0;
        public static final int OUTPUTTYPE_COLUMN = 1;
        public static final int INITIALSTATE_COLUMN = 2;
        public static final int OUTPUTADDRESS_COLUMN = 3;
    }
    private String[] outputConfigColumnNames = {rb.getString("HeadingOutputCircuitAddress"),
                                        rb.getString("HeadingOutputType"),
                                        rb.getString("HeadingInitialState"),
                                        rb.getString("HeadingOutputAddress")};
    private String[] outputType = new String[16];
    private String[] initialState = new String[16];

    /**
     * Set up table for selecting output default parameters for SignalMan or Switchman nodes
     */
    public class SwitchConfigModel extends NodeConfigModel
    {
        public String getColumnName(int c) {return switchConfigColumnNames[c];}
        public Class getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 3;}
        public int getRowCount () {return numrows;}
        public void setNumRows(int r) {
            numrows = r;
        }
        public void setEditMode(boolean b) {
            editmode = b;
        }
        public boolean getEditMode() {
            return editmode;
        }
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r);
            }
            else if (c==1) {
                return initialState[r];
            }
            else if (c==2) {
                // Find Acela Node address
                nodeAddress = readNodeAddress();
                if (nodeAddress < 0) return Integer.toString(0);
                // get the AcelaNode corresponding to this node address
                curNode = (AcelaNode) AcelaTrafficController.instance().getNodeFromAddress(nodeAddress);
                if (curNode == null) {
                    statusText1.setText(rb.getString("Error4"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return Integer.toString(0);
                }
                return Integer.toString(curNode.getStartingOutputAddress()+r);
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==1) {
                initialState[r] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {
            if ((c==1) && editmode) {
                return (true);
            }
            return (false);
        }
		
        public static final int SWITCHCIRCUITADDRESS_COLUMN = 0;
        public static final int SWITCHINITIALSTATE_COLUMN = 1;
        public static final int SWITCHADDRESS_COLUMN = 2;
    }
    private String[] switchConfigColumnNames = {rb.getString("HeadingOutputCircuitAddress"),
                                        rb.getString("HeadingInitialState"),
                                        rb.getString("HeadingOutputAddress")};
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NodeConfigFrame.class.getName());

}
