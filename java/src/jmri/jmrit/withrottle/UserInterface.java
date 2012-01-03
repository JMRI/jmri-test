
package jmri.jmrit.withrottle;


/**
 *	UserInterface.java
 *	Create a window for WiThrottle information, advertise service, and create a thread for it to run in.
 *
 *	@author Brett Hoffman   Copyright (C) 2009, 2010
 *	@version $Revision$
 */


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.ResourceBundle;
import java.util.ArrayList;

import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.util.JmriJFrame;
import jmri.util.zeroconf.ZeroConfService;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.StopAllButton;


//	listen() has to run in a separate thread.
public class UserInterface extends JmriJFrame implements DeviceListener, DeviceManager {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserInterface.class.getName());
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    JMenuBar menuBar;
    JMenuItem serverOnOff;
    JPanel panel;
    JLabel portLabel = new JLabel(rb.getString("LabelPending"));
    JLabel manualPortLabel = new JLabel();
    JLabel numConnected;
    JScrollPane scrollTable;
    JTable withrottlesList;
    WiThrottlesListModel withrottlesListModel;
    UserPreferencesManager userPreferences = InstanceManager.getDefault(UserPreferencesManager.class);
    String rosterGroupSelectorPreferencesName = this.getClass().getName() + ".rosterGroupSelector";
    RosterGroupComboBox rosterGroupSelector = new RosterGroupComboBox(userPreferences.getComboBoxLastSelection(rosterGroupSelectorPreferencesName));

//	Server iVars
    int port;
    ZeroConfService service;
    boolean isListen = true;
    ServerSocket socket = null;
    ArrayList<DeviceServer> deviceList;


    UserInterface(){
        super(false, false);
        if (deviceList == null) deviceList = new ArrayList<DeviceServer>(1);

        createWindow();

        setShutDownTask();
        createServerThread();
    }	//	End of constructor


    public void createServerThread(){
        ServerThread s = new ServerThread(this);
        s.start();
    }



    protected void createWindow(){
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        getContentPane().add(panel);
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.5;
        con.weighty = 0;

        JLabel label = new JLabel(MessageFormat.format(rb.getString("LabelAdvertising"), new Object[]{DeviceServer.getWiTVersion()}));
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 2;
        panel.add(label, con);

        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 2;
        panel.add(portLabel, con);

        con.gridy = 2;
        panel.add(manualPortLabel, con);

        numConnected = new JLabel(rb.getString("LabelClients") + " " + deviceList.size());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 2;
        con.ipadx = 5;
        con.gridwidth = 1;
        panel.add(numConnected, con);

        JPanel rgsPanel = new JPanel();
        rgsPanel.add(new JLabel(rb.getString("RosterGroupLabel")));
        rgsPanel.add(rosterGroupSelector);
        rgsPanel.setToolTipText(rb.getString("RosterGroupToolTip"));
        JToolBar withrottleToolBar = new JToolBar();
        withrottleToolBar.setFloatable(false);
        withrottleToolBar.add(new StopAllButton());
	withrottleToolBar.add(new LargePowerManagerButton());
        withrottleToolBar.add(rgsPanel);
        con.weightx = 0.5;
        con.ipadx = 0;
        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 2;
        panel.add(withrottleToolBar, con);
/*
        JLabel vLabel = new JLabel("v"+DeviceServer.getWiTVersion());
        con.weightx = 0;
        con.gridx = 2;
        con.gridy = 3;
        panel.add(vLabel, con);
*/
        JLabel icon;
        java.net.URL imageURL = ClassLoader.getSystemResource("resources/IconForWiThrottle.gif");

        if (imageURL != null) {
            ImageIcon image = new ImageIcon(imageURL);
            icon = new JLabel(image);
            con.weightx = 0.5;
            con.gridx = 2;
            con.gridy = 0;
            con.ipady = 5;
            con.gridheight = 2;
            panel.add(icon,con);
        }





//  Add a list of connected devices and the address they are set to.

        withrottlesListModel = new WiThrottlesListModel(deviceList);
        withrottlesList = new JTable(withrottlesListModel);
        withrottlesList.setPreferredScrollableViewportSize(new Dimension(300, 80));

        withrottlesList.setRowHeight(20);
        scrollTable = new JScrollPane(withrottlesList);


        con.gridx = 0;
        con.gridy = 4;
        con.weighty = 1.0;
        con.ipadx = 10;
        con.ipady = 10;
        con.gridheight = 3;
        con.gridwidth = 3;
        panel.add(scrollTable, con);

		
//  Create the menu to use with WiThrottle window. Has to be before pack() for Windows.

        buildMenu();
        
//  Set window size & location
        this.setTitle("WiThrottle");
        this.pack();

        this.setResizable(false);
        Rectangle screenRect = new Rectangle(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());

//  Centers on top edge of screen
        this.setLocation((screenRect.width/2) - (this.getWidth()/2), 0);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setVisible(true);

        rosterGroupSelector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                userPreferences.addComboBoxLastSelection(rosterGroupSelectorPreferencesName, (String)((JComboBox) e.getSource()).getSelectedItem());
            }
        });
    }

    protected void buildMenu(){
        this.setJMenuBar(new JMenuBar());

        JMenu menu = new JMenu(rb.getString("MenuMenu"));
        serverOnOff = new JMenuItem(rb.getString("MenuMenuStop"));
        serverOnOff.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (isListen){	//	Stop server
                    disableServer();
                    serverOnOff.setText(rb.getString("MenuMenuStart"));
                    portLabel.setText(rb.getString("LabelNone"));
                    manualPortLabel.setText(null);
                }else{	//	Restart server
                    serverOnOff.setText(rb.getString("MenuMenuStop"));
                    isListen = true;

                    createServerThread();
                }
            }
        });

        menu.add(serverOnOff);

        menu.add(new ControllerFilterAction());

        Action prefsAction = new apps.gui3.TabbedPreferencesAction(
                            ResourceBundle.getBundle("apps.AppsBundle").getString("MenuItemPreferences"),
                            "WITHROTTLE");

        menu.add(prefsAction);

        this.getJMenuBar().add(menu);

        // add help menu
        addHelpMenu("package.jmri.jmrit.withrottle.UserInterface", true);
    }

    public void listen(){
        int socketPort = 0;
        if (WiThrottleManager.withrottlePreferencesInstance().isUseFixedPort()){
            socketPort = Integer.parseInt(WiThrottleManager.withrottlePreferencesInstance().getPort());
        }

        try{	//Create socket on available port
            socket = new ServerSocket(socketPort);
        } catch(IOException e1){
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        if(log.isDebugEnabled()) log.debug("WiThrottle listening on TCP port: " + port);

        service = ZeroConfService.create("_withrottle._tcp.local.", port);
        service.publish();
            
        if (service.isPublished()) {
        	Inet4Address hostAddr = null;
            //Determine the first externally published IPv4 address for this system. This is presented in the GUI for
            //those users who can't, or don't want to use ZeroConf to connect to the WiThrottle.
            try {
            	for (Inet4Address addr : service.serviceInfo().getInet4Addresses()) {
            		if (addr != null && !addr.isLoopbackAddress()) {
            			hostAddr = addr;
            			break;
            		}
            	}
            	if (hostAddr != null) {
            		portLabel.setText(hostAddr.getHostName());
            		manualPortLabel.setText(hostAddr.getHostAddress() + ":" + port);
            	} else {
            		portLabel.setText(Inet4Address.getLocalHost().getHostName());
            		manualPortLabel.setText(Inet4Address.getLocalHost().getHostAddress() + ":" + port);
            	}
            } catch (Exception except) {
            	log.error("Failed to determine this system's IP address: " + except.toString());
        		portLabel.setText("null");
        		manualPortLabel.setText("null:" + port);
            }
        } else {
            log.error("JmDNS Failure");
            portLabel.setText("failed to advertise service");
        }
        
        while (isListen){ //Create DeviceServer threads
            DeviceServer device;
            try{
                log.debug("Creating new DeviceServer(socket)");
                device = new DeviceServer(socket.accept(), this);

                Thread t = new Thread(device);
                device.addDeviceListener(this);
                log.debug("Starting DeviceListener thread");
                t.start();
            } catch (IOException e3){
                if (isListen)log.error("Listen Failed on port " + port);
                return;
            }

        }


    }


    public void notifyDeviceConnected(DeviceServer device){

        deviceList.add(device);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        pack();
    }

    public void notifyDeviceDisconnected(DeviceServer device){
        if (deviceList.size()<1) return;
        if (!deviceList.remove(device)) return;

        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());
        withrottlesListModel.updateDeviceList(deviceList);
        device.removeDeviceListener(this);
        pack();
    }

    public void notifyDeviceAddressChanged(DeviceServer device){
        withrottlesListModel.updateDeviceList(deviceList);
    }
