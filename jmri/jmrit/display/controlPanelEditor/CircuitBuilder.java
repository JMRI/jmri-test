package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.display.*;
import jmri.jmrit.display.palette.IndicatorItemPanel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.picker.PickListModel;

import java.awt.*;
import java.awt.event.*;
//import java.awt.event.KeyEvent;

import java.awt.geom.Rectangle2D;

import java.awt.dnd.DropTargetListener;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import jmri.jmrit.display.Editor;
import jmri.util.NamedBeanHandle;
//import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.logix.*;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class CircuitBuilder extends ControlPanelEditor implements DropTargetListener {

    static int STRUT_SIZE = 10;

    private JMenu _circuitMenu;
    private JMenu _todoMenu;
    
    // map track icon to OBlock to which it belongs
    private Hashtable<Positionable, OBlock> _iconMap = new Hashtable<Positionable, OBlock>();

    // map OBlock to List of icons that represent it
    private Hashtable<OBlock, ArrayList<Positionable>> _circuitMap;
    
    // list of track icons not belonging to an OBlock
    private ArrayList<Positionable> _darkTrack;
    
    // list of OBlocks with no icons
    private ArrayList<OBlock> _bareBlock;
    
    // list of icons needing converting
    private ArrayList<Positionable> _unconvertedTrack;
    
    // list of OBlocks whose icons need converting
    private ArrayList<OBlock> _convertBlock;
    
    // map of PortalIcons by portal name
    private Hashtable<String, PortalIcon> _portalIconMap;
    
    // OBlock list to open edit frames 
    private PickListModel _model;

    // "Editing Frames" - Called from menu in Main Frame
    private EditCircuitFrame _editCircuitFrame;
    private EditPortalFrame  _editPortalFrame; 
    private EditCircuitPaths _editPathsFrame;
    private boolean _firstEditCircuitFrame = true;
    private boolean _firstEditPortalFrame = true;
    private boolean _firstEditCircuitPaths = true;

    // list of icons making a circuit - used by editing frames to indicate block(s) being worked on
    private ArrayList<Positionable> _circuitIcons;      // Dark Blue
    private ArrayList<Positionable> _circuit2Icons;     // Light Blue for adjacent block

    private JTextField _sysNameBox = new JTextField();
    private JTextField _userNameBox = new JTextField();
    private OBlock  _currentBlock;
    private JDialog _dialog;

    public final static ResourceBundle rbcp = ControlPanelEditor.rbcp;
    public final static Color _editGroupColor = new Color(0, 0, 255);
    public final static Color _editGroup2Color = new Color(200, 200, 255);
    public final static Color _pathColor = Color.green;
    public final static Color _highlightColor = new Color(255, 100, 200);

    /******************************************************************/

    public CircuitBuilder() {
        _menuBar = new JMenuBar();
        if (log.isDebugEnabled()) log.debug("CircuitBuilder ctor width= "+getSize().width);
    }

    public CircuitBuilder(String name) {
        super(name);
        if (log.isDebugEnabled()) log.debug("CircuitBuilder ctor "+name);
    }

    // Do the things needed by ChangeView
    protected void init(String name) {
        setJMenuBar(_menuBar);

        super.setTargetPanel(null, null);
//        super.setTargetPanelSize(300, 300);
        makeDataFlavors();

        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("Serif", Font.PLAIN, 12),
                                                     Color.black, new Color(255, 250, 210), Color.black));
        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);
        
        _circuitMap = new Hashtable<OBlock, ArrayList<Positionable>>();
        OBlockManager manager = InstanceManager.oBlockManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            _circuitMap.put(block, new ArrayList<Positionable>());
        }

        checkCircuits();

        // make menus
