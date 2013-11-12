package jmri.profile;

import java.awt.Frame;
import java.io.IOException;

/**
 *
 * @author rhwood
 */
public class ProfileManagerSwingUtil {

    public static Profile promptForProfile(Frame f) throws IOException {
        if (ProfileManager.defaultManager().getActiveProfile() == null) {
            ProfileManager.defaultManager().readActiveProfile();
            // Automatically start with only profile if only one profile
            if (ProfileManager.defaultManager().getProfiles().length == 1) {
                ProfileManager.defaultManager().setActiveProfile(ProfileManager.defaultManager().getProfiles(0));
                // Display profile selector if user did not choose to auto start with last used profile
            } else if (!ProfileManager.defaultManager().isAutoStartActiveProfile()) {
                ProfileManagerDialog pmd = new ProfileManagerDialog(f, true);
                pmd.setLocationRelativeTo(f);
                pmd.setVisible(true);
                ProfileManager.defaultManager().saveActiveProfile();
            }
        }
        return ProfileManager.defaultManager().getActiveProfile();
    }

}