/**
 * Received an UDID, filter out any duplicate.
 * @param device
 */
    public void notifyDeviceInfoChanged(DeviceServer device){

        //  Filter duplicate connections
        if ((device.getUDID() != null) && (deviceList.size() > 0)){
            for (int i = 0;i < deviceList.size();i++){
                DeviceServer listDevice = deviceList.get(i);
                if ((device != listDevice) && (listDevice.getUDID() != null) && (listDevice.getUDID().equals(device.getUDID()))){
                    //  If in here, array contains duplicate of a device
                    log.debug("Has duplicate of device, clearing old one.");
                    listDevice.closeThrottles();
                    break;
                }
            }
        }
        withrottlesListModel.updateDeviceList(deviceList);
    }

//	Clear out the deviceList array and close each device thread
    private void stopDevices(){
        DeviceServer device;
        int cnt = 0;
        if (deviceList.size()>0) do{
            device = deviceList.get(0);
            if (device != null){
                device.closeSocket();   //Tell device to stop its throttles, close its sockets
                                        //close() will throw read error and it will be caught
                                        //and drop the thread.
                cnt++;
                if (cnt>200){
                    break;
                }
            }
        }while (!deviceList.isEmpty());
        deviceList.clear();
        withrottlesListModel.updateDeviceList(deviceList);
        numConnected.setText(rb.getString("LabelClients") + " " + deviceList.size());

    }

    private jmri.implementation.AbstractShutDownTask task = null;
    @Override
    protected void setShutDownTask() {
        if (jmri.InstanceManager.shutDownManagerInstance()!=null) {
            task =
                    new jmri.implementation.AbstractShutDownTask(getTitle()){
                        public boolean execute() {
                            disableServer();
                            return true;
                        }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(task);
        }
    }


    private void disableServer(){
        isListen = false;
        stopDevices();
        try{
            socket.close();
            log.debug("UI socket just closed");
            service.stop();
        } catch (IOException ex){
            log.error("socket in ServerThread won't close");
            return;
        }
    }

    public String getSelectedRosterGroup() {
        return rosterGroupSelector.getSelectedRosterGroup();
    }
    
}

//  listen() has to run in a separate thread.
class ServerThread extends Thread {
    UserInterface UI;

    ServerThread(UserInterface _UI){
        UI = _UI;
    }

    @Override
    public void run() {
        UI.listen();
        log.debug("Leaving serverThread.run()");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ServerThread.class.getName());
}

 	  	 

 	  	 
