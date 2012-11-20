package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.iharder.dnd.FileDrop.Listener;

import org.jdom.Element;

// Should be named ThrottleFrame, but ThrottleFrame already exit, hence ThrottleWindow
public class ThrottleWindow extends JmriJFrame {
	private static final ResourceBundle throttleBundle = ThrottleBundle.bundle();

    private JPanel throttlesPanel;
    private ThrottleFrame currentThrottleFrame;
    private CardLayout throttlesLayout;
    
    private JCheckBoxMenuItem viewControlPanel;
    private JCheckBoxMenuItem viewFunctionPanel;
    private JCheckBoxMenuItem viewAddressPanel;
    private JMenuItem viewAllButtons;
    private JMenuItem fileMenuSave;
    private JMenuItem editMenuExportRoster;
    
    private JButton jbPrevious = null;
    private JButton jbNext = null;
    private JButton jbPreviousRunning = null;
    private JButton jbNextRunning = null;
    private JButton jbThrottleList = null;
    private JButton jbNew = null;
    private JButton jbClose = null;
    private JButton jbMode = null;
    private JToolBar throttleToolBar;
    
    private String titleText = "";
    private String titleTextType = "rosterID";
   
    private PowerManager powerMgr = null;
    
    private ThrottlePanelCyclingKeyListener throttlePanelsCyclingKeyListener;
	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;
	
	private HashMap<String, ThrottleFrame> throttleFrames = new HashMap<String, ThrottleFrame>(5);
	private int cardCounterID = 0; // to generate unique names for each card
	private int cardCounterNB = 1; // real counter
	
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	
    /**
     *  Default constructor
     */
    public ThrottleWindow()
    {
        super();
        throttlePanelsCyclingKeyListener = new ThrottlePanelCyclingKeyListener();
    	powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null)
            log.info("No power manager instance found, panel not active");
        initGUI();
    }
    
    private void initGUI()
    {
        setTitle(throttleBundle.getString("ThrottleFrameTitle"));
        setLayout(new BorderLayout());
        throttlesLayout = new CardLayout();
        throttlesPanel = new JPanel(throttlesLayout);
        throttlesPanel.setDoubleBuffered(true);
        if ( (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
        	&& ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingToolBar()))
        	initializeToolbar();
/*        if ( (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
        		&& ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isResizingWindow()))
        	setResizable(false);*/
        initializeMenu();
        
        setCurrentThrottleFrame ( new ThrottleFrame(this) );
        getCurrentThrottleFrame().setTitle("default");
        throttlesPanel.add(getCurrentThrottleFrame(),"default");
        throttleFrames.put("default",getCurrentThrottleFrame());
        add(throttlesPanel,BorderLayout.CENTER);
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, getCurrentThrottleFrame());

        this.addWindowListener(	new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		ThrottleWindow me = (ThrottleWindow) e.getSource();
        		ThrottleFrameManager.instance().requestThrottleWindowDestruction(me);
        		if (throttleToolBar != null) {
        			Component[] cmps = throttleToolBar.getComponents();
        			if (cmps != null)
        				for (int i=0; i<cmps.length; i++)
        					if (cmps[i] instanceof Jynstrument)
        						((Jynstrument)cmps[i]).exit();
        		}
        	}
        });
        updateGUI();
    }
    
    public void updateGUI() {
    	// title bar
    	getCurrentThrottleFrame().setFrameTitle();
    	// menu items
    	viewAddressPanel.setEnabled(isEditMode);
    	viewControlPanel.setEnabled(isEditMode);
    	viewFunctionPanel.setEnabled(isEditMode);
    	if (isEditMode) {
    		viewAddressPanel.setSelected( getCurrentThrottleFrame().getAddressPanel().isVisible() );
    		viewControlPanel.setSelected( getCurrentThrottleFrame().getControlPanel().isVisible() );
    		viewFunctionPanel.setSelected( getCurrentThrottleFrame().getFunctionPanel().isVisible() );
    	}
		fileMenuSave.setEnabled( getCurrentThrottleFrame().getLastUsedSaveFile() != null || getCurrentThrottleFrame().getRosterEntry() != null);
		editMenuExportRoster.setEnabled( getCurrentThrottleFrame().getRosterEntry() != null);
    	// toolbar items
    	if (jbPrevious != null) // means toolbar enabled
    		if ( cardCounterNB > 1 ) {
    			jbPrevious.setEnabled( true );
    			jbNext.setEnabled( true );
    			jbClose.setEnabled( true );
    			jbPreviousRunning.setEnabled( true );
    			jbNextRunning.setEnabled( true );
    		}
    		else {
    			jbPrevious.setEnabled( false );
    			jbNext.setEnabled( false );
    			jbClose.setEnabled( false );
    			jbPreviousRunning.setEnabled( false );
    			jbNextRunning.setEnabled( false );
    		}
    }
    
    private void initializeToolbar()
    {
    	throttleToolBar = new JToolBar("Throttles toolbar");
    	
    	jbNew = new JButton();
 //   	nouveau.setText(throttleBundle.getString("ThrottleToolBarNew"));
    	jbNew.setIcon(new NamedIcon("resources/icons/throttles/add.png","resources/icons/throttles/add.png"));
    	jbNew.setToolTipText(throttleBundle.getString("ThrottleToolBarNewToolTip"));
    	jbNew.setVerticalTextPosition(JButton.BOTTOM);
    	jbNew.setHorizontalTextPosition(JButton.CENTER);
    	jbNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addThrottleFrame();
			}
		});
    	throttleToolBar.add(jbNew);
    	
    	jbClose = new JButton();
