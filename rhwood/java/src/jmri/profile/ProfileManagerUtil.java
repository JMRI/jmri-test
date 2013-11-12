/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.profile;

import java.io.File;
import java.io.IOException;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ProfileManagerUtil {

    private static final Logger log = LoggerFactory.getLogger(ProfileManagerUtil.class);

    /**
     * Create a default profile if no profiles exist.
     *
     * @return A new profile or null if profiles already exist.
     * @throws IOException
     */
    public static Profile createDefaultProfile() throws IllegalArgumentException, IOException {
        if (ProfileManager.defaultManager().getProfiles().length == 0) {
            String pn = Bundle.getMessage("defaultProfileName");
            String pid = FileUtil.sanitizeFilename(pn);
            File pp = new File(FileUtil.getPreferencesPath() + pid);
            Profile profile = new Profile(pn, pid, pp);
            ProfileManager.defaultManager().addProfile(profile);
            log.info("Created default profile \"{}\"", pn);
            return profile;
        } else {
            return null;
        }
    }

    /**
     * Copy a JMRI configuration not in a profile and its user preferences to a
     * profile.
     *
     * @param config
     * @param name
     * @return The profile with the migrated configuration.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static Profile migrateConfigToProfile(File config, String name) throws IllegalArgumentException, IOException {
        String pid = FileUtil.sanitizeFilename(name);
        File pp = new File(FileUtil.getPreferencesPath(), pid);
        Profile profile = new Profile(name, pid, pp);
        FileUtil.copy(config, new File(profile.getPath(), Profile.CONFIG_FILENAME));
        FileUtil.copy(new File(config.getParentFile(), "UserPrefs" + config.getName()), new File(profile.getPath(), "UserPrefs" + Profile.CONFIG_FILENAME)); // NOI18N
        ProfileManager.defaultManager().addProfile(profile);
        log.info("Migrated \"{}\" config to profile \"{}\"", name, name);
        return profile;
    }

    public static void migrateToProfiles(String configFilename) throws IllegalArgumentException, IOException {
        File configFile;
        if (!new File(configFilename).isAbsolute()) {
            configFile = new File(FileUtil.getPreferencesPath() + configFilename);
        } else {
            configFile = new File(configFilename);
        }
        if (ProfileManager.defaultManager().getProfiles().length == 0) { // - PCat - PConf
            if (!configFile.exists()) { // - PCat - PConf - XConf = new use
                ProfileManager.defaultManager().setActiveProfile(ProfileManagerUtil.createDefaultProfile());
            } else { // - PCat - PConf + XConf = migrate
                ProfileManager.defaultManager().setActiveProfile(ProfileManagerUtil.migrateConfigToProfile(configFile, jmri.Application.getApplicationName()));
            }
        } else if (configFile.exists()) { // + PCat - PConf + XConf = migrate
            ProfileManager.defaultManager().setActiveProfile(ProfileManagerUtil.migrateConfigToProfile(configFile, jmri.Application.getApplicationName()));
        } // all other cases need no prep
    }

}
