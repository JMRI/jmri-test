// AppConfigPanel.java

package apps;

import jmri.GuiLafConfigPane;
import jmri.InstanceManager;
import jmri.jmrix.JmrixConfigPane;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.*;

import java.util.ArrayList;

/**
 * Basic configuration GUI infrastructure.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author      Matthew Harris copyright (c) 2009
 * @version	$Revision: 1.30 $
 */
public class AppConfigPanel extends JPanel {

    protected ResourceBundle rb;

    /**
     * Construct a configuration panel for inclusion in a preferences
     * or configuration dialog.
     * @param nConnections number of connections configured, e.g. the number of connection
     *      sub-panels included
     */
    public AppConfigPanel(int nConnections) {
        super();
        log.debug("start app");

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Communications
        log.debug("start comm");
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutConnection")));
        addAndRemember(p1);

        // Swing GUI LAF
        log.debug("start laf");
        super.add(p3 = new GuiLafConfigPane());
        // place at beginning of preferences list to avoid UI anomalies
        clist.add(0, p3);
        p3.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutGUI")));

        // default programmer configuration
        log.debug("start prog");
        jmri.jmrit.symbolicprog.ProgrammerConfigPane p4;
        addAndRemember(p4 = new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p4.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));

        // add button to show advanced section
        log.debug("start adv but");
        add(new JSeparator(JSeparator.HORIZONTAL));
        showAdvanced = new JCheckBox(rb.getString("ButtonShowAdv"));
        showAdvanced.setAlignmentX(1.f);
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.setAlignmentX(1.f);
        p5.add(showAdvanced);
        add(p5);

