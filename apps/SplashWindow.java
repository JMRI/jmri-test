// SplashWindow.java

package apps;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import jmri.util.JmriJFrame;

/**
 * A splash screen for showing during JMRI startup
 * @author	Bob Jacobsen   Copyright 2003
 * @author  Dennis Miller Copyright 2007
 * @version     $Revision: 1.7 $
 */
public class SplashWindow extends JmriJFrame {
    Image splashIm;

    public SplashWindow() {
        super("JMRI");

        // get the splash image
       MediaTracker mt = new MediaTracker(this);
       splashIm = Toolkit.getDefaultToolkit(
           ).getImage("resources"+File.separator+"logo.gif");
       mt.addImage(splashIm,0);
       try {
          mt.waitForID(0);
       } catch(InterruptedException ie){
         Thread.currentThread().interrupt(); // retain if needed later
       }

        JLabel l = new JLabel(new ImageIcon(splashIm, "JMRI splash screen"));
        l.setOpaque(true);
        getContentPane().add(l);
        pack();

        /* Center the window and pad the frame size slightly to put some space 
         * between logo and frame border*/
        Dimension screenDim =
             Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        winDim.height = winDim.height + 10;
        winDim.width = winDim.width + 10;
        setLocation((screenDim.width - winDim.width) / 2,
                (screenDim.height - winDim.height) / 2);
        setSize(winDim.width, winDim.height);

        // and show
        setVisible(true);
    }

/*  paint method required for Java 1.1.8 removed as it caused a
 *  transparent frame under Java 1.6 */
}


