// CheckForUpdateAction.java

package apps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to check for more recent JMRI version.
 * Checks a jmri.org URL for information.
 *
 * @author	Bob Jacobsen    Copyright (C) 2007, 2014
 * @author  Matt Harris Copyright (C) 2008
 *
 * @version         $Revision$
 */
public class CheckForUpdateAction extends jmri.util.swing.JmriAbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6718445705950604552L;

	public CheckForUpdateAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public CheckForUpdateAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public CheckForUpdateAction() { super(Bundle.getMessage("TitleUpdate"));}

    public void actionPerformed(ActionEvent ev) {

        final JFrame frame = new JmriJFrame(Bundle.getMessage("TitleUpdate"), false, false); 
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        
        JTextArea text = new JTextArea();
        text.setEditable(false);
        frame.add(text);
        
        String productionrelease = "";
        String testrelease = "";
        
        InputStream in = null;
        try { 
            String urlname = "http://jmri.org/releaselist";
            URL url = new URL(urlname);
            in = url.openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // search for releases
            String line;
            while((line = reader.readLine()) != null) {
                if (line.startsWith("production")) productionrelease = getNumber(reader);
                if (line.startsWith("test")) testrelease = getNumber(reader);
            }
        } catch (java.net.MalformedURLException e) {
            log.error("Unexpected failure in URL parsing", e);
            return;
        }
        catch(FileNotFoundException e){
            log.debug("Unable to get version info from web"+e);
        }
        catch(IOException e){
            log.debug("Unexpected failure during reading"+e);
        }
        finally {
            try {
                if (in != null) in.close();
            } catch (IOException e1) { log.error("Exception closing input stream", e1);}
        }
        
        // add content here!
        text.append("Most recent production release: "+productionrelease+"\n");
        text.append("Most recent test release: "+testrelease+"\n");
        text.append("You have: "+jmri.Version.name()+"\n"); // cleaner form is getCanonicalVersion()
        
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        
        JButton go = new JButton(Bundle.getMessage("ButtonDownloadPage"));
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI("http://jmri.org/download"));
                } 
                catch (java.net.URISyntaxException e) {
                    log.error("Invalid page requested", e);
                }
                catch (java.io.IOException e) {
                    log.error("Could no load page", e);
                }
            }
        });
        p.add(go);

        JButton close = new JButton(Bundle.getMessage("ButtonClose"));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
        p.add(close);

        frame.add(p);
        frame.pack();

        // show
        frame.setVisible(true);

    }

    String getNumber(BufferedReader reader) throws java.io.IOException {
        String line = reader.readLine();
        line = reader.readLine();
        return line.substring(0, line.length()-1);  // drop trailing :
    }
    
    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    static Logger log = LoggerFactory.getLogger(CheckForUpdateAction.class.getName());

}

/* @(#)CheckForUpdateAction.java */
