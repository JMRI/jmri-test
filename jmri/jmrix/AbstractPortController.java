// AbstractPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;
import java.util.Vector;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;

import gnu.io.CommPortIdentifier;
import javax.swing.JOptionPane;
//import jmri.util.SystemType;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance.
 * SerialPortAdapter is an Interface, and its implementing
 * classes also inherit from various PortController types.  But we
 * want some common behaviours for those, so we put them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.26 $
 */
abstract public class AbstractPortController implements PortAdapter {

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public boolean status() {return opened;}
    
    protected boolean opened = false;

    abstract public String getCurrentPortName();
    
    /**
     * Get an array of valid values for "option 1"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return ""; }

    /**
     * Set the second port option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

    /**
     * Get an array of valid values for "option 2"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption2() { return new String[]{""}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return ""; }

    /**
     * Set the second port option.
     */
    public void configureOption2(String value) { mOpt2 = value; }
    protected String mOpt2  = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[0];
        return mOpt2;
    }
    
    /**
    * Get and set of the Manufacturer for network (TCP/IP) based
    * connections is handled by the ConnectionConfig code in each
    * connector.  this is here as we implement the serialdriveradpter.
    */
    public String getManufacturer() { return mManufacturer; }
    public void setManufacturer(String Manufacturer) { mManufacturer = Manufacturer; }
    protected String mManufacturer = null;

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPortController.class.getName());

}
