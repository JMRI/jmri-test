// SpeedoDial.java

package jmri.jmrix.bachrus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrit.catalog.*;

/**
 * Creates a JPanel containing an Dial type speedo display.
 *
 * <p> Based on analogue clock frame by Dennis Miller
 *
 * @author                     Andrew Crosland Copyright (C) 2010
 * @version                    $Revision: 1.3 $
 */
public class SpeedoDial extends JPanel {

    // GUI member declarations
    float speedAngle = 0.0F;
    int speedDigits = 0;
    
    // Create a Panel that has a dial drawn on it scaled to the size of the panel
    // Define common variables
    Image logo;
    Image scaledLogo;
    NamedIcon jmriIcon;
    NamedIcon scaledIcon;
    int minuteX[] = {-12, -11, -24, -11, -11, 0, 11, 11, 24, 11, 12};
    int minuteY[] = {-31, -261, -266, -314, -381, -391, -381, -314, -266, -261, -31};
    int scaledMinuteX[] = new int[minuteX.length];
    int scaledMinuteY[] = new int[minuteY.length];
    int rotatedMinuteX[] = new int[minuteX.length];
    int rotatedMinuteY[] = new int[minuteY.length];

    Polygon minuteHand;
    Polygon scaledMinuteHand;
    int minuteHeight;
    float scaleRatio;
    int faceSize;
    int panelWidth;
    int panelHeight;
    int size;
    int logoWidth;
    int logoHeight;

    // centreX, centreY are the coordinates of the centre of the dial
    int centreX;
    int centreY;

    int units = Speed.MPH;
    
