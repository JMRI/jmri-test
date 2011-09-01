// DecoderPro3Window.java

 package apps.gui3.dp3;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;

import java.util.List;
import java.util.ResourceBundle;
import jmri.util.swing.ResizableImagePanel;
import jmri.jmrit.decoderdefn.DecoderFile;

// for ugly code
import jmri.Programmer;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.roster.swing.*;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import java.beans.PropertyChangeListener;


/**
 * Standalone DecoderPro3 Window (new GUI)
 *
 * Ignores WindowInterface.
 *
 * TODO:
 * Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet (dummy object below)
 * Color only covering borders
 * No reset toolbar support yet
 * No glass pane support (See DecoderPro3Panes class and usage below)
 * Special panes (Roster entry, attributes, graphics) not included
 * How do you pick a programmer file? (hardcoded)
 * Initialization needs partial deferal, too for 1st pane to appear
 * 
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneSet
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision$
 */
 
public class DecoderPro3Window 
        extends jmri.util.swing.multipane.TwoPaneTBWindow {

    static int openWindowInstances = 0;
    
    /**
    * Loads Decoder Pro 3 with the default set of menus and toolbars
    */ 
    public DecoderPro3Window(){
        super("DecoderPro", 
                 new File("xml/config/apps/decoderpro/Gui3Menus.xml"),
                 new File("xml/config/apps/decoderpro/Gui3MainToolBar.xml"));
        buildWindow();
    }
    
    /**
    * Loads Decoder Pro 3 with specific menu and toolbar files
    */ 
    public DecoderPro3Window(File menuFile, File toolbarFile) {
        super("DecoderPro", 
    	        menuFile, 
    	        toolbarFile);
        buildWindow();
    }
    
    protected void buildWindow(){
        openWindowInstances++;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    	getTop().add(createTop());
        //This value may return null if the DP3 window has been called from a the traditional JMRI menu frame
        if(apps.gui3.Apps3.buttonSpace()!=null)
            getToolBar().add(apps.gui3.Apps3.buttonSpace());
        getBottom().setMinimumSize(new Dimension(0, 250));
        getBottom().add(createBottom());
        statusBar();
        systemsMenu();
        helpMenu(getMenu(), this);
        setSize(getMaximumSize());
        setVisible(true);
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            //System.out.println(jmri.managers.ManagerDefaultSelector.instance.getDefault(jmri.ProgrammerManager.class));
            //System.out.println(jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer());
        }
        if(p.getSimplePreferenceState(DecoderPro3Window.class.getName()+"hideSummary")){
            hideBottomPane(true);
            hideSummary=true;
        }
        
        String rosterGroup = p.getComboBoxLastSelection(rosterGroupSelectionCombo);
        Roster.instance().setRosterGroup(rosterGroup);
        Roster.instance().addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("ActiveRosterGroup")){
                    String activeGroup = ((String)e.getNewValue());
                    p.addComboBoxLastSelection(rosterGroupSelectionCombo, activeGroup);
                    activeRosterGroupField.setText(activeGroup);
                }
            }
        });
        activeRosterGroupField.setText(Roster.getRosterGroupName());
        getToolBar().add(modePanel);
        getToolBar().add(new jmri.jmrit.roster.SelectRosterGroupPanelAction("Select Group").makePanel());

    }
    
    jmri.UserPreferencesManager p;
    final String rosterGroupSelectionCombo = this.getClass().getName()+".rosterGroupSelected";
    
    /*
    * This status bar needs sorting out properly
    */
    void statusBar(){
        Border blackline = BorderFactory.createMatteBorder(0,0,0,1,Color.black);
        JLabel programmerLabel = new JLabel();
        //programmerLabel.setBorder(blackline);
        Font statusBarFont = programmerLabel.getFont().deriveFont(10f);
        programmerLabel.setFont(statusBarFont);
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            programmerLabel.setText ("Programmer " + "Is Available");
            programmerLabel.setForeground(new Color(0, 128, 0));
        } else {
            programmerLabel.setText("No Programmer Available");
            programmerLabel.setForeground(Color.red);
        }
        getStatus().add(programmerLabel);
        getStatus().add(Box.createHorizontalGlue());
        JLabel spacerLabel = new JLabel("   ");
        spacerLabel.setBorder(blackline);
        getStatus().add(spacerLabel);
        JLabel statusTitle = new JLabel("Programmer Status : ");
        statusTitle.setFont(statusBarFont);
        getStatus().add(statusTitle);
        statusField.setFont(statusBarFont);
        statusField.setText("idle");
        getStatus().add(statusField);
        spacerLabel = new JLabel("   ");
        spacerLabel.setBorder(blackline);
        getStatus().add(spacerLabel);
        statusTitle = new JLabel("Active Roster Group : ");
        statusTitle.setFont(statusBarFont);
        getStatus().add(statusTitle);
        activeRosterGroupField.setFont(statusBarFont);
        getStatus().add(activeRosterGroupField);
    }
    
    protected void systemsMenu() {
        jmri.jmrix.ActiveSystemsMenu.addItems(getMenu());
        getMenu().add(new jmri.util.WindowMenu(this));
    }
    
    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        try {
            // create menu and standard items
            JMenu helpMenu = jmri.util.HelpUtil.makeHelpMenu("package.apps.Apps", true);
            
            // tell help to use default browser for external types
            javax.help.SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
                
            // use as main help menu 
            menuBar.add(helpMenu);
            
        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }

    }
    
    jmri.jmrit.roster.swing.RosterTable rtable;
    ResourceBundle rb = ResourceBundle.getBundle("apps.gui3.dp3.DecoderPro3Bundle");
    
    JComponent createTop() {
        JPanel retval = new JPanel();
        retval.setLayout(new BorderLayout());
        //retval.add(createToolBarPanel(), BorderLayout.NORTH);
        // set up roster table
         
        rtable = new RosterTable(false);
        retval.add(rtable, BorderLayout.CENTER);
        // add selection listener
        rtable.getTable().getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting()) {
                        for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                            if (rtable.getTable().getSelectionModel().isSelectedIndex(i)) {
                                locoSelected(rtable.getModel().getValueAt(i, RosterTableModel.IDCOL).toString());
                                break;
                            }
                        }    
                    }
                }
            }
        );
        
        return retval;
    }
    
    /**
     * An entry has been selected in the Roster Table, 
     * activate the bottom part of the window
     */
    void locoSelected(String id) {
        log.debug("locoSelected ID "+id);
        // convert to roster entry
        re = Roster.instance().entryFromTitle(id);
        
        updateDetails();
    }
    
    ProgDebugger programmer = new ProgDebugger();
        
    JPanel rosterDetailPanel = new JPanel();
        
    JComponent createBottom(){
        
        locoImage = new ResizableImagePanel(null, 240, 160);
        locoImage.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        locoImage.setOpaque(true);
		locoImage.setRespectAspectRatio(true);
        rosterDetailPanel.setLayout(new BorderLayout());
        rosterDetailPanel.add(locoImage, BorderLayout.WEST);
        rosterDetailPanel.add(rosterDetails(), BorderLayout.CENTER);
        rosterDetailPanel.add(bottomRight(), BorderLayout.EAST);
        if(p.getSimplePreferenceState(DecoderPro3Window.class.getName()+"hideRosterImage")){
            locoImage.setVisible(false);
            hideRosterImage=true;
        }
        return rosterDetailPanel;
    }
    
    JLabel statusField = new JLabel();
    JLabel activeRosterGroupField = new JLabel();
    
    final ResourceBundle rbroster = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
    
    JTextPane filename 		= new JTextPane();
    JTextPane dateUpdated   	= new JTextPane();
    JTextPane decoderModel 	= new JTextPane();
    JTextPane decoderFamily 	= new JTextPane();
    
    JTextPane id 		= new JTextPane();
    JTextPane roadName 	= new JTextPane();
    JTextPane maxSpeed		= new JTextPane();
 
    JTextPane roadNumber 	= new JTextPane();
    JTextPane mfg 		= new JTextPane();
    JTextPane model		= new JTextPane();
    JTextPane owner		= new JTextPane();
    
    ResizableImagePanel locoImage;
    
    boolean hideRosterImage = false;
    
    JPanel rosterDetails(){
        JPanel panel = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(30,20);
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.EAST;
        cL.insets = new Insets (0,0,0,15);
        JLabel row0Label = new JLabel(rbroster.getString("FieldID"), JLabel.LEFT);
        gbLayout.setConstraints(row0Label,cL);
        panel.setLayout(gbLayout);
        panel.add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id,cR);
        formatTextAreaAsLabel(id);
        panel.add(id);

        cL.gridy = 1;
        JLabel row1Label = new JLabel(rbroster.getString("FieldRoadName"), JLabel.LEFT);
        gbLayout.setConstraints(row1Label,cL);
        panel.add(row1Label);

        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName,cR);
        formatTextAreaAsLabel(roadName);
        panel.add(roadName);

        cL.gridy = 2;
        JLabel row2Label = new JLabel(rbroster.getString("FieldRoadNumber"));
        gbLayout.setConstraints(row2Label,cL);
        panel.add(row2Label);

        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber,cR);
        formatTextAreaAsLabel(roadNumber);
        panel.add(roadNumber);
        
        cL.gridy = 3;
        JLabel row3Label = new JLabel(rbroster.getString("FieldManufacturer"));
        gbLayout.setConstraints(row3Label,cL);
        panel.add(row3Label);

        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg,cR);
        formatTextAreaAsLabel(mfg);
        panel.add(mfg);

        cL.gridy = 4;
        JLabel row4Label = new JLabel(rbroster.getString("FieldOwner"));
        gbLayout.setConstraints(row4Label,cL);
        panel.add(row4Label);

        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner,cR);
        formatTextAreaAsLabel(owner);
        panel.add(owner);

        cL.gridy = 5;
        JLabel row5Label = new JLabel(rbroster.getString("FieldModel"));
        gbLayout.setConstraints(row5Label,cL);
        panel.add(row5Label);

        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model,cR);
        formatTextAreaAsLabel(model);
        panel.add(model);

        cL.gridy = 6;

        cR.gridy = 6;

        cL.gridy = 7;

        cR.gridy = 7;

        
        cL.gridy = 8;


        cR.gridy = 8;


        cL.gridy = 9;
        JLabel row9Label = new JLabel(rbroster.getString("FieldDecoderFamily"));
        gbLayout.setConstraints(row9Label,cL);
        panel.add(row9Label);

        cR.gridy = 9;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily,cR);
        formatTextAreaAsLabel(decoderFamily);
        panel.add(decoderFamily);

        cL.gridy = 10;
        JLabel row10Label = new JLabel(rbroster.getString("FieldDecoderModel"));
        gbLayout.setConstraints(row10Label,cL);
        panel.add(row10Label);

        cR.gridy = 10;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel,cR);
        formatTextAreaAsLabel(decoderModel);
        panel.add(decoderModel);

        cL.gridy = 11;

        cR.gridy = 11;

        cL.gridy = 12;
        JLabel row12Label = new JLabel(rbroster.getString("FieldFilename"));
        gbLayout.setConstraints(row12Label,cL);
        panel.add(row12Label);

        cR.gridy = 12;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename,cR);
        formatTextAreaAsLabel(filename);
        panel.add(filename);

        cL.gridy = 13;
        JLabel row13Label = new JLabel(rbroster.getString("FieldDateUpdated"));
        gbLayout.setConstraints(row13Label,cL);
        panel.add(row13Label);

        cR.gridy = 13;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dateUpdated,cR);
        panel.add(dateUpdated);
        formatTextAreaAsLabel(dateUpdated);
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(panel);
        return retval;
    }
    
    void formatTextAreaAsLabel(JTextPane pane){
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }
    
    void updateDetails(){
        if(re==null){
            filename.setText("");
            dateUpdated.setText("");
            decoderModel.setText("");
            decoderFamily.setText("");
            
            
            id.setText("");
            roadName.setText("");
         
            roadNumber.setText("");
            mfg.setText("");
            model.setText("");
            owner.setText("");
            locoImage.setImagePath("");
        } else {
            filename.setText(re.getFileName());
            dateUpdated.setText(re.getDateUpdated());
            decoderModel.setText(re.getDecoderModel());
            decoderFamily.setText(re.getDecoderFamily());
            
            
            id.setText(re.getId());
            roadName.setText(re.getRoadName());
            
            roadNumber.setText(re.getRoadNumber());
            mfg.setText(re.getMfg());
            model.setText(re.getModel());
            owner.setText(re.getOwner());
            locoImage.setImagePath(re.getImagePath());
            if(hideRosterImage)
                locoImage.setVisible(false);
            else
                locoImage.setVisible(true);
                
            prog1Button.setEnabled(true);
            prog2Button.setEnabled(true);
            throttleLabels.setEnabled(true);
            rosterMedia.setEnabled(true);
            throttleLaunch.setEnabled(true);
            if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
                service.setEnabled(true);
                ops.setEnabled(true);
            } else 
                edit.setSelected(true);
            edit.setEnabled(true);
        }
    }
    
    JRadioButton service = new JRadioButton("<HTML>Service Mode<br>(Programming Track)</HTML>");
    JRadioButton ops = new JRadioButton("<HTML>Operations Mode<br>(Programming On Main)</HTML>");
    JRadioButton edit = new JRadioButton("<HTML>Edit Only</HTML>");
    
    jmri.jmrit.progsupport.ProgModeSelector modePanel = new jmri.jmrit.progsupport.ProgDeferredServiceModePane();
    
    JButton prog1Button = new JButton("Basic Programmer");
    JButton prog2Button = new JButton("Comprehensive Programmer");
    JButton throttleLabels = new JButton("Throttle Labels");
    JButton rosterMedia = new JButton("Roster Media");
    JButton throttleLaunch = new JButton("Launch Throttle");
    
    ActionListener programModeListener = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateProgMode();
                }
            };
            
    void updateProgMode(){
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            service.setEnabled(true);
            ops.setEnabled(true);
        } else 
            edit.setSelected(true);
    }
    /**
    * Simple method to change over the programmer buttons, this should be impliemented button
    * with the buttons in their own class etc, but this will work for now.
    * Basic button is button Id 1, comprehensive button is button id 2
    */
    public void setProgrammerLaunch(int buttonId, String programmer, String buttonText){
        if(buttonId == 1){
            programmer1 = programmer;
            prog1Button.setText(buttonText);
        } else if (buttonId == 2){
            programmer2 = programmer;
            prog2Button.setText(buttonText);
        }
    }
    
    String programmer1 = "Basic";
    String programmer2 = "Comprehensive";
    
    JPanel bottomRight(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup progMode = new ButtonGroup();
        progMode.add(service);
        progMode.add(ops);
        progMode.add(edit);
        
        service.setEnabled(false);
        ops.setEnabled(false);
        edit.setEnabled(false);

        JPanel progModePanel = new JPanel();
        progModePanel.add(service);
        progModePanel.add(ops);
        progModePanel.add(edit);
        
        service.addActionListener(programModeListener);
        ops.addActionListener(programModeListener);
        edit.addActionListener(programModeListener);
        
        service.setSelected(true);
        
        panel.add(progModePanel);
        
        JPanel buttonHolder = new JPanel();
        GridLayout buttonLayout = new GridLayout(3, 2, 5, 5);
        buttonHolder.setLayout(buttonLayout);
        
        buttonHolder.add(prog1Button);
        buttonHolder.add(prog2Button);
        buttonHolder.add(throttleLabels);
        buttonHolder.add(rosterMedia);
        buttonHolder.add(throttleLaunch);
        
        panel.add(buttonHolder);
        
        prog1Button.setEnabled(false);
        prog1Button.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    startProgrammer(null, re, programmer1);
                }
            });
        prog2Button.setEnabled(false);
        prog2Button.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    startProgrammer(null, re, programmer2);
            }
        });
        throttleLabels.setEnabled(false);
        throttleLabels.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    edit.setSelected(true);
                    startProgrammer(null, re, "dp3"+File.separator+"ThrottleLabels");
            }
        });
        rosterMedia.setEnabled(false);
        rosterMedia.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    edit.setSelected(true);
                    startProgrammer(null, re, "dp3"+File.separator+"MediaPane");
            }
        });
        
        throttleLaunch.setEnabled(false);
        throttleLaunch.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Launch Throttle pressed");
                ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
                tf.toFront();
                tf.getAddressPanel().setRosterEntry(re);
            }
        });
        
        return panel;
    }
    
    //current selected loco
    RosterEntry re;
    
        /**
     * Identify locomotive complete, act on it by setting the GUI.
     * This will fire "GUI changed" events which will reset the
     * decoder GUI.
     * @param dccAddress
     */
    protected void selectLoco(int dccAddress) {
        // raise the button again
        //idloco.setSelected(false);
        // locate that loco
        List<RosterEntry> l = Roster.instance().matchingList(null, null, Integer.toString(dccAddress),
                                                null, null, null, null);
        if (log.isDebugEnabled()) log.debug("selectLoco found "+l.size()+" matches");
        if (l.size() > 0) {
            re = l.get(0);
            updateDetails();
            JTable table = rtable.getTable();
            int entires = table.getRowCount();
            for (int i = 0; i<entires; i++){
                if(table.getValueAt(i, 0).equals(re.getId())){
                    table.addRowSelectionInterval(i,i);
                }
            }
        } else {
            log.warn("Read address "+dccAddress+", but no such loco in roster");
            JOptionPane.showMessageDialog(null, "No Such loco in the Roster");
        }
    }

    protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                    String filename) {
        //String title = rbt.getString("FrameNewEntryTitle");
        String title = re.getId();
        Programmer pProg = null;
        JFrame p = new PaneProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         null, false){
                protected JPanel getModePane() { return null; }
            };
        
        if(service.isSelected()){
            p = new PaneServiceProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         modePanel.getProgrammer()){
            };
        }
        else if(ops.isSelected()){
            int address = Integer.parseInt(re.getDccAddress());
            boolean longAddr = re.isLongAddress();
            pProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getAddressedProgrammer(longAddr, address);
            p = new PaneOpsProgFrame(decoderFile, re, title, "programmers"+File.separator+filename+".xml",
                    pProg);
        }
        p.pack();
        p.setVisible(true);
    }
    
    boolean allowQuit = true;
    
    /**
    * For use when the DP3 window is called from another JMRI instance, set this to prevent the DP3 from shutting down
    * JMRI when the window is closed.
    */
    protected void allowQuit(boolean allowQuit){
        this.allowQuit=allowQuit;
    }
    
    public void windowClosing(java.awt.event.WindowEvent e) {
        super.windowClosing(e);
        openWindowInstances--;
        if (allowQuit && openWindowInstances==0)
            jmri.InstanceManager.shutDownManagerInstance().shutdown();
    }

    //Matches the first argument in the array against a locally know method
    public void remoteCalls(String args[]){
        args[0] = args[0].toLowerCase();
        
        if(args[0].equals("identifyloco")){
            startIdentifyLoco();
        } else if(args[0].equals("printloco")){
            if (checkIfEntrySelected()) printLoco(false);
        } else if(args[0].equals("printpreviewloco")){
             if (checkIfEntrySelected()) printLoco(true);
        } else if(args[0].equals("exportloco")){
             if (checkIfEntrySelected()) exportLoco();
        } else if(args[0].equals("basicprogrammer")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "Basic");
        } else if(args[0].equals("comprehensiveprogrammer")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "Comprehensive");
        } else if(args[0].equals("editthrottlelabels")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "dp3"+File.separator+"ThrottleLabels");
        } else if(args[0].equals("editrostermedia")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "dp3"+File.separator+"MediaPane");
        } else if(args[0].equals("hiderosterimage")){
            hideRosterImage();
        } else if(args[0].equals("summarypane")){
            hideSummary();
        } else if(args[0].equals("copyloco")){
            if (checkIfEntrySelected()) copyLoco();
        }  else if(args[0].equals("deleteloco")){
            if (checkIfEntrySelected()) deleteLoco();
        } else
            log.error ("method " + args[0] + " not found");
    }
    
    boolean checkIfEntrySelected(){
        if (re == null){
            JOptionPane.showMessageDialog(null, "Please select a loco from the roster first");
            return false;
        }
        return true;
    }
    
        /**
     * Identify loco button pressed, start the identify operation
     * This defines what happens when the identify is done.
     */
     //taken out of CombinedLocoSelPane
    protected void startIdentifyLoco() {
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
                        !jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            log.error("Identify loco called when no service mode programmer is available");
            JOptionPane.showMessageDialog(null, "Identify loco called when no service mode programmer is available");
            return;
        }
        // start identifying a loco
        final DecoderPro3Window me = this;
        IdentifyLoco id = new IdentifyLoco() {
                private DecoderPro3Window who = me;
                protected void done(int dccAddress) {
                    // if Done, updated the selected decoder
                    who.selectLoco(dccAddress);
                }
                protected void message(String m) {
                    statusField.setText(m);
                }
                protected void error() {
                    // raise the button again
                    //idloco.setSelected(false);
                }
            };
        id.start();
    }
    
    protected void hideRosterImage(){
        hideRosterImage=!hideRosterImage;
        p.setSimplePreferenceState(DecoderPro3Window.class.getName()+"hideRosterImage",hideRosterImage);
        if(hideRosterImage){
            locoImage.setVisible(false);
        } else {
            locoImage.setVisible(true);
        }
    }
    
    protected void exportLoco(){
        ExportRosterItem act = new ExportRosterItem("Export", this, re);
        act.actionPerformed(null);
    }
    
    static class ExportRosterItem extends jmri.jmrit.roster.ExportRosterItemAction{
        ExportRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            setExistingEntry(re);
        }
        protected boolean selectFrom(){ return true;}
    }
    
    protected void copyLoco(){
        CopyRosterItem act = new CopyRosterItem("Copy", this, re);
        act.actionPerformed(null);
    }
    
    static class CopyRosterItem extends jmri.jmrit.roster.CopyRosterItemAction{
        CopyRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            setExistingEntry(re);
        }
        protected boolean selectFrom(){ return true;}
    }

    protected void deleteLoco(){
        DeleteRosterItem act = new DeleteRosterItem("Delete", this, re);
        act.actionPerformed(null);
    }

    static class DeleteRosterItem extends jmri.jmrit.roster.DeleteRosterItemAction{
        DeleteRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            this.re = re;
        }
        RosterEntry re;
        protected String selectRosterEntry(){
            return re.getId();
        }
    }

    protected void printLoco(boolean boo){
    
    }
    
    boolean hideSummary=false;
    protected void hideSummary(){
        hideSummary=!hideSummary;
        p.setSimplePreferenceState(DecoderPro3Window.class.getName()+"hideSummary",hideSummary);
        hideBottomPane(hideSummary);
    }
    
    public Object getRemoteObject(String value){
        value=value.toLowerCase();
        if(value.equals("hidesummary")){
            return hideSummary;
        }
        return null;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3Window.class.getName());
}

/* @(#)DecoderPro3Window.java */
