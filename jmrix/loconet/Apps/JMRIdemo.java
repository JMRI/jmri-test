/** 
 * JMRIdemo.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

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
        
	// populate GUI
        // create text box for advice
        JLabel helpLabel1 = new JLabel();
		add(helpLabel1, BorderLayout.NORTH);     
		helpLabel1.setText("Pick an input source from the left menu first");
			
        // Create menu categories and add to the menu bar, add actions to menus
        JMenu inputMenu = new JMenu("Input");
        menuBar.add(inputMenu);
        	inputMenu.add(new jmri.jmrix.loconet.hexfile.LnHexFileAction("Loconet Hex File"));
        	inputMenu.add(new jmri.jmrix.loconet.ms100.MS100Action("MS100"));
        	inputMenu.add(new jmri.jmrix.loconet.locobuffer.LocoBufferAction("LocoBuffer"));
        	inputMenu.add(new JSeparator());
        	inputMenu.add(new jmri.jmrix.nce.serialdriver.SerialDriverAction("NCE Serial"));

        JMenu funcMenu = new JMenu("Tools");
        menuBar.add(funcMenu);
	        funcMenu.add(new jmri.simpleprog.SimpleProgAction("Simple Programmer"));
	        funcMenu.add(new jmri.symbolicprog.SymbolicProgAction("Symbolic Programmer"));
	        funcMenu.add(new jmri.jmrix.loconet.locoecho.LocoEchoAction("Turnout Control"));
	        funcMenu.add(new jmri.jmrit.powerpanel.PowerPanelAction("Power Control"));

        JMenu locoMenu = new JMenu("LocoNet");
        menuBar.add(locoMenu);
	        locoMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
    	    locoMenu.add(new jmri.jmrix.loconet.slotmon.SlotMonAction("Slot Monitor"));
        	locoMenu.add(new jmri.jmrix.loconet.locogen.LocoGenAction("Send Packet"));

        JMenu nceMenu = new JMenu("NCE");
        menuBar.add(nceMenu);
	        nceMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction("Command Monitor"));
	        nceMenu.add(new jmri.jmrix.nce.packetgen.NcePacketGenAction("Send Command"));
	}

	// Main entry point
    public static void main(String s[]) {
    
    	// initialize log4j - from logging control file (lcf) only 
    	// if can find it!
    	String logFile = "default.lcf";
    	if (new java.io.File(logFile).canRead()) {
    		org.apache.log4j.PropertyConfigurator.configure("default.lcf");
    	} else {
	    	org.apache.log4j.BasicConfigurator.configure();
    	}
    		
    	// create the demo frame and menus
        JMRIdemo containedPane = new JMRIdemo();
        JFrame frame = new JFrame("JMRI demo main panel");
        frame.addWindowListener(new BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
    }
	
	// GUI members
    private JMenuBar menuBar;	
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}