        // add advanced section itself
        log.debug("start adv");
        advScroll = new JPanel();
        advScroll.setLayout(new BoxLayout(advScroll, BoxLayout.Y_AXIS));
        advancedPane = new JPanel();
        JScrollPane js = new JScrollPane(advancedPane);
        advancedPane.setLayout(new BoxLayout(advancedPane, BoxLayout.Y_AXIS));
        advScroll.setVisible(false);  // have to click first
        advScroll.add(js);
        add(advScroll);
        showAdvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showAdvanced.isSelected()) {
                    if (!localeAdded) {
                        localeSpace.add(p3.doLocale());
                        localeAdded = true;
                    }
                    advScroll.setVisible(true);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
                else {
                    advScroll.setVisible(false);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
            }
        });

        // fill advanced section
        log.debug("start comm 2");
        if (nConnections > 1) {
            if (p2 == null) p2 = JmrixConfigPane.instance(2);
            p2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection2")));
            advancedPane.add(p2);
            clist.add(p2);
            if (p2a == null) p2a = JmrixConfigPane.instance(3);
            p2a.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection3")));
            advancedPane.add(p2a);
            clist.add(p2a);
            if (p2b == null) p2b = JmrixConfigPane.instance(4);
            p2b.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection4")));
            advancedPane.add(p2b);
            clist.add(p2b);
        }

        // add advanced programmer options
        JPanel advProgSpace = new JPanel();
        advProgSpace.add(p4.getAdvancedPanel());
        advProgSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));
        advancedPane.add(advProgSpace);

        // reserve space for Locale later
        log.debug("start res locale");
        localeSpace  = new JPanel();
        localeSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLocale")));
        localeAdded = false;
        advancedPane.add(localeSpace);

        log.debug("start act");
        PerformActionPanel action = new PerformActionPanel();
        action.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupActions")));
        advancedPane.add(action);
        clist.add(action);

        log.debug("start button");
        if (Apps.buttonSpace()!=null) {
            CreateButtonPanel buttons = new CreateButtonPanel();
            buttons.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutCreateButton")));
            advancedPane.add(buttons);
            clist.add(buttons);
        }

        log.debug("start file");
        PerformFilePanel files = new PerformFilePanel();
        files.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupFiles")));
        advancedPane.add(files);
        clist.add(files);

        log.debug("start scripts");
        PerformScriptPanel scripts = new PerformScriptPanel();
        scripts.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupScripts")));
        advancedPane.add(scripts);
        clist.add(scripts);

        // default roster location configuration
        log.debug("start roster");
        JPanel roster = new jmri.jmrit.roster.RosterConfigPane();
        roster.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRoster")));
        advancedPane.add(roster);
        clist.add(roster);

        // put the "Save" button at the bottom
        JButton save = new JButton(rb.getString("ButtonSave"));
        add(save);  // don't want to persist the button!
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });

    }

    JCheckBox showAdvanced;
    JPanel advScroll;
    JPanel advancedPane;

    JPanel localeSpace = null;
    boolean localeAdded = false;

    static JmrixConfigPane p1 = null;
    static JmrixConfigPane p2 = null;
    static JmrixConfigPane p2a = null;
    static JmrixConfigPane p2b = null;
    GuiLafConfigPane p3;
    private static String none = "(none)";

    public Component addAndRemember(Component c) {
        clist.add(c);
        super.add(c);
        return c;
    }

    public static String getConnection1() {
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        return p1.getCurrentProtocolName();
    }
    public static String getPort1() {
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        return p1.getCurrentProtocolInfo();
    }
    public static String getConnection2() {
        if (p2 == null) p2 = JmrixConfigPane.instance(2);
        if (p2 == null) return none;
        return p2.getCurrentProtocolName();
    }
    public static String getPort2() {
        if (p2 == null) p2 = JmrixConfigPane.instance(2);
        if (p2 == null) return none;
        return p2.getCurrentProtocolInfo();
    }
    public static String getConnection3() {
        if (p2a == null) p2a = JmrixConfigPane.instance(3);
        if (p2a == null) return none;
        return p2a.getCurrentProtocolName();
    }
    public static String getPort3() {
        if (p2a == null) p2a = JmrixConfigPane.instance(3);
        if (p2a == null) return none;
        return p2a.getCurrentProtocolInfo();
    }
    public static String getConnection4() {
        if (p2b == null) p2b = JmrixConfigPane.instance(4);
        if (p2b == null) return none;
        return p2b.getCurrentProtocolName();
    }
    public static String getPort4() {
        if (p2b == null) p2b = JmrixConfigPane.instance(4);
        if (p2b == null) return none;
        return p2b.getCurrentProtocolInfo();
    }

    /**
     * Remember items to persist
     */
    ArrayList<Component> clist = new ArrayList<Component>();

    public void dispose() {
        clist.clear();
    }

    protected void saveContents() {
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        for (int i = 0; i<clist.size(); i++) {
            InstanceManager.configureManagerInstance().registerPref(clist.get(i));
        }

        InstanceManager.configureManagerInstance().storePrefs();
    }
    
    /**
     * Detect duplicate connection types
     * It depends on all connections have the first word be the same
     * if they share the same type. So LocoNet ... is a fine example.
     * @return true if OK, false if duplicates present.
     */
    private boolean checkDups() {
    	String c1 = getConnection1();
    	int x = c1.indexOf(" ");
    	if (x > 0) c1 = c1.substring(0, x);
    	String p1 = getPort1();
    	
    	String c2 = getConnection2();
    	x = c2.indexOf(" ");
    	if (x > 0) c2 = c2.substring(0, x);
    	String p2 = getPort2();
    	
    	String c3 = getConnection3();
    	x = c3.indexOf(" ");
    	if (x > 0) c3 = c3.substring(0, x);
    	String p3 = getPort3();
    	
    	String c4 = getConnection4();
    	x = c4.indexOf(" ");
    	if (x > 0) c4 = c4.substring(0, x);
    	String p4 = getPort4();
    	    	
    	if (c1.compareToIgnoreCase(none) != 0) {
    		if (c1.compareToIgnoreCase(c2) == 0) return false;
    		if (c1.compareToIgnoreCase(c3) == 0) return false;
    		if (c1.compareToIgnoreCase(c4) == 0) return false;
    	}
    	if (p1.compareToIgnoreCase(none) != 0) {
    		if (p1.compareToIgnoreCase(p2) == 0) return false;
    		if (p1.compareToIgnoreCase(p3) == 0) return false;
    		if (p1.compareToIgnoreCase(p4) == 0) return false;
    	}
    	
    	if (c2.compareToIgnoreCase(none) != 0) {
    		if (c2.compareToIgnoreCase(c1) == 0) return false;
    		if (c2.compareToIgnoreCase(c3) == 0) return false;
    		if (c2.compareToIgnoreCase(c4) == 0) return false;
    	}
    	if (p2.compareToIgnoreCase(none) != 0) {
    		if (p2.compareToIgnoreCase(p1) == 0) return false;
    		if (p2.compareToIgnoreCase(p3) == 0) return false;
    		if (p2.compareToIgnoreCase(p4) == 0) return false;
    	}
    	
    	if (c3.compareToIgnoreCase(none) != 0) {
    		if (c3.compareToIgnoreCase(c1) == 0) return false;
    		if (c3.compareToIgnoreCase(c2) == 0) return false;
    		if (c3.compareToIgnoreCase(c4) == 0) return false;
    	}
    	if (p3.compareToIgnoreCase(none) != 0) {
    		if (p3.compareToIgnoreCase(p1) == 0) return false;
    		if (p3.compareToIgnoreCase(p2) == 0) return false;
    		if (p3.compareToIgnoreCase(p4) == 0) return false;
    	}
    	
    	if (c4.compareToIgnoreCase(none) != 0) {
    		if (c4.compareToIgnoreCase(c1) == 0) return false;
    		if (c4.compareToIgnoreCase(c2) == 0) return false;
    		if (c4.compareToIgnoreCase(c3) == 0) return false;
    	}
    	if (p4.compareToIgnoreCase(none) != 0) {
    		if (p4.compareToIgnoreCase(p1) == 0) return false;
    		if (p4.compareToIgnoreCase(p2) == 0) return false;
    		if (p4.compareToIgnoreCase(p3) == 0) return false;
    	}
    	return true;
    }
    
    /**
     * Checks to see if user selected a valid serial port
     * @return true if okay
     */
    private boolean checkPortName(){
    	if (getPort1().equals(JmrixConfigPane.NONE_SELECTED) || getPort1().equals(JmrixConfigPane.NO_PORTS_FOUND))
    		return false;
    	return true;
    }

    /**
     * Handle the Save button:  Backup the file, write a new one, prompt for
     * what to do next.  To do that, the last step is to present a dialog
     * box prompting the user to end the program.
     */
    public void savePressed() {
    	if(!checkPortName()){
    		if (JOptionPane.showConfirmDialog(null,
    				MessageFormat.format(rb.getString("MessageSerialPortWarning"),new Object[]{getPort1()}),
                    rb.getString("MessageSerialPortNotValid"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION)
    			return;
    	}
    	
    	boolean dups = checkDups(); // true if OK, which is a little confusing
    	if (!dups) {
    		dups = JOptionPane.showConfirmDialog(null,
                    rb.getString("MessageLongDupsWarning"),
                    rb.getString("MessageShortDupsWarning"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    		if (!dups)
    			return;	//leave window open
    	}
    	if (dups) {
	        saveContents();
            
            final jmri.managers.DefaultUserMessagePreferences p;
            p = jmri.managers.DefaultUserMessagePreferences.instance();
            p.resetChangeMade();
            if(p.getQuitAfterSave()==0x00){
                final JDialog dialog = new JDialog();
                dialog.setTitle(rb.getString("MessageShortQuitWarning"));
                dialog.setLocationRelativeTo(null);
                dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JLabel question = new JLabel(rb.getString("MessageLongQuitWarning"));
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(question);
                final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                remember.setFont(remember.getFont().deriveFont(10f));
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                //user preferences do not have the save option, but once complete the following line can be removed
                //Need to get the method to save connection configuration.
                JButton yesButton = new JButton("Yes");
                JButton noButton = new JButton("No");
                JPanel button = new JPanel();
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.add(yesButton);
                button.add(noButton);
                container.add(button);
                
                noButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if(remember.isSelected()){
                            p.setQuitAfterSave(0x01);
                        }
                        dialog.dispose();
                    }
                });
                
                yesButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if(remember.isSelected()) {
                            p.setQuitAfterSave(0x02);
                            saveContents();
                        }
                        dialog.dispose();
                        // end the program
                        dispose();
                        
                        
                        // do orderly shutdown.  Note this
                        // invokes Apps.handleQuit, even if this 
                        // panel hasn't been created by an Apps subclass.
                        Apps.handleQuit();
                    }
                });
                container.add(remember);
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);
            } else if (p.getQuitAfterSave()==0x02) {
	            // end the program
	            dispose();
	            
	            // do orderly shutdown.  Note this
	            // invokes Apps.handleQuit, even if this 
	            // panel hasn't been created by an Apps subclass.
	            Apps.handleQuit();
            
            }

	        /*if (JOptionPane.showConfirmDialog(null,
	                                          rb.getString("MessageLongQuitWarning"),
	                                          rb.getString("MessageShortQuitWarning"),
	                                          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
	
	            // end the program
	            dispose();
	            
	            
	            // do orderly shutdown.  Note this
	            // invokes Apps.handleQuit, even if this 
	            // panel hasn't been created by an Apps subclass.
	            Apps.handleQuit();

	        }*/
    	}
        // don't end the program, just close the window
        if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).setVisible(false);
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppConfigPanel.class.getName());

}
