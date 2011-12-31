/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.interfaces.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.Version;
import com.dmdirc.util.collections.MapList;
import com.dmdirc.util.collections.WeakMapList;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * The identity manager manages all known identities, providing easy methods
 * to access them.
 */
public class IdentityManager implements IdentityFactory, IdentityController {

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER = java.util.logging
            .Logger.getLogger(IdentityManager.class.getName());

    /** A singleton instance of IdentityManager. */
    private static final IdentityManager INSTANCE = new IdentityManager();

    /**
     * The identities that have been loaded into this manager.
     *
     * Standard identities are inserted with a <code>null</code> key, custom
     * identities use their custom type as the key.
     */
    private final MapList<String, Identity> identities
            = new MapList<String, Identity>();

    /**
     * The {@link IdentityListener}s that have registered with this manager.
     *
     * Listeners for standard identities are inserted with a <code>null</code>
     * key, listeners for a specific custom type use their type as the key.
     */
    private final MapList<String, IdentityListener> listeners
            = new WeakMapList<String, IdentityListener>();

    /** The identity file used for the global config. */
    private Identity config;

    /** The identity file used for addon defaults. */
    private Identity addonConfig;

    /** The identity file bundled with the client containing version info. */
    private Identity versionConfig;

    /** The config manager used for global settings. */
    private ConfigManager globalconfig;

    /** Creates a new instance of IdentityManager. */
    private IdentityManager() {
    }

    /** {@inheritDoc} */
    @Override
    public void initialise() throws InvalidIdentityFileException {
        identities.clear();

        loadVersionIdentity();
        loadDefaults();
        loadUserIdentities();
        loadConfig();

        if (getIdentitiesByType("profile").isEmpty()) {
            try {
                Identity.buildProfile("Default Profile");
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.FATAL, "Unable to write default profile", ex);
            }
        }

        // Set up the identity used for the addons defaults
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();
        target.setOrder(500000);

        final ConfigFile addonConfigFile = new ConfigFile((File) null);
        final Map<String, String> addonSettings = new HashMap<String, String>();
        addonSettings.put("name", "Addon defaults");
        addonConfigFile.addDomain("identity", addonSettings);

        addonConfig = new Identity(addonConfigFile, target);
        registerIdentity(addonConfig);