    int mphLimit = 80;
    int mphInc = 40;
    int kphLimit = 140;
    int kphInc = 60;
    float priMajorTick;
    float priMinorTick;
    float secTick;

        
    public SpeedoDial() {
        super();

        // Load the JMRI logo and pointer for the dial
        // Icons are the original size version kept for to allow for mulitple resizing
        // and scaled Icons are the version scaled for the panel size
        jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        logo = jmriIcon.getImage();

        // Create an unscaled pointer to get the original size (height)to use
        // in the scaling calculations
        minuteHand = new Polygon(minuteX, minuteY, 11);
        minuteHeight = minuteHand.getBounds().getSize().height;

        // Add component listener to handle frame resizing event
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                scaleFace();
            }});

        setPreferredSize(new java.awt.Dimension(300,300));
    }
        
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;

        // overridden Paint method to draw the speedo dial

        g2.translate(centreX, centreY);

        // Draw the dial outline scaled to the panel size with a dot in the middle and
        // center ring for the pointer
        g2.setColor(Color.white);
        g2.fillOval(-faceSize/2, -faceSize/2, faceSize, faceSize);
        g2.setColor(Color.black);
        g2.drawOval(-faceSize/2, -faceSize/2, faceSize, faceSize);

        int dotSize = faceSize/40;
        g2.fillOval(-dotSize*2, -dotSize*2, 4*dotSize, 4*dotSize);

        // Draw the JMRI logo
        g2.drawImage(scaledLogo, -logoWidth/2, -faceSize/4, logoWidth, logoHeight, this);

        // Currently selected units are plotted every 10 units with major and minor
        // tick marks around the outer edge of the dial
        // Other units are plotted in a differrent color, smaller font with dots
        // in an inner ring
        // Scaled font size for primary units
        int fontSize = faceSize/10;
        if (fontSize < 1) fontSize=1;
        Font sizedFont = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(sizedFont);
        FontMetrics fontM = g2.getFontMetrics(sizedFont);

        // Draw the speed markers for the primary units
        int dashSize = size/60;
        if (units == Speed.MPH) {
            priMajorTick = 240/(mphLimit/10);
            priMinorTick = priMajorTick/5;
            secTick = 240/(Speed.mphToKph(mphLimit)/10);
        } else {
            priMajorTick = 240/(kphLimit/10);
            priMinorTick = priMajorTick/5;
            secTick = 240/(Speed.kphToMph(kphLimit)/10);
        }
        // i is degrees clockwise from the X axis
        // Add minor tick marks
        for (float i = 150; i < 391; i = i + priMinorTick) {
            g2.drawLine(dotX(faceSize/2, i), dotY(faceSize/2, i),
                       dotX(faceSize/2 - dashSize, i), dotY(faceSize/2 - dashSize, i));
        }
        // Add major tick marks and digits
        int j = 0;
        for (float i = 150; i < 391; i = i + priMajorTick) {
            g2.drawLine(dotX(faceSize/2, i), dotY(faceSize/2, i),
                       dotX(faceSize/2 - 3 * dashSize, i), dotY(faceSize/2 - 3 * dashSize, i));
            String speed = Integer.toString(10*j);
            int xOffset = fontM.stringWidth(speed);
            int yOffset = fontM.getHeight();
            // offset by 210 degrees to start in lower left quadrant and work clockwise
            g2.drawString(speed, dotX(faceSize/2-6*dashSize,j*priMajorTick-210) - xOffset/2,
                               dotY(faceSize/2-6*dashSize,j*priMajorTick-210) + yOffset/4);
            j++;
        }

        // Add dots and digits for secondary units
        // First make a smaller font
        fontSize = faceSize/15;
        if (fontSize < 1) fontSize=1;
        sizedFont = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(sizedFont);
        fontM = g2.getFontMetrics(sizedFont);
        g2.setColor(Color.green);
        j = 0;
        for (float i = 150; i < 391; i = i + secTick) {
            g2.fillOval(dotX(faceSize/2 - 10 * dashSize, i), dotY(faceSize/2 - 10 * dashSize, i),
                        5, 5);
            if (((j & 1) == 0) ||(units == Speed.KPH)) {
                // kph are plotted every 20 when secondary, mph every 10
                String speed = Integer.toString(10*j);
                int xOffset = fontM.stringWidth(speed);
                int yOffset = fontM.getHeight();
                // offset by 210 degrees to start in lower left quadrant and work clockwise
                g2.drawString(speed, dotX(faceSize/2-13*dashSize,j*secTick-210) - xOffset/2,
                                   dotY(faceSize/2-13*dashSize,j*secTick-210) + yOffset/4);
            }
            j++;
        }
        g2.setColor(Color.black);

        // Draw pointer rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Graphics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) (scaledMinuteX[i]*Math.cos(toRadians(speedAngle))
                                    - scaledMinuteY[i]*Math.sin(toRadians(speedAngle)));
            rotatedMinuteY[i] = (int) (scaledMinuteX[i]*Math.sin(toRadians(speedAngle))
                                    + scaledMinuteY[i]*Math.cos(toRadians(speedAngle)));
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);
        g2.fillPolygon(scaledMinuteHand);

        // Draw units indicator in slightly smaller font than speed digits
        String unitsString = (units == Speed.MPH) ? "MPH" : "KPH";
        int unitsFontSize = (int) (faceSize/10*.75);
        if (unitsFontSize < 1) unitsFontSize = 1;
        Font unitsSizedFont = new Font("Serif", Font.PLAIN, unitsFontSize);
        g2.setFont(unitsSizedFont);
        FontMetrics amPmFontM = g2.getFontMetrics(unitsSizedFont);
        g2.drawString(unitsString, -amPmFontM.stringWidth(unitsString)/2, faceSize/5 );
        
        // Show numeric speed
        String speedString = Integer.toString(speedDigits);
        int digitsFontSize = (int) (fontSize*1.5);
        Font digitsSizedFont = new Font("Serif", Font.PLAIN, digitsFontSize);
        g2.setFont(digitsSizedFont);
        FontMetrics digitsFontM = g2.getFontMetrics(digitsSizedFont);
        
        // draw a box around the digital speed
        int pad = (int)(digitsFontSize*0.2);
        int h = (int)(digitsFontM.getAscent()*0.8);
        int w = digitsFontM.stringWidth("999");
        if (pad < 2) { pad = 2; }
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(-w/2-pad, 2*faceSize/5-h-pad, w+pad*2, h+pad*2);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(-w/2-pad, 2*faceSize/5-h-pad, w+pad*2, h+pad*2);

        g2.setColor(Color.BLACK);
        g2.drawString(speedString, -digitsFontM.stringWidth(speedString)/2, 2*faceSize/5 );
    }

    // Method to convert degrees to radians
    // Math.toRadians was not available until Java 1.2
    float toRadians(float degrees) {
        return degrees/180.0F*(float)Math.PI;
    }

    // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
    int dotX (float radius, float angle) {
        int xDist;
        xDist = (int) Math.round(radius * Math.cos(toRadians(angle)));
        return xDist;
    }

    // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
    int dotY (float radius, float angle) {
        int yDist;
        yDist = (int) Math.round(radius * Math.sin(toRadians(angle)));
        return yDist;
    }

    // Method called on resizing event - sets various sizing variables
    // based on the size of the resized panel and scales the logo/hands
    public void scaleFace() {
        int panelHeight = this.getSize().height;
        int panelWidth = this.getSize().width;
        size = Math.min(panelHeight, panelWidth);
        faceSize = (int) (size * .97);
        if (faceSize == 0){faceSize=1;}

        // Had trouble getting the proper sizes when using Images by themselves so
        // use the NamedIcon as a source for the sizes
        int logoScaleWidth = faceSize/6;
        int logoScaleHeight = (int) ((float)logoScaleWidth * (float)jmriIcon.getIconHeight()/jmriIcon.getIconWidth());
        scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight, Image.SCALE_SMOOTH);
        scaledIcon.setImage(scaledLogo);
        logoWidth = scaledIcon.getIconWidth();
        logoHeight = scaledIcon.getIconHeight();

        scaleRatio=faceSize/2.7F/minuteHeight;
        for (int i = 0; i < minuteX.length; i++) {
            scaledMinuteX[i] =(int) (minuteX[i]*scaleRatio);
            scaledMinuteY[i] = (int) (minuteY[i]*scaleRatio);
        }
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY, scaledMinuteX.length);

        centreX = panelWidth/2;
        centreY = panelHeight/2;

        return ;
    }
    
    @SuppressWarnings("deprecation")
    void update(float speed) {
        // hand rotation starts at 12 o'clock position so offset it by 120 degrees
        // scale by the angle between major tick marks divided by 10
        if (units == Speed.MPH) {
            speedDigits = Math.round((float)Speed.kphToMph(speed));
            speedAngle = -120 + Speed.kphToMph(speed*priMajorTick/10);
        } else {
            speedDigits = Math.round(speed);
            speedAngle = -120+speed*priMajorTick/10;
        }
        repaint();
    }

    void update() {
        repaint();
    }

    void setUnitsMph() { units = Speed.MPH; }
    void setUnitsKph() { units = Speed.KPH; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeedoDial.class.getName());
}

