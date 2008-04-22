/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.config;

import com.dmdirc.Main;
import com.dmdirc.Precondition;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.InvalidConfigFileException;
import com.dmdirc.util.WeakList;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
    
/**
 * The identity manager manages all known identities, providing easy methods
 * to access them.
 * 
 * @author chris
 */
public final class IdentityManager {
    
    /** The identities that have been loaded into this manager. */
    private final static List<Identity> identities = new ArrayList<Identity>();
    
    /** The config managers that have registered with this manager. */
    private final static List<ConfigManager> managers = new WeakList<ConfigManager>();
    
    /** The identity file used for the global config. */
    private static Identity config;
    
    /** The identity file used for addon defaults. */
    private static Identity addonConfig;
    
    /** The config manager used for global settings. */
    private static ConfigManager globalconfig;
    
    /** Creates a new instance of IdentityManager. */
    private IdentityManager() {
    }
    
    /** Loads all identity files. */
    public static void load() {
        identities.clear();
        managers.clear();
        
        loadDefaults();
        loadUser();
        loadConfig();
        
        if (getProfiles().size() == 0) {
            Identity.buildProfile("Default Profile");
        }
        
        // Set up the identity used for the addons defaults
        final ConfigTarget target = new ConfigTarget();
        final Properties properties = new Properties();
        target.setGlobalDefault();
        target.setOrder(500000);
        properties.setProperty("identity.name", "Action defaults");        
        addonConfig = new Identity(properties, target);
        IdentityManager.addIdentity(addonConfig);
        
        if (!getGlobalConfig().hasOption("identity", "defaultsversion")) {
            Logger.userError(ErrorLevel.FATAL, "Default settings "
                    + "could not be loaded");
        }
    }
    
    /** Loads the default (built in) identities. */
    private static void loadDefaults() {
        final String[] targets = {"default", "modealiases"};
        final String dir = getDirectory();
        
        for (String target : targets) {
            final File file = new File(dir + target);
            if (!file.exists() || (file.isDirectory() && file.listFiles().length == 0)) {
                file.mkdirs();
                extractIdentities(target);
            }
        }
    }
    
    /**
     * Extracts the specific set of default identities to the user's identity
     * folder.
     * 
     * @param target The target to be extracted
     */
    private static void extractIdentities(final String target) {
        try {
            ResourceManager.getResourceManager().extractResources(
                    "com/dmdirc/config/defaults/" + target,
                    getDirectory() + target, false);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to extract default "
                    + "identities: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the directory used to store identities in.
     * 
     * @return The identity directory path
     */
    public static String getDirectory() {
        return Main.getConfigDir() + "identities" + System.getProperty("file.separator"); 
    }
    
    /** Loads user-defined identity files. */
    public static void loadUser() {
        final File dir = new File(getDirectory());
        
        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to create identity dir");
            }
        }
        
        loadUser(dir);
    }
     