//        _menuBar = getTargetFrame().getJMenuBar();
        _todoMenu =  new JMenu(rbcp.getString("MenuToDo"));
        _menuBar.add(_todoMenu, 0);
        makeToDoMenu();
        _circuitMenu = new JMenu(rbcp.getString("MenuCircuit"));
        _menuBar.add(_circuitMenu, 0);
        makeIconMenu();
        makeZoomMenu();
        makeOptionMenu();
        makeCircuitMenu();
        makeEditMenu();
        makeFileMenu();

        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        pack();
        setVisible(true);
    }
    /**
    * Add icon 'pos' to circuit 'block'
    */
    private void addIcon(OBlock block, Positionable pos) {
        ArrayList<Positionable> icons = _circuitMap.get(block);
        if (icons==null) {
            icons = new ArrayList<Positionable>();
        }
        if (pos!=null) {
            if (!icons.contains(pos)) {
                icons.add(pos);
            }
            _iconMap.put(pos, block);
        }
        _circuitMap.put(block, icons);
        _darkTrack.remove(pos);
        if (log.isDebugEnabled()) log.debug("addIcon: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    // display "todo" items
    private void makeToDoMenu() {
        _todoMenu.removeAll();

        JMenu blockNeeds = new JMenu(rbcp.getString("blockNeedsIconsItem"));
        _todoMenu.add(blockNeeds);
        ActionListener editCircuitAction = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String sysName = event.getActionCommand();
                    editCircuit(sysName);
                }
        };
        if (_bareBlock.size()>0) {
            for (int i=0; i<_bareBlock.size(); i++) {
                OBlock block = _bareBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        rbcp.getString("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);                                                  
            }
        } else {
            blockNeeds.add(new JMenuItem(rbcp.getString("circuitsHaveIcons")));
        }

        blockNeeds = new JMenu(rbcp.getString("blocksNeedConversionItem"));
        _todoMenu.add(blockNeeds);
        if (_convertBlock.size()>0) {
            for (int i=0; i<_convertBlock.size(); i++) {
                OBlock block = _convertBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        rbcp.getString("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);                                                  
            }
        } else {
            blockNeeds.add(new JMenuItem(rbcp.getString("circuitIconsConverted")));
        }
        JMenuItem iconNeeds;
        if (_unconvertedTrack.size()>0) {
            iconNeeds = new JMenuItem(rbcp.getString("iconsNeedConversionItem"));
            iconNeeds.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _selectionGroup = new ArrayList<Positionable>();
                    for (int i=0; i<_unconvertedTrack.size(); i++) {
                        _selectionGroup.add(_unconvertedTrack.get(i));
                    }
                    repaint();
                }
             });
        } else {
            iconNeeds = new JMenuItem(rbcp.getString("IconsConverted"));
        }
        _todoMenu.add(iconNeeds);

        if (_darkTrack.size()>0) {
            iconNeeds = new JMenuItem(rbcp.getString("iconsNeedsBlocksItem"));
            iconNeeds.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _selectionGroup = new ArrayList<Positionable>();
                    for (int i=0; i<_darkTrack.size(); i++) {
                        _selectionGroup.add(_darkTrack.get(i));
                    }
                    repaint();
                }
             });
        } else {
            iconNeeds = new JMenuItem(rbcp.getString("IconsHaveCircuits"));
        }
        _todoMenu.add(iconNeeds);

        JMenuItem pError = new JMenuItem(rbcp.getString("CheckPortalPaths"));
        _todoMenu.add(pError);
        pError.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    errorCheck();
                }
            });

    }

    private void errorCheck() {
        WarrantTableAction.initPathPortalCheck();
        OBlockManager manager = InstanceManager.oBlockManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            WarrantTableAction.checkPathPortals(manager.getBySystemName(sysNames[i]));
        }
        WarrantTableAction.showPathPortalErrors();
    }

    
    private void makeCircuitMenu() {
        _circuitMenu.removeAll();

        JMenuItem circuitItem = new JMenuItem(rbcp.getString("newCircuitItem"));
        _circuitMenu.add(circuitItem);
        circuitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newCircuit();
                }
            });

        JMenuItem editCircuitItem = new JMenuItem(rbcp.getString("editCircuitItem"));
        _circuitMenu.add(editCircuitItem);
        JMenuItem editPortalsItem = new JMenuItem(rbcp.getString("editPortalsItem"));
        _circuitMenu.add(editPortalsItem);
        JMenuItem editCircuitPathsItem = new JMenuItem(rbcp.getString("editCircuitPathsItem"));
        _circuitMenu.add(editCircuitPathsItem);

        if ( _circuitMap.keySet().size()>0) {
            editCircuitItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editCircuit("editCircuitItem");
                    }
                });
            editPortalsItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editPortals("editPortalsItem");
                    }
                });
            editCircuitPathsItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editCircuitPaths("editCircuitPathsItem");
                    }
                });
        } else {
            editCircuitItem.add(new JMenuItem(rbcp.getString("noCircuitsItem")));
            editPortalsItem.add(new JMenuItem(rbcp.getString("noCircuitsItem")));
            editCircuitPathsItem.add(new JMenuItem(rbcp.getString("noCircuitsItem")));
        }
    }
    
    private void makeFileMenu() {
        _fileMenu = new JMenu(rb.getString("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        /*
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(rb.getString("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        */
        JMenuItem editItem = new JMenuItem(rb.getString("CPEView"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    hidePortalIcons();
                    if (_editPathsFrame!=null) {
                        _editPathsFrame.closingEvent();
                        _editPathsFrame = null;
                    }
                    if (_editCircuitFrame!=null) {
                       _editCircuitFrame.closingEvent();
                       _editCircuitFrame = null;
                    }
                    if (_editPortalFrame!=null) {
                        _editPortalFrame.closingEvent();
                        _editPortalFrame = null;
                    }
					changeView("jmri.jmrit.display.controlPanelEditor.ControlPanelEditor");
                }
            });
    }
    
    private void hidePortalIcons() {
        java.util.List<Positionable> content = getContents();
        for (int i=0; i<content.size(); i++) {
            if (content.get(i) instanceof PortalIcon) {
                ((PortalIcon)content.get(i)).setStatus(PortalIcon.HIDDEN);
            }
        }
    }

    /**************** Set up editing Frames *****************/

    protected void newCircuit() {
        if (editingOK()) {
            addCircuitDialog();
            if (_currentBlock!=null) {
                if (_editCircuitFrame==null) {
                    _selectionGroup = null;
                    _editCircuitFrame = new EditCircuitFrame(rbcp.getString("newCircuitItem"), this, _currentBlock);
                }
                if (_firstEditCircuitFrame) {
                    _editCircuitFrame.setLocationRelativeTo(this);
                    _firstEditCircuitFrame = false;
                }
            }
        }
    }

    protected void editCircuit(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                setSelectionGroup(_currentBlock);
                _editCircuitFrame = new EditCircuitFrame(rbcp.getString("OpenCircuitItem"), this, _currentBlock);
                if (_firstEditCircuitFrame) {
                    _editCircuitFrame.setLocationRelativeTo(this);
                    _firstEditCircuitFrame = false;
                }
            }
        }
    }

    protected void editPortals(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                setSelectionGroup(_currentBlock);
                // check icons to be indicator type
                _circuitIcons = _circuitMap.get(_currentBlock);
                if (!iconsConverted()) {
                    _selectionGroup = null;
                    _circuitIcons = null;
                } else {
                    _editPortalFrame = new EditPortalFrame(rbcp.getString("OpenPortalTitle"), this, _currentBlock);
                    if (_firstEditPortalFrame) {
                        _editPortalFrame.setLocationRelativeTo(this);
                        _firstEditPortalFrame = false;
                    }
                }
            }

        }
    }

    protected void editCircuitPaths(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                setSelectionGroup(_currentBlock);
                // check icons to be indicator type
                _circuit2Icons = _circuitMap.get(_currentBlock);
                if (!iconsConverted()) {
                    _selectionGroup = null;
                    _circuitIcons = null;
                } else {
                    _selectionGroup = null;
                    _currentBlock.setState(OBlock.UNOCCUPIED);
                    _currentBlock.allocate(EditCircuitPaths.TEST_PATH);
                    _editPathsFrame = new EditCircuitPaths(rbcp.getString("OpenPathTitle"), this, _currentBlock);
                    _editPathsFrame.setLocationRelativeTo(this);
                    if (_firstEditCircuitPaths) {
                        _editPathsFrame.setLocationRelativeTo(this);
                        _firstEditCircuitPaths = false;
                    }
                }
            }
        }
    }

    private boolean editingOK() {
        if (_editCircuitFrame!=null || _editPathsFrame!=null || _editPortalFrame!=null) {
			// Already editing a circuit, ask for completion of that edit
	        JOptionPane.showMessageDialog(_editCircuitFrame,
                    rbcp.getString("AlreadyEditing"), rb.getString("errorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
            if (_editPathsFrame!=null) {
                _editPathsFrame.toFront();
                _editPathsFrame.setVisible(true);
            } else if (_editCircuitFrame!=null) {
                _editCircuitFrame.toFront();
                _editCircuitFrame.setVisible(true);
            } else if (_editPortalFrame!=null) {
                _editPortalFrame.toFront();
                _editPortalFrame.setVisible(true);
            }
            return false;
        }
        checkCircuits();
        return true;
    }

    /**
    * Create a new OBlock
    * Used by New to set up _editCircuitFrame
    * @return system name of OBlock
    */
    private void addCircuitDialog() {
        _dialog = new JDialog(this, rbcp.getString("TitleCircuitDialog"), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(rbcp.getString("createOBlock")));
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeSystemNamePanel());
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeDoneButtonPanel(true));
        panel.add(mainPanel);
        _dialog.getContentPane().add(panel);
        _dialog.setLocation(getLocation().x+100, getLocation().y+100);
        _dialog.pack();
        _dialog.setVisible(true);
    }

    /**
    * Create a new OBlock
    * Used by New to set up _editCircuitFrame
    * @return system name of OBlock
    */
    private void editCircuitDialog(String title) {
        _dialog = new JDialog(this, rbcp.getString(title), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(rbcp.getString("selectOBlock")));
        mainPanel.add(p);

        _model = PickListModel.oBlockPickModelInstance();
        JTable table = _model.makePickTable();
        mainPanel.add(new JScrollPane(table));
/*
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeSystemNamePanel());
        */
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeDoneButtonPanel(false));
        panel.add(mainPanel);
        _dialog.getContentPane().add(panel);
        _dialog.setLocation(getLocation().x+100, getLocation().y+100);
        _dialog.pack();
        _dialog.setVisible(true);
    }

    private JPanel makeSystemNamePanel() {
        _sysNameBox.setText("");
        _userNameBox.setText("");
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel(); 
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(new JLabel(rb.getString("SystemName")), c);
        c.gridy = 1;
        p.add(new JLabel(rb.getString("UserName")),c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_sysNameBox,c);
        c.gridy = 1;
        p.add(_userNameBox,c);
        namePanel.add(p);
        return namePanel;
    }

    private JPanel makeDoneButtonPanel(boolean add) {
        JPanel buttonPanel = new JPanel();
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton;
        if (add) {
            doneButton = new JButton(rbcp.getString("ButtonAddCircuit"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        if (doAddAction()) {
                            _dialog.dispose();
                        }
                    }
            });
        } else {
            doneButton = new JButton(rbcp.getString("ButtonOpenCircuit"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        if (doOpenAction()) {
                            _dialog.dispose();
                        }
                    }
            });
        }
        panel0.add(doneButton);

        JButton cancelButton = new JButton(rbcp.getString("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _sysNameBox.setText("");
                    _currentBlock = null;
                    _dialog.dispose();
                }
        });
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
        return buttonPanel;
    }

    private boolean doAddAction() {
        String sysname = _sysNameBox.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNameBox.getText();
            if (uname!=null && uname.trim().length()==0) {
                uname = null;
            }
            _currentBlock = InstanceManager.oBlockManagerInstance().createNewOBlock(sysname, uname);
            if (_currentBlock!=null) {
                return true;
            } else {
                int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                                rbcp.getString("blockExists"), sysname), 
                                rbcp.getString("AskTitle"), JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                    _currentBlock = InstanceManager.oBlockManagerInstance().getBySystemName(sysname);
                    if (_currentBlock==null) {
                        return false;
                    }
                    setSelectionGroup(_currentBlock);
                    _editCircuitFrame = new EditCircuitFrame(rbcp.getString("OpenCircuitItem"), this, _currentBlock);
                    return true;
                }            }
        }
        _currentBlock = null;
        return false;
    }

    private boolean doOpenAction() {
        int row = _model.getTable().getSelectedRow();
        if (row >= 0) {
           _currentBlock = (OBlock)_model.getBeanAt(row);
           setSelectionGroup(_currentBlock);
           return true;
        } else {
            JOptionPane.showMessageDialog(this, rbcp.getString("selectOBlock"), 
                            rbcp.getString("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
        _currentBlock = null;
        return false;
    }
    /************************** end setup frames ******************************/

    /************************* Closing Editing Frames ********************/

    /**
    * Update block data in menus
    */
    protected void closeCircuitFrame(OBlock block) {
        if (block!=null) {
            setIconGroup(block);
        }
        _selectionGroup = null;
        _circuitIcons = null;
        _editCircuitFrame = null;
        closeEditFrame();
    }

    /**
    *  Edit frame closing, set block's icons
    */
    private void setIconGroup(OBlock block) {
        ArrayList<Positionable> oldIcons = _circuitMap.get(block);
        if (oldIcons!=null) {
            for (int i=0; i<oldIcons.size(); i++) {
                Positionable pos = oldIcons.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(null);
                }
                _iconMap.remove(pos);
            }
        }
        // the _selectionGroup for all edit frames is full collection of icons
        // comprising the block.  Gather them and store in the block's hashMap
        ArrayList<Positionable> icons = new ArrayList<Positionable>();
        if (_selectionGroup!=null) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                Positionable pos = _selectionGroup.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(new NamedBeanHandle<OBlock>(block.getSystemName(), block));
                }
                icons.add(pos);
                _iconMap.put(pos, block);
            }
        }
        _circuitMap.put(block, icons);
        if (log.isDebugEnabled()) log.debug("setIconGroup: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    protected void closePathFrame(OBlock block) {
        _selectionGroup = null;
        _circuit2Icons = null;
        _currentBlock.deAllocate(null);
        _editPathsFrame = null;
        closeEditFrame();
    }

    protected void closePortalFrame(OBlock block) {
        _selectionGroup = null;
        _circuitIcons = null;
        _editPortalFrame = null;
        _circuit2Icons = null;
        closeEditFrame();
    }

    private void closeEditFrame() {
        _currentBlock = null;
        checkCircuits();
        makeToDoMenu();
        makeCircuitMenu();
        _highlightcomponent = null;
        TargetPane targetPane = (TargetPane)getTargetPanel();
        targetPane.setDefaultColors();
        repaint();
    }
    /**************** end closing frames ********************/
    /**
    * Find the blocks with no icons and the blocks with icons that need conversion
    * Setup for main Frame - used in both initialization and close of an editing frame
    * Build _darkTrack to aid subsquent call to
    */
    private void checkCircuits() {

        _portalIconMap = new Hashtable<String, PortalIcon>();
        _darkTrack = new ArrayList<Positionable>();
        _unconvertedTrack = new ArrayList<Positionable>();
        Iterator<Positionable> it = getContents().iterator();
        while (it.hasNext()) {
            Positionable pos = it.next();
            // if (log.isDebugEnabled()) log.debug("Positionable "+pos.getClass().getName());
            if (pos instanceof IndicatorTrack) {
                OBlock block = ((IndicatorTrack)pos).getOccBlock();
                if (block!=null) {
                    addIcon(block, pos);
                } else {
                    _darkTrack.add(pos);
                }
            } else if (isUnconvertedTrack(pos)) {
                if (!_darkTrack.contains(pos)) {
                    _darkTrack.add(pos);
                }
                if (!_unconvertedTrack.contains(pos)) {
                    _unconvertedTrack.add(pos);
                }
            } else if (pos instanceof PortalIcon) {
                PortalIcon pIcon = (PortalIcon)pos;
                pIcon.setStatus(PortalIcon.BLOCK);
                _portalIconMap.put(pIcon.getName(), pIcon);
            }
        }

        _bareBlock = new ArrayList<OBlock>();
        _convertBlock = new ArrayList<OBlock>();
        ArrayList<Positionable> blockTrack = new ArrayList<Positionable>();
        OBlockManager manager = InstanceManager.oBlockManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            ArrayList<Positionable> icons = _circuitMap.get(block);
            if (log.isDebugEnabled()) log.debug("checkCircuits: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
            if (icons==null || icons.size()==0) {
                _bareBlock.add(block);
                _convertBlock.add(block);
            } else {
                blockTrack.addAll(icons);
                for (int k=0; k<icons.size(); k++) {
                    Positionable pos = icons.get(k);
                    if (!(pos instanceof IndicatorTrack)) {
                        if (!_convertBlock.contains(block)) {
                            _convertBlock.add(block);
                            break;
                        }
                    }
                }
            }
        }
    }
    /*************** Frame Utilities **************/
    
    /**
    * Used by Portal Frame
    */
    protected void clearAdjacentBlock() {
        _circuit2Icons = null;
        repaint();
    }

    protected java.util.List<Positionable> getSelectionGroup() {
        return _selectionGroup;
    }
    
    /**
    * Used by Portal Frame
    */
    protected java.util.List<Positionable> getCircuitIcons(OBlock block) {
        return _circuitMap.get(block);
    }

    /**
    * Used by Portal Frame
    */
    protected OBlock getBlock(Positionable pos) {
        return _iconMap.get(pos);
    }

    /**
    * Used by Path Frame
    */
    protected java.util.List<Positionable> getCircuitGroup2() {
        return _circuit2Icons;
    }

    /**
    * Used by Path Frame
    */    
    protected Hashtable<String, PortalIcon> getPortalIconMap() {
        return _portalIconMap;
    }

    protected void highlight(Positionable pos) {
        _highlightcomponent = new Rectangle(pos.getX(), pos.getY(), 
                                            pos.maxWidth(), pos.maxHeight());
    }

    /**
    * Remove block, but keep the track icons. Set block reference in icon null
    */
    protected void removeBlock (OBlock block) {
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list!=null) {
            for (int i=0; i<list.size(); i++) {
                Positionable pos = list.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(null);
                }
                _darkTrack.add(pos);
            }
        }
