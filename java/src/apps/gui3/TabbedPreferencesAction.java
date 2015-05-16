package apps.gui3;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.netbeans.api.options.OptionsDisplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tabbed Preferences Action for dealing with all the preferences in a single
 * view with a list option to the left hand side.
 * <P>
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision$
 */
public class TabbedPreferencesAction extends jmri.util.swing.JmriAbstractAction {

    private static final long serialVersionUID = 7214016766558164269L;

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s
     * @param category
     * @param subCategory
     */
    public TabbedPreferencesAction(String s, String category, String subCategory) {
        super(s);
        preferencesItem = category;
    }

    public TabbedPreferencesAction(String s, String category) {
        super(s);
        preferencesItem = category;
    }

    public TabbedPreferencesAction(String s) {
        super(s);
    }

    public TabbedPreferencesAction() {
        this(Bundle.getMessage("MenuItemPreferences"));
    }

    public TabbedPreferencesAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public TabbedPreferencesAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public TabbedPreferencesAction(String s, WindowInterface wi, String category, String subCategory) {
        super(s, wi);
        preferencesItem = category;
    }

    public TabbedPreferencesAction(String s, Icon i, WindowInterface wi, String category) {
        super(s, i, wi);
        preferencesItem = category;
    }

    String preferencesItem = null;

    public void actionPerformed() {
        this.showPreferences();
    }

    private void showPreferences() {
        if (preferencesItem != null) {
            OptionsDisplayer.getDefault().open(preferencesItem);
        }
        OptionsDisplayer.getDefault().open();
    }

    synchronized static void setWait(boolean boo) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    void setTitle() { //Note required as sub-panels will set them
    }

    String helpTarget() {
        return "package.apps.TabbedPreferences";
    }

    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    static Logger log = LoggerFactory.getLogger(TabbedPreferencesAction.class.getName());

}
