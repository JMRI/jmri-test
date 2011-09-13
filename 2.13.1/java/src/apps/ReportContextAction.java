// ReportContextAction.java

package apps;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jmri.jmrit.mailreport.ReportContext;
import jmri.util.JmriJFrame;

/**
 * Swing action to display the JMRI context for the user
 *
 * @author	Bob Jacobsen    Copyright (C) 2007
 * @author  Matt Harris Copyright (C) 2008
 *
 * @version         $Revision$
 */
public class ReportContextAction extends AbstractAction {

    public ReportContextAction() { super();}

    JTextArea pane;
    static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

    public void actionPerformed(ActionEvent ev) {

        final JFrame frame = new JmriJFrame(rb.getString("TitleContext"));  // JmriJFrame to ensure fits on screen

        final Clipboard clipboard = frame.getToolkit().getSystemClipboard();

        pane = new JTextArea();
        pane.append("\n"); // add a little space at top
        pane.setEditable(false);
        pane.setLineWrap(true);
        pane.setWrapStyleWord(true);
        pane.setColumns(120);

        JScrollPane  scroll = new JScrollPane(pane);
        frame.add(scroll, BorderLayout.CENTER);
        
        ReportContext r = new ReportContext();
        addString(r.getReport());
        
        pane.append("\n"); // add a little space at bottom

        // Add button to allow copy to clipboard
        JPanel p = new JPanel();
        JButton copy = new JButton(rb.getString("ButtonCopyClip"));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StringSelection text = new StringSelection(pane.getText());
                clipboard.setContents(text, text);
            }
        });
        p.add(copy);
        JButton close = new JButton(rb.getString("ButtonClose"));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
        p.add(close);
        frame.add(p, BorderLayout.SOUTH);
        frame.pack();

        // start scrolled to top
        pane.setCaretPosition(0);
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());

        // show
        frame.setVisible(true);

    }

    void addString(String val) {
        pane.append(val+"\n");
    }
    void addProperty(String prop) {
        addString(prop+": "+System.getProperty(prop)+"  ");
    }
    
}

/* @(#)ReportContextAction.java */
