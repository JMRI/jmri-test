package org.jmri.core.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.InstanceManager;
import jmri.jmrix.swing.ComponentFactory;
import org.jmri.application.JmriApplication;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
@ActionID(id = "org.jmri.core.ui.SystemMenuPresenter", category = "JMRI")
@ActionRegistration(displayName = "#SystemMenuPresenterTitle", lazy = false)
@ActionReference(path = "Menu/Connections")
@Messages({"SystemMenuPresenterTitle=Loading..."})
public class SystemMenuPresenter extends AbstractAction implements DynamicMenuContent, Runnable {

    private final ArrayList<JMenu> menus = new ArrayList<>();
    private final JMenu placeholder = new JMenu(Bundle.SystemMenuPresenterTitle());

    private static final long serialVersionUID = 5301119837256151484L;
    private final static Logger log = LoggerFactory.getLogger(SystemMenuPresenter.class);

    public SystemMenuPresenter() {
        this.menus.add(this.placeholder);
        this.getSystems();
    }

    @Override
    public JMenuItem[] getMenuPresenters() {
        return this.menus.toArray(new JMenuItem[0]);
    }

    @Override
    public JMenuItem[] synchMenuPresenters(JComponent[] jcs) {
        return this.getMenuPresenters();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new AssertionError("Should never be called");
    }

    private void getSystems() {
        JmriApplication app = Lookup.getDefault().lookup(JmriApplication.class);
        if (app.isShown()) {
            this.addMenus();
        } else {
            app.defer(JmriApplication.State.SHOWN, this::addMenus);
        }
    }

    private void addMenus() {

        // get ComponentFactory objects and create menus
        List<ComponentFactory> list = InstanceManager.getList(ComponentFactory.class);
        if (list != null) {
            for (ComponentFactory memo : list) {
                JMenu menu = memo.getMenu();
                this.addMenu(menu);
            }
        }

        // the following is somewhat brute-force!
        if (jmri.jmrix.acela.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.acela.AcelaMenu"));
        }

        if (jmri.jmrix.bachrus.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.bachrus.SpeedoMenu"));
        }

        if (jmri.jmrix.cmri.serial.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.cmri.CMRIMenu"));
        }

        if (jmri.jmrix.easydcc.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));
        }

        /*        if (jmri.jmrix.dcc4pc.ActiveFlag.isActive())
         this.addMenu(this.getMenu("jmri.jmrix.dcc4pc.Dcc4PcMenu"));*/
        if (jmri.jmrix.grapevine.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.grapevine.GrapevineMenu"));
        }

        if (jmri.jmrix.oaktree.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.oaktree.OakTreeMenu"));
        }

        if (jmri.jmrix.pricom.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.pricom.PricomMenu"));
        }

        if (jmri.jmrix.qsi.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.qsi.QSIMenu"));
        }

        if (jmri.jmrix.rps.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.rps.RpsMenu"));
        }

        if (jmri.jmrix.secsi.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.secsi.SecsiMenu"));
        }

        if (jmri.jmrix.sprog.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.sprog.SPROGMenu"));
        }

        if (jmri.jmrix.sprog.ActiveFlagCS.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.sprog.SPROGCSMenu"));
        }

        if (jmri.jmrix.srcp.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.srcp.SystemMenu"));
        }

        if (jmri.jmrix.tmcc.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.tmcc.TMCCMenu"));
        }

        /*        if (jmri.jmrix.wangrow.ActiveFlag.isActive())
         this.addMenu(this.getMenu("jmri.jmrix.wangrow.WangrowMenu"));*/
        if (jmri.jmrix.xpa.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.xpa.XpaMenu"));
        }

        if (jmri.jmrix.zimo.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.zimo.Mx1Menu"));
        }

        if (jmri.jmrix.direct.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.direct.DirectMenu"));
        }

        if (jmri.jmrix.maple.ActiveFlag.isActive()) {
            this.addMenu(this.getMenu("jmri.jmrix.maple.MapleMenu"));
        }

        this.menus.remove(this.placeholder);

        if (this.menus.isEmpty()) {
            this.menus.add(new JMenu(""));
        }
        if (log.isDebugEnabled()) {
            log.debug("System Menu contains:");
            for (JMenu menu : this.menus) {
                log.info("    {}", menu.getText());
            }
        }
        FileUtil.getConfigFile("Menu").refresh(true);
    }

    private void addMenu(JMenu menu) {
        if (menu != null) {
            this.menus.add(menu);
        }
    }

    private JMenu getMenu(String className) {
        try {
            return (JMenu) Class.forName(className).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Could not load class {}", className, e);
            return null;
        }
    }

    @Override
    public void run() {
        // Do nothing
    }
}
