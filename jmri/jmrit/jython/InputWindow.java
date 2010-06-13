// InputWindow.java

package jmri.jmrit.jython;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.FlowLayout;
import jmri.util.PythonInterp;

/**
 * This Action runs creates a JFrame for sending input to the
 * global jython interpreter
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.8 $
 */
public class InputWindow extends JPanel {

    JTextArea area;
    JButton button;

    public InputWindow() {
        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        
        area = new JTextArea(12, 50);
        JScrollPane js = new JScrollPane(area);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(js);
        
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(button = new JButton(rb.getString("ButtonExecute")));
        add(p);
        
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonPressed();
            }
        });

        // set a monospaced font
        int size = area.getFont().getSize();
        area.setFont(new Font("Monospaced", Font.PLAIN, size));

    }
    

    void buttonPressed() {
        PythonInterp.getPythonInterpreter();

        String cmd = area.getText()+"\n";

        // The command must end with exactly one \n
        while ((cmd.length()>1) && cmd.charAt(cmd.length()-2)=='\n')
            cmd = cmd.substring(0, cmd.length()-1);

        // add the text to the output frame
        String echo = ">>> "+cmd;
        // intermediate \n characters need to be prefixed
        echo = echo.replaceAll("\n", "\n... ");
        echo = echo.substring(0, echo.length()-4);
        PythonInterp.getOutputArea().append(echo);

        // and execute
        PythonInterp.execCommand(cmd);
    }

}

/* @(#)InputWindow.java */