        if (!getGlobalConfiguration().hasOptionString("identity", "defaultsversion")) {
            Logger.userError(ErrorLevel.FATAL, "Default settings "
                    + "could not be loaded");
        }
    }

    /**
     * Loads all identity files.
     *
     * @throws InvalidIdentityFileException If there is an error with the config
     *                                      file.
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static void load() throws InvalidIdentityFileException {
        INSTANCE.initialise();
    }

    /** Loads the default (built in) identities. */
    private void loadDefaults() {
        final String[] targets = {"default", "modealiases"};
        final String dir = getDirectory();

        for (String target : targets) {
            final File file = new File(dir + target);

            if (file.exists() && !file.isDirectory()) {
                boolean success = false;
                for (int i = 0; i < 10 && !success; i++) {
                    final String suffix = ".old" + (i > 0 ? "-" + i : "");
                    success = file.renameTo(new File(file.getParentFile(), target + suffix));
                }

                if (!success) {
                    Logger.userError(ErrorLevel.HIGH, "Unable to create directory for "
                            + "default settings folder (" + target + ")", "A file "
                            + "with that name already exists, and couldn't be renamed."
                            + " Rename or delete " + file.getAbsolutePath());
                    continue;
                }
            }

            if (!file.exists() || file.listFiles() == null || file.listFiles().length == 0) {
                file.mkdirs();
                extractIdentities(target);
            }

            loadUser(file);
        }

        extractFormatters();

        // If the bundled defaults are newer than the ones the user is
        // currently using, extract them.
        if (getGlobalConfiguration().hasOptionString("identity", "defaultsversion")
                && getGlobalConfiguration().hasOptionString("updater", "bundleddefaultsversion")) {
            final Version installedVersion = new Version(getGlobalConfiguration()
                    .getOption("identity", "defaultsversion"));
            final Version bundledVersion = new Version(getGlobalConfiguration()
                    .getOption("updater", "bundleddefaultsversion"));

            if (bundledVersion.compareTo(installedVersion) > 0) {
                extractIdentities("default");
                loadUser(new File(dir, "default"));
            }
        }
    }

    /**
     * Extracts the bundled formatters to the user's identity folder.
     */
    private void extractFormatters() {
        try {
            ResourceManager.getResourceManager().extractResource(
                    "com/dmdirc/config/defaults/default/formatter",
                    getIdentityDirectory() + "default/", false);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to extract default "
                    + "formatters: " + ex.getMessage());
        }
    }

    /**
     * Extracts the specific set of default identities to the user's identity
     * folder.
     *
     * @param target The target to be extracted
     */
    private void extractIdentities(final String target) {
        try {
            ResourceManager.getResourceManager().extractResources(
                    "com/dmdirc/config/defaults/" + target,
                    getIdentityDirectory() + target, false);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to extract default "
                    + "identities: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getIdentityDirectory() {
        return Main.getConfigDir() + "identities" + System.getProperty("file.separator");
    }

    /**
     * Retrieves the directory used to store identities in.
     *
     * @return The identity directory path
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static String getDirectory() {
        return INSTANCE.getIdentityDirectory();
    }

    /** {@inheritDoc} */
    @Override
    public void loadUserIdentities() {
        final File dir = new File(getIdentityDirectory());

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
     * Loads user-defined identity files.
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static void loadUser() {
        INSTANCE.loadUserIdentities();
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
    private void loadUser(final File dir) {
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
    private void loadIdentity(final File file) {
        synchronized (identities) {
            for (Identity identity : getAllIdentities()) {
                if (identity.isFile(file)) {
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
            registerIdentity(new Identity(file, false));
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

    /**
     * Retrieves all known identities.
     *
     * @return A set of all known identities
     * @since 0.6.4
     */
    private Set<Identity> getAllIdentities() {
        final Set<Identity> res = new LinkedHashSet<Identity>();

        for (Map.Entry<String, List<Identity>> entry : identities.entrySet()) {
            res.addAll(entry.getValue());
        }

        return res;
    }

    /**
     * Returns the "group" to which the specified identity belongs. For custom
     * identities this is the custom identity type, otherwise this is
     * <code>null</code>.
     *
     * @param identity The identity whose group is being retrieved
     * @return The group of the specified identity
     * @since 0.6.4
     */
    private String getGroup(final Identity identity) {
        return identity.getTarget().getType() == ConfigTarget.TYPE.CUSTOM
                ? identity.getTarget().getData() : null;
    }

    /** {@inheritDoc} */
    @Override
    public void loadVersionIdentity() {
        try {
            versionConfig = new Identity(Main.class.getResourceAsStream("version.config"), false);
            registerIdentity(versionConfig);
        } catch (IOException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load version information", ex);
        } catch (InvalidIdentityFileException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load version information", ex);
        }
    }

    /**
     * Loads the version information.
     *
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static void loadVersion() {
        INSTANCE.loadVersionIdentity();
    }

    /**
     * Loads the config identity.
     *
     * @throws InvalidIdentityFileException if there is a problem with the
     * config file.
     */
    private void loadConfig() throws InvalidIdentityFileException {
        try {
            final File file = new File(Main.getConfigDir() + "dmdirc.config");

            if (!file.exists()) {
                file.createNewFile();
            }

            config = new Identity(file, true);
            config.setOption("identity", "name", "Global config");
            registerIdentity(config);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.FATAL, "I/O error when loading global config: "
                    + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Identity getGlobalConfigIdentity() {
        return config;
    }

    /**
     * Retrieves the identity used for the global config.
     *
     * @return The global config identity
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static Identity getConfigIdentity() {
        return INSTANCE.getGlobalConfigIdentity();
    }

    /** {@inheritDoc} */
    @Override
    public Identity getGlobalAddonIdentity() {
        return addonConfig;
    }

    /**
     * Retrieves the identity used for addons defaults.
     *
     * @return The addons defaults identity
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static Identity getAddonIdentity() {
        return INSTANCE.getGlobalAddonIdentity();
    }

    /** {@inheritDoc} */
    @Override
    public Identity getGlobalVersionIdentity() {
        return versionConfig;
    }

    /**
     * Retrieves the identity bundled with the DMDirc client containing
     * version information.
     *
     * @return The version identity
     * @since 0.6.3m2
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static Identity getVersionIdentity() {
        return INSTANCE.getGlobalVersionIdentity();
    }

    /** {@inheritDoc} */
    @Override
    public void saveAll() {
        synchronized (identities) {
            for (Identity identity : getAllIdentities()) {
                identity.save();
            }
        }
    }

    /**
     * Saves all modified identity files to disk.
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static void save() {
        INSTANCE.saveAll();
    }

    /** {@inheritDoc} */
    @Override
    public void registerIdentity(final Identity identity) {
        Logger.assertTrue(identity != null);

        final String target = getGroup(identity);

        if (identities.containsValue(target, identity)) {
            unregisterIdentity(identity);
        }

        synchronized (identities) {
            identities.add(target, identity);
        }

        LOGGER.log(Level.FINER, "Adding identity: {0} (group: {1})",
                new Object[]{identity, target});

        synchronized (listeners) {
            for (IdentityListener listener : listeners.safeGet(target)) {
                listener.identityAdded(identity);
            }
        }
    }

    /**
     * Adds the specific identity to this manager.
     *
     * @param identity The identity to be added
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition("The specified Identity is not null")
    public static void addIdentity(final Identity identity) {
        INSTANCE.registerIdentity(identity);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterIdentity(final Identity identity) {
        Logger.assertTrue(identity != null);

        final String group = getGroup(identity);

        Logger.assertTrue(identities.containsValue(group, identity));

        synchronized (identities) {
            identities.remove(group, identity);
        }

        synchronized (listeners) {
            for (IdentityListener listener : listeners.safeGet(group)) {
                listener.identityRemoved(identity);
            }
        }
    }

    /**
     * Removes an identity from this manager.
     * @param identity The identity to be removed
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition({
        "The specified Identity is not null",
        "The specified Identity has previously been added and not removed"
    })
    public static void removeIdentity(final Identity identity) {
        INSTANCE.unregisterIdentity(identity);
    }

    /** {@inheritDoc} */
    @Override
    public void registerIdentityListener(final IdentityListener listener) {
        registerIdentityListener(null, listener);
    }

    /**
     * Adds a new identity listener which will be informed of all settings
     * identities which are added to this manager.
     *
     * @param listener The listener to be added
     * @since 0.6.4
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition("The specified listener is not null")
    public static void addIdentityListener(final IdentityListener listener) {
        INSTANCE.registerIdentityListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void registerIdentityListener(final String type, final IdentityListener listener) {
        Logger.assertTrue(listener != null);

        synchronized (listeners) {
            listeners.add(type, listener);
        }
    }

    /**
     * Adds a new identity listener which will be informed of all identities
     * of the specified custom type which are added to this manager.
     *
     * @param type The type of identities to listen for
     * @param listener The listener to be added
     * @since 0.6.4
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition("The specified listener is not null")
    public static void addIdentityListener(final String type, final IdentityListener listener) {
        INSTANCE.registerIdentityListener(type, listener);
    }

    /** {@inheritDoc} */
    @Override
    public List<Identity> getIdentitiesByType(final String type) {
        return Collections.unmodifiableList(identities.safeGet(type));
    }

    /**
     * Retrieves a list of identities that belong to the specified custom type.
     *
     * @param type The type of identity to search for
     * @return A list of matching identities
     * @since 0.6.4
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static List<Identity> getCustomIdentities(final String type) {
        return INSTANCE.getIdentitiesByType(type);
    }

    /** {@inheritDoc} */
    @Override
    public List<Identity> getIdentitiesForManager(final ConfigManager manager) {
        final List<Identity> sources = new ArrayList<Identity>();

        synchronized (identities) {
            for (Identity identity : identities.safeGet(null)) {
                if (manager.identityApplies(identity)) {
                    sources.add(identity);
                }
            }
        }

        Collections.sort(sources);

        return sources;
    }

    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified config manager.
     *
     * @param manager The manager requesting sources
     * @return A list of all matching config sources
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static List<Identity> getSources(final ConfigManager manager) {
        return INSTANCE.getIdentitiesForManager(manager);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ConfigManager getGlobalConfiguration() {
        if (globalconfig == null) {
            globalconfig = new ConfigManager("", "", "", "");
        }

        return globalconfig;
    }

    /**
     * Retrieves the global config manager.
     *
     * @return The global config manager
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    public static ConfigManager getGlobalConfig() {
        return INSTANCE.getGlobalConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    public Identity createChannelConfig(final String network, final String channel) {
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
            for (Identity identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.CHANNEL
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setChannel(myTarget);

        try {
            return Identity.buildIdentity(target);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create channel identity", ex);
            return null;
        }
    }

    /**
     * Retrieves the config for the specified channel@network. The config is
     * created if it doesn't exist.
     *
     * @param network The name of the network
     * @param channel The name of the channel
     * @return A config source for the channel
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition({
        "The specified network is non-null and not empty",
        "The specified channel is non-null and not empty"
    })
    public static Identity getChannelConfig(final String network, final String channel) {
        return INSTANCE.createChannelConfig(network, channel);
    }

    /** {@inheritDoc} */
    @Override
    public Identity createNetworkConfig(final String network) {
        if (network == null || network.isEmpty()) {
            throw new IllegalArgumentException("getNetworkConfig called "
                    + "with null or empty network\n\nNetwork:" + network);
        }

        final String myTarget = network.toLowerCase();

        synchronized (identities) {
            for (Identity identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.NETWORK
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork(myTarget);

        try {
            return Identity.buildIdentity(target);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create network identity", ex);
            return null;
        }
    }

    /**
     * Retrieves the config for the specified network. The config is
     * created if it doesn't exist.
     *
     * @param network The name of the network
     * @return A config source for the network
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition("The specified network is non-null and not empty")
    public static Identity getNetworkConfig(final String network) {
        return INSTANCE.createNetworkConfig(network);
    }

    /** {@inheritDoc} */
    @Override
    public Identity createServerConfig(final String server) {
        if (server == null || server.isEmpty()) {
            throw new IllegalArgumentException("getServerConfig called "
                    + "with null or empty server\n\nServer: " + server);
        }

        final String myTarget = server.toLowerCase();

        synchronized (identities) {
            for (Identity identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.SERVER
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setServer(myTarget);

        try {
            return Identity.buildIdentity(target);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create network identity", ex);
            return null;
        }
    }

    /**
     * Retrieves the config for the specified server. The config is
     * created if it doesn't exist.
     *
     * @param server The name of the server
     * @return A config source for the server
     * @deprecated Use non-static methods instead
     */
    @Deprecated
    @Precondition("The specified server is non-null and not empty")
    public static Identity getServerConfig(final String server) {
        return INSTANCE.createServerConfig(server);
    }

    /**
     * Gets a singleton instance of the Identity Manager.
     *
     * @return A singleton instance of the IdentityManager.
     */
    public static IdentityManager getIdentityManager() {
        return INSTANCE;
    }

}
