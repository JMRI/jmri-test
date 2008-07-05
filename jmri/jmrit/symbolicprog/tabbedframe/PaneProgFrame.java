// PaneProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.Programmer;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryPane;
import jmri.jmrit.symbolicprog.*;
import jmri.util.JmriJFrame;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;
import jmri.util.BusyGlassPane;
import java.awt.event.ItemListener;
import java.awt.Cursor;

import java.awt.event.ItemEvent;
import jmri.ProgDeferredServiceModePane;

/**
 * Frame providing a command station programmer from decoder definition files.
 * @author    Bob Jacobsen Copyright (C) 2001, 2004, 2005, 2008
 * @author    D Miller Copyright 2003, 2005
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.65 $
 */
abstract public class PaneProgFrame extends JmriJFrame
    implements java.beans.PropertyChangeListener  {

    static final java.util.ResourceBundle rbt 
        = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");

    // members to contain working variable, CV values, Indexed CV values
    JLabel              progStatus   = new JLabel(rbt.getString("StateIdle"));
    CvTableModel        cvModel      = null;
    IndexedCvTableModel iCvModel     = null;
    VariableTableModel  variableModel;

    ResetTableModel     resetModel   = null;
    JMenu               resetMenu    = null;

    Programmer          mProgrammer;
    JPanel              modePane     = null;;
    boolean             _opsMode;

    RosterEntry         _rosterEntry    = null;
    RosterEntryPane     _rPane          = null;

    List                paneList        = new ArrayList();
    int                 paneListIndex;

    BusyGlassPane       glassPane;
    List                activeComponents = new ArrayList();

    String              filename        = null;

    // GUI member declarations
    JTabbedPane tabPane = new JTabbedPane();
    JToggleButton readChangesButton = new JToggleButton(rbt.getString("ButtonReadChangesAllSheets"));
    JToggleButton writeChangesButton = new JToggleButton(rbt.getString("ButtonWriteChangesAllSheets"));
    JToggleButton readAllButton = new JToggleButton(rbt.getString("ButtonReadAllSheets"));
    JToggleButton writeAllButton = new JToggleButton(rbt.getString("ButtonWriteAllSheets"));

    ItemListener l1;
    ItemListener l2;
    ItemListener l3;
    ItemListener l4;

    ShutDownTask decoderDirtyTask;
    ShutDownTask fileDirtyTask;
    
    /**
     * Abstract method to provide a JPanel setting the programming
     * mode, if appropriate. A null value is ignored.
     */
    abstract JPanel getModePane();

    protected void installComponents() {

        // create ShutDownTasks
        if (jmri.InstanceManager.shutDownManagerInstance()!=null) {
            if (getModePane()!=null && decoderDirtyTask == null) decoderDirtyTask = 
                                            new SwingShutDownTask("DecoderPro Decoder Window Check", 
                                                                  rbt.getString("PromptQuitWindowNotWrittenDecoder"), 
                                                                  (String)null, 
                                                                  (java.awt.Component)this
                                                                   ){
                                                public boolean checkPromptNeeded() {
                                                    return !checkDirtyDecoder();
                                                }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(decoderDirtyTask);
            if (fileDirtyTask == null) fileDirtyTask = 
                                            new SwingShutDownTask("DecoderPro Decoder Window Check", 
                                                                  rbt.getString("PromptQuitWindowNotWrittenConfig"), 
                                                                  rbt.getString("PromptSaveQuit"), 
                                                                  (java.awt.Component)this
                                                                   ){
                                                public boolean checkPromptNeeded() {
                                                    return !checkDirtyFile();
                                                }
                                                public boolean doPrompt() {
                                                    boolean result = storeFile(); // storeFile false if failed, abort shutdown
                                                    return result;
                                                }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(fileDirtyTask);
        }
                
        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // add a "File" menu
        JMenu fileMenu = new JMenu(rbt.getString("MenuFile"));
        menuBar.add(fileMenu);

        // add a "Factory Reset" menu
        if (!_opsMode) {
            resetMenu = new JMenu(rbt.getString("MenuReset"));
            menuBar.add(resetMenu);
            resetMenu.add(new FactoryResetAction(rbt.getString("MenuFactoryReset"), resetModel, this));
            resetMenu.setEnabled(false);
        }
        // Add a save item
        fileMenu.add(new AbstractAction(rbt.getString("MenuSave")) {
            public void actionPerformed(ActionEvent e) {
                storeFile();
            }
        });

        JMenu printSubMenu = new JMenu(rbt.getString("MenuPrint"));
        printSubMenu.add(new PrintAction(rbt.getString("MenuPrintAll"), this, false));
        printSubMenu.add(new PrintCvAction(rbt.getString("MenuPrintCVs"), cvModel, this, false));
        fileMenu.add(printSubMenu);
        
        JMenu printPreviewSubMenu = new JMenu(rbt.getString("MenuPrintPreview"));
        printPreviewSubMenu.add(new PrintAction(rbt.getString("MenuPrintPreviewAll"), this, true));
        printPreviewSubMenu.add(new PrintCvAction(rbt.getString("MenuPrintPreviewCVs"), cvModel, this, true));
        fileMenu.add(printPreviewSubMenu);

        // add "Import" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu importSubMenu = new JMenu(rbt.getString("MenuImport"));
        fileMenu.add(importSubMenu);
        importSubMenu.add(new Pr1ImportAction(rbt.getString("MenuImportPr1"), cvModel, this));

        // add "Export" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu exportSubMenu = new JMenu(rbt.getString("MenuExport"));
        fileMenu.add(exportSubMenu);
        exportSubMenu.add(new CsvExportAction(rbt.getString("MenuExportCSV"), cvModel, this));
        exportSubMenu.add(new Pr1ExportAction(rbt.getString("MenuExportPr1DOS"), cvModel, this));
        exportSubMenu.add(new Pr1WinExportAction(rbt.getString("MenuExportPr1WIN"), cvModel, this));

        // to control size, we need to insert a single
        // JPanel, then have it laid out with BoxLayout
        JPanel pane = new JPanel();

        // general GUI config
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        // configure GUI elements
        
        // set read buttons enabled state, tooltips
        enableReadButtons();
        
        readChangesButton.addItemListener(l1 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(readChangesButton);
                    readChangesButton.setText(rbt.getString("ButtonStopReadChangesAll"));
                    readChanges();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    readChangesButton.setText(rbt.getString("ButtonReadChangesAllSheets"));
                }
            }
        });

        readAllButton.addItemListener(l3 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(readAllButton);
                    readAllButton.setText(rbt.getString("ButtonStopReadAll"));
                    readAll();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    readAllButton.setText(rbt.getString("ButtonReadAllSheets"));
                }
            }
        });

        writeChangesButton.setToolTipText(rbt.getString("TipWriteHighlightedValues"));
        writeChangesButton.addItemListener(l2 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(writeChangesButton);
                    writeChangesButton.setText(rbt.getString("ButtonStopWriteChangesAll"));
                    writeChanges();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    writeChangesButton.setText(rbt.getString("ButtonWriteChangesAllSheets"));
                }
            }
        });

        writeAllButton.setToolTipText(rbt.getString("TipWriteAllValues"));
        writeAllButton.addItemListener(l4 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(writeAllButton);
                    writeAllButton.setText(rbt.getString("ButtonStopWriteAll"));
                    writeAll();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    writeAllButton.setText(rbt.getString("ButtonWriteAllSheets"));
                }
            }
        });

        // most of the GUI is done from XML in readConfig() function
        // which configures the tabPane
        pane.add(tabPane);

        // see if programming mode is available
        modePane = getModePane();
        if (modePane!=null) {
            // if so, configure programming part of GUI
            
            // add buttons
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(readChangesButton);
            bottom.add(writeChangesButton);
            bottom.add(readAllButton);
            bottom.add(writeAllButton);
            pane.add(bottom);
            
            // add programming mode
            pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            pane.add(modePane);

            // add programming status message
            pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            progStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            pane.add(progStatus);
        }

        // and put that pane into the JFrame
        getContentPane().add(pane);
    }

    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    public Dimension getMaximumSize() {
        Dimension screen = getToolkit().getScreenSize();
        return new Dimension(screen.width, screen.height-35);
    }

    /**
     * Enable the read all and read changes button if possible.
     * This checks to make sure this is appropriate, given
     * the attached programmer's capability.
     */
    void enableReadButtons() {
        readChangesButton.setToolTipText(rbt.getString("TipReadChanges"));
        readAllButton.setToolTipText(rbt.getString("TipReadAll"));
        // check with CVTable programmer to see if read is possible
        if (cvModel!= null && cvModel.getProgrammer()!= null
            && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
            readChangesButton.setToolTipText(rbt.getString("TipNoRead"));
            readAllButton.setToolTipText(rbt.getString("TipNoRead"));
        } else {
            readChangesButton.setEnabled(true);
            readAllButton.setEnabled(true);
        }
    }

    /**
     * Initialization sequence:
     * <UL>
     * <LI> Ask the RosterEntry to read its contents
     * <LI> If the decoder file is specified, open and load it, otherwise
     *		get the decoder filename from the RosterEntry and load that.
     *		Note that we're assuming the roster entry has the right decoder,
     *		at least w.r.t. the loco file.
     * <LI> Fill CV values from the roster entry
     * <LI> Create the programmer panes
     * </UL>
     * @param pDecoderFile       XML file defining the decoder contents
     * @param pRosterEntry      RosterEntry for information on this locomotive
     * @param pFrameTitle       Name/title for the frame
     * @param pProgrammerFile   Name of the programmer file to use
     * @param pProg             Programmer object to be used to access CVs
     */
    public PaneProgFrame(DecoderFile pDecoderFile, RosterEntry pRosterEntry,
                        String pFrameTitle, String pProgrammerFile, Programmer pProg, boolean opsMode) {
        super(pFrameTitle);

        _opsMode = opsMode;

        // create the tables
        mProgrammer   = pProg;
        cvModel       = new CvTableModel(progStatus, mProgrammer);
        iCvModel      = new IndexedCvTableModel(progStatus, mProgrammer);

        variableModel = new VariableTableModel(progStatus, new String[]  {"Name", "Value"},
                                                 cvModel, iCvModel);

        resetModel    = new ResetTableModel(progStatus, mProgrammer);

        // handle the roster entry
        _rosterEntry =  pRosterEntry;
        if (_rosterEntry == null) log.error("null RosterEntry pointer");
        filename = pProgrammerFile;
        installComponents();

        if (_rosterEntry.getFileName() != null) {
            // set the loco file name in the roster entry
            _rosterEntry.readFile();  // read, but don't yet process
        }

        if (pDecoderFile != null)
            loadDecoderFile(pDecoderFile);
        else
            loadDecoderFromLoco(pRosterEntry);

        // save default values
        saveDefaults();

        // finally fill the CV values from the specific loco file
        if (_rosterEntry.getFileName() != null) _rosterEntry.loadCvModel(cvModel, iCvModel);

        // mark file state as consistent
        variableModel.setFileDirty(false);

        // if the Reset Table was used lets enable the menu item
        if (!_opsMode) {
            if (resetModel.getRowCount() > 0) {
                resetMenu.setEnabled(true);
            }
        }
        // and build the GUI
        loadProgrammerFile(pRosterEntry);

        // optionally, add extra panes from the decoder file
        Attribute a;
        if ( (a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
             && a.getValue().equals("yes")) {
            if (decoderRoot != null) {
                List paneList = decoderRoot.getChildren("pane");
                if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions from decoder file");
                for (int i=0; i<paneList.size(); i++) {
                    // load each pane
                    String pname = ((Element)(paneList.get(i))).getAttribute("name").getValue();
                    newPane( pname, ((Element)(paneList.get(i))), modelElem, true);  // show even if empty??
                }
            }
        }

        pack();

        if (log.isDebugEnabled()) log.debug("PaneProgFrame \""+pFrameTitle
                                            +"\" constructed for file "+_rosterEntry.getFileName()
                                            +", unconstrained size is "+super.getPreferredSize()
                                            +", constrained to "+getPreferredSize());
    }

    /**
     * Data element holding the 'model' element representing the decoder type
     */
    Element modelElem = null;

    Element decoderRoot = null;

    protected void loadDecoderFromLoco(RosterEntry r) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
        // locate a decoder like that.
        List l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
        if (l.size() == 0) {
            log.debug("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
        }
        if (l.size() > 0) {
            DecoderFile d = (DecoderFile)l.get(0);
            loadDecoderFile(d);
        } else {
            if (decoderModel.equals(""))
                log.debug("blank decoderModel requested, so nothing loaded");
            else
                log.warn("no matching \""+decoderModel+"\" decoder found for loco, no decoder info loaded");
        }
    }

    protected void loadDecoderFile(DecoderFile df) {
        if (df == null) {
            log.warn("loadDecoder file invoked with null object");
            return;
        }
        if (log.isDebugEnabled()) log.debug("loadDecoderFile from "+df.fileLocation
                                        +" "+df.getFilename());

        try {
            decoderRoot = df.rootFromName(df.fileLocation+df.getFilename());
        } catch (Exception e) { log.error("Exception while loading decoder XML file: "+df.getFilename()+" exception: "+e); }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);
        // load reset from decoder tree
        if (variableModel.piCv() >= 0) {
            resetModel.setPiCv(variableModel.piCv());
        }
        if (variableModel.siCv() >= 0) {
            resetModel.setSiCv(variableModel.siCv());
        }
        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // get the showEmptyPanes attribute, if yes/no update our state
        if (decoderRoot.getAttribute("showEmptyPanes") != null) {
            if (log.isDebugEnabled()) log.debug("Found in decoder "+decoderRoot.getAttribute("showEmptyPanes").getValue());
            if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("yes"))
                setShowEmptyPanes(true);
            else if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("no"))
                setShowEmptyPanes(false);
            // leave alone for "default" value
            if (log.isDebugEnabled()) log.debug("result "+getShowEmptyPanes());
        }
        
        // save the pointer to the model element
        modelElem = df.getModelElement();
    }

    protected void loadProgrammerFile(RosterEntry r) {
        // Open and parse programmer file
        XmlFile pf = new XmlFile(){};  // XmlFile is abstract
        try {
            programmerRoot = pf.rootFromName(filename);

            // get the showEmptyPanes attribute, if yes/no update our state
            if (programmerRoot.getChild("programmer").getAttribute("showEmptyPanes") != null) {
                if (log.isDebugEnabled()) log.debug("Found in programmer "+programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue());
                if (programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue().equals("yes"))
                    setShowEmptyPanes(true);
                else if (programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue().equals("no"))
                    setShowEmptyPanes(false);
                // leave alone for "default" value
                if (log.isDebugEnabled()) log.debug("result "+getShowEmptyPanes());
            }

            // load programmer config from programmer tree
            readConfig(programmerRoot, r);
            
        }
        catch (Exception e) {
            log.error("exception reading programmer file: "+filename+" exception: "+e);
            // provide traceback too
            e.printStackTrace();
        }
    }

    Element programmerRoot = null;


    /**
     * @return true if decoder needs to be written
     */
    protected boolean checkDirtyDecoder() {
        if (log.isDebugEnabled()) log.debug("Checking decoder dirty status. CV: "+cvModel.decoderDirty()+" variables:"+variableModel.decoderDirty());
        return (getModePane()!= null && (cvModel.decoderDirty() || variableModel.decoderDirty()) ); 
    }
    
    /**
     * @return true if file needs to be written
     */
    protected boolean checkDirtyFile() {
        return (variableModel.fileDirty() || _rPane.guiChanged(_rosterEntry));
    }
    protected void handleDirtyFile() {
    }
    
    
    /**
     * Close box has been clicked; handle check for dirty with respect to
     * decoder or file, then close.
     * @param e Not used
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
    
        // Don't want to actually close if we return early
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        // check for various types of dirty - first table data not written back
        if (log.isDebugEnabled()) log.debug("Checking decoder dirty status. CV: "+cvModel.decoderDirty()+" variables:"+variableModel.decoderDirty());
        if (checkDirtyDecoder()) {
            if (JOptionPane.showConfirmDialog(null,
                                              rbt.getString("PromptCloseWindowNotWrittenDecoder"),
                                              rbt.getString("PromptChooseOne"), 
                                              JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
        }
        if (checkDirtyFile()) {
            int option = JOptionPane.showOptionDialog(null,rbt.getString("PromptCloseWindowNotWrittenConfig"),
                        rbt.getString("PromptChooseOne"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                        new String[]{rbt.getString("PromptSaveAndClose"), rbt.getString("PromptClose"), rbt.getString("PromptCancel")}, 
                                rbt.getString("PromptSaveAndClose"));
            if (option==0) {
                // save requested
                if (!storeFile()) return;   // don't close if failed
            } else if (option ==2) {
                // cancel requested
                return; // without doing anything
            }
        }
        // Check for a "<new loco>" roster entry; if found, remove it
        List l = Roster.instance().matchingList(null, null, null, null, null, null, rbt.getString("LabelNewDecoder"));
        if (l.size() > 0 && log.isDebugEnabled()) log.debug("Removing "+l.size()+" <new loco> entries");
        while (l.size() > 0 ) {
            Roster.instance().removeEntry((RosterEntry)l.get(0));
            l = Roster.instance().matchingList(null, null, null, null, null, null, rbt.getString("LabelNewDecoder"));
        }
        
        // OK, continue close
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        // deregister shutdown hooks
        if (jmri.InstanceManager.shutDownManagerInstance()!=null)
            jmri.InstanceManager.shutDownManagerInstance().deregister(decoderDirtyTask);
        decoderDirtyTask = null;
        if (jmri.InstanceManager.shutDownManagerInstance()!=null)
            jmri.InstanceManager.shutDownManagerInstance().deregister(fileDirtyTask);
        fileDirtyTask = null;
        
        // do the close itself
        super.windowClosing(e);
    }

    void readConfig(Element root, RosterEntry r) {
        // check for "programmer" element at start
        Element base;
        if ( (base = root.getChild("programmer")) == null) {
            log.error("xml file top element is not programmer");
            return;
        }

        // add the Info tab
        tabPane.addTab("Roster Entry", makeInfoPane(r));

        // for all "pane" elements in the programmer
        List paneList = base.getChildren("pane");
        if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions");
        for (int i=0; i<paneList.size(); i++) {
            // load each pane
            String name = ((Element)(paneList.get(i))).getAttribute("name").getValue();
            newPane( name, ((Element)(paneList.get(i))), modelElem, false);  // dont force showing if empty
        }
    }

    /**
     * reset all CV values to defaults stored earlier.  This will in turn update
     * the variables
     */
    protected void resetToDefaults() {
        int n = defaultCvValues.length;
        for (int i=0; i<n; i++) {
            CvValue cv = cvModel.getCvByNumber(defaultCvNumbers[i]);
            if (cv == null) log.warn("Trying to set default in CV "+defaultCvNumbers[i]
                                     +" but didn't find the CV object");
            else cv.setValue(defaultCvValues[i]);
        }
        n = defaultIndexedCvValues.length;
        for (int i=0; i<n; i++) {
            CvValue cv = iCvModel.getCvByRow(i);
            if (cv == null) log.warn("Trying to set default in indexed CV from row "+i
                                     +" but didn't find the CV object");
            else cv.setValue(defaultIndexedCvValues[i]);
        }
    }

    int defaultCvValues[] = null;
    int defaultCvNumbers[] = null;
    int defaultIndexedCvValues[] = null;

    /**
     * Save all CV values.  These stored values are used by
     * resetToDefaults
     */
    protected void saveDefaults() {
        int n = cvModel.getRowCount();
        defaultCvValues = new int[n];
        defaultCvNumbers = new int[n];

        for (int i=0; i<n; i++) {
            CvValue cv = cvModel.getCvByRow(i);
            defaultCvValues[i] = cv.getValue();
            defaultCvNumbers[i] = cv.number();
        }

        n = iCvModel.getRowCount();
        defaultIndexedCvValues = new int[n];

        for (int i=0; i<n; i++) {
            CvValue cv = iCvModel.getCvByRow(i);
            defaultIndexedCvValues[i] = cv.getValue();
        }
    }

    protected JPanel makeInfoPane(RosterEntry r) {
        // create the identification pane (not configured by file now; maybe later?
        
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add roster info
        _rPane = new RosterEntryPane(r);
        _rPane.setMaximumSize(_rPane.getPreferredSize());
        body.add(_rPane);

        // add the store button
        JButton store = new JButton(rbt.getString("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    storeFile();
                }
            });

        // add the reset button
        JButton reset = new JButton(rbt.getString("ButtonResetDefaults"));
        reset.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.setPreferredSize(reset.getPreferredSize());
        reset.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    resetToDefaults();
                }
            });

        store.setPreferredSize(reset.getPreferredSize());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        
        buttons.add(store);
        buttons.add(reset);
        
        body.add(buttons);
        outer.add(scrollPane);

        // arrange for the dcc address to be updated
        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
            };
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr==null) log.debug("DCC Address monitor didnt find a Short Address variable");
        else primaryAddr.addPropertyChangeListener(dccNews);
        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr==null) log.debug("DCC Address monitor didnt find an Long Address variable");
        else extendAddr.addPropertyChangeListener(dccNews);
        addMode = variableModel.findVar("Address Format");
        if (addMode==null) log.debug("DCC Address monitor didnt find an Address Format variable");
        else addMode.addPropertyChangeListener(dccNews);

        return outer;
    }

    // hold refs to variables to check dccAddress
    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    VariableValue addMode = null;

    void updateDccAddress() {
        boolean longMode = false;
        if (log.isDebugEnabled())
            log.debug("updateDccAddress: short "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
                      " long "+(extendAddr==null?"<null>":extendAddr.getValueString())+
                      " mode "+(addMode==null?"<null>":addMode.getValueString()));
        String newAddr = null;
        if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
            // short address mode
            longMode = false;
            if (primaryAddr != null && !primaryAddr.getValueString().equals(""))
                newAddr = primaryAddr.getValueString();
        }
        else {
            // long address
            if (extendAddr != null && !extendAddr.getValueString().equals(""))
                longMode = true;
                newAddr = extendAddr.getValueString();
        }
        // update if needed
        if (newAddr!=null) {
            // store DCC address, type
            _rPane.setDccAddress(newAddr);
            _rPane.setDccAddressLong(longMode);
        }
    }

    public void newPane(String name, Element pane, Element modelElem, boolean enableEmpty) {
        if (log.isDebugEnabled()) log.debug("newPane with enableEmpty "+enableEmpty+" getShowEmptyPanes() "+getShowEmptyPanes());
        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(this, name, pane, cvModel, iCvModel, variableModel, modelElem);

        // how to handle the tab depends on whether it has contents and option setting
        if ( enableEmpty || (p.cvList.size()!=0) || (p.varList.size()!=0 || (p.indexedCvList.size()!=0)) ) {
            tabPane.addTab(name, p);  // always add if not empty
        } else if (getShowEmptyPanes()) {
            // here empty, but showing anyway as disabled
            tabPane.addTab(name, p);
            int index = tabPane.indexOfTab(name);
            tabPane.setEnabledAt(index, false);
            jmri.util.JTabbedPaneUtil.setToolTipTextAt(tabPane, index,
                    rbt.getString("TipTabDisabledNoCategory"));
        } else {
            // here not showing tab at all
        }

        // and remember it for programming
        paneList.add(p);
    }

    /**
     *
     */
    void prepGlassPane(AbstractButton activeButton) {
        List rectangles = new ArrayList();

        if (glassPane != null) {
            glassPane.dispose();
        }
        activeComponents.clear();
        activeComponents.add(activeButton);
        if (activeButton == readChangesButton || activeButton == readAllButton ||
            activeButton == writeChangesButton || activeButton == writeAllButton) {
            if (activeButton == readChangesButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).readChangesButton);
                }
            } else if (activeButton == readAllButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).readAllButton);
                }
            } else if (activeButton == writeChangesButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).writeChangesButton);
                }
            } else if (activeButton == writeAllButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).writeAllButton);
                }
            }
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                rectangles.add(tabPane.getUI().getTabBounds(tabPane,i));
            }
        }
        glassPane = new BusyGlassPane(activeComponents, rectangles, this.getContentPane(), this);
        this.setGlassPane(glassPane);
    }

    void paneFinished() {
        if (!isBusy()) {
            if (glassPane != null) {
                glassPane.setVisible(false);
                glassPane.dispose();
                glassPane = null;
            }
            setCursor(Cursor.getDefaultCursor());
            enableButtons(true);
        }
    }

    void enableButtons(boolean stat) {
        if (stat) {
            enableReadButtons();
        } else {
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
        }
        writeChangesButton.setEnabled(stat);
        writeAllButton.setEnabled(stat);
        if (modePane != null) {
            ((ProgDeferredServiceModePane)modePane).setButton.setEnabled(stat);
        }
    }

    boolean justChanges;

    boolean isBusy() { return _busy; }
    private boolean _busy = false;
    private void setBusy(boolean stat) {
        _busy = stat;

        for (int i = 0; i < paneList.size(); i++) {
            if (stat) {
                ((PaneProgPane)paneList.get(i)).enableButtons(false);
            } else {
                ((PaneProgPane)paneList.get(i)).enableButtons(true);
            }
        }
        if (!stat) {
            paneFinished();
        }
    }

    /**
     * invoked by "Read Changes" button, this sets in motion a
     * continuing sequence of "read changes" operations on the
     * panes. Each invocation of this method reads one pane; completion
     * of that request will cause it to happen again, reading the next pane, until
     * there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is complete.
     */
    public boolean readChanges() {
        if (log.isDebugEnabled()) log.debug("readChanges starts");
        justChanges = true;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane)paneList.get(i)).setToRead(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        readChangesButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        // start operation
        return doRead();
    }

    /**
     * invoked by "Read All" button, this sets in motion a
     * continuing sequence of "read all" operations on the
     * panes. Each invocation of this method reads one pane; completion
     * of that request will cause it to happen again, reading the next pane, until
     * there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is complete.
     */
    public boolean readAll() {
        if (log.isDebugEnabled()) log.debug("readAll starts");
        justChanges = false;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane)paneList.get(i)).setToRead(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        readAllButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        // start operation
        return doRead();
    }

    boolean doRead() {
        _read = true;
        while (paneListIndex < paneList.size()) {
            if (log.isDebugEnabled()) log.debug("doRead on "+paneListIndex);
            _programmingPane = (PaneProgPane)paneList.get(paneListIndex);
            // some programming operations are instant, so need to have listener registered at readPaneAll
            _programmingPane.addPropertyChangeListener(this);
            boolean running;
            if (justChanges)
                running = _programmingPane.readPaneChanges();
            else
                running = _programmingPane.readPaneAll();

            paneListIndex++;

            if (running) {
                // operation in progress, stop loop until called back
                if (log.isDebugEnabled()) log.debug("doRead expecting callback from readPane "+paneListIndex);
                return true;
            } else {
                _programmingPane.removePropertyChangeListener(this);
            }
        }
        // nothing to program, end politely
        _programmingPane = null;
        enableButtons(true);
        setBusy(false);
        readChangesButton.setSelected(false);
        readAllButton.setSelected(false);
        if (log.isDebugEnabled()) log.debug("doRead found nothing to do");
        return false;
    }

    /**
     * invoked by "Write All" button, this sets in motion a
     * continuing sequence of "write all" operations on each pane.
     * Each invocation of this method writes one pane; completion
     * of that request will cause it to happen again, writing the next pane, until
     * there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is complete.
     */
    public boolean writeAll() {
        if (log.isDebugEnabled()) log.debug("writeAll starts");
        justChanges = false;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane)paneList.get(i)).setToWrite(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        writeAllButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        return doWrite();
    }

    /**
     * invoked by "Write Changes" button, this sets in motion a
     * continuing sequence of "write changes" operations on each pane.
     * Each invocation of this method writes one pane; completion
     * of that request will cause it to happen again, writing the next pane, until
     * there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is complete.
     */
    public boolean writeChanges() {
        if (log.isDebugEnabled()) log.debug("writeChanges starts");
        justChanges = true;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane)paneList.get(i)).setToWrite(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        writeChangesButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        return doWrite();
    }

    boolean doWrite() {
        _read = false;
        while (paneListIndex < paneList.size()) {
            if (log.isDebugEnabled()) log.debug("doWrite starts on "+paneListIndex);
            _programmingPane = (PaneProgPane)paneList.get(paneListIndex);
            // some programming operations are instant, so need to have listener registered at readPane
            _programmingPane.addPropertyChangeListener(this);
            boolean running;
            if (justChanges)
                running = _programmingPane.writePaneChanges();
            else
                running = _programmingPane.writePaneAll();

            paneListIndex++;

            if (running) {
                // operation in progress, stop loop until called back
                if (log.isDebugEnabled()) log.debug("doWrite expecting callback from writePane "+paneListIndex);
                return true;
            }
            else
                _programmingPane.removePropertyChangeListener(this);
        }
        // nothing to program, end politely
        _programmingPane = null;
        enableButtons(true);
        setBusy(false);
        writeChangesButton.setSelected(false);
        writeAllButton.setSelected(false);
        if (log.isDebugEnabled()) log.debug("doWrite found nothing to do");
        return false;
    }

    public void printPanes(HardcopyWriter w) {
        printInfoSection(w);
        try {
           String s = "\n\n";
           w.write(s, 0, s.length());
         }
         catch (IOException e) {
           log.error("Error printing Info Section: " + e);
         }

        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("start printing page "+i);
            PaneProgPane pane = (PaneProgPane)paneList.get(i);
            pane.printPane(w);
        }
        w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),w.getCharactersPerLine() + 1);
    }

    public void printInfoSection(HardcopyWriter w) {
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        _rosterEntry.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }

    boolean _read = true;
    PaneProgPane _programmingPane = null;

    /**
     * get notification of a variable property change in the pane, specifically "busy" going to
     * false at the end of a programming operation
     * @param e Event, used to find source
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event
        if (_programmingPane == null) {
            log.warn("unexpected propertChange: "+e);
            return;
        } else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
                                                   +" new value: "+e.getNewValue());
        log.debug("check valid: "+(e.getSource() == _programmingPane)+" "+(!e.getPropertyName().equals("Busy"))+" "+(((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
        if (e.getSource() == _programmingPane &&
            e.getPropertyName().equals("Busy") &&
            ((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  {

            if (log.isDebugEnabled()) log.debug("end of a programming pane operation, remove");

            // remove existing listener
            _programmingPane.removePropertyChangeListener(this);
            _programmingPane = null;
            // restart the operation
            if (_read && readChangesButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart readChanges");
                doRead();
            }
            else if (_read && readAllButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart readAll");
                doRead();
            }
            else if (writeChangesButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart writeChanges");
                doWrite();
            }
            else if (writeAllButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart writeAll");
                doWrite();
            }
            else {
                if (log.isDebugEnabled()) log.debug(
                    "read/write end because button is lifted");
                setBusy(false);
            }
        }
    }

    /**
     * Store the locomotives information in the roster (and a RosterEntry file).
     * @return false if store failed
     */
    public boolean storeFile() {
        log.debug("storeFile starts");

        // reload the RosterEntry
        updateDccAddress();
        _rPane.update(_rosterEntry);

        // id has to be set!
        if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals(rbt.getString("LabelNewDecoder"))) {
            log.debug("storeFile without a filename; issued dialog");
            JOptionPane.showMessageDialog(this, rbt.getString("PromptFillInID"));
            return false;
        }
        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();
        String filename = _rosterEntry.getFileName();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, iCvModel, variableModel );

        // mark this as a success
        variableModel.setFileDirty(false);

        // and store an updated roster file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        Roster.writeRosterFile();

        // show OK status
        progStatus.setText(java.text.MessageFormat.format(
                                rbt.getString("StateSaveOK"),
                                new String[]{filename}));
        return true;
    }

    /**
     * local dispose, which also invokes parent. Note that
     * we remove the components (removeAll) before taking those
     * apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) log.debug("dispose local");

        // remove listeners (not much of a point, though)
        readChangesButton.removeItemListener(l1);
        writeChangesButton.removeItemListener(l2);
        readAllButton.removeItemListener(l3);
        writeAllButton.removeItemListener(l4);
        if (_programmingPane != null) _programmingPane.removePropertyChangeListener(this);

        // dispose the list of panes
        for (int i=0; i<paneList.size(); i++) {
            PaneProgPane p = (PaneProgPane) paneList.get(i);
            p.dispose();
        }
        paneList.clear();

        // dispose of things we owned, in order of dependence
        _rPane.dispose();
        variableModel.dispose();
        cvModel.dispose();
        iCvModel.dispose();

        // remove references to everything we remember
        progStatus = null;
        cvModel = null;
        iCvModel = null;
        variableModel = null;
        _rosterEntry = null;
        _rPane = null;

        paneList.clear();
        paneList = null;
        _programmingPane = null;

        tabPane = null;
        readChangesButton = null;
        writeChangesButton = null;
        readAllButton = null;
        writeAllButton = null;

        if (log.isDebugEnabled()) log.debug("dispose superclass");
        removeAll();
        super.dispose();

    }

    /**
     * Option to control appearance of empty panes
     */
    public static void setShowEmptyPanes(boolean yes) {
        showEmptyPanes = yes;
        // new Exception().printStackTrace();
    }
    public static boolean getShowEmptyPanes() {
        return showEmptyPanes;
    }
    static boolean showEmptyPanes = true;

    public RosterEntry getRosterEntry() { return _rosterEntry; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}

