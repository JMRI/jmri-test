package jmri.jmrit.display.panelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.util.JmriJFrame;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.*;

import jmri.jmrit.display.Editor;
import javax.swing.*;


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

    public PanelEditor(String name) {
        super(name);
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
					changeView("jmri.jmrit.display.controlPanelEditor.configurexml.ControlPanelEditorXml");
                }
            });

        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rb.getString("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (deletePanel() ) {
                        dispose();
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
            hiddenBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        setShowHidden(hiddenBox.isSelected());
                    }
                });                    
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
                    setAllPositionable(false);
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
        getTargetFrame().setVisible(true);
        if (_debug) log.debug("PanelEditor ctor done.");
    }  // end ctor

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
            JFrameItem frame = super.getIconFrame(item.getName());
            if (frame != null) {
                frame.getEditor().reset();
                frame.setVisible(true);
                _addIconBox.setSelectedIndex(-1);
            } else {
                log.error("Unable to open Icon Editor \""+item.getName()+"\"");
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
                        dispose();
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
    }
    /**
     * Set an object's location when it is created.
     */
    public void setNextLocation(Positionable obj) {
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
        JPopupMenu popup = new JPopupMenu();

        if (p.doPopupMenu()) {
            popup.add(p.getNameString());

            setPositionableMenu(p, popup);

            if (p.isPositionable()) {
                setShowCoordinatesMenu(p, popup);
                setShowAlignmentMenu(p, popup);
            }
            setDisplayLevelMenu(p, popup);

                 // items not common to all, but common to type
            if (p instanceof PositionableLabel ) {
                setHiddenMenu(p, popup);
                PositionableLabel pl = (PositionableLabel)p;
                if (pl.isIcon()) {
                    popup.addSeparator();
                    pl.setRotateOrthogonalMenu(popup);        
                    pl.setRotateMenu(popup);        
                    pl.setScaleMenu(popup);        
                    popup.addSeparator();
                    pl.setEditIconMenu(popup);        
                }
                if (pl.isText()) {
                    popup.addSeparator();
                    pl.setFixedTextMenu(popup);        
                    pl.setTextMarginMenu(popup);        
                    pl.setBackgroundFontColorMenu(popup);        
                    popup.addSeparator();
                    pl.setTextBorderMenu(popup);        
                    popup.addSeparator();
                    pl.setTextFontMenu(popup);
                    pl.setTextEditMenu(popup);
                    pl.setTextJustificationMenu(popup);
                }
                if (pl.isControl()) {
                    pl.setDisableControlMenu(popup);
                }
                setShowTooltipMenu(p, popup);
            } else if (p instanceof PositionableJComponent ) {
                p.setScaleMenu(popup);        
            } else if (p instanceof PositionableJPanel ) {
                p.setEditIconMenu(popup);        
            }
            // for Positionables with unique settings
            p.showPopUp(popup);

            setRemoveMenu(p, popup);
        } else {
            p.showPopUp(popup);
        }
        popup.show((Component)p, p.getWidth()/2, p.getHeight()/2);
    }
	
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PanelEditor.class.getName());
}
