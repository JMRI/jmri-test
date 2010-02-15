package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Reporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * An icon to display info from a Reporter, e.g. transponder or RFID reader.<P>
 *
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.22 $
 */

public class ReporterIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public ReporterIcon(Editor editor) {
        // super ctor call to make sure this is a String label
        super("???", editor);
        setDisplayLevel(Editor.LABELS);
        setText("???");
    }

    // the associated Reporter object
    Reporter reporter = null;

    /**
     * Attached a named Reporter to this display item
     * @param pName Used as a system/user name to lookup the Reporter object
     */
     public void setReporter(String pName) {
         if (InstanceManager.reporterManagerInstance()!=null) {
             reporter = InstanceManager.reporterManagerInstance().
                 provideReporter(pName);
             if (reporter != null) {
                 setReporter(reporter);
             } else {
                 log.error("Reporter '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No ReporterManager for this protocol, icon won't see changes");
         }
     }

    public void setReporter(Reporter r) {
        if (reporter != null) {
            reporter.removePropertyChangeListener(this);
        }
        reporter = r;
        if (reporter != null) {
            displayState();
            reporter.addPropertyChangeListener(this);
        }
    }

    public Reporter getReporter() { return reporter; }

    // update icon as state changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
        displayState();
    }

    public String getNameString() {
        String name;
        if (reporter == null) name = rb.getString("NotConnected");
        else if (reporter.getUserName()!=null)
            name = reporter.getUserName()+" ("+reporter.getSystemName()+")";
        else
            name = reporter.getSystemName();
        return name;
    }


    /**
     * Drive the current state of the display from the state of the
     * Reporter.
     */
    void displayState() {
        if (reporter.getCurrentReport()!=null) {
        	if (reporter.getCurrentReport().equals(""))
        		setText(rb.getString("Blank"));
        	else
        	 	setText(reporter.getCurrentReport().toString());
        } else {
        	setText(rb.getString("NoReport"));
		}
		updateSize();
        return;
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editReporter();
            }
        };
        _iconEditorFrame = makeAddIconFrame("EditReporter", "addReportValueToPanel", 
                                     "SelectReporter", _iconEditor, this);
        _iconEditor.setPickList(PickListModel.reporterPickModelInstance());
        _iconEditor.complete(addIconAction, null, true, true);
        _iconEditor.setSelection(reporter);

    }
    void editReporter() {
        setReporter((Reporter)_iconEditor.getTableSelection());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    public void dispose() {
        reporter.removePropertyChangeListener(this);
        reporter = null;
        
        super.dispose();
    }

    public int maxHeight() {
        return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
    }
    public int maxWidth() {
        return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterIcon.class.getName());
}
