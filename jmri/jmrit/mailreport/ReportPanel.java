// ReportPanel.java

package jmri.jmrit.mailreport;

import jmri.util.javamail.MailMessage;

import java.awt.FlowLayout;
import javax.swing.*;

/**
 * User interface for sending a problem report via email.
 * <p>
 * The report is sent to a dedicated Google group, from which 
 * people can retrieve it.  
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2009
 * @version			$Revision: 1.5 $
 */
public class ReportPanel extends JPanel {

    static java.util.ResourceBundle rb = null;
    
    // member declarations
    JButton sendButton;
    JTextField emailField = new JTextField(40);
    JTextField summaryField = new JTextField(40);
    JTextArea descField = new JTextArea(5,40);
    JCheckBox checkContext;
    JCheckBox checkLog;

    public ReportPanel() {
        if (rb == null) rb = java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1;
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel(rb.getString("LabelTop")));
        add(p1);
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        JLabel l = new JLabel(rb.getString("LabelEmail"));
        l.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(l);
        emailField.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(emailField);
        add(p1);

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        l = new JLabel(rb.getString("LabelSummary"));
        l.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(l);
        summaryField.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(summaryField);
        add(p1);

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        l = new JLabel(rb.getString("LabelDescription"));
        p1.add(l);
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        p1.add(descField);
        add(p1);

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        checkContext = new JCheckBox(rb.getString("CheckContext"));
        checkContext.setSelected(true);
        p1.add(checkContext);
        
        checkLog = new JCheckBox(rb.getString("CheckLog"));
        checkLog.setSelected(true);
        p1.add(checkLog);
        add(p1);
        
        sendButton = new javax.swing.JButton(rb.getString("ButtonSend"));
        sendButton.setToolTipText(rb.getString("TooltipSend"));
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        add(sendButton);

    }
   
    @SuppressWarnings("unchecked")
	public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.debug("start send");
        // create message
        MailMessage msg = new MailMessage(
                               rb.getString("Destination"),
                               rb.getString("MailHost"),
                               summaryField.getText());
        msg.setFrom(emailField.getText());
        msg.prepare();
        
        // add from line
        msg.setText("Original poster: "+emailField.getText()+"\n");
        
        // add user reason
        msg.setText(descField.getText()+"\n");
        
        // add the context if OK
        if (checkContext.isSelected()) {
            String report = "=========================================================\n"
                        +(new ReportContext()).getReport();
            msg.setText(report);
        }
        
        // add the log if OK
        if (checkLog.isSelected()) {
            // search for an appender that stores a file
            for (java.util.Enumeration<org.apache.log4j.Appender> en = log.getRootLogger().getAllAppenders(); en.hasMoreElements() ;) {
                // does this have a file?
                org.apache.log4j.Appender a = en.nextElement();
                // see if it's one of the ones we know
                if (log.isDebugEnabled()) log.debug("check appender "+a);
                try {
                    org.apache.log4j.FileAppender f = (org.apache.log4j.FileAppender)a;
                    log.debug("find file: "+f.getFile());
                    msg.setFileAttachment(f.getFile());
                } catch (ClassCastException ex) {}
            } 
        }
        
        // and try to send it
        try {
            msg.send();
            log.debug("send complete");
            // close containing Frame
            getTopLevelAncestor().setVisible(false);
        } catch (Exception e1) {
            log.warn("send failed", e1);
            JOptionPane.showMessageDialog(null, rb.getString("ErrMessage"), rb.getString("ErrTitle"), JOptionPane.ERROR_MESSAGE);
        }        
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReportPanel.class.getName());
}
