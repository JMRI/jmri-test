/**
 * QSIMenu.java
 */

package jmri.jmrix.qsi;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri QSI-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2007
 * @version     $Revision$
 */
public class QSIMenu extends JMenu {
    public QSIMenu(String name) {
        this();
        setText(name);
    }

    public QSIMenu() {

        super();

        //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        // setText(rb.getString("MenuSystems"));
        setText("QSI");

        add(new jmri.jmrix.qsi.qsimon.QsiMonAction());
        add(new jmri.jmrix.qsi.packetgen.PacketGenAction());

    }

}


