package org.jmri.application.trainpro.welcome;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author rhwood
 */
public class GradientPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2d = (Graphics2D) grphcs;
        GradientPaint gp = new GradientPaint(
                0, 0, this.getBackground(),
                0, h, this.getForeground());

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}
