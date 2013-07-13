package jmri.jmrit.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * Adds capability for a object to load a browser page or bring a panel frame to the top.
 * 
 * Implementations must include code to do this:
 * 
            if (url.startsWith("frame:")) {
                // locate JmriJFrame and push to front
                String frame = url.substring(6);
                final jmri.util.JmriJFrame jframe = jmri.util.JmriJFrame.getFrame(frame);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jframe.toFront();
                        jframe.repaint();
                    }
                });                
            } else {
                jmri.util.ExternalLinkContentViewerUI.activateURL(new java.net.URL(url));
            }
 * This code typically is done within the following Positionable call
 *         
    public void doMouseClicked(MouseEvent event);
 *
 *
 ** Positionable object may use the following call to edit the "url" string
 *
    public boolean setLinkMenu(JPopupMenu popup) {
    	// perhaps check if "url" is OK or just this
        popup.add(CoordinateEdit.getLinkEditAction(this, "EditLink"));
    	return true;
    }
 *
 * See LinkingLabel.java or AnalogClock2Display.java for examples
 */
public interface LinkingObject extends Cloneable  {
    public String getUrl();
    public void setUrl(String u);
    public void updateSize();
    public boolean setLinkMenu(JPopupMenu popup);
//    public void doMouseClicked(MouseEvent event);   
}