// BoosterProgPanel.java

 package jmri.jmrix.nce.boosterprog;

import java.util.ResourceBundle;

import javax.swing.*;

import jmri.*;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Panel for configuring a NCE booster
 *
 * @author	ken cameron Copyright (C) 2010
 * Derived from BoosterProgFrame by
 * @author		Bob Jacobsen   Copyright (C) 2004
 * @version             $Revision: 1.1.2.1 $
 */
public class BoosterProgPanel extends jmri.jmrix.nce.swing.NcePanel {
	
    JTextField start = new JTextField(6);
    JTextField length = new JTextField(12);

    JLabel status = new JLabel();
    
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.boosterprog.BoosterProgBundle");

    private NceTrafficController tc = null;
    
    public BoosterProgPanel() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				//log.error("BoosterProg initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.boosterprog.BoosterProgBundle"; }
    
    public String getTitle() { 
        return rb.getString("TitleBoosterProg"); 
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
    	this.memo = m;
        this.tc = m.getNceTrafficController();
        
        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // install items in GUI, one line at a time
        
        // box of entries
        
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        
        add(new JLabel(rb.getString("Warn1")));
        add(new JLabel(rb.getString("Warn2")));
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton b = new JButton(rb.getString("ButtonSet"));
        p.add(new JLabel(rb.getString("LabelStart")));
        start.setText("30");
        p.add(start);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setStartPushed();
            }
        });
        box.add(p);
        
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        b = new JButton(rb.getString("ButtonSet"));
        p.add(new JLabel(rb.getString("LabelDuration")));
        length.setText("420");
        p.add(length);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDurationPushed();
            }
        });
        box.add(p);
        
        add(box);
        
        add(status);
        status.setText(rb.getString("StatusOK"));
    }

    static Programmer p = null;
    
    static void getProgrammer() {
        p = InstanceManager.programmerManagerInstance().
                            getAddressedProgrammer(true, 0);
    }
    
    static void releaseProgrammer() {
        if (p!=null)
            InstanceManager.programmerManagerInstance().
                            releaseAddressedProgrammer(p);
        p = null;
    }
    
    void setStartPushed() {
        getProgrammer();
        status.setText(rb.getString("StatusProgramming"));
        int val = Integer.parseInt(start.getText());
        
        try {
           p.writeCV(255, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    status.setText(rb.getString("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(rb.getString("StatusError")+e);
        } finally { releaseProgrammer(); }
    }
    
    static public void setStart(int val) {
        getProgrammer();
        
        try {
           p.writeCV(255, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                }
            });
        } catch (ProgrammerException e) {
        } finally { releaseProgrammer(); }
    }
    
    static public void setDuration(final int val) {
        getProgrammer();
        
        try {
           p.writeCV(253, val/256, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                    try {
                        p.writeCV(254, val % 256, new ProgListener() {
                            public void programmingOpReply(int value, int retval) {}
                        });
                    } catch (ProgrammerException e) {
                    } finally { releaseProgrammer(); }
                }
            });
        } catch (ProgrammerException e) {
            releaseProgrammer();
        } 
    }
    
    void setDurationPushed() {
        getProgrammer();
        status.setText(rb.getString("StatusProgramming"));
        int val = Integer.parseInt(length.getText())/256;
        
        try {
           p.writeCV(253, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {}
                    }
                    durationPart2();
                }
            });
        } catch (ProgrammerException e) {
            status.setText(rb.getString("StatusError")+e);
            releaseProgrammer();
        } 
    }
    
    void durationPart2() {
        status.setText(rb.getString("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) % 256;
        
        try {
           p.writeCV(254, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    status.setText(rb.getString("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(rb.getString("StatusError")+e);
        } finally { releaseProgrammer(); }
    }
}