//        InstanceManager.oBlockManagerInstance().deregister(block);
        _circuitMap.remove(block);
        block.dispose();
    }

    protected void addBlock (OBlock block) {
        ArrayList<Positionable> icons = new ArrayList<Positionable>();
        _circuitMap.put(block, icons);
        if (log.isDebugEnabled()) log.debug("addBlock: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    protected void setEditColors() {
        TargetPane targetPane = (TargetPane)getTargetPanel();
        targetPane.setHighlightColor(_editGroupColor);
        targetPane.setSelectGroupColor(_editGroupColor);
    }


    /***************** Overriden methods of Editor *******************/

    public void paintTargetPanel(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if (_circuitIcons!=null){
            java.awt.Stroke stroke = g2d.getStroke();
            Color color = g2d.getColor();
            g2d.setColor(_editGroupColor);
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            for(int i=0; i<_circuitIcons.size();i++){
                g.drawRect(_circuitIcons.get(i).getX(), _circuitIcons.get(i).getY(), 
                           _circuitIcons.get(i).maxWidth(), _circuitIcons.get(i).maxHeight());
            }
            g2d.setColor(color);
            g2d.setStroke(stroke);
        }
        if (_circuit2Icons!=null) {
            java.awt.Stroke stroke = g2d.getStroke();
            Color color = g2d.getColor();
            g2d.setColor(_editGroup2Color);
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            for(int i=0; i<_circuit2Icons.size();i++){
                g.drawRect(_circuit2Icons.get(i).getX(), _circuit2Icons.get(i).getY(), 
                           _circuit2Icons.get(i).maxWidth(), _circuit2Icons.get(i).maxHeight());
            }
            g2d.setColor(color);
            g2d.setStroke(stroke);
        }
    }

    public void setAllEditable(boolean state) {
		_editable = state;
        for (int i = 0; i<_contents.size(); i++) {
            _contents.get(i).setEditable(state);
        }
        _highlightcomponent = null;
        _selectionGroup = null;
    }

    /**
    *  Create popup for a Positionable object
    * Popup items common to all positionable objects are done before
    * and after the items that pertain only to specific Positionable
    * types.
    *
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent)p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            // items common to all
            if (p.doViemMenu()) {
                popup.add(p.getNameString());
                setPositionableMenu(p, popup);
                if (p.isPositionable()) {
                    setShowCoordinatesMenu(p, popup);
                    setShowAlignmentMenu(p, popup);
                }
                setDisplayLevelMenu(p, popup);
                //setHiddenMenu(p, popup);
                popup.addSeparator();
            }
            //setCopyMenu(p, popup);

            // items with defaults or using overrides
            boolean popupSet = false;
            popupSet |= p.setRotateMenu(popup);        
            popupSet |= p.setScaleMenu(popup);        
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            if (event.isControlDown()) {
                popupSet = p.setEditIconMenu(popup);    // old IconEditor        
            } else {
                popupSet = p.setEditItemMenu(popup);    // ItemPalette Editor        
            }
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setTextEditMenu(popup);
            popupSet |= setTextAttributes(p, popup);
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);

            // for Positionables with unique settings
            p.showPopUp(popup);

            setShowTooltipMenu(p, popup);
            setRemoveMenu(p, popup);
        } else {
            p.showPopUp(popup);
        }
        popup.show((Component)p, p.getWidth()/2+(int)((getPaintScale()-1.0)*p.getX()),
                    p.getHeight()/2+(int)((getPaintScale()-1.0)*p.getY()));
    }
*/
/********************* convert plain track to indicator track **************/

    IndicatorItemPanel _trackPanel;
    IndicatorTOItemPanel _trackTOPanel;
    PositionableLabel _oldIcon;
    JDialog _convertDialog; 

    /**
    * Check if the block being edited has all its icons converted to indicator icons
    */
    protected boolean iconsConverted() {
        if (_selectionGroup!=null && _selectionGroup.size()>0) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                Positionable pos = _selectionGroup.get(i);
                if (!(pos instanceof IndicatorTrack)) {
                    int result = JOptionPane.showConfirmDialog(this, rbcp.getString("notIndicatorIcon"), 
                                    rbcp.getString("incompleteCircuit"), JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.QUESTION_MESSAGE);
                    if (result==JOptionPane.YES_OPTION) {
                        convertIcons();
                        break;
                    } else {
                        _selectionGroup = null;
                        _circuitIcons = null;
                        return false;
                    }
                }
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, rbcp.getString("needIcons"), 
                            rbcp.getString("noIcons"), JOptionPane.INFORMATION_MESSAGE);
        }
        return false;
    }

    protected void convertIcons() {
        // converting an icon will update the icon list.  Thus refresh the list each time
        // a conversion is done.
        boolean convert = true;
        while (convert) {
            convert = false;
            java.util.List<Positionable> list = _selectionGroup;
            if (list!=null) {
                for (int i=0; i<list.size(); i++) {
                    Positionable pos = list.get(i);
                    if (!(pos instanceof IndicatorTrack)) {
                        convertIcon(pos);
                        if (log.isDebugEnabled()) log.debug("convertIcons: pos= "+pos.getClass().getName());
                        convert = true;
                        break;
                    }
                }
            } 
        }
    }

    protected void convertIcon(Positionable pos) {
        _oldIcon = (PositionableLabel)pos;
        ((TargetPane)getTargetPanel()).setHighlightColor(_highlightColor);

        _highlightcomponent = new Rectangle(_oldIcon.getX(), _oldIcon.getY(), 
                                            _oldIcon.maxWidth(), _oldIcon.maxHeight());
        repaint();
        if (pos instanceof TurnoutIcon) {
            makePalettteFrame("IndicatorTO");
            _trackTOPanel = new IndicatorTOItemPanel(null, "IndicatorTO", null, null, this);
            _convertDialog.add(_trackTOPanel);
            ActionListener updateAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    convertTO();
                }
            };
            _trackTOPanel.init(updateAction);
        } else {
            makePalettteFrame("IndicatorTrack");
            _trackPanel = new IndicatorItemPanel(null, "IndicatorTrack", null, this);
            _convertDialog.add(_trackPanel);
            ActionListener updateAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    convertSeg();
                }
            };
            _trackPanel.init(updateAction);
        }
        _convertDialog.pack();
        _convertDialog.setVisible(true);
        repaint();
    }

    private void makePalettteFrame(String title) {
        makePalette();
        _convertDialog = new JDialog(this, 
                                    java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString(title)),
                                    true);
        _convertDialog.setLocationRelativeTo(this);
        _convertDialog.setLocationRelativeTo(this);
        _convertDialog.toFront();
    }

    private void convertTO() {
        IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(this);
        OBlock block = _editCircuitFrame.getBlock();
        t.setOccBlockHandle(new NamedBeanHandle<OBlock>(block.getSystemName(), block));       
        t.setTurnout( ((TurnoutIcon)_oldIcon).getNamedTurnout());
        t.setFamily(_trackTOPanel.getFamilyName());

        Hashtable <String, Hashtable <String, NamedIcon>> iconMap = _trackTOPanel.getIconMaps();
        Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
            String status = entry.getKey();
            Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                t.setIcon(status, ent.getKey(), ent.getValue());
            }
        }
        t.setLevel(Editor.TURNOUTS);
        finishConvert(block, t);
    }
    private void convertSeg() {
        IndicatorTrackIcon t = new IndicatorTrackIcon(this);
        OBlock block = _editCircuitFrame.getBlock();
        t.setOccBlockHandle(new NamedBeanHandle<OBlock>(block.getSystemName(), block));       
        t.setFamily(_trackPanel.getFamilyName());

        Hashtable<String, NamedIcon> iconMap = _trackPanel.getIconMap();
        if (iconMap!=null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        t.setLevel(Editor.TURNOUTS);
        finishConvert(block, t);
    }

    private void finishConvert(OBlock block, Positionable pos) {
        _selectionGroup.remove(_oldIcon);
        removeFromContents(_oldIcon);
        pos.setLocation(_oldIcon.getLocation());
        putItem(pos);
        _selectionGroup.add(pos);
        pos.updateSize();
        _oldIcon.remove();

        _oldIcon = null;
        _trackPanel = null;
        _trackTOPanel = null;
        _convertDialog.dispose();
        _convertDialog = null;
        _highlightcomponent = null;
        ((TargetPane)getTargetPanel()).setHighlightColor(_editGroupColor);
        repaint();
    }
    /*************** end convert icons *******************/

/**************** select - deselect track icons ************************/

    /**
    * select and highlight block's icons for block editing
    */
    private void setSelectionGroup(OBlock block) {
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list!=null) {
            _selectionGroup = new ArrayList<Positionable>();
            for (int i=0; i<list.size(); i++) {
                _selectionGroup.add(list.get(i));
            }
            if (log.isDebugEnabled()) log.debug("setSelectionGroup: block "+block.getDisplayName()+" has "+ list.size() +" icons.");
        } else {
            _selectionGroup = null;
            if (log.isDebugEnabled()) log.debug("setSelectionGroup: block "+block.getDisplayName()+" has 0 icons.");
        }
        repaint();
    }

    protected boolean isTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel)pos;
            if (pl.isIcon()) {
                 NamedIcon icon = (NamedIcon)pl.getIcon();
                 if (icon!=null) {
                     String fileName = icon.getURL();
                     // getURL() returns Unix separatorChar= "/" even on windows
                     // so don't use java.io.File.separatorChar
                     if (fileName.contains("/track/") ||
                            (fileName.contains("/tracksegments/") && !fileName.contains("circuit"))) {
                         return true;
                     }
                 }
            }

        }
        return false;
    }

    private boolean isUnconvertedTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return false;
        } else  if (pos instanceof TurnoutIcon) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel)pos;
            if (pl.isIcon()) {
                 NamedIcon icon = (NamedIcon)pl.getIcon();
                 if (icon!=null) {
                     String fileName = icon.getURL();
                     // getURL() returns Unix separatorChar= "/" even on windows
                     // so don't use java.io.File.separatorChar
                     if (fileName.contains("/track/") ||
                            (fileName.contains("/tracksegments/") && !fileName.contains("circuit"))) {
                         return true;
                     }
                 }
            }

        }
        return false;
    }

    /**
    * Can a path in this circuit be drawn through this icon? 
    */
    private boolean okPath(Positionable pos, OBlock block) {
        java.util.List icons = _circuitMap.get(block);
        if (pos instanceof PortalIcon) {
            Portal portal = ((PortalIcon)pos).getPortal();
            if (portal!=null) {
                if (block.equals(portal.getFromBlock()) || block.equals(portal.getToBlock())) {
                    ((PortalIcon)pos).setStatus(PortalIcon.PATH);
                    return true;
                }
            }
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                rbcp.getString("portalNotInCircuit"),block.getDisplayName()), 
                            rbcp.getString("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!icons.contains(pos)) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                rbcp.getString("iconNotInCircuit"),block.getDisplayName()), 
                            rbcp.getString("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
    * Can this icon be added to the circuit?
    */
    private boolean okToAdd(Positionable pos, OBlock editBlock) {
        if (pos instanceof IndicatorTrack) {
            OBlock block = ((IndicatorTrack)pos).getOccBlock();
            if (block!=null) {
                if (!block.equals(editBlock)) {
                    int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                                        rbcp.getString("iconBlockConflict"), 
                                        block.getDisplayName(), editBlock.getDisplayName()),
                                    rbcp.getString("whichCircuit"), JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.QUESTION_MESSAGE);
                    if (result==JOptionPane.YES_OPTION) {
                        // move icon from block to editBlock 
                        java.util.List ic = _circuitMap.get(block);
                        ic.remove(pos);
                        ((IndicatorTrack)pos).setOccBlockHandle(new NamedBeanHandle<OBlock>(editBlock.getSystemName(), editBlock));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**************************** Mouse *************************/

    /**
    * Keep selections when editing.  Restore what super nulls
    */
    public void mousePressed(MouseEvent event) {
        ArrayList<Positionable> saveSelectionGroup = _selectionGroup;
        super.mousePressed(event);
        if (_editCircuitFrame!=null || _editPathsFrame!=null) {
            _selectionGroup = saveSelectionGroup;
        }
    }

    public void mouseReleased(MouseEvent event) {
        Positionable selection = getCurrentSelection(event);
        super.mouseReleased(event);
        handleSelection(selection, event);
        /*
        if (_editCircuitFrame!=null) {
            _editCircuitFrame.updateIconList(_selectionGroup);
        } else if (_editPortalFrame!=null ) { //&& _circuit2Icons==null) {
            _selectionGroup = null;
            _highlightcomponent = null;
        } else if (_editPathsFrame!=null) {
            _highlightcomponent = null;
        }
        */
    }

    /**
    * No dragging when editing
    */
    public void mouseDragged(MouseEvent event) {
        if (_editCircuitFrame!=null || _editPathsFrame!=null) { //|| _circuit2Icons==null) {
            return;     // no dragging when editing
        }
        if (_editPortalFrame!=null && !(_currentSelection instanceof PortalIcon)) {
            return;
        }
        super.mouseDragged(event);
    }

    /***************** Overriden methods of Editor *******************/
    
    /**
    * Return a List of all items whose bounding rectangle contain the mouse position.
    * ordered from top level to bottom
    * Modified to collect only track icons
    */
    protected java.util.List <Positionable> getSelectedItems(MouseEvent event) {
        if (_editCircuitFrame==null ) { //&& _editPathsFrame==null) {
            return super.getSelectedItems(event);
        }
        double x = event.getX();
        double y = event.getY();
        Rectangle rect = new Rectangle();
        ArrayList <Positionable> selections = new ArrayList <Positionable>();
        for (int i=0; i<_contents.size(); i++) {
            Positionable p = _contents.get(i);
            if (isTrack(p)) {
                rect= p.getBounds(rect);
                Rectangle2D.Double rect2D = new Rectangle2D.Double(rect.x*_paintScale,
                                                                   rect.y*_paintScale,
                                                                   rect.width*_paintScale,
                                                                   rect.height*_paintScale);
                if (rect2D.contains(x, y) && (p.getDisplayLevel()>BKG || event.isControlDown())) {
                    boolean added =false;
                    int level = p.getDisplayLevel();
                    for (int k=0; k<selections.size(); k++) {
                        if (level >= selections.get(k).getDisplayLevel()) {
                            selections.add(k, p);
                            added = true;       // OK to lie in the case of background icon
                            break;
                        }
                    }
                    if (!added) {
                        selections.add(p);
                    }
                }
            }
        }
        return selections;
    }

    /*
    * Gather all items inside _selectRect
    *
    protected void  makeSelectionGroup(MouseEvent event) {
        if (_editCircuitFrame!=null) {
            Rectangle test = new Rectangle();
            java.util.List<Positionable> list = getContents();
            for (int i=0; i < list.size(); i++) {
                Positionable comp = list.get(i);
                if (isTrack(comp)) {
                    if (event.isShiftDown()) {
                        if (_selectRect.intersects(comp.getBounds(test)) && 
                                        (event.isControlDown() || comp.getDisplayLevel()>BKG)) {
                            _selectionGroup.add(comp);
                        }

                    } else {
                        if (_selectRect.contains(comp.getBounds(test)) && 
                                        (event.isControlDown() || comp.getDisplayLevel()>BKG)) {
                            _selectionGroup.add(comp);
                        }
                    }
                }
            }
        } else if (_editPathsFrame!=null) {
            _selectionGroup = _circuitIcons;
        } else {
            super.makeSelectionGroup(event);
        }
        if (log.isDebugEnabled()) log.debug("makeSelectionGroup: "+(_selectionGroup==null?"0":_selectionGroup.size())+" selected.");
    }

    /*
    * For the param, selection, Add to or delete from _selectionGroup. 
    * If not there, add.
    * If there, delete.
    */
    protected void handleSelection(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null) {
            if (isTrack(selection)) {
                if (_selectionGroup==null) {
                    _selectionGroup = new ArrayList<Positionable>();
                }
                if (log.isDebugEnabled()) log.debug("handleSelection: _selectionGroup.size()= "+_selectionGroup.size());
                if (_selectionGroup.contains(selection)) {
                    _selectionGroup.remove(selection);
                } else if (okToAdd(selection, _editCircuitFrame.getBlock())) {
                    _selectionGroup.add(selection);
                }
            }
            _editCircuitFrame.updateIconList(_selectionGroup);
            _editCircuitFrame.toFront();
        } else if (_editPathsFrame!=null) {
            if (selection instanceof IndicatorTrack || selection instanceof PortalIcon) {
                OBlock block = _editPathsFrame.getBlock();
                java.util.List<Positionable> pathGroup = _editPathsFrame.getPathGroup();
                if (!event.isShiftDown()) {
                    if (pathGroup.contains(selection)) {
                        pathGroup.remove(selection);
                        if (selection instanceof PortalIcon) {
                            ((PortalIcon)selection).setStatus(PortalIcon.BLOCK);
                        } else {
                            //_selectionGroup.remove(selection);
                            ((IndicatorTrack)selection).removePath(EditCircuitPaths.TEST_PATH);
                            if (log.isDebugEnabled()) log.debug("removePath TEST_PATH");
                        }
                    } else if (okPath(selection, block)) {
                        pathGroup.add(selection);
                        if (selection instanceof IndicatorTrack) {
                            //_selectionGroup.add(selection);
                            ((IndicatorTrack)selection).addPath(EditCircuitPaths.TEST_PATH);
                            if (log.isDebugEnabled()) log.debug("addPath TEST_PATH");
                        }
                    } else {
                        return;
                    }
                }
                int state = block.getState() | OBlock.ALLOCATED;
                block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
            }
            _highlightcomponent = null;
            _editPathsFrame.toFront();
        } else if (_editPortalFrame!=null) {
            if (log.isDebugEnabled()) log.debug("selection= "+(selection==null?"null":
                                                            selection.getClass().getName()));
            if (selection instanceof PortalIcon) {
                _editPortalFrame.checkPortalIcon((PortalIcon)selection);
            } /*else {
                if (isTrack(selection)) {
                    OBlock block = _editPortalFrame.getHomeBlock();
                    OBlock selectBlock = _iconMap.get(selection);
                    if (log.isDebugEnabled()) log.debug("modifySelectionGroup: homeBlock= "+block.getDisplayName()+" selectBlock= "
                                       +(selectBlock==null?"null":selectBlock.getDisplayName()));
                    if (selectBlock!=null) {
                        if (selectBlock.equals(block)) {
                            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                                rbcp.getString("selectAdjacentCircuit"),block.getDisplayName()), 
                                            rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            _circuit2Icons = _circuitMap.get(selectBlock);
                            _editPortalFrame.setAdjacentBlock(selectBlock);
                            ((TargetPane)getTargetPanel()).setDefaultColors();
                        }
                        return;
                    }
                }
            } */
            _selectionGroup = null;
            _highlightcomponent = null;
            _editPortalFrame.toFront();
        }
    }


    protected void modifySelectionGroup(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null || _editPortalFrame!=null ||_editPathsFrame!=null) {
            return;
        }
        super.modifySelectionGroup(selection, event);
        if (log.isDebugEnabled()) log.debug("modifySelectionGroup: "+(_selectionGroup==null?"0":_selectionGroup.size())+" selected.");
    }

    /**************************** static methods ************************/

    protected static void doSize(JComponent comp, int max, int min) {
        Dimension dim = comp.getPreferredSize();
        dim.width = max;
        comp.setMaximumSize(dim);
        dim.width = min;
        comp.setMinimumSize(dim);
    }

    protected static JPanel makeTextBoxPanel(boolean vertical, JTextField textField, String label,
                                             boolean editable, String tooltip) {
        JPanel panel = makeBoxPanel(vertical, textField, label, tooltip);
        textField.setEditable(editable);
        textField.setBackground(Color.white);        
        return panel;
    }

    protected static JPanel makeBoxPanel(boolean vertical, JComponent textField, String label,
                                         String tooltip) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        JLabel l = new JLabel(rbcp.getString(label));
        if (vertical) {
            c.anchor = java.awt.GridBagConstraints.SOUTH;
            l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            textField.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        } else {
            c.anchor = java.awt.GridBagConstraints.EAST;
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            textField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        }
        panel.add(l, c);
        if (vertical) {
            c.anchor = java.awt.GridBagConstraints.NORTH;
            c.gridy = 1;
        } else {
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.gridx = 1;
        }
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        doSize(textField, 9000, 200);    // default
        panel.add(textField, c);
        if (tooltip!=null) {
            textField.setToolTipText(rbcp.getString(tooltip));
            l.setToolTipText(rbcp.getString(tooltip));
            panel.setToolTipText(rbcp.getString(tooltip));
        }
        return panel;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CircuitBuilder.class.getName());
}

