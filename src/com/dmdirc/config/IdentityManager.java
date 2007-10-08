/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The identity manager manages all known identities, providing easy methods
 * to access them.
 * @author chris
 */
public final class IdentityManager {
    
    /** The identities that have been loaded into this manager. */
    private final static List<Identity> identities = new ArrayList<Identity>();
    
    /** The config managers that have registered with this manager. */
    private final static List<ConfigManager> managers = new ArrayList<ConfigManager>();
    
    /** The identity file used for the global config. */
    private static Identity config;
    
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
    }
    
    /** Loads the default (built in) identities. */
    private static void loadDefaults() {
        final ClassLoader cldr = IdentityManager.class.getClassLoader();
        
        final String base = "com/dmdirc/config/defaults/";
        
        final String[] urls = {"asuka", "bahamut", "defaults", "generic", 
        "hyperion", "ircu", "plexus", "snircd", "unreal", "inspircd",  };
        
        for (String url : urls) {
            try {
                final InputStream res = cldr.getResourceAsStream(base + url);
                if (res == null) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load default identity: " + url);
                } else {
                    addIdentity(new Identity(res, false));
                }
            } catch (InvalidIdentityFileException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Invalid identity file");
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to load identity file");
            }
        }
    }
    
    /** Loads user-defined identity files. */
    private static void loadUser() {
        final String fs = System.getProperty("file.separator");
        final String location = Main.getConfigDir() + "identities" + fs;
        final File dir = new File(location);
        
        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to create identity dir");
            }
        }
        
        if (dir == null || dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load user identity files");
        } else {
            for (File file : dir.listFiles()) {
                try {
                    addIdentity(new Identity(file, false));
                } catch (InvalidIdentityFileException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Invalid identity file");
                } catch (IOException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load identity file");
                }
            }
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
            Logger.userError(ErrorLevel.MEDIUM, "I/O error when loading file: " + ex.getMessage());
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
     * Saves all modified identity files to disk.
     */
    public static void save() {
        for (Identity identity : identities) {
            identity.save();
        }
    }
    
    /**
     * Adds the specific identity to this manager.
     * @param identity The identity to be added
     */
    public static void addIdentity(final Identity identity) {
        Logger.doAssertion(identity != null);
        
        identities.add(identity);
        
        for (ConfigManager manager : managers) {
            manager.checkIdentity(identity);
        }
    }
    
    /**
     * Removes an identity from this manager.
     * @param identity The identity to be removed
     */
    public static void removeIdentity(final Identity identity) {
        Logger.doAssertion(identity != null && identities.contains(identity));
        
        identities.remove(identity);
    }
    
    /**
     * Adds a config manager to this manager.
     * @param manager The ConfigManager to add
     */
    public static void addConfigManager(final ConfigManager manager) {
        Logger.doAssertion(manager != null);
        
        managers.add(manager);
    }
    
    /**
     * Retrieves a list of identities that serve as profiles.
     * @return A list of profiles
     */
    public static List<Identity> getProfiles() {
        final List<Identity> profiles = new ArrayList<Identity>();
        
        for (Identity identity : identities) {
            if (identity.isProfile()) {
                profiles.add(identity);
            }
        }
        
        return profiles;
    }
    
    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified target.
     * @param ircd The server's ircd
     * @param network The name of the network
     * @param server The server's name
     * @param channel The channel name (in the form channel@network)
     * @return A list of all matching config sources
     */
    public static List<Identity> getSources(final String ircd,
            final String network, final String server, final String channel) {
        
        final List<Identity> sources = new ArrayList<Identity>();
        
        String comp = "";
        
        for (Identity identity : identities) {
            switch (identity.getTarget().getType()) {
            case IRCD:
                comp = ircd;
                break;
            case NETWORK:
                comp = network;
                break;
            case SERVER:
                comp = server;
                break;
            case CHANNEL:
                comp = channel;
                break;
            case PROFILE:
                comp = null;
                break;
            default:
                comp = "";
                break;
            }
            
            if (comp != null && comp.equalsIgnoreCase(identity.getTarget().getData())) {
                sources.add(identity);
            }
        }
        
        Collections.sort(sources);
        
        return sources;
    }
    
    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified target.
     * @param ircd The server's ircd
     * @param network The name of the network
     * @param server The server's name
     * @return A list of all matching config sources
     */
    public static List<Identity> getSources(final String ircd,
            final String network, final String server) {
        return getSources(ircd, network, server, "<Unknown>");
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
    public static Identity getChannelConfig(final String network,
            final String channel) {
        Logger.doAssertion(network != null && !network.isEmpty()
               && channel != null && !channel.isEmpty());
        
        final String myTarget = (channel + "@" + network).toLowerCase();
        
        for (Identity identity : identities) {
            if (identity.getTarget().getType() == ConfigTarget.TYPE.CHANNEL
                    && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                return identity;
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
    public static Identity getNetworkConfig(final String network) {
        Logger.doAssertion(network != null && !network.isEmpty());
        
        final String myTarget = network.toLowerCase();
        
        for (Identity identity : identities) {
            if (identity.getTarget().getType() == ConfigTarget.TYPE.NETWORK
                    && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                return identity;
            }
        }
        
        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork(myTarget);
        
        return Identity.buildIdentity(target);
    }
    
}
