/**
 * JMRIdemo.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version
 */

package apps.JmriDemo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class JMRIdemo extends JPanel {
	public JMRIdemo() {

        super(true);

	// create basic GUI
		setLayout(new BorderLayout());
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

    // load preferences
    	JmriDemoConfigAction prefs
    				= new JmriDemoConfigAction("Preferences...");

	// populate GUI
        // create text box for advice
        JLabel helpLabel1 = new JLabel();
		add(helpLabel1, BorderLayout.NORTH);
		helpLabel1.setText("Pick an input source from the input menu first");

        // Create menu categories and add to the menu bar, add actions to menus
        JMenu inputMenu = new JMenu("Input");
        menuBar.add(inputMenu);
        	inputMenu.add(new jmri.jmrix.loconet.hexfile.LnHexFileAction("Loconet Hex File"));
        	inputMenu.add(new jmri.jmrix.loconet.ms100.MS100Action("MS100"));
        	inputMenu.add(new jmri.jmrix.loconet.locobuffer.LocoBufferAction("LocoBuffer"));
        	inputMenu.add(new JSeparator());
        	inputMenu.add(new jmri.jmrix.nce.serialdriver.SerialDriverAction("NCE Serial"));
        	inputMenu.add(new JSeparator());
        	inputMenu.add(new AbstractAction("Quit"){
    				public void actionPerformed(ActionEvent e) {
    					System.exit(0);
    				}
        		});

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
	        editMenu.add(prefs);


        JMenu funcMenu = new JMenu("Tools");
        menuBar.add(funcMenu);
	        funcMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction("Simple Programmer"));
	        funcMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction("Decoder Pro programmer"));
	        funcMenu.add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction("Turnout Control"));
	        funcMenu.add(new jmri.jmrit.powerpanel.PowerPanelAction("Power Control"));

        JMenu locoMenu = new JMenu("LocoNet");
        menuBar.add(locoMenu);
	        locoMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
    	    locoMenu.add(new jmri.jmrix.loconet.slotmon.SlotMonAction("Slot Monitor"));
        	locoMenu.add(new jmri.jmrix.loconet.locogen.LocoGenAction("Send Packet"));

        	// temporarily remove this one
//        locoMenu.add(locoio = new jmri.jmrix.loconet.locoio.LocoIOAction("LocoIO programmer"));

        JMenu nceMenu = new JMenu("NCE");
        menuBar.add(nceMenu);
	        nceMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction("Command Monitor"));
	        nceMenu.add(new jmri.jmrix.nce.packetgen.NcePacketGenAction("Send Command"));

        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
	        devMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));
	        // devMenu.add(new jmri.jmrit.symbolicprog.symbolicframe.SymbolicProgAction("Symbolic Programmer"));
	        devMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
	        devMenu.add(new jmri.jmrit.decoderdefn.NameCheckAction("Check decoder names", this));
	        devMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction("Check programmer names", this));
	        devMenu.add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction("Create decoder index"));
	}

	// Main entry point
    public static void main(String s[]) {

    	// initialize log4j - from logging control file (lcf) only
    	// if can find it!
    	String logFile = "default.lcf";
    	try {
	    	if (new java.io.File(logFile).canRead()) {
	   	 		org.apache.log4j.PropertyConfigurator.configure("default.lcf");
	    	} else {
		    	org.apache.log4j.BasicConfigurator.configure();
	    	}
	    }
		catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

		log.info("JMRIdemo starts");

    	// create the demo frame and menus
        JMRIdemo containedPane = new JMRIdemo();
        JFrame frame = new JFrame("JMRI demo main panel");
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
		log.info("JMRIdemo main initialization done");

		// for debugging, start the LocoIO programmer always
		//locoio.actionPerformed(null);
    }

	static Action locoio = null;

	// GUI members
    private JMenuBar menuBar;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}

