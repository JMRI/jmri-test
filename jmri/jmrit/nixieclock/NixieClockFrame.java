// NixieClockFrame.java

package jmri.jmrit.nixieclock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import jmri.*;

import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.catalog.*;

/**
 * Frame providing a simple clock showing Nixie tubes.
 * <P>
 * A Run/Stop button is built into this, but because I
 * don't like the way it looks, it's not currently
 * displayed in the GUI.
 *
 * Modified by Dennis Miller for resizing Nov, 2004
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public class NixieClockFrame extends javax.swing.JFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel h1;  // msb of hours
    JLabel h2;
    JLabel m1;  // msb of minutes
    JLabel m2;
    JLabel colon;

    double aspect;
    double iconAspect;

    Timebase clock;
    javax.swing.Timer timer;
    static int delay = 2*1000;  // update display every two seconds

    static NamedIcon tubes[] = new NamedIcon[10];
    static NamedIcon baseTubes[] = new NamedIcon[10];
    static NamedIcon colonIcon;
    static NamedIcon baseColon;
    //"base" variables used to hold original gifs, other variables used with scaled images

    public NixieClockFrame() {

        clock = InstanceManager.timebaseInstance();

        //Load the images
        for (int i = 0; i < 10; i++) {
          baseTubes[i] = new NamedIcon("resources/icons/misc/Nixie/M" + i + ".gif", "resources/icons/misc/Nixie/M" + i + ".gif");
          tubes[i] = new NamedIcon("resources/icons/misc/Nixie/M" + i + ".gif", "resources/icons/misc/Nixie/M" + i + ".gif");
        }
        colonIcon = new NamedIcon("resources/icons/misc/Nixie/colon.gif", "resources/icons/misc/Nixie/colon.gif");
        baseColon = new NamedIcon("resources/icons/misc/Nixie/colon.gif", "resources/icons/misc/Nixie/colon.gif");


        // determine aspect ratio of a single digit graphic
        iconAspect = 24./32.;

        // determine the aspect ratio of the 4 digit base graphic plus a half digit for the colon
        // this DOES NOT allow space for the Run/Stop button, if it is
        // enabled.  When the Run/Stop button is enabled, the layout will have to be changed
        aspect =  (4.5*24.)/32.;

        // set time to now
        clock.setTime(new Date());
        try { clock.setRate(4.); } catch (Exception e) {}

        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);

        // init GUI
        m1 = new JLabel(tubes[0]);
        m2 = new JLabel(tubes[0]);
        h1 = new JLabel(tubes[0]);
        h2 = new JLabel(tubes[0]);
        colon = new JLabel(colonIcon);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(h1);
        getContentPane().add(h2);
        getContentPane().add(colon);
        getContentPane().add(m1);
        getContentPane().add(m2);

        getContentPane().add(b = new JButton("Stop"));
        b.addActionListener( new ButtonListener());
        // since Run/Stop button looks crummy, don't display for now
        b.setVisible(false);

        update();
        pack();

        // start timer
         if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        update();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
        // Add component listener to handle frame resizing event
        this.addComponentListener(
                        new ComponentAdapter()
                {
                    public void componentResized(ComponentEvent e)
                    {
                        scaleImage();
                    }
                });

    }


    // Added method to scale the clock digit images to fit the
    // size of the display window

    public void scaleImage() {
      int iconHeight;
      int iconWidth;
      int frameHeight = this.getContentPane().getHeight();
      int frameWidth = this.getContentPane().getWidth();
      if (frameWidth/frameHeight > aspect) {
        iconHeight = frameHeight;
        iconWidth = (int) (iconAspect * (float) iconHeight);
      }
      else {
        //this DOES NOT allow space for the Run/Stop button, if it is
        //enabled.  When the Run/Stop button is enabled, the layout will have to be changed
        iconWidth = (int) (frameWidth/4.5);
        iconHeight = (int) (iconWidth/iconAspect);
      }
      for (int i = 0; i < 10; i++) {
        Image baseImage = baseTubes[i].getImage();
        Image scaledImage = baseImage.getScaledInstance(iconWidth,iconHeight,Image.SCALE_SMOOTH);
        tubes[i].setImage(scaledImage);
      }
      Image baseImage = baseColon.getImage();
      Image scaledImage = baseImage.getScaledInstance(iconWidth/2,iconHeight,Image.SCALE_SMOOTH);
      colonIcon.setImage(scaledImage);

//      Ugly hack to force frame to redo the layout.
//      Without this the image is scaled but the label size and position doesn't change.
//      doLayout() doesn't work either
      this.hide();
      this.remove(b);
      this.getContentPane().add(b);
      this.show();
      return ;
    }

    void update() {
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();

        h1.setIcon(tubes[hours/10]);
        h2.setIcon(tubes[hours-(hours/10)*10]);
        m1.setIcon(tubes[minutes/10]);
        m2.setIcon(tubes[minutes-(minutes/10)*10]);
    }


    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        timer.stop();
        dispose();
    }

    /**
     * Handle a change to clock properties
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		boolean now = clock.getRun();
		if (now) b.setText("Stop");
		else b.setText("Run");
    }

    JButton b;

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean next = !clock.getRun();
			clock.setRun(next);
			if (next) b.setText("Stop");
			else b.setText("Run ");
		}
	}
}
