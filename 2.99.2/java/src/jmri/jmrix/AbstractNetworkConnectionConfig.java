// AbstractNetworkConnectionConfig.java

package jmri.jmrix;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
abstract public class AbstractNetworkConnectionConfig extends AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for an object being created during load process
     */
    public AbstractNetworkConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        adapter = p;
    }

    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractNetworkConnectionConfig() {
    }

    boolean init = false;
    
    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        hostNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.setHostName(hostNameField.getText());
                p.addComboBoxLastSelection(adapter.getClass().getName()+".hostname", hostNameField.getText());
            }
        });
        hostNameField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
               adapter.setHostName(hostNameField.getText());
               p.addComboBoxLastSelection(adapter.getClass().getName()+".hostname", hostNameField.getText());
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        portField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
            }
        });

        portField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
               try{
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        opt1Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption1((String)opt1Box.getSelectedItem());
            }
        });
        opt2Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption2((String)opt2Box.getSelectedItem());
            }
        });

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
                public void focusGained(FocusEvent e){ }
            });
            connectionNameField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
                public void focusGained(FocusEvent e){ }
            });
        }        init = true;
    }

    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    protected JTextField hostNameField = new JTextField();
    protected JLabel hostNameFieldLabel;
    protected JTextField portField = new JTextField();
    protected JLabel portFieldLabel;
    protected jmri.jmrix.NetworkPortAdapter adapter = null;

    public jmri.jmrix.NetworkPortAdapter getAdapter() { return adapter; }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    public String getInfo() {
        return adapter.getCurrentPortName();
    }

    //static java.util.ResourceBundle rb = 
        //java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
    public void loadDetails(final JPanel details) {
    	_details = details;
        setInstance();

        if(hostNameField.getActionListeners().length >0)
        	hostNameField.removeActionListener(hostNameField.getActionListeners()[0]);

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS=NUMOPTIONS+2;
        }
        opt1List = adapter.validOption1();
        opt1BoxLabel = new JLabel(adapter.option1Name());
        opt1Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt1Box.getActionListeners().length >0)
        	opt1Box.removeActionListener(opt1Box.getActionListeners()[0]);
        for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
        
        opt2List = adapter.validOption2();
        opt2BoxLabel = new JLabel(adapter.option2Name());
        opt2Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt2Box.getActionListeners().length >0)
        	opt2Box.removeActionListener(opt2Box.getActionListeners()[0]);
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

        portField.setToolTipText("Port address setting of the TCP Connection");
        portField.setEnabled(true);

        if (opt1List.length>1) {
            NUMOPTIONS++;
            opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
            opt1Box.setEnabled(true);
            opt1Box.setSelectedItem(adapter.getCurrentOption1Setting());
        } else {
            opt1Box.setToolTipText("There are no options for this protocol");
            opt1Box.setEnabled(false);
        }
        if (opt2List.length>1) {
            NUMOPTIONS++;
            opt2Box.setToolTipText("");
            opt2Box.setEnabled(true);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
        } else {
            opt2Box.setToolTipText("There are no options for this protocol");
            opt2Box.setEnabled(false);
        }
        
        hostNameField.setText(adapter.getHostName());
        hostNameFieldLabel = new JLabel("IP Address: ");
        if(adapter.getHostName()==null || adapter.getHostName().equals("") ){
            hostNameField.setText(p.getComboBoxLastSelection(adapter.getClass().getName()+".hostname"));
            adapter.setHostName(hostNameField.getText());
        }
        portField.setText(""+adapter.getPort());
        
        portFieldLabel = new JLabel("TCP/UDP Port:");
        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e){
                    showAdvancedItems();
                }
            });
        showAdvancedItems();

        init = false;		// need to reload action listeners
        checkInitDone();
    }
        
    protected void showAdvancedItems(){
        _details.removeAll();
        int stdrows = 0;
        boolean incAdvancedOptions=true;
        if(!isPortAdvanced()) stdrows++;
        if(!isHostNameAdvanced()) stdrows++;
        if ((!isOptList1Advanced())&&(opt1List.length>1)) stdrows++;
        if ((!isOptList2Advanced())&&(opt2List.length>1)) stdrows++;
        if(adapter.getSystemConnectionMemo()!=null) stdrows=stdrows+2;
        if (stdrows == NUMOPTIONS){
            incAdvancedOptions=false;
        } else{
            stdrows++;
        }
        if (showAdvanced.isSelected()) {
            int advrows = stdrows;
            if(isPortAdvanced()) advrows++;
            if(isHostNameAdvanced()) advrows++;
            if ((isOptList1Advanced())&&(opt1List.length>1)) advrows++;
            if ((isOptList2Advanced())&&(opt2List.length>1)) advrows++;
            _details.setLayout(new GridLayout(advrows,2));
            addStandardDetails(incAdvancedOptions);
            if(isHostNameAdvanced()){
                _details.add(hostNameFieldLabel);
                _details.add(hostNameField);
            }
            
            if(isPortAdvanced()){
                _details.add(portFieldLabel);
                _details.add(portField);
            }
            if ((isOptList1Advanced())&&(opt1List.length>1)) {
                _details.add(opt1BoxLabel);
                _details.add(opt1Box);
            }
            if ((isOptList2Advanced())&&(opt2List.length>1)) {
                _details.add(opt2BoxLabel);
                _details.add(opt2Box);
            }
        } else {
            _details.setLayout(new GridLayout(stdrows,2));
            addStandardDetails(incAdvancedOptions);
        }
        _details.validate();
        if (_details.getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).pack();
        }
        _details.repaint();
    }
    
    protected void addStandardDetails(boolean incAdvanced){
        if(!isHostNameAdvanced()){
            _details.add(hostNameFieldLabel);
            _details.add(hostNameField);
        }
        
        if(!isPortAdvanced()){
            _details.add(portFieldLabel);
            _details.add(portField);
        }
        
        if ((!isOptList1Advanced())&&(opt1List.length>1)){
            _details.add(opt1BoxLabel);
            _details.add(opt1Box);
        }
        
        if ((!isOptList2Advanced())&&(opt2List.length>1)) {
            _details.add(opt2BoxLabel);
            _details.add(opt2Box);
        }
        if(adapter.getSystemConnectionMemo()!=null){
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
        }
        if (incAdvanced){
            _details.add(new JLabel(" "));
            _details.add(showAdvanced);
        }

    }
    
    public boolean isHostNameAdvanced() { return false; }
    public boolean isPortAdvanced() { return true; }

    
    public String getManufacturer() { return adapter.getManufacturer(); }
    public void setManufacturer(String manufacturer) { adapter.setManufacturer(manufacturer); }

    public boolean getDisabled() {
        if (adapter==null) return true;
        return adapter.getDisabled();
    }
    public void setDisabled(boolean disabled) {
        if(adapter!=null)
            adapter.setDisabled(disabled);
    }
    
    public String getConnectionName() { 
        if(adapter.getSystemConnectionMemo()!=null)
            return adapter.getSystemConnectionMemo().getUserName();
        else return name();
    }
    
    public void dispose() { 
        if (adapter!=null){
            adapter.dispose();
            adapter=null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractNetworkConnectionConfig.class.getName());

}

