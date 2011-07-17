// ConnectionTypeList.java

package jmri.jmrix;

/**
 * Definition of objects to handle configuring a layout connection.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.2 $
 * @see JmrixConfigPane
 * @see AbstractPortController
 */
public interface ConnectionTypeList  {

    public String[] getAvailableProtocolClasses();

}

