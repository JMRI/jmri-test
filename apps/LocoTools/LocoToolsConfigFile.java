// LocoToolsConfigFile.java

package apps.LocoTools;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import apps.*;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates the preferences information for the
 * LocoTools application. Works with the LocoToolsConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version		 	$Revision: 1.3 $
 * @see apps.AbstractConfigFrame
 */
public class LocoToolsConfigFile extends apps.AbstractConfigFile {

    public void writeFile(String name, AbstractConfigFrame f) {
        try {
            // This is taken in large part from "Java and XML" page 368

            // create file Object
            XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
            File file = new File(prefsDir()+name);

            // create root element
            Element root = new Element("preferences-config");
            Document doc = new Document(root);
            doc.setDocType(new DocType("preferences-config","preferences-config.dtd"));

            // add connection element
            Element values;
            root.addContent(f.getConnection());

            // add gui element
            root.addContent(f.getGUI());

            // add programmer element
            root.addContent(f.getProgrammer());

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(file);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);
            o.close();
        }
        catch (Exception e) {
            log.error(e);
        }
    }

    protected String configFileName() { return "LocoToolsConfig.xml";}

    public String defaultConfigFilename() { return configFileName();}

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoToolsConfigFile.class.getName());
}
