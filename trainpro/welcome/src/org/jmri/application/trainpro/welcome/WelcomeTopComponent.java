/*============================================================================*
 * WARNING      This class contains automatically modified code.      WARNING *
 *                                                                            *
 * The method initComponents() and the variable declarations between the      *
 * "// Variables declaration - do not modify" and                             *
 * "// End of variables declaration" comments will be overwritten if modified *
 * by hand. Using the NetBeans IDE to edit this file is strongly recommended. *
 *                                                                            *
 * See http://jmri.org/help/en/html/doc/Technical/NetBeansGUIEditor.shtml for *
 * more information.                                                          *
 *============================================================================*/
package org.jmri.application.trainpro.welcome;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.modules.Modules;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.jmri.application.trainpro.welcome//Welcome//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WelcomeTopComponent",
        iconBase = "org/jmri/application/trainpro/welcome/welcome.gif",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@Messages({
    "CTL_WelcomeTopComponent=Start Page",
    "HINT_WelcomeTopComponent=Welcome to JMRI TrainPro"
})
public final class WelcomeTopComponent extends TopComponent implements HyperlinkListener {

    private final static Logger log = LoggerFactory.getLogger(WelcomeTopComponent.class);

    public WelcomeTopComponent() {
        initComponents();
        setName(Bundle.CTL_WelcomeTopComponent());
        setToolTipText(Bundle.HINT_WelcomeTopComponent());
        jScrollPane1.getViewport().setOpaque(false);
    }

    private static long getModified(URL url) {
        try {
            URLConnection conn = url.openConnection();
            long lastMod = conn.getLastModified();
            if (lastMod != 0) {
                log.info("Found getLastModified of {0}", lastMod);
                return lastMod;
            } else {
                log.info("Returning hash code of content", lastMod);
                String content = getContent(conn);
                return content.hashCode();
            }
        } catch (IOException ex) {
            log.info("Loading welcome page modified date from web failed", ex);
        }
        return 0;
    }

    public void loadPage() {
        try {
            URL startUrl = new URL(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.http.link",
                    Modules.getDefault().findCodeNameBase("org.jmri.application.trainpro").getSpecificationVersion()));
            long lastMod = getModified(startUrl);
            NbPreferences.forModule(getClass()).putLong("LAST_PAGE_UPDATE", lastMod);
            if (lastMod == 0) {
                startUrl = new URL(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.local.link"));
            }
            jEditorPane1.setPage(startUrl);
        } catch (IOException ex) {
            log.info("Loading welcome page from web failed", ex.getMessage());
            try {
                jEditorPane1.setPage(new URL(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.local.link")));
            } catch (IOException ex1) {
                log.error("Could not open local help page!", ex1.getMessage());
            }
        }
    }

    public static void checkOpen() {
        checkOpen(0);
    }

    public static void checkOpen(long lastMod) {
        try {
            log.info("Checking open...");
            long lastCheck = NbPreferences.forModule(WelcomeTopComponent.class).getLong("LAST_PAGE_UPDATE", 0);
            URL startUrl = new URL(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.http.link",
                    Modules.getDefault().findCodeNameBase("org.jmri.application.trainpro").getSpecificationVersion()));
            if (lastMod == 0) {
                lastMod = getModified(startUrl);
            }
            log.info("Checking page id {} vs stored id {}", lastMod, lastCheck);
            if (lastCheck != lastMod) {
                WelcomeTopComponent tc = (WelcomeTopComponent) WindowManager.getDefault().findTopComponent("WelcomeTopComponent");
                if (tc != null) {
                    tc.open();
                    tc.requestActive();
                } else {
                    log.warn("Did not find Welcome Screen window");
                }
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent he) {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (Desktop.isDesktopSupported()) {
                try {
                    try {
                        Desktop.getDesktop().browse((new URL(MessageFormat.format(he.getURL().toString(),
                                Modules.getDefault().findCodeNameBase("org.jmri.application.trainpro").getSpecificationVersion()))).toURI()); // NOI18N
                    } catch (IllegalArgumentException e) {
                        Desktop.getDesktop().browse(he.getURL().toURI());
                    }
                } catch (IOException | URISyntaxException e) {
                    log.error("Unable to open {} in desktop browser: {}", he.getURL(), e.getMessage());
                }
            }
        }
    }

    private static String getContent(URLConnection connection) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException ex) {
            log.info("Reading welcome page content from web failed: {}", ex.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.info("Closing reader for welcome page content from web failed: {}", ex.getMessage());
            }
        }
        return "";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new org.jmri.application.trainpro.welcome.GradientPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(170, 170, 204));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jEditorPane1.setOpaque(false);
        jScrollPane1.setViewportView(jEditorPane1);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jmri/application/trainpro/welcome/logo.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Helvetica", 1, 48)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addContainerGap(314, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private org.jmri.application.trainpro.welcome.GradientPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        this.loadPage();
        jEditorPane1.addHyperlinkListener(this);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
