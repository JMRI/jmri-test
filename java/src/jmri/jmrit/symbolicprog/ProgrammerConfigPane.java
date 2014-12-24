// ProgrammerConfigPane.java

package jmri.jmrit.symbolicprog;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provide GUI to configure symbolic programmer defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ProgrammerConfigPane extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3341676760826030384L;

	public ProgrammerConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel("Format:"));
        p.add(programmerBox = new JComboBox(jmri.jmrit.symbolicprog.ProgDefault.findListOfProgFiles()));
        programmerBox.setSelectedItem(jmri.jmrit.symbolicprog.ProgDefault.getDefaultProgFile());
        add(p);

        // also create the advanced panel
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        advancedPanel.add(showEmptyTabs = new JCheckBox("Show empty tabs"));
        showEmptyTabs.setSelected(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame.getShowEmptyPanes());
        advancedPanel.add(ShowCvNums = new JCheckBox("Show CV numbers in tool tips"));
        ShowCvNums.setSelected(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame.getShowCvNumbers());
        this.add(advancedPanel);
        this.add(javax.swing.Box.createVerticalGlue());
    }
    JComboBox programmerBox;

    /**
     * This constructor does nothing different than the default constructor.
     *
     * @deprecated since 3.9.5
     */
    public ProgrammerConfigPane(boolean include) {
        this();
    }
    
    public String getSelectedItem() {
        return (String) programmerBox.getSelectedItem();
    }

    public JPanel getAdvancedPanel() {
        return advancedPanel;
    }

    JPanel advancedPanel;
    JCheckBox showEmptyTabs;
    JCheckBox ShowCvNums;

    public boolean getShowEmptyTabs() { return showEmptyTabs.isSelected(); }
    public boolean getShowCvNums() { return ShowCvNums.isSelected(); }
}

