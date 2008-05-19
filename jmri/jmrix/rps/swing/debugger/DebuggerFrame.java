// DebuggerFrame.java
 
 package jmri.jmrix.rps.swing.debugger;

import jmri.jmrix.rps.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Frame for manual operation and debugging of the RPS system
 *
 * @author	   Bob Jacobsen   Copyright (C) 2008
 * @version   $Revision: 1.2 $
 */


public class DebuggerFrame extends jmri.util.JmriJFrame 
            implements ReadingListener, MeasurementListener {

    public DebuggerFrame() {
        super();
        
        times = new JTextField[NUMSENSORS];
        residuals = new JLabel[NUMSENSORS];
        
        for (int i = 0; i < NUMSENSORS; i++) {
            times[i] = new JTextField(10);
            times[i].setText("");
            residuals[i] = new JLabel("          ");
        }
    }

    protected String title() { return "RPS Debugger"; }  // product name, not translated

    public void dispose() {
        // separate from data source
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
        // and unwind swing
        super.dispose();
    }

    java.text.NumberFormat nf;

    JComboBox mode;
    JButton doButton;
    
    static final int NUMSENSORS = 6;
    
    JTextField[] times;
    JLabel[] residuals;
    
    
    JTextField vs = new JTextField(18);
    JTextField offset = new JTextField(10);

    JTextField x = new JTextField(18);
    JTextField y = new JTextField(18);
    JTextField z = new JTextField(18);
    JLabel code = new JLabel();
    
    JTextField id = new JTextField(5);
    
    public void initComponents() {
        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add panes in the middle
        JPanel p, p1, p2;
 
        // Time inputs
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        p.add(new JLabel("Time measurements: "));
        
        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.GridLayout(NUMSENSORS, 2));
        
        for (int i = 0; i< NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r"+(i+1)+":"));
            p1.add(times[i]);   
            p3.add(p1);
            p1 = new JPanel();
            p1.add(new JLabel("r-t: "));
            p1.add(residuals[i]);
            p3.add(p1);         
        }
        p.add(p3);

        // add id field at bottom
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.add(new JLabel("Id: "));
        p5.add(id);
        p.add(p5);

        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // x, y, z results
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Results:"));
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("X:"));
            p1.add(x);
        p.add(p1);
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("Y:"));
            p1.add(y);
        p.add(p1);
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("Z:"));
            p1.add(z);
        p.add(p1);
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("Code:"));
            p1.add(code);
        p.add(p1);
        getContentPane().add(p);
        
        getContentPane().add(new JSeparator());

        // add controls at bottom
        p = new JPanel();
        
        mode = new JComboBox(new String[]{"From time fields", "from X,Y,Z fields", "from time file", "from X,Y,Z file"});
        p.add(mode);
        p.setLayout(new FlowLayout());
        
        doButton = new JButton("Do Once");
        doButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doOnce();
                }
            });
        p.add(doButton);
        getContentPane().add(p);
        
        // start working
        Distributor.instance().addReadingListener(this);
        Distributor.instance().addMeasurementListener(this);
        
        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.debugger.DebuggerFrame", true);

        // prepare for display
        pack();
    }
            
    /**
     * Invoked by button to do one cycle
     */
    void doOnce() {
        switch (mode.getSelectedIndex()) {
        default: // should not happen
            log.error("Did not expect selected mode "+mode.getSelectedIndex());
            return;
        case 0: // From time fields
            doReadingFromTimeFields();
            return;
        case 1: // From X,Y,Z fields
            doMeasurementFromPositionFields();
            return;
        case 2: // From time file
            try {
                doLoadReadingFromFile();
                doReadingFromTimeFields();
            } catch (java.io.IOException e) {log.error("exception "+e);}
            return;
        case 3: // From X,Y,Z file
            try {
                doLoadMeasurementFromFile();
            } catch (java.io.IOException e) {log.error("exception "+e);}
            return;
         
        }
        // Should not actually get here
    }
    
    void doLoadReadingFromFile() throws java.io.IOException {
        if (readingInput == null) {
            setupReadingFile();
        }
        
        // get and load a line
        if (!readingInput.readRecord()) {
            // read failed, try once to get another file
            setupReadingFile();
            if (!readingInput.readRecord()) throw new java.io.IOException("no valid file");
        }
        // item 0 is the ID, not used right now
        for (int i = 0; i< Math.min(NUMSENSORS, readingInput.getColumnCount()+1); i++) {
            times[i].setText(readingInput.get(i+1));
        }
    }
    
    void setupReadingFile() throws java.io.IOException {
        // get file
        readingInput = null;
        
        readingFileChooser.rescanCurrentDirectory();
        int retVal = readingFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
    
        // create and keep reader
        java.io.Reader reader = new java.io.FileReader(
                                        readingFileChooser.getSelectedFile());
        readingInput = new com.csvreader.CsvReader(reader);
    }
    
    void doLoadMeasurementFromFile() throws java.io.IOException {
        if (measurementInput == null) {
            setupMeasurementFile();
        }
        
        // get and load a line
        if (!measurementInput.readRecord()) {
            // read failed, try once to get another file
            setupMeasurementFile();
            if (!measurementInput.readRecord()) throw new java.io.IOException("no valid file");
        }

        // item 0 is the ID, not used right now
        Measurement m = new Measurement(null, 
                            Double.valueOf(measurementInput.get(1)).doubleValue(),
                            Double.valueOf(measurementInput.get(2)).doubleValue(),
                            Double.valueOf(measurementInput.get(3)).doubleValue(),
                            Engine.instance().getVSound(),
                            0,
                            "Data File"
                        );
        
        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }
    
    void setupMeasurementFile() throws java.io.IOException {
        // get file
        measurementInput = null;
        
        measurementFileChooser.rescanCurrentDirectory();
        int retVal = measurementFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
    
        // create and keep reader
        java.io.Reader reader = new java.io.FileReader(
                                        measurementFileChooser.getSelectedFile());
        measurementInput = new com.csvreader.CsvReader(reader);
    }

    void setResidual(int i, Measurement m) {
        Point3d p = Engine.instance().getReceiverPosition(i+1);
        Point3d x = new Point3d((float)m.getX(), (float)m.getY(), (float)m.getZ());
        
        double rt = p.distance(x)/Engine.instance().getVSound();
        int res = (int) (rt-m.getReading().getValue(i));
        residuals[i].setText(""+res);
    }
    
    Measurement lastPoint = null;
    
    Reading getReadingFromTimeFields() {
        // parse input
        int count = 0;
        for (int i = 0; i<NUMSENSORS; i++) {
            if (!times[i].getText().equals("")) count++;
        }
        
        double[] values = new double[count];
        
        int index = 0;
        for (int i = 0; i<NUMSENSORS; i++) {
            if (!times[i].getText().equals("")) {
                values[index] = Double.valueOf(times[i].getText()).doubleValue();
                index++;
            }
        }

        // get the id number
        int idnum = 21;
        try {
            idnum = Integer.valueOf(id.getText()).intValue();
        } catch (Exception e) {}

        Reading r = new Reading(idnum, values);
        return r;
    }
        
    void doReadingFromTimeFields() {
        // get the reading
        Reading r = getReadingFromTimeFields();

        // and forward
        Distributor.instance().submitReading(r);
    }
    
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work used.

        id.setText(""+r.getID());
        
        // Display this set of time values
        for (int i = 0; i<Math.min(r.getNSample(), times.length); i++) {
            times[i].setText(nf.format(r.getValue(i)));
        }
        
    }

    void doMeasurementFromPositionFields() {
        // contain dummy Reading
        int idnum = 21;
        try {
            idnum = Integer.valueOf(id.getText()).intValue();
        } catch (Exception e) {}
        
        Reading r = new Reading(idnum, new double[]{0.,0.,0.,0.});
        
        Measurement m = new Measurement(r, 
                            Double.valueOf(x.getText()).doubleValue(),
                            Double.valueOf(y.getText()).doubleValue(),
                            Double.valueOf(z.getText()).doubleValue(),
                            Engine.instance().getVSound(),
                            0,
                            "Position Data"
                        );

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }
    
    public void notify(Measurement m) {
        // show result
        x.setText(nf.format(m.getX()));
        y.setText(nf.format(m.getY()));
        z.setText(nf.format(m.getZ()));
        code.setText(""+m.getCode());
        try {
            for (int i=0; i<NUMSENSORS; i++) 
                setResidual(i, m);
        } catch (Exception e) {
            log.error("Error setting residual: "+e);
        }
    }
    
    // to find and remember the input files
    com.csvreader.CsvReader readingInput = null;
    final javax.swing.JFileChooser readingFileChooser = new JFileChooser("rps/readings.csv");

    com.csvreader.CsvReader measurementInput = null;
    final javax.swing.JFileChooser measurementFileChooser = new JFileChooser("rps/positions.csv");
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebuggerFrame.class.getName());
}
