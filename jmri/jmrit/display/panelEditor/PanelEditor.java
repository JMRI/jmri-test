package jmri.jmrit.display.panelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.util.JmriJFrame;

import jmri.jmrit.display.*;
import jmri.configurexml.*;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import jmri.jmrit.display.Editor;
import javax.swing.*;

import org.jdom.Element;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame.
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add.
 * <P>
 * All created objects are put specific levels depending on their
 * type (higher levels are in front):
 * <UL>
 * <LI>BKG background
 * <LI>ICONS icons and other drawing symbols
 * <LI>LABELS text labels
 * <LI>TURNOUTS turnouts and other variable track items
 * <LI>SENSORS sensors and other independently modified objects
 * </UL>
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 * <P>
 * If you close the Editor window, the target is left alone and
 * the editor window is just hidden, not disposed.
 * If you close the target, the editor and target are removed,
 * and dispose is run. To make this logic work, the PanelEditor
 * is descended from a JFrame, not a JPanel.  That way it
 * can control its own visibility.
 * <P>
 * The title of the target and the editor panel are kept
 * consistent via the {#setTitle} method.
 *
 * @author  Bob Jacobsen  Copyright: Copyright (c) 2002, 2003, 2007
 * @author  Dennis Miller 2004
 * @author  Howard G. Penny Copyright: Copyright (c) 2005
 * @author  Matthew Harris Copyright: Copyright (c) 2009
 * @author  Pete Cressman Copyright: Copyright (c) 2009, 2010
 * 
 */

public class PanelEditor extends Editor implements ItemListener {

    public boolean _debug;

    JTextField nextX = new JTextField(rb.getString("DefaultX"),4);
    JTextField nextY = new JTextField(rb.getString("DefaultY"),4);

    JCheckBox editableBox = new JCheckBox(rb.getString("CheckBoxEditable"));
    JCheckBox positionableBox = new JCheckBox(rb.getString("CheckBoxPositionable"));
    JCheckBox controllingBox = new JCheckBox(rb.getString("CheckBoxControlling"));
    JCheckBox showCoordinatesBox = new JCheckBox(rb.getString("CheckBoxShowCoordinates"));
    JCheckBox showTooltipBox = new JCheckBox(rb.getString("CheckBoxShowTooltips"));
    JCheckBox hiddenBox = new JCheckBox(rb.getString("CheckBoxHidden"));
    JCheckBox menuBox = new JCheckBox(rb.getString("CheckBoxMenuBar"));
    JLabel scrollableLabel = new JLabel(rb.getString("ComboBoxScrollable"));
    JComboBox scrollableComboBox = new JComboBox();

    JButton labelAdd = new JButton(rb.getString("ButtonAddText"));
    JTextField nextLabel = new JTextField(10);

    JComboBox _addIconBox;

    public PanelEditor() {}

    public PanelEditor(String name) {
        super(name);
        init(name);
    }

    protected void init(String name) {
        _debug = log.isDebugEnabled();
        java.awt.Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        contentPane.add(common);
        setAllEditable(true);
        setShowHidden(true);
        super.setTargetPanel(null, makeFrame(name));
        super.setTargetPanelSize(400, 300);
        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("SansSerif", Font.PLAIN, 12),
                                                     Color.black, new Color(215, 225, 255), Color.black));
        // set scrollbar initial state
        setScroll(SCROLL_BOTH);

        // add menu - not using PanelMenu, because it now
        // has other stuff in it?
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.display.NewPanelAction(rb.getString("MenuItemNew")));
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(rb.getString("MIStoreImageIndex"));
        fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
                PanelEditor panelEd;
                public void actionPerformed(ActionEvent event) {
					jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex(panelEd);
                }
                ActionListener init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        JMenuItem editItem = new JMenuItem(rb.getString("editIndexMenu"));
        editItem.addActionListener(new ActionListener() {
                PanelEditor panelEd;
                public void actionPerformed(ActionEvent e) {
                    ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                    ii.pack();
                    ii.setVisible(true);
                }
                ActionListener init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        fileMenu.add(editItem);

        editItem = new JMenuItem(rb.getString("CPEView"));
        fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					changeView("jmri.jmrit.display.controlPanelEditor.ControlPanelEditor");
                }
            });

        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rb.getString("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (deletePanel() ) {
                        dispose(true);
                    }
                }
            });

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.PanelEditor", true);

        // allow naming the panel
        {
            JPanel namep = new JPanel();
            namep.setLayout(new FlowLayout());
            JButton b = new JButton("Set panel name");
            b.addActionListener(new ActionListener() {
                PanelEditor editor;
                public void actionPerformed(ActionEvent e) {
                    // prompt for name
                    String newName = JOptionPane.showInputDialog(null, rb.getString("PromptNewName"));
                    if (newName==null) return;  // cancelled
                    
                    if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(newName)){
                    	JOptionPane.showMessageDialog(null, rb.getString("CanNotRename"), rb.getString("PanelExist"),
                    			JOptionPane.ERROR_MESSAGE);
                    	return;
                    }
                    if (getTargetPanel().getTopLevelAncestor()!=null) {
                         ((JFrame)getTargetPanel().getTopLevelAncestor()).setTitle(newName);
                    }
                    editor.setTitle();
					jmri.jmrit.display.PanelMenu.instance().renameEditorPanel(editor);
                }
                ActionListener init(PanelEditor e) {
                    editor = e;
                    return this;
                }
            }.init(this));
            namep.add(b);
            this.getContentPane().add(namep);
        }
        // add a text label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(labelAdd);
            labelAdd.setEnabled(false);
            labelAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextLabel);
            labelAdd.addActionListener( new ActionListener() {
                    PanelEditor editor;
                    public void actionPerformed(ActionEvent a) {
                        editor.addLabel(nextLabel.getText());
                    }
                    ActionListener init(PanelEditor e) {
                        editor = e;
                        return this;
                    }
                }.init(this));
            nextLabel.addKeyListener(new KeyAdapter() {
                      public void keyReleased(KeyEvent a){
                          if (nextLabel.getText().equals("")) {
                            labelAdd.setEnabled(false);
                            labelAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                          }
                          else {
                            labelAdd.setEnabled(true);
                            labelAdd.setToolTipText(null);
                          }
                      }
                  });
            this.getContentPane().add(panel);
        }

        // Selection of the type of entity for the icon to represent is done from a combobox
        _addIconBox = new JComboBox();
        _addIconBox.setMinimumSize(new Dimension(75,75));
        _addIconBox.setMaximumSize(new Dimension(200,200));
        _addIconBox.addItem(new ComboBoxItem("RightTOEditor"));
        _addIconBox.addItem(new ComboBoxItem("LeftTOEditor"));
        _addIconBox.addItem(new ComboBoxItem("SlipTOEditor"));
        _addIconBox.addItem(new ComboBoxItem("SensorEditor"));
        _addIconBox.addItem(new ComboBoxItem("SignalHeadEditor"));
        _addIconBox.addItem(new ComboBoxItem("SignalMastEditor"));
        _addIconBox.addItem(new ComboBoxItem("MemoryEditor"));
        _addIconBox.addItem(new ComboBoxItem("ReporterEditor"));
        _addIconBox.addItem(new ComboBoxItem("LightEditor"));
        _addIconBox.addItem(new ComboBoxItem("BackgroundEditor"));
        _addIconBox.addItem(new ComboBoxItem("MultiSensorEditor"));
        _addIconBox.addItem(new ComboBoxItem("AddRPSreporter"));
        _addIconBox.addItem(new ComboBoxItem("AddFastClock"));
        _addIconBox.addItem(new ComboBoxItem("IconEditor"));
        _addIconBox.setSelectedIndex(-1);
        _addIconBox.addItemListener(this);  // must be AFTER no selection is set
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(rb.getString("selectTypeIcon")));
        p1.add(p2);
        p1.add(_addIconBox);
        contentPane.add(p1);

        // Build resource catalog and load CatalogTree.xml now
        jmri.jmrit.catalog.CatalogPanel catalog = new jmri.jmrit.catalog.CatalogPanel();
        catalog.createNewBranch("IFJAR", "Program Directory", "resources");

        // edit, position, control controls
        {
            // edit mode item
            contentPane.add(editableBox);
            editableBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        setAllEditable(editableBox.isSelected());
                        hiddenCheckBoxListener();
                    }
                });
            editableBox.setSelected(isEditable());
            // positionable item
            contentPane.add(positionableBox);
            positionableBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        setAllPositionable(positionableBox.isSelected());
                    }
                });                    
            positionableBox.setSelected(allPositionable());
            // controlable item
            contentPane.add(controllingBox);
            controllingBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        setAllControlling(controllingBox.isSelected());
                    }
                });                    
            controllingBox.setSelected(allControlling());
            // hidden item
            contentPane.add(hiddenBox);
            hiddenCheckBoxListener();
            hiddenBox.setSelected(showHidden());

            contentPane.add(showCoordinatesBox);
            showCoordinatesBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	setShowCoordinates(showCoordinatesBox.isSelected());
                }
            });
            showCoordinatesBox.setSelected(showCoordinates());

            contentPane.add(showTooltipBox);
            showTooltipBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	setAllShowTooltip(showTooltipBox.isSelected());
                }
            });
            showTooltipBox.setSelected(showTooltip());

            contentPane.add(menuBox);
            menuBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setPanelMenu(menuBox.isSelected());
                }
            });
            menuBox.setSelected(true);

            // Show/Hide Scroll Bars
            JPanel scrollPanel = new JPanel();
            scrollPanel.setLayout(new FlowLayout());
            scrollableLabel.setLabelFor(scrollableComboBox);
            scrollPanel.add(scrollableLabel);
            scrollPanel.add(scrollableComboBox);
            contentPane.add(scrollPanel);
            scrollableComboBox.addItem(rb.getString("ScrollNone"));
            scrollableComboBox.addItem(rb.getString("ScrollBoth"));
            scrollableComboBox.addItem(rb.getString("ScrollHorizontal"));
            scrollableComboBox.addItem(rb.getString("ScrollVertical"));
            scrollableComboBox.setSelectedIndex(SCROLL_BOTH);
            scrollableComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setScroll(scrollableComboBox.getSelectedIndex());
                }
            });
       }

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);

        // when this window closes, set contents of target uneditable
        addWindowListener(new java.awt.event.WindowAdapter() {
            PanelEditor panelEd;
                public void windowClosing(java.awt.event.WindowEvent e) {
                    jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(panelEd);
                }
                java.awt.event.WindowAdapter init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        // and don't destroy the window
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        // move this editor panel off the panel's position
        getTargetFrame().setLocationRelativeTo(this);
        getTargetFrame().pack();
        getTargetFrame().setVisible(true);
        if (_debug) log.debug("PanelEditor ctor done.");
    }  // end ctor

    /**
    * Initializes the hiddencheckbox and its listener.
    * This has been taken out of the init, as checkbox is
    * enable/disabled by the editableBox.
    */
    private void hiddenCheckBoxListener(){
        setShowHidden(hiddenBox.isSelected());
        if (editableBox.isSelected()){
            hiddenBox.setEnabled(false);
            hiddenBox.setSelected(true);
        } else {
            hiddenBox.setEnabled(true);
            hiddenBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setShowHidden(hiddenBox.isSelected());
                }
            });
        }
    
    }
    
    /**
     * After construction, initialize all the widgets to their saved config settings.
     */
    public void initView() {
        editableBox.setSelected(isEditable());
        positionableBox.setSelected(allPositionable());
        controllingBox.setSelected(allControlling());
        showCoordinatesBox.setSelected(showCoordinates());
        showTooltipBox.setSelected(showTooltip());
        hiddenBox.setSelected(showHidden());
        menuBox.setSelected(getTargetFrame().getJMenuBar().isVisible());
    }

    class ComboBoxItem {
        String name;
        ComboBoxItem(String n) {
            name = n;
        }
        String getName() {
            return name;
        }
        public String toString() {
            return rb.getString(name);
        }
    }

    int locationX = 0;
    int locationY = 0;
    static final int DELTA = 20; 

    /*
    *  itemListener for JComboBox
    */
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ComboBoxItem item = (ComboBoxItem)e.getItem();
            String name = item.getName();
            JFrameItem frame = super.getIconFrame(name);
            if (frame != null) {
                frame.getEditor().reset();
                frame.setVisible(true);
                _addIconBox.setSelectedIndex(-1);
            } else {
                if (name.equals("AddFastClock")) {
                    addClock();
                } else if (name.equals("AddRPSreporter")) {
                    addRpsReporter();
                } else { 
                    log.error("Unable to open Icon Editor \""+item.getName()+"\"");
                }
            }
            /* Classic PanelEditor has separate text entry field
            int which = _addIconBox.getSelectedIndex()+1;
            _addIconBox.setSelectedIndex(-1);
            switch (which) {
                case 0:
                    addTextEditor();
                    // no frame (uses JOptionPane)
                    return;
                case 1:
                    addRightTOEditor();
                    break;
                case 2:
                    addLeftTOEditor();
                    break;
                case 3:
                    addSensorEditor();
                    break;
                case 4:
                    addSignalHeadEditor();
                    break;
                case 5:
                    addSignalMastEditor();
                    break;
                case 6:
                    addMemoryEditor();
                    break;
                case 7:
                    addReporterEditor();
                    break;
                case 8:
                    addLightEditor();
                    break;
                case 9:
                    addBackgroundEditor();
                    break;
                case 10:
                    addMultiSensorEditor();
                    break;
                case 11:
                    addRpsReporter();
                    return;
                case 12:
                    addClock();
                    return;
                case 13:
                    addIconEditor();
                    break;
                default:
                    return;
            }
            // frame added in the above switch 
            frame = super.getIconFrame(item.getName());
            frame.setLocation(locationX, locationY);
            locationX += DELTA;
            locationY += DELTA;
            frame.setVisible(true);
            
            _addIconBox.setSelectedIndex(-1);
            */
        }
    }

   /**
     * Handle close of editor window.
     * <P>
     * Overload/override method in JmriJFrame parent, 
     * which by default is permanently closing the window.
     * Here, we just want to make it invisible, so we
     * don't dispose it (yet).
     **/
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
    }

    /**
     * Create sequence of panels, etc, for layout:
     * JFrame contains its ContentPane
     *    which contains a JPanel with BoxLayout (p1)
     *       which contains a JScollPane (js)
     *            which contains the targetPane
     *
     */
    public JmriJFrame makeFrame(String name) {
        JmriJFrame targetFrame = new JmriJFrame(name);
        targetFrame.setVisible(false);

        //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(new AbstractAction(rb.getString("OpenEditor")) {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                }
            });
		editMenu.addSeparator();
        editMenu.add(new AbstractAction(rb.getString("DeletePanel")){
                public void actionPerformed(ActionEvent e) {
                    if (deletePanel()) {
                        dispose(true);
                    }
                }
            });
        targetFrame.setJMenuBar(menuBar);
        // add maker menu
        JMenu markerMenu = new JMenu(rb.getString("MenuMarker"));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(rb.getString("AddLoco")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(rb.getString("AddLocoRoster")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(rb.getString("RemoveMarkers")){
        	public void actionPerformed(ActionEvent e) {
        		removeMarkers();
            }
        });
         
        menuBar.add(jmri.jmrit.logix.WarrantTableAction.makeWarrantMenu());

        targetFrame.addHelpMenu("package.jmri.jmrit.display.PanelTarget", true);
        return targetFrame;

    }

    /*************** implementation of Abstract Editor methods ***********/
    /**
     * The target window has been requested to close, don't delete it at this
	 *   time.  Deletion must be accomplished via the Delete this panel menu item.
     */
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        targetWindowClosing(true);
    }
    /**
     * Called from TargetPanel's paint method for additional drawing by editor view
     */
    protected void paintTargetPanel(Graphics g) {
        /*Graphics2D g2 = (Graphics2D)g;
        drawPositionableLabelBorder(g2);*/
    }

    /**
     * Set an object's location when it is created.
     */
    protected void setNextLocation(Positionable obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        obj.setLocation(x,y);
    }    
    /**
    *  Create popup for a Positionable object
    * Popup items common to all positionable objects are done before
    * and after the items that pertain only to specific Positionable
    * types.
    */

    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent)p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            // items for all Positionables
            if (p.doViemMenu()) {
                popup.add(p.getNameString());
                setPositionableMenu(p, popup);
                if (p.isPositionable()) {
                    setShowCoordinatesMenu(p, popup);
                    setShowAlignmentMenu(p, popup);
                }
                setDisplayLevelMenu(p, popup);
                setHiddenMenu(p, popup);
                popup.addSeparator();
            }

            // Positionable items with defaults or using overrides
            boolean popupSet =false;
            popupSet = p.setRotateOrthogonalMenu(popup);
            popupSet = p.setRotateMenu(popup);
            popupSet = p.setScaleMenu(popup);
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setEditIconMenu(popup);        
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setTextEditMenu(popup);
            PositionablePopupUtil util = p.getPopupUtility();
            if (util!=null) {
                util.setFixedTextMenu(popup);        
                util.setTextMarginMenu(popup);        
                util.setTextBorderMenu(popup);        
                util.setTextFontMenu(popup);
                util.setBackgroundMenu(popup);
                util.setTextJustificationMenu(popup);
                util.copyItem(popup);
                popup.addSeparator();
                util.propertyUtil(popup);
                popupSet = true;
            }
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);
            
            // for Positionables with unique item settings
            p.showPopUp(popup);

            setRemoveMenu(p, popup);
        } else {
            p.showPopUp(popup);
        }
        popup.show((Component)p, p.getWidth()/2, p.getHeight()/2);
    }

    public void showToolTip(Positionable selection, MouseEvent event) {
        ToolTip tip = selection.getTooltip();
        String txt = tip.getText();
        if (txt==null) {
            tip.setText(selection.getNameString());
        }
        tip.setLocation(selection.getX()+selection.getWidth()/2, selection.getY()+selection.getHeight());
        setToolTip(tip);
    }
