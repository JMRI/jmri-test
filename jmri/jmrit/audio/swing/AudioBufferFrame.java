// AudioBufferFrame.java

package jmri.jmrit.audio.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;
import jmri.util.FileChooserFilter;
import jmri.util.FileUtil;

/**
 * Defines a GUI to edit AudioBuffer objects
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.3 $
 */
public class AudioBufferFrame extends AbstractAudioFrame {

    private static int counter = 1;

    private boolean _newBuffer;

    // UI components for Add/Edit Buffer
    JLabel urlLabel = new JLabel(rba.getString("LabelURL"));
    JTextField url = new JTextField(40);
    JButton buttonBrowse = new JButton(rba.getString("ButtonBrowse"));
//    JLabel formatLabel = new JLabel(rba.getString("LabelFormat"));
//    JTextField format = new JTextField(20);
    JLabel loopStartLabel = new JLabel(rba.getString("LabelLoopStart"));
    JSpinner loopStart = new JSpinner();
    JLabel loopEndLabel = new JLabel(rba.getString("LabelLoopEnd"));
    JSpinner loopEnd = new JSpinner();
    JFileChooser fileChooser;


    public AudioBufferFrame(String title, AudioTableDataModel model) {
        super(title, model);
        layoutFrame();

        // For now, disable editing of loop points
        // TODO: enable editing of looping points
        loopStart.setEnabled(false);
        loopStartLabel.setEnabled(false);
        loopEnd.setEnabled(false);
        loopEndLabel.setEnabled(false);
    }

    @Override
    public void layoutFrame() {
        super.layoutFrame();
        JPanel p;

        JPanel p2;
        p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(rba.getString("LabelSample")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p2 = new JPanel(); p2.setLayout(new FlowLayout());
        p2.add(urlLabel);
        p2.add(url);
        buttonBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browsePressed(e);
            }
        });
        p2.add(buttonBrowse);
        p.add(p2);
//        p2 = new JPanel(); p2.setLayout(new FlowLayout());
//        p2.add(formatLabel);
//        p2.add(format);
//        p.add(p2);
        main.add(p);

        p = new JPanel(); p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(rba.getString("LabelLoopPoints")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(loopStartLabel);
        loopStart.setPreferredSize(new JTextField(8).getPreferredSize());
        loopStart.setModel(
                new SpinnerNumberModel(new Long(0), new Long(0), new Long(Long.MAX_VALUE), new Long(1)));
        loopStart.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                loopEnd.setValue(
                        ((Long)loopStart.getValue()
                        <(Long)loopEnd.getValue())
                        ?loopEnd.getValue()
                        :loopStart.getValue());
            }
        });
        p.add(loopStart);
        p.add(loopEndLabel);
        loopEnd.setPreferredSize(new JTextField(8).getPreferredSize());
        loopEnd.setModel(
                new SpinnerNumberModel(new Long(0), new Long(0), new Long(Long.MAX_VALUE), new Long(1)));
        loopEnd.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                loopStart.setValue(
                        ((Long)loopEnd.getValue()
                        <(Long)loopStart.getValue())
                        ?loopEnd.getValue()
                        :loopStart.getValue());
            }
        });
        p.add(loopEnd);
        main.add(p);

        JButton ok;
        frame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        });
    }

    /**
     * Method to populate the Edit Buffer frame with default values
     */
    public void resetFrame() {
        sysName.setText("IAB"+counter++);
        userName.setText(null);
        url.setText(null);
//        format.setText(null);
        loopStart.setValue(new Long(0));
        loopEnd.setValue(new Long(0));

        this._newBuffer = true;
    }

    /**
     * Method to populate the Edit Buffer frame with current values
     */
    @Override
    public void populateFrame(Audio a) {
        super.populateFrame(a);
        AudioBuffer b = (AudioBuffer) a;
        url.setText(b.getURL());
//        format.setText(b.toString());
        loopStart.setValue(b.getStartLoopPoint());
        loopEnd.setValue(b.getEndLoopPoint());
        loopStart.setEnabled(true);
        loopStartLabel.setEnabled(true);
        loopEnd.setEnabled(true);
        loopEndLabel.setEnabled(true);

        this._newBuffer = false;
    }

    void browsePressed(ActionEvent e) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser("resources"+File.separator+"sounds"+File.separator);
            FileChooserFilter audioFileFilter = new FileChooserFilter("Audio Files (*.wav)");
            audioFileFilter.addExtension("wav");
            fileChooser.setFileFilter(audioFileFilter);
        }

        // Show dialog
        fileChooser.rescanCurrentDirectory();
        int retValue = fileChooser.showOpenDialog(this);

        // Process selection
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = new String();
            if (!url.getText().equals((fileName=FileUtil.getPortableFilename(file)))) {
                url.setText(fileName);
            }
        }
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        AudioBuffer b;
        try {
            AudioManager am = InstanceManager.audioManagerInstance();
            b = (AudioBuffer) am.provideAudio(sName);
            if (b==null) throw new AudioException("Problem creating buffer");
            if (_newBuffer && am.getByUserName(user)!=null) {
                am.deregister(b);
                counter--;
                throw new AudioException("Duplicate user name - please modify");
            }
            b.setUserName(user);
            if (_newBuffer || !b.getURL().equals(url.getText())) {
                b.setURL(url.getText());
                log.debug("After load, end loop point = " + b.getEndLoopPoint());
                //b.setStartLoopPoint((Long)loopStart.getValue());
                //b.setEndLoopPoint((Long)loopEnd.getValue());
            } else {
                if (!b.getURL().equals(url.getText())) {
                    log.debug("Sound changed from: " + b.getURL());
                    b.setURL(url.getText());
                }
            }

            // Notify changes
            model.fireTableDataChanged();
        } catch (AudioException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), rb.getString("AudioCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioBufferFrame.class.getName());

}

/* @(#)AudioBufferFrame.java */