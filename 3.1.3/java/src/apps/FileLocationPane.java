// FileLocationPane.java

package apps;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Provide GUI to configure the Default File Locations
 * <P>
 * Provides GUI configuration for the default file locations by
 * displaying textfields for the user to directly enter in their own path or
 * a Set button is provided so that the user can select the path.
 *
 * @author      Kevin Dickerson   Copyright (C) 2010
 * @version	$Revision$
 */
 
public class FileLocationPane extends JPanel {

    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    public FileLocationPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    

        add(PrefLocation());
        add(ScriptsLocation());
        
        /*p = new JPanel();
        JLabel throttle = new JLabel("Default Throttle Location");
        p.add(throttle);
        p.add(throttleLocation);
        throttleLocation.setColumns(20);
        throttleLocation.setText(jmri.jmrit.throttle.ThrottleFrame.getDefaultThrottleFolder());
        add(p);*/
        
    }
    
    public static void save(){
        jmri.jmrit.XmlFile.setScriptsFileLocationDefault(scriptLocation.getText());
        jmri.jmrit.XmlFile.setUserFileLocationDefault(userLocation.getText());
        //jmri.jmrit.throttle.ThrottleFrame.setDefaultThrottleLocation(throttleLocation.getText());
    }
    
    private JPanel ScriptsLocation(){
        JButton bScript = new JButton(rb.getString("ButtonSetDots"));
        final JFileChooser fcScript;
        fcScript = new JFileChooser(jmri.jmrit.XmlFile.scriptsDir());

        fcScript.setDialogTitle(rb.getString("MessageSelectDirectory"));
        fcScript.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcScript.setAcceptAllFileFilterUsed(false);
        bScript.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // get the file
                fcScript.showOpenDialog(null);
                if (fcScript.getSelectedFile()==null) return; // cancelled
                scriptLocation.setText(fcScript.getSelectedFile()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JLabel scripts = new JLabel(rb.getString("ScriptDir"));
        p.add(scripts);
        p.add(scriptLocation);
        p.add(bScript);
        scriptLocation.setColumns(30);
        scriptLocation.setText(jmri.jmrit.XmlFile.scriptsDir());
        return p;
    }
    
    private JPanel PrefLocation(){
        JPanel p = new JPanel();
        JLabel users = new JLabel(rb.getString("PrefDir"));
        p.add(users);
        p.add(userLocation);
        userLocation.setColumns(30);
        userLocation.setText(jmri.jmrit.XmlFile.userFileLocationDefault());
        
        JButton bUser = new JButton(rb.getString("ButtonSetDots"));
        final JFileChooser fcUser;
        fcUser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

        fcUser.setDialogTitle(rb.getString("MessageSelectDirectory"));
        fcUser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcUser.setAcceptAllFileFilterUsed(false);
        bUser.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // get the file
                fcUser.showOpenDialog(null);
                if (fcUser.getSelectedFile()==null) return; // cancelled
                userLocation.setText(fcUser.getSelectedFile()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        p.add(bUser);
        return p;
    }

    private static JTextField scriptLocation = new JTextField();
    private static JTextField userLocation = new JTextField();
    //protected static JTextField throttleLocation = new JTextField();

}

