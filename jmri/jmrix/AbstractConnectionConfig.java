// AbstractSerialConnectionConfig.java

package jmri.jmrix;

/*import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Color;
import java.util.Vector;*/

import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.29 $
 */
abstract public class AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractConnectionConfig() {
    }

    protected boolean init = false;

    abstract void checkInitDone();

    protected JComboBox opt1Box = new JComboBox();
    protected JLabel opt1BoxLabel = new JLabel();
    
    protected JComboBox opt2Box = new JComboBox();

    protected JLabel opt2BoxLabel = new JLabel();
    protected JCheckBox showAdvanced = new JCheckBox("Additional Connection Settings");
    protected String[] opt1List;
    protected String[] opt2List;
    
    protected JPanel _details;
    //protected jmri.jmrix.PortAdapter adapter = null;
    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    abstract public String getInfo();

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
	abstract public void loadDetails(final JPanel details) ;

    
    protected int NUMOPTIONS = 2;
    
    abstract void showAdvancedItems();
    
    abstract void addStandardDetails(boolean incAdvanced);
    
    public boolean isOptList1Advanced() { return true; }
    public boolean isOptList2Advanced() { return true; }
    
    abstract public String getManufacturer();
    abstract public void setManufacturer(String manufacturer);

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConnectionConfig.class.getName());

}