    /**
     * Recursively loads files from the specified directory.
     * 
     * @param dir The directory to be loaded
     */
    @Precondition({
        "The specified File is not null",
        "The specified File is a directory"
    })
    private static void loadUser(final File dir) {
        Logger.assertTrue(dir != null);
        Logger.assertTrue(dir.isDirectory());
        
        if (dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Unable to load user identity files from "
                    + dir.getAbsolutePath());
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    loadUser(file);
                } else {
                    loadIdentity(file);
                }
            }
        }
    }
    
    /**
     * Loads an identity from the specified file. If the identity already
     * exists, it is told to reload instead.
     * 
     * @param file The file to load the identity from.
     */
    private static void loadIdentity(final File file) {
        synchronized (identities) {
            for (Identity identity : identities) {
                if (file.equals(identity.getFile().getFile().getFile())) {
                    try {
                        identity.reload();
                    } catch (IOException ex) {
                        Logger.userError(ErrorLevel.MEDIUM,
                                "I/O error when reloading identity file: "
                                + file.getAbsolutePath() + " (" + ex.getMessage() + ")");
                    } catch (InvalidConfigFileException ex) {
                        // Do nothing
                    }

                    return;
                }
            }
        }
        
        try {
            addIdentity(new Identity(file, false));
        } catch (InvalidIdentityFileException ex) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid identity file: " + file.getAbsolutePath()
                    + " (" + ex.getMessage() + ")");
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "I/O error when reading identity file: "
                    + file.getAbsolutePath());
        }
    }
    
    /** Loads the config identity. */
    private static void loadConfig() {
        try {
            final File file = new File(Main.getConfigDir() + "dmdirc.config");
            
            if (!file.exists()) {
                file.createNewFile();
            }
            
            config = new Identity(file, true);
            config.setOption("identity", "name", "Global config");
            addIdentity(config);
        } catch (InvalidIdentityFileException ex) {
            // This shouldn't happen as we're forcing it to global
            Logger.appError(ErrorLevel.HIGH, "Unable to load global config", ex);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "I/O error when loading file: "
                    + ex.getMessage());
        }
    }
    
    /**
     * Retrieves the identity used for the global config.
     *
     * @return The global config identity
     */
    public static Identity getConfigIdentity() {
        return config;
    }
    
    /**
     * Retrieves the identity used for addons defaults.
     * 
     * @return The addons defaults identity
     */
    public static Identity getAddonIdentity() {
        return addonConfig;
    }
    
    /**
     * Saves all modified identity files to disk.
     */
    public static void save() {
        synchronized (identities) {
            for (Identity identity : identities) {
                identity.save();
            }
        }
    }
    
    /**
     * Adds the specific identity to this manager.
     * @param identity The identity to be added
     */
    @Precondition("The specified Identity is not null")
    public static void addIdentity(final Identity identity) {
        Logger.assertTrue(identity != null);
        
        if (identities.contains(identity)) {
            removeIdentity(identity);
        }
        
        synchronized (identities) {
            identities.add(identity);
        }
        
        synchronized (managers) {
            for (ConfigManager manager : managers) {
                manager.checkIdentity(identity);
            }
        }
    }
    
    /**
     * Removes an identity from this manager.
     * @param identity The identity to be removed
     */
    @Precondition({
        "The specified Identity is not null",
        "The specified Identity has previously been added and not removed"
    })
    public static void removeIdentity(final Identity identity) {
        Logger.assertTrue(identity != null);
        Logger.assertTrue(identities.contains(identity));
        
        synchronized (identities) {
            identities.remove(identity);
        }
        
        synchronized (managers) {
            for (ConfigManager manager : managers) {
                manager.removeIdentity(identity);
            }
        }
    }
    
    /**
     * Adds a config manager to this manager.
     * @param manager The ConfigManager to add
     */
    @Precondition("The specified ConfigManager is not null")
    public static void addConfigManager(final ConfigManager manager) {
        Logger.assertTrue(manager != null);
        
        synchronized (managers) {
            managers.add(manager);
        }
    }
    
    /**
     * Retrieves a list of identities that serve as profiles.
     * @return A list of profiles
     */
    public static List<Identity> getProfiles() {
        final List<Identity> profiles = new ArrayList<Identity>();
        
        synchronized (identities) {
            for (Identity identity : identities) {
                if (identity.isProfile()) {
                    profiles.add(identity);
                }
            }
        }
        
        return profiles;
    }
    
    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified config manager.
     * 
     * @param manager The manager requesting sources
     * @return A list of all matching config sources
     */
    public static List<Identity> getSources(final ConfigManager manager) {
        
        final List<Identity> sources = new ArrayList<Identity>();
        
        synchronized (identities) {
            for (Identity identity : identities) {
                if (manager.identityApplies(identity)) {
                    sources.add(identity);
                }
            }
        }
        
        Collections.sort(sources);
        
        return sources;
    }
    
    /**
     * Retrieves the global config manager.
     *
     * @return The global config manager
     */
    public static synchronized ConfigManager getGlobalConfig() {
        if (globalconfig == null) {
            globalconfig = new ConfigManager("", "", "");
        }
        
        return globalconfig;
    }
    
    /**
     * Retrieves the config for the specified channel@network. The config is
     * created if it doesn't exist.
     * @param network The name of the network
     * @param channel The name of the channel
     * @return A config source for the channel
     */
    @Precondition({
        "The specified network is non-null and not empty",
        "The specified channel is non-null and not empty"
    })
    public static Identity getChannelConfig(final String network,
            final String channel) {
        if (network == null || network.isEmpty()) {
            throw new IllegalArgumentException("getChannelConfig called "
                    + "with null or empty network\n\nNetwork: " + network);
        }
        
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("getChannelConfig called "
                    + "with null or empty channel\n\nChannel: " + channel);
        }        

        final String myTarget = (channel + "@" + network).toLowerCase();
        
        synchronized (identities) {
            for (Identity identity : identities) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.CHANNEL
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }
        
        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setChannel(myTarget);
        
        return Identity.buildIdentity(target);
    }
    
    /**
     * Retrieves the config for the specified network. The config is
     * created if it doesn't exist.
     * @param network The name of the network
     * @return A config source for the network
     */
    @Precondition("The specified network is non-null and not empty")
    public static Identity getNetworkConfig(final String network) {
        if (network == null || network.isEmpty()) {
            throw new IllegalArgumentException("getNetworkConfig called "
                    + "with null or empty network\n\nNetwork:" + network);
        }

        final String myTarget = network.toLowerCase();
        
        synchronized (identities) {
            for (Identity identity : identities) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.NETWORK
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }
        
        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork(myTarget);
        
        return Identity.buildIdentity(target);
    }
    
    /**
     * Retrieves the config for the specified server. The config is
     * created if it doesn't exist.
     * 
     * @param server The name of the server
     * @return A config source for the server
     */
    @Precondition("The specified server is non-null and not empty")
    public static Identity getServerConfig(final String server) {
        if (server == null || server.isEmpty()) {
            throw new IllegalArgumentException("getServerConfig called "
                    + "with null or empty server\n\nServer: " + server);
        }
        
        final String myTarget = server.toLowerCase();
        
        synchronized (identities) {
            for (Identity identity : identities) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.SERVER
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }
        
        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setServer(myTarget);
        
        return Identity.buildIdentity(target);
    }    
    
}