//    	close.setText(throttleBundle.getString("ThrottleToolBarClose"));
    	jbClose.setIcon(new NamedIcon("resources/icons/throttles/remove.png","resources/icons/throttles/remove.png"));
    	jbClose.setToolTipText(throttleBundle.getString("ThrottleToolBarCloseToolTip"));
    	jbClose.setVerticalTextPosition(JButton.BOTTOM);
    	jbClose.setHorizontalTextPosition(JButton.CENTER);
    	jbClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeThrottleFrame();
			}
		});
    	throttleToolBar.add(jbClose);
    	
    	throttleToolBar.addSeparator();

       	jbPreviousRunning = new JButton();
       	//   	previous.setText(throttleBundle.getString("ThrottleToolBarPrev"));
       	jbPreviousRunning.setIcon(new NamedIcon("resources/icons/throttles/previous-jump.png","resources/icons/throttles/previous-jump.png"));
       	jbPreviousRunning.setVerticalTextPosition(JButton.BOTTOM);
       	jbPreviousRunning.setHorizontalTextPosition(JButton.CENTER);
       	jbPreviousRunning.setToolTipText(throttleBundle.getString("ThrottleToolBarPrevRunToolTip"));
       	jbPreviousRunning.addActionListener(new ActionListener() {
       		public void actionPerformed(ActionEvent e) {
       			previousRunningThrottleFrame();
       		}
       	});
       	throttleToolBar.add(jbPreviousRunning);
       	
    	jbPrevious = new JButton();
 //   	previous.setText(throttleBundle.getString("ThrottleToolBarPrev"));
    	jbPrevious.setIcon(new NamedIcon("resources/icons/throttles/previous.png","resources/icons/throttles/previous.png"));
    	jbPrevious.setVerticalTextPosition(JButton.BOTTOM);
    	jbPrevious.setHorizontalTextPosition(JButton.CENTER);
    	jbPrevious.setToolTipText(throttleBundle.getString("ThrottleToolBarPrevToolTip"));
    	jbPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousThrottleFrame();
			}
		});
    	throttleToolBar.add(jbPrevious);
    	
    	jbNext = new JButton();
 //   	next.setText(throttleBundle.getString("ThrottleToolBarNext"));
    	jbNext.setIcon(new NamedIcon("resources/icons/throttles/next.png","resources/icons/throttles/next.png"));
    	jbNext.setToolTipText(throttleBundle.getString("ThrottleToolBarNextToolTip"));
    	jbNext.setVerticalTextPosition(JButton.BOTTOM);
    	jbNext.setHorizontalTextPosition(JButton.CENTER);
    	jbNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextThrottleFrame();
			}
		});
    	throttleToolBar.add(jbNext);

//       	throttleToolBar.addSeparator();


       	jbNextRunning = new JButton();
       	//   	next.setText(throttleBundle.getString("ThrottleToolBarNext"));
       	jbNextRunning.setIcon(new NamedIcon("resources/icons/throttles/next-jump.png","resources/icons/throttles/next-jump.png"));
       	jbNextRunning.setToolTipText(throttleBundle.getString("ThrottleToolBarNextRunToolTip"));
       	jbNextRunning.setVerticalTextPosition(JButton.BOTTOM);
       	jbNextRunning.setHorizontalTextPosition(JButton.CENTER);
       	jbNextRunning.addActionListener(new ActionListener() {
       		public void actionPerformed(ActionEvent e) {
       			nextRunningThrottleFrame();
       		}
       	});
       	throttleToolBar.add(jbNextRunning);
       	
    	throttleToolBar.addSeparator();
    	
    	throttleToolBar.add(new StopAllButton());
    	
		if (powerMgr != null)
			throttleToolBar.add(new LargePowerManagerButton());
		
    	throttleToolBar.addSeparator();

    	jbMode = new JButton();
