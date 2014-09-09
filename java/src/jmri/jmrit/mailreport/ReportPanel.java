// ReportPanel.java

package jmri.jmrit.mailreport;

import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.*;
import jmri.util.MultipartMessage;
import jmri.util.javaworld.GridLayout2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending a problem report via email.
 * <p>
 * The report is sent to a dedicated SourceForge mailing list, from which 
 * people can retrieve it.  
 * <P>
 * @author          Bob Jacobsen   Copyright (C) 2009
 * @author          Matthew Harris  Copyright (c) 2014
 * @version         $Revision$
 */
public class ReportPanel extends JPanel {

    static java.util.ResourceBundle rb = null;

    // member declarations
    JButton sendButton;
    JTextField emailField = new JTextField(40);
    JTextField summaryField = new JTextField(40);
    JTextArea descField = new JTextArea(8,40);
    JCheckBox checkContext;
    JCheckBox checkNetwork;
    JCheckBox checkLog;
    JCheckBox checkCopy;

    public ReportPanel() {
        if (rb == null) rb = java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1;

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel(rb.getString("LabelTop")));
        add(p1);

        // grid of options
        p1 = new JPanel();
        p1.setLayout(new GridLayout2(3,2));
        add(p1);

        JLabel l = new JLabel(rb.getString("LabelEmail"));
        l.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(l);
        emailField.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(emailField);

        l = new JLabel(rb.getString("LabelSummary"));
        l.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(l);
        summaryField.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(summaryField);

        l = new JLabel(rb.getString("LabelDescription"));
        p1.add(l);
        // This ensures that the long-description JTextArea font
        // is the same as the JTextField fields.
        // With some L&F, default font for JTextArea differs.
        descField.setFont(summaryField.getFont());
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        p1.add(descField);

        // buttons on bottom
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        checkContext = new JCheckBox(rb.getString("CheckContext"));
        checkContext.setSelected(true);
        checkContext.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkNetwork.setEnabled(checkContext.isSelected());
            }
        });
        p1.add(checkContext);

        checkNetwork = new JCheckBox(rb.getString("CheckNetwork"));
        checkNetwork.setSelected(true);
        p1.add(checkNetwork);        

        checkLog = new JCheckBox(rb.getString("CheckLog"));
        checkLog.setSelected(true);
        p1.add(checkLog);

        checkCopy = new JCheckBox(rb.getString("CheckCopy"));
        checkCopy.setSelected(true);
        p1.add(checkCopy);
        add(p1);

        sendButton = new javax.swing.JButton(rb.getString("ButtonSend"));
        sendButton.setToolTipText(rb.getString("TooltipSend"));
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        add(sendButton);

                }

    @SuppressWarnings("unchecked")
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        try {
            sendButton.setEnabled(false);
            log.debug("initial checks");
            InternetAddress email = new InternetAddress(emailField.getText());
            email.validate();

            log.debug("start send");
            String charSet = "UTF-8";  //NO18N
            String requestURL = "http://jmri.org/problem-report.php";  //NO18N

            MultipartMessage msg = new MultipartMessage(requestURL, charSet);

            // add reporter email address
            msg.addFormField("reporter", emailField.getText());

            // add if to Cc sender
            msg.addFormField("sendcopy", checkCopy.isSelected()?"yes":"no");

            // add problem summary
            msg.addFormField("summary", summaryField.getText());

            // build detailed error report (include context if selected)
            String report = descField.getText() + "\r\n";
            if (checkContext.isSelected()) {
                report += "=========================================================\r\n"; //NO18N
                report += ( new ReportContext()).getReport(checkNetwork.isSelected() && checkNetwork.isEnabled());
            }
            msg.addFormField("problem", report);

            // add the log if OK
            if (checkLog.isSelected()) {
                // search for an appender that stores a file
                for (java.util.Enumeration<org.apache.log4j.Appender> en = org.apache.log4j.Logger.getRootLogger().getAllAppenders(); en.hasMoreElements() ;) {
                    // does this have a file?
                    org.apache.log4j.Appender a = en.nextElement();
                    // see if it's one of the ones we know
                    if (log.isDebugEnabled()) log.debug("check appender "+a);
                    try {
                        org.apache.log4j.FileAppender f = (org.apache.log4j.FileAppender) a;
                        log.debug("find file: "+f.getFile());
                        msg.addFilePart("logfileupload[]", new File(f.getFile()), "application/octet-stream");
                    } catch (ClassCastException ex) {}
                }
            }

            // finalise and get server response (if any)
            log.debug("posting report...");
            List<String> response = msg.finish();
            log.debug("send complete");
            log.debug("server response:");
            boolean checkResponse = false;
            for (String line: response) {
                log.debug(line);
                if (line.contains("<p>Message successfully sent!</p>")) {
                    checkResponse = true;
                }
            }

            if (checkResponse) {
                JOptionPane.showMessageDialog(null, rb.getString("InfoMessage"), rb.getString("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
                // close containing Frame
                getTopLevelAncestor().setVisible(false);
            } else {
                JOptionPane.showMessageDialog(null, rb.getString("ErrMessage"), rb.getString("ErrTitle"), JOptionPane.ERROR_MESSAGE);
                sendButton.setEnabled(true);
            }


        } catch (IOException ex) {
            log.error("Error when attempting to send report: " + ex);
            sendButton.setEnabled(true);
        } catch (AddressException ex) {
            log.error("Invalid email address: " + ex);
            JOptionPane.showMessageDialog(null, rb.getString("ErrAddress"), rb.getString("ErrTitle"), JOptionPane.ERROR_MESSAGE);
            sendButton.setEnabled(true);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ReportPanel.class.getName());
}