/*
    public void mouseReleased(MouseEvent event) {
        super.mouseReleased(event);

        
        // if not sending MouseClicked, do it here
        if (jmri.util.swing.SwingSettings.getNonStandardMouseEvent())
            mouseClicked(event);
    }

    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);

        if (allPositionable() && _selectRect!=null) {
            if (_selectionGroup==null && _dragging) {
                makeSelectionGroup(event);
            }
        }
    }
*/
   /******************************************************/

    boolean delayedPopupTrigger;

    public void mousePressed(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mousePressed at ("+event.getX()+","+event.getY()+") _dragging="+_dragging);
        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;
        List <Positionable> selections = getSelectedItems(event);
        if (_dragging) {
            return;
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
            if (event.isPopupTrigger()) {
                if (_debug) log.debug("mousePressed calls showPopUp");
                if (event.isMetaDown() || event.isAltDown()){
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    // no possible conflict with moving, display the popup now
                    if (_selectionGroup!=null){
                        //Will show the copy option only
                        showMultiSelectPopUp(event, _currentSelection);
                    } else {
                        showPopUp(_currentSelection, event);
                    }
                }
            } else if (!event.isControlDown()){ 
                _currentSelection.doMousePressed(event);
                if (_selectRect==null)
                    _selectionGroup = null;
            }
        } else {
            if (event.isPopupTrigger()) {
                if (event.isMetaDown() || event.isAltDown()){
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    if (_multiItemCopyGroup!=null){
                        pasteItemPopUp(event);
                    } else if (_selectionGroup!=null) {
                        showMultiSelectPopUp(event, _currentSelection);
                    } else {
                        backgroundPopUp(event);
                        _currentSelection = null;
                    }
                }
            } else {
                _currentSelection = null;
            }
        }
        //if ((event.isControlDown() || _selectionGroup!=null) && _currentSelection!=null){
        if ((event.isControlDown()) || event.isMetaDown() || event.isAltDown()){
            //Don't want to do anything, just want to catch it, so that the next two else ifs are not
            //executed
        } else if (_currentSelection==null || 
                    (_selectRect!=null && !_selectRect.contains(_anchorX, _anchorY))){
                _selectRect = new Rectangle(_anchorX, _anchorY, 0, 0);
                _selectionGroup = null;
        } else {
            _selectRect = null;
            _selectionGroup = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseReleased(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseReleased at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" selectRect is "+(_selectRect==null? "null":"not null"));
        List <Positionable> selections = getSelectedItems(event);

        if (_dragging) {
            mouseDragged(event);
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
        } else {
            if ((event.isPopupTrigger()||delayedPopupTrigger) && !_dragging){
                if (_multiItemCopyGroup!=null)
                    pasteItemPopUp(event);
                else {
                    backgroundPopUp(event);
                    _currentSelection = null;
                }
            } 
            else{
                _currentSelection = null;

            }
        }
        /*if (event.isControlDown() && _currentSelection!=null && !event.isPopupTrigger()){
            amendSelectionGroup(_currentSelection, event);*/
        if ((event.isPopupTrigger() || delayedPopupTrigger) && _currentSelection != null && !_dragging) {
            if (_selectionGroup!=null){
                //Will show the copy option only
                showMultiSelectPopUp(event, _currentSelection);

            } else {
                showPopUp(_currentSelection, event);
            }
        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseReleased(event);
            }
            if (allPositionable() && _selectRect!=null) {
                if (_selectionGroup==null && _dragging) {
                    makeSelectionGroup(event);
                }
            }
        }
        delayedPopupTrigger = false;
        _dragging = false;

        // if not sending MouseClicked, do it here
        if (jmri.util.swing.SwingSettings.getNonStandardMouseEvent())
            mouseClicked(event);
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseDragged(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if ((event.isPopupTrigger()) || (!event.isMetaDown() && !event.isAltDown())) {
            if (_currentSelection!=null) {
                List <Positionable> selections = getSelectedItems(event);
                if (selections.size() > 0) {
                    if (selections.get(0)!=_currentSelection) {
                        _currentSelection.doMouseReleased(event);
                    }
                } else {
                    _currentSelection.doMouseReleased(event);
                }
            }
            return; 
        }
        if (_currentSelection!=null || _selectionGroup!=null) {
            if (!getFlag(OPTION_POSITION, _currentSelection.isPositionable())) { return; }
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
            if (_selectionGroup!=null) {
                for (int i=0; i<_selectionGroup.size(); i++){
                    moveItem(_selectionGroup.get(i), deltaX, deltaY);
                }
                _highlightcomponent = null;
            } else {
                moveItem(_currentSelection, deltaX, deltaY);
                _highlightcomponent = new Rectangle(_currentSelection.getX(), _currentSelection.getY(), _currentSelection.maxWidth(), _currentSelection.maxHeight());
            }
        } else {
            if (allPositionable() && _selectionGroup==null) {
                drawSelectRect(event.getX(), event.getY());
            }
        }
        _dragging = true;
        _lastX = event.getX();
        _lastY = event.getY();
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseMoved(MouseEvent event) {
        //if (_debug) log.debug("mouseMoved at ("+event.getX()+","+event.getY()+")"); 
        if (_dragging || event.isPopupTrigger()) { return; }

        List <Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                selection = selections.get(1); 
            } else {
                selection = selections.get(0); 
            }
        }
        if (isEditable() && selection!=null && selection.getDisplayLevel()>BKG){
            _highlightcomponent = new Rectangle(selection.getX(), selection.getY(), selection.maxWidth(), selection.maxHeight());
            _targetPanel.repaint();
        } else {
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
        if (selection!=null && selection.getDisplayLevel()>BKG && selection.showTooltip()) {
            showToolTip(selection, event);
            //selection.highlightlabel(true);
            _targetPanel.repaint();
        } else {
            setToolTip(null);
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
    }

    public void mouseClicked(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseClicked at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" selectRect is "+(_selectRect==null? "null":"not null"));
        List <Positionable> selections = getSelectedItems(event);

        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1); 
            } else {
                _currentSelection = selections.get(0); 
            }
        } else {
            _currentSelection = null;
            if (event.isPopupTrigger()){
                if (_multiItemCopyGroup==null)
                    pasteItemPopUp(event);
                else
                    backgroundPopUp(event);
            }
        }
        if (event.isPopupTrigger() && _currentSelection != null && !_dragging) {
            if (_selectionGroup!=null)
                showMultiSelectPopUp(event, _currentSelection);
            else
                showPopUp(_currentSelection, event);
            //_selectionGroup = null; //Show popup only works for a single item

        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseClicked(event);
            }
        }
        _targetPanel.repaint(); // needed for ToolTip
        if (event.isControlDown() && _currentSelection!=null && !event.isPopupTrigger()){
            amendSelectionGroup(_currentSelection);
        }
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint();  // needed for ToolTip
    }

    protected ArrayList <Positionable> _multiItemCopyGroup = null;  // items gathered inside fence
    
    protected void copyItem(Positionable p){
        _multiItemCopyGroup = new ArrayList <Positionable>();
        _multiItemCopyGroup.add(p);
    }
    
    protected void pasteItemPopUp(final MouseEvent event){
        if (!isEditable())
            return;
        if (_multiItemCopyGroup==null)
            return;
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Paste");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { pasteItem(event); }
        });
        setBackgroundMenu(popup);
        popup.add(edit);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    
    protected void backgroundPopUp(MouseEvent event){
        if (!isEditable())
            return;
        JPopupMenu popup = new JPopupMenu();
        setBackgroundMenu(popup);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    
    protected void showMultiSelectPopUp(final MouseEvent event, Positionable p){
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Copy");
        if (p.isPositionable()) {
            setShowAlignmentMenu(p, popup);
        }
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                _multiItemCopyGroup = new ArrayList <Positionable>();
                _multiItemCopyGroup = _selectionGroup;
            }
        });
        setRemoveMenu(p, popup);
        popup.add(edit);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }
    public void putItem(Positionable l) {
        super.putItem(l);
        /*This allows us to catch any new items that are being pasted into the panel
        and add them to the selection group, so that the user can instantly move them around*/
        //!!!
        if (pasteItem){
            amendSelectionGroup(l);
        }
    }
    
    private void amendSelectionGroup(Positionable p){
        if (p==null) return;
        if (_selectionGroup==null){
            _selectionGroup = new ArrayList <Positionable>();
        }
        boolean removed = false;
        for(int i=0; i<_selectionGroup.size();i++){
            if (_selectionGroup.get(i)==p){
                _selectionGroup.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed)
            _selectionGroup.add(p);
        else if (removed && _selectionGroup.isEmpty())
            _selectionGroup=null;
        _targetPanel.repaint();
    }
    
    protected boolean pasteItem = false;
    
    protected void pasteItem(MouseEvent e){
        pasteItem = true;
        XmlAdapter adapter;
        String className;
        int x;
        int y;
        int xOrig;
        int yOrig;
        if (_multiItemCopyGroup!=null) {
            JComponent copied;
            int xoffset;
            int yoffset;
            x = _multiItemCopyGroup.get(0).getX();
            y = _multiItemCopyGroup.get(0).getY();
            xoffset=e.getX()-x;
            yoffset=e.getY()-y;
            for(int i = 0; i<_multiItemCopyGroup.size(); i++){
                copied = (JComponent)_multiItemCopyGroup.get(i);
                xOrig = copied.getX();
                yOrig = copied.getY();
                x = xOrig+xoffset;
                y = yOrig+yoffset;
                if (x<0) x=1;
                if (y<0) y=1;
                className=ConfigXmlManager.adapterName(copied);
                copied.setLocation(x, y);
                try{
                    adapter = (XmlAdapter)Class.forName(className).newInstance();
                    Element el = adapter.store(copied);
                    adapter.load(el, this);
                } catch (Exception ex) {
                    log.debug(ex);
                }
                copied.setLocation(xOrig, yOrig);
            }
        }
        pasteItem = false;
        _targetPanel.repaint();
    }

    /**
    * Add an action to remove the Positionable item.
    */
    public void setRemoveMenu(Positionable p, JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("Remove")) {
            Positionable comp;
            public void actionPerformed(ActionEvent e) { 
                if (_selectionGroup==null)
                    comp.remove();
                else
                    removeMultiItems();
            }
            AbstractAction init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
    }
    
    private void removeMultiItems(){
        boolean itemsInCopy = false;
        if (_selectionGroup==_multiItemCopyGroup){
            itemsInCopy=true;
        }
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable comp = _selectionGroup.get(i);
            comp.remove();
        }
        //As we have removed all the items from the panel we can remove the group.
        _selectionGroup = null;
        //If the items in the selection group and copy group are the same we need to
        //clear the copy group as the originals no longer exist.
        if (itemsInCopy)
            _multiItemCopyGroup = null;
    }
    
    public void setBackgroundMenu(JPopupMenu popup){
        JMenu edit = new JMenu(rb.getString("FontBackgroundColor"));
        makeColorMenu(edit);
        popup.add(edit);
    
    }
        
    protected void makeColorMenu(JMenu colorMenu) {
        ButtonGroup buttonGrp = new ButtonGroup();
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Black"), Color.black);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("DarkGray"),Color.darkGray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Gray"),Color.gray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("LightGray"),Color.lightGray);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("White"),Color.white);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Red"),Color.red);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Orange"),Color.orange);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Yellow"),Color.yellow);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Green"),Color.green);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Blue"),Color.blue);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Magenta"),Color.magenta);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Clear"), null);
    }
    
    protected void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
                           final String name, Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            Color desiredColor;
            public void actionPerformed(ActionEvent e) {
                if(desiredColor!=null)
                    setBackgroundColor(desiredColor);
                else
                    clearBackgroundColor();
            }
            ActionListener init (Color c) {
                desiredColor = c;
                return this;
            }
        }.init(color);
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        if (color==null) { color = defaultBackgroundColor; }
        setColorButton(getBackgroundColor(), color, r);
        colorButtonGroup.add(r);
        menu.add(r);
    }
    
    protected void setColorButton(Color color, Color buttonColor, JRadioButtonMenuItem r) {
        if (_debug)
            log.debug("setColorButton: color="+color+" (RGB= "+(color==null?"":color.getRGB())+
                      ") buttonColor= "+buttonColor+" (RGB= "+(buttonColor==null?"":buttonColor.getRGB())+")");
        if (buttonColor!=null) {
            if (color!=null && buttonColor.getRGB() == color.getRGB()) {
                 r.setSelected(true);
            } else r.setSelected(false);
        } else {
            if (color==null)  r.setSelected(true);
            else  r.setSelected(false);
        }
    }

   /******************************************************/

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PanelEditor.class.getName());
}