//    	jbMode.setText(throttleBundle.getString("ThrottleToolBarEdit"));
    	jbMode.setIcon(new NamedIcon("resources/icons/throttles/edit-view.png","resources/icons/throttles/edit-view.png"));
    	jbMode.setToolTipText(throttleBundle.getString("ThrottleToolBarEditToolTip"));
    	jbMode.setVerticalTextPosition(JButton.BOTTOM);
    	jbMode.setHorizontalTextPosition(JButton.CENTER);
    	jbMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchMode();
			}
		});
    	throttleToolBar.add(jbMode);
    	
    	throttleToolBar.addSeparator();

    	jbThrottleList = new JButton();
 //   	stop.setText(throttleBundle.getString("ThrottleToolBarOpenThrottleList"));
    	jbThrottleList.setIcon(new NamedIcon("resources/icons/throttles/list.png","resources/icons/throttles/list.png"));
    	jbThrottleList.setToolTipText(throttleBundle.getString("ThrottleToolBarOpenThrottleListToolTip"));
    	jbThrottleList.setVerticalTextPosition(JButton.BOTTOM);
    	jbThrottleList.setHorizontalTextPosition(JButton.CENTER);
    	jbThrottleList.addActionListener(new ThrottlesListAction() );
    	throttleToolBar.add(jbThrottleList);

       	// Receptacle for Jynstruments
    	new FileDrop(throttleToolBar, new Listener() {
    		public void filesDropped(File[] files) {
        		for (int i=0; i<files.length; i++)
        			ynstrument(files[i].getPath());
        		}
    	});

    	add(throttleToolBar, BorderLayout.PAGE_START);
    }

    private boolean isEditMode = true;
    private void switchMode() {
    	isEditMode = ! isEditMode;
    	if (! throttleFrames.isEmpty() )
    		for (Iterator<ThrottleFrame> tfi = throttleFrames.values().iterator(); tfi.hasNext(); ) {
    			tfi.next().switchMode();
    		}
        updateGUI();
    }
    
    public Jynstrument ynstrument(String path) {
    	Jynstrument it = JynstrumentFactory.createInstrument(path, this);
    	if (it == null) {
    		log.error("Error while creating Jynstrument "+path);
    		return null;
    	}
    	ThrottleFrame.setTransparent(it, true);
    	it.setVisible(true);
    	throttleToolBar.add(it);
    	throttleToolBar.repaint();
    	return it;
    }
    
    /**
     *  Set up View, Edit and Power Menus
     */
    private void initializeMenu() {
        JMenu fileMenu = new JMenu(throttleBundle.getString("ThrottleFileMenu"));

        JMenuItem fileMenuLoad = new JMenuItem(throttleBundle.getString("ThrottleFileMenuLoadThrottle"));
        fileMenuLoad.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().loadThrottle(null);
            }
        });
        fileMenuSave = new JMenuItem(throttleBundle.getString("ThrottleFileMenuSaveThrottle"));
        fileMenuSave.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().saveThrottle();
            }
        });
        JMenuItem fileMenuSaveAs = new JMenuItem(throttleBundle.getString("ThrottleFileMenuSaveAsThrottle"));
        fileMenuSaveAs.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().saveThrottleAs();
            }
        });
        
        
        
        fileMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(throttleBundle.getString("MenuItemNewThrottle")));
        fileMenu.add(fileMenuLoad);
        fileMenu.add(fileMenuSave);
        fileMenu.add(fileMenuSaveAs);
        fileMenu.addSeparator();
        
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");
        fileMenu.add(new jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction(rb.getString("MenuItemLoadThrottleLayout")));
        fileMenu.add(new jmri.jmrit.throttle.StoreXmlThrottlesLayoutAction(rb.getString("MenuItemSaveThrottleLayout" )));
        fileMenu.addSeparator();
        fileMenu.add(new jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction(rb.getString("MenuItemLoadDefaultThrottleLayout" )));
        fileMenu.add(new jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction(rb.getString("MenuItemSaveAsDefaultThrottleLayout" )));
        fileMenu.addSeparator();
        fileMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction(rb.getString("MenuItemStartWiThrottle")));

        JMenu viewMenu = new JMenu(throttleBundle.getString("ThrottleMenuView"));
        viewAddressPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewAddressPanel"));
        viewAddressPanel.setSelected(true);
        viewAddressPanel.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                getCurrentThrottleFrame().getAddressPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        viewControlPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewControlPanel"));
        viewControlPanel.setSelected(true);
        viewControlPanel.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                getCurrentThrottleFrame().getControlPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        viewFunctionPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewFunctionPanel"));
        viewFunctionPanel.setSelected(true);
        viewFunctionPanel.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                getCurrentThrottleFrame().getFunctionPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        viewAllButtons = new JMenuItem(throttleBundle.getString("ThrottleMenuViewAllFunctionButtons"));
        viewAllButtons.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent ev) {
                getCurrentThrottleFrame().getFunctionPanel().resetFnButtons();
                getCurrentThrottleFrame().getFunctionPanel().setEnabled();
            }
        });

        JMenuItem makeAllComponentsInBounds = new JMenuItem(throttleBundle.getString("ThrottleMenuViewMakeAllComponentsInBounds"));
        makeAllComponentsInBounds.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent ev) {
                getCurrentThrottleFrame().makeAllComponentsInBounds();
            }
        });

        JMenuItem switchViewMode = new JMenuItem(throttleBundle.getString("ThrottleMenuViewSwitchMode"));
        switchViewMode.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent ev) {
                switchMode();
            }
        });
        JMenuItem viewThrottlesList = new JMenuItem(throttleBundle.getString("ThrottleMenuViewViewThrottleList"));
        viewThrottlesList.addActionListener(new ThrottlesListAction());

        viewMenu.add(viewAddressPanel);
        viewMenu.add(viewControlPanel);
        viewMenu.add(viewFunctionPanel);
        viewMenu.addSeparator();
        viewMenu.add(viewAllButtons);
        viewMenu.add(makeAllComponentsInBounds);
        viewMenu.addSeparator();
        viewMenu.add(switchViewMode);
        viewMenu.add(viewThrottlesList);

        JMenu editMenu = new JMenu(throttleBundle.getString("ThrottleMenuEdit"));
        JMenuItem preferencesItem = new JMenuItem(throttleBundle.getString("ThrottleMenuEditFrameProperties"));
        editMenu.add(preferencesItem);
        preferencesItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editPreferences();
            }
        });
        editMenuExportRoster = new JMenuItem(throttleBundle.getString("ThrottleMenuEditSaveCustoms"));
        editMenu.add(editMenuExportRoster);
        editMenuExportRoster.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().saveRosterChanges();
            }
        });
        editMenu.addSeparator();
        editMenu.add(new jmri.jmrit.throttle.ThrottlesPreferencesAction(rb.getString("MenuItemThrottlesPreferences"))); // now in tabbed preferences

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
        this.getJMenuBar().add(editMenu);
        this.getJMenuBar().add(viewMenu);

        if (powerMgr != null) {
            JMenu powerMenu = new JMenu(throttleBundle.getString("ThrottleMenuPower"));
            JMenuItem powerOn = new JMenuItem(throttleBundle.getString("ThrottleMenuPowerOn"));
            powerMenu.add(powerOn);
            powerOn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        powerMgr.setPower(PowerManager.ON);
                    } catch (JmriException e1) {
                        log.error("Error when setting power " + e1);
                    }
                }
            });

            JMenuItem powerOff = new JMenuItem(throttleBundle.getString("ThrottleMenuPowerOff"));
            powerMenu.add(powerOff);
            powerOff.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        powerMgr.setPower(PowerManager.OFF);
                    } catch (JmriException e1) {
                        log.error("Error when setting power " + e1);
                    }
                }
            });

            this.getJMenuBar().add(powerMenu);

            if ((!jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle())
                    || (!jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingToolBar())) {
                this.getJMenuBar().add(new SmallPowerManagerButton());
            }
        }

        // add help selection
        addHelpMenu("package.jmri.jmrit.throttle.ThrottleFrame", true);
    }
        
    private void editPreferences(){
        ThrottleFramePropertyEditor editor = new ThrottleFramePropertyEditor();
        editor.setThrottleFrame(this);
        editor.setLocation(this.getLocationOnScreen());
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
    }
    
    /**
	 * Handle my own destruction.
	 * <ol>
	 * <li> dispose of sub windows.
	 * <li> notify my manager of my demise.
	 * </ol>
	 * 
	 */
    public void dispose()
    {
    	if ((throttleFrames!= null) && (! throttleFrames.isEmpty() ))
    		for (Iterator<ThrottleFrame> tfi = throttleFrames.values().iterator(); tfi.hasNext(); ) {
    			tfi.next().dispose();
    		}
    	throttleFrames = null;
        throttlesPanel.removeAll();
        removeAll();
        super.dispose();
    }
    
	public JCheckBoxMenuItem getViewControlPanel() {
		return viewControlPanel;
	}

	public JCheckBoxMenuItem getViewFunctionPanel() {
		return viewFunctionPanel;
	}

	public JCheckBoxMenuItem getViewAddressPanel() {
		return viewAddressPanel;
	}
	
	public ThrottleFrame getCurrentThrottleFrame() {
		return currentThrottleFrame;
	}

	public void setCurrentThrottleFrame(ThrottleFrame tf) {
		if (getCurrentThrottleFrame() != null)
			log.debug("setCurrentThrottleFrame from "+ getCurrentThrottleFrame().getAddressPanel().getCurrentAddress()+" to "+ tf.getAddressPanel().getCurrentAddress());
		pcs.firePropertyChange("ThrottleFrame", getCurrentThrottleFrame(), tf);
		currentThrottleFrame = tf;
	}
	
	public void removeThrottleFrame(ThrottleFrame tf) {
		if ( cardCounterNB > 1 ) // we don't like empty ThrottleWindow
		{
			cardCounterNB--;
			if (getCurrentThrottleFrame() == tf)	{
				log.debug("Closing last created");
			}
			throttlesPanel.remove( tf );
			throttleFrames.remove( tf.getTitle() );
			tf.dispose();
			throttlesLayout.invalidateLayout(throttlesPanel);
		}
		updateGUI();
	}
	
	public void nextThrottleFrame() {
		throttlesLayout.next(throttlesPanel);
		updateGUI();
	}
	
	public void previousThrottleFrame() {
		throttlesLayout.previous(throttlesPanel);
		updateGUI();
	}
	
	public void previousRunningThrottleFrame() {
        if (! throttleFrames.isEmpty() ) {
        	ThrottleFrame cf = this.getCurrentThrottleFrame();
        	ThrottleFrame nf = null;
        	boolean passed = false;
        	for (Iterator<ThrottleFrame> tfi = throttleFrames.values().iterator(); tfi.hasNext(); ) {
        		ThrottleFrame tf = tfi.next();
        		if (tf != cf) {
	        		if ((tf.getAddressPanel() != null) && (tf.getAddressPanel().getThrottle() != null) && (tf.getAddressPanel().getThrottle().getSpeedSetting() > 0))
	        			if (passed) { // if we found something and passed current value, then break
	        				nf = tf;
	        				break;
	        			} 
	        			else if (nf == null)
	        				nf = tf;
        		}
	        	else
	        		passed = true;
        	}
        	if (nf != null) 
        		nf.toFront();
        }
	}
	
	public void nextRunningThrottleFrame() {
        if (! throttleFrames.isEmpty() ) {
        	ThrottleFrame cf = this.getCurrentThrottleFrame();
        	ThrottleFrame nf = null;
        	for (Iterator<ThrottleFrame> tfi = throttleFrames.values().iterator(); tfi.hasNext(); ) {
        		ThrottleFrame tf = tfi.next();
        		if ((tf!=cf) && (tf.getAddressPanel() != null) && (tf.getAddressPanel().getThrottle() != null) && (tf.getAddressPanel().getThrottle().getSpeedSetting() > 0))
        			nf = tf;
        		if ((tf==cf) && (nf != null)) // if we found something, then break, else go to end
        			break;
        	}
        	if (nf != null)
        		nf.toFront();
        }
	}
	
	public void removeThrottleFrame() {
		removeThrottleFrame( getCurrentThrottleFrame() );
	}

	public void addThrottleFrame(ThrottleFrame tp) {
		cardCounterID++; cardCounterNB++;
		String txt = "Card-"+cardCounterID;
		tp.setTitle(txt);
		throttleFrames.put(txt,tp);
        throttlesPanel.add(tp,txt);
        throttlesLayout.show(throttlesPanel, txt);
        if (! isEditMode)
        	tp.switchMode();
		updateGUI();
	}
	
	public ThrottleFrame addThrottleFrame() {
		setCurrentThrottleFrame ( new ThrottleFrame(this) );
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, getCurrentThrottleFrame() );
		addThrottleFrame(getCurrentThrottleFrame());
		return getCurrentThrottleFrame();
	}

	public void toFront(String throttleFrameTitle) {
		throttlesLayout.show(throttlesPanel, throttleFrameTitle);
		setVisible(true);
		requestFocus();
		toFront();
	}

	public String getTitleTextType() {
		return titleTextType;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public void setTitleTextType(String titleTextType) {
		this.titleTextType = titleTextType;
	}
	
	public Element getXml() { 	
		Element me  = new Element("ThrottleWindow");
        me.setAttribute("title", titleText);
        me.setAttribute("titleType", titleTextType);	
        
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);        
        children.add(WindowPreferences.getPreferences(this));
        if (! throttleFrames.isEmpty() ) {
        	ThrottleFrame cf = this.getCurrentThrottleFrame();
        	for (Iterator<ThrottleFrame> tfi = throttleFrames.values().iterator(); tfi.hasNext(); ) {
        		ThrottleFrame tf = tfi.next();
        		if ((jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()) && (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isSavingThrottleOnLayoutSave())) {
        			tf.toFront();
        			tf.saveThrottle();
        		}
        		Element tfe = tf.getXmlFile();
        		if (tfe == null)
        			tfe =  tf.getXml();
        		children.add( tfe ); 
        	}
        	if (cf != null)
        		cf.toFront();
        }
        
        // Save Jynstruments
        if (throttleToolBar!=null){
		Component[] cmps = throttleToolBar.getComponents();
		if (cmps != null)
			for (int i=0; i<cmps.length; i++) {
				try {
					if (cmps[i] instanceof Jynstrument) {
						Jynstrument jyn = (Jynstrument) cmps[i];
						Element elt = new Element("Jynstrument");
						elt.setAttribute("JynstrumentFolder", FileUtil.getPortableFilename( jyn.getFolder() )); 
						Element je = jyn.getXml();
						if (je != null) {
							java.util.ArrayList<Element> jychildren = new java.util.ArrayList<Element>(1);
							jychildren.add( je );
							elt.setContent(jychildren);
						}
						children.add(elt);
					}

				} catch (Exception ex) {
					log.debug("Got exception (no panic) "+ex);
				}
			}
        }
        me.setContent(children);        
        return me;
	}

	@SuppressWarnings("unchecked")
	public void setXml(Element e) {
		if (e.getAttribute("title") != null)
			setTitle(e.getAttribute("title").getValue());
		if (e.getAttribute("title") != null)
			setTitleText ( e.getAttribute("title").getValue() );
		if (e.getAttribute("titleType") != null)
			setTitleTextType ( e.getAttribute("titleType").getValue()) ;
        
        Element window = e.getChild("window");
        if (window != null)
        	WindowPreferences.setPreferences(this, window);
        
        List<Element> tfes = e.getChildren("ThrottleFrame");
        if ((tfes != null) && (tfes.size()>0))
        	for (int i=0; i<tfes.size(); i++) {
        		ThrottleFrame tf;
        		if (i == 0)
        			tf = getCurrentThrottleFrame();
        		else
        			tf = addThrottleFrame();
        		tf.setXml(tfes.get(i));
        	}
        
		List<Element> jinsts = e.getChildren("Jynstrument");
        if ((jinsts != null) && (jinsts.size()>0)) {
        	for (int i=0; i<jinsts.size(); i++) {
        		Jynstrument jyn = ynstrument( FileUtil.getExternalFilename( jinsts.get(i).getAttributeValue("JynstrumentFolder")) );
                if ((jyn != null) && (jinsts.get(i)!=null))
                	jyn.setXml(jinsts.get(i));
        	}
        }
        
        updateGUI();
	}
	
	/**
	 * A KeyAdapter that listens for the key that cycles through the
	 * ThrottlePanels.
	 */
	class ThrottlePanelCyclingKeyListener extends KeyAdapter {
		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void keyReleased(KeyEvent e) {
			if (e.isAltDown() && e.getKeyCode() == NEXT_THROTTLE_KEY) {
				log.debug("next");
				nextThrottleFrame();                    
			}
			else if (e.isAltDown() && e.getKeyCode() == PREV_THROTTLE_KEY) {
				log.debug("previous");
				previousThrottleFrame();
			}
		}
	}

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleWindow.class.getName());
}
