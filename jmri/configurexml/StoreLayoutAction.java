// StoreLayoutAction.java

package jmri.configurexml;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;

import jmri.jmrit.XmlFile;

/**
 * Store the layout hardware config
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Id: StoreLayoutAction.java,v 1.1 2002-03-28 02:42:35 jacobsen Exp $
 * @see             jmri.jmrit.XmlFile
 */
public class StoreLayoutAction extends AbstractAction {

	public StoreLayoutAction(String s) {
		super(s);
	}

    public void actionPerformed(ActionEvent e) {
		File fp = new File("layout.temp.config.xml");
        LayoutConfigXML layout = new LayoutConfigXML();

		// write it out
		//try {
			layout.writeFile(fp);
		//} catch (java.io.IOException ex) {
		//	log.error("Error writing layout config file: "+ex.getMessage());
		//}
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreLayoutAction.class.getName());

}
