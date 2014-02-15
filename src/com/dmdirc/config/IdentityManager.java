/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.Precondition;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
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

import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The identity manager manages all known identities, providing easy methods
 * to access them.
 */
public class IdentityManager implements IdentityFactory, IdentityController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(IdentityManager.class);

    /** A regular expression that will match all characters illegal in file names. */
    private static final String ILLEGAL_CHARS = "[\\\\\"/:\\*\\?\"<>\\|]";

    /** The domain used for identity settings. */
    private static final String IDENTITY_DOMAIN = "identity";

    /** The domain used for profile settings. */
    private static final String PROFILE_DOMAIN = "profile";

    /** A singleton instance of IdentityManager. */
    private static IdentityManager instance;

    /** Base configuration directory where the main configuration file will be located. */
    private final String configDirectory;

    /** Directory to save and load identities in. */
    private final String identitiesDirectory;

    /**
     * The identities that have been loaded into this manager.
     *
     * Standard identities are inserted with a <code>null</code> key, custom
     * identities use their custom type as the key.
     */
    private final MapList<String, ConfigProvider> identities = new MapList<>();

    /**
     * The {@link IdentityListener}s that have registered with this manager.
     *
     * Listeners for standard identities are inserted with a <code>null</code>
     * key, listeners for a specific custom type use their type as the key.
     */
    private final MapList<String, ConfigProviderListener> listeners = new WeakMapList<>();

    /** The identity file used for the global config. */
    private ConfigProvider config;

    /** The identity file used for addon defaults. */
    private ConfigProvider addonConfig;

    /** The identity file bundled with the client containing version info. */
    private ConfigProvider versionConfig;

    /** The config manager used for global settings. */
    private AggregateConfigProvider globalconfig;

    /**
     * Creates a new instance of IdentityManager.
     *
     * @param baseDirectory The BASE config directory.
     * @param identitiesDirectory The directory to store identities in.
     */
    public IdentityManager(final String baseDirectory, final String identitiesDirectory) {
        this.configDirectory = baseDirectory;
        this.identitiesDirectory = identitiesDirectory;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public String getConfigurationDirectory() {
        return configDirectory;
    }

    /**
     * Loads all identity files.
     *
     * @throws InvalidIdentityFileException If there is an error with the config
     * file.
     */
    public void initialise() throws InvalidIdentityFileException {
        identities.clear();

        loadVersionIdentity();
        loadDefaults();
        loadUserIdentities();
        loadConfig();

        if (getProvidersByType("profile").isEmpty()) {
            createProfileConfig("Default Profile");
        }

        // Set up the identity used for the addons defaults
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();
        target.setOrder(500000);

        final ConfigFile addonConfigFile = new ConfigFile((File) null);
        final Map<String, String> addonSettings = new HashMap<>();
        addonSettings.put("name", "Addon defaults");
        addonConfigFile.addDomain("identity", addonSettings);

        addonConfig = new ConfigFileBackedConfigProvider(addonConfigFile, target);
        addConfigProvider(addonConfig);

        if (!getGlobalConfiguration().hasOptionString("identity", "defaultsversion")) {
            Logger.userError(ErrorLevel.FATAL, "Default settings "
                    + "could not be loaded");
        }
    }

    /** Loads the default (built in) identities. */
    private void loadDefaults() {
        final String[] targets = {"default", "modealiases"};

        for (String target : targets) {
            final File file = new File(identitiesDirectory + target);

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
                loadUser(new File(identitiesDirectory, "default"));
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
                    identitiesDirectory + "default/", false);
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
                    identitiesDirectory + target, false);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to extract default "
                    + "identities: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void loadUserIdentities() {
        final File dir = new File(identitiesDirectory);

        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to create identity dir", ex);
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
    private void loadUser(final File dir) {
        checkNotNull(dir);
        checkArgument(dir.isDirectory());

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
            for (ConfigProvider identity : getAllIdentities()) {
                if ((identity instanceof ConfigFileBackedConfigProvider) && ((ConfigFileBackedConfigProvider) identity).isFile(file)) {
                    // TODO: This manager should keep a list of files->identities instead of
                    //       relying on the identities remembering.
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
            addConfigProvider(new ConfigFileBackedConfigProvider(file, false));
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
    private Set<ConfigProvider> getAllIdentities() {
        final Set<ConfigProvider> res = new LinkedHashSet<>();

        for (Map.Entry<String, List<ConfigProvider>> entry : identities.entrySet()) {
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
    private String getGroup(final ConfigProvider identity) {
        return identity.getTarget().getType() == ConfigTarget.TYPE.CUSTOM
                ? identity.getTarget().getData() : null;
    }

    /** {@inheritDoc} */
    @Override
    public void loadVersionIdentity() {
        try {
            versionConfig = new ConfigFileBackedConfigProvider(IdentityManager.class.getResourceAsStream("/com/dmdirc/version.config"), false);
            addConfigProvider(versionConfig);
        } catch (IOException | InvalidIdentityFileException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load version information", ex);
        }
    }

    /**
     * Loads the config identity.
     *
     * @throws InvalidIdentityFileException if there is a problem with the
     * config file.
     */
    private void loadConfig() throws InvalidIdentityFileException {
        try {
            final File file = new File(configDirectory + "dmdirc.config");

            if (!file.exists()) {
                file.createNewFile();
            }

            config = new ConfigFileBackedConfigProvider(file, true);
            config.setOption("identity", "name", "Global config");
            addConfigProvider(config);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.FATAL, "I/O error when loading global config: "
                    + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider getUserSettings() {
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider getAddonSettings() {
        return addonConfig;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider getVersionSettings() {
        return versionConfig;
    }

    /** {@inheritDoc} */
    @Override
    public void saveAll() {
        synchronized (identities) {
            for (ConfigProvider identity : getAllIdentities()) {
                identity.save();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addConfigProvider(final ConfigProvider identity) {
        checkNotNull(identity);

        final String target = getGroup(identity);

        if (identities.containsValue(target, identity)) {
            removeConfigProvider(identity);
        }

        synchronized (identities) {
            identities.add(target, identity);
        }

        log.debug("Adding identity: {} (group: {})",
                new Object[]{ identity, target });

        synchronized (listeners) {
            for (ConfigProviderListener listener : listeners.safeGet(target)) {
                listener.configProviderAdded(identity);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeConfigProvider(final ConfigProvider identity) {
        checkNotNull(identity);

        final String group = getGroup(identity);

        checkArgument(identities.containsValue(group, identity));

        synchronized (identities) {
            identities.remove(group, identity);
        }

        synchronized (listeners) {
            for (ConfigProviderListener listener : listeners.safeGet(group)) {
                listener.configProviderRemoved(identity);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerIdentityListener(final ConfigProviderListener listener) {
        registerIdentityListener(null, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterIdentityListener(final ConfigProviderListener listener) {
        listeners.removeFromAll(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void registerIdentityListener(final String type, final ConfigProviderListener listener) {
        checkNotNull(listener);

        synchronized (listeners) {
            listeners.add(type, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ConfigProvider> getProvidersByType(final String type) {
        return Collections.unmodifiableList(identities.safeGet(type));
    }

    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified config manager.
     *
     * @param manager The manager requesting sources
     * @return A list of all matching config sources
     */
    List<ConfigProvider> getIdentitiesForManager(final ConfigManager manager) {
        final List<ConfigProvider> sources = new ArrayList<>();

        synchronized (identities) {
            for (ConfigProvider identity : identities.safeGet(null)) {
                if (manager.identityApplies(identity)) {
                    sources.add(identity);
                }
            }
        }

        Collections.sort(sources, new ConfigProviderTargetComparator());

        log.debug("Found {} source(s) for {}", sources.size(), manager);

        return sources;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized AggregateConfigProvider getGlobalConfiguration() {
        if (globalconfig == null) {
            globalconfig = createAggregateConfig("", "", "", "");
        }

        return globalconfig;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createChannelConfig(final String network, final String channel) {
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
            for (ConfigProvider identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.CHANNEL
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setChannel(myTarget);

        return createConfig(target);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createNetworkConfig(final String network) {
        if (network == null || network.isEmpty()) {
            throw new IllegalArgumentException("getNetworkConfig called "
                    + "with null or empty network\n\nNetwork:" + network);
        }

        final String myTarget = network.toLowerCase();

        synchronized (identities) {
            for (ConfigProvider identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.NETWORK
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork(myTarget);

        return createConfig(target);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createServerConfig(final String server) {
        if (server == null || server.isEmpty()) {
            throw new IllegalArgumentException("getServerConfig called "
                    + "with null or empty server\n\nServer: " + server);
        }

        final String myTarget = server.toLowerCase();

        synchronized (identities) {
            for (ConfigProvider identity : identities.safeGet(null)) {
                if (identity.getTarget().getType() == ConfigTarget.TYPE.SERVER
                        && identity.getTarget().getData().equalsIgnoreCase(myTarget)) {
                    return identity;
                }
            }
        }

        // We need to create one
        final ConfigTarget target = new ConfigTarget();
        target.setServer(myTarget);

        return createConfig(target);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createCustomConfig(final String name, final String type) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<String, String>(2));

        settings.get(IDENTITY_DOMAIN).put("name", name);
        settings.get(IDENTITY_DOMAIN).put("type", type);

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create identity", ex);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createProfileConfig(final String name) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<String, String>(1));
        settings.put(PROFILE_DOMAIN, new HashMap<String, String>(2));

        final String nick = System.getProperty("user.name").replace(' ', '_');

        settings.get(IDENTITY_DOMAIN).put("name", name);
        settings.get(PROFILE_DOMAIN).put("nicknames", nick);
        settings.get(PROFILE_DOMAIN).put("realname", nick);

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create identity", ex);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProvider createConfig(final ConfigTarget target) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<String, String>(2));
        settings.get(IDENTITY_DOMAIN).put("name", target.getData());
        settings.get(IDENTITY_DOMAIN).put(target.getTypeName(), target.getData());

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create identity", ex);
            return null;
        }
    }

    /**
     * Creates a new identity containing the specified properties.
     *
     * @param settings The settings to populate the identity with
     * @return A new identity containing the specified properties
     * @throws IOException If the file cannot be created
     * @throws InvalidIdentityFileException If the settings are invalid
     * @since 0.6.3m1
     */
    protected ConfigFileBackedConfigProvider createIdentity(final Map<String, Map<String, String>> settings)
            throws IOException, InvalidIdentityFileException {
        if (!settings.containsKey(IDENTITY_DOMAIN)
                || !settings.get(IDENTITY_DOMAIN).containsKey("name")
                || settings.get(IDENTITY_DOMAIN).get("name").isEmpty()) {
            throw new InvalidIdentityFileException("identity.name is not set");
        }

        final String name = settings.get(IDENTITY_DOMAIN).get("name").replaceAll(ILLEGAL_CHARS, "_");

        File file = new File(identitiesDirectory + name);
        int attempt = 1;

        while (file.exists()) {
            file = new File(identitiesDirectory + name + "-" + attempt);
            attempt++;
        }

        final ConfigFile configFile = new ConfigFile(file);

        for (Map.Entry<String, Map<String, String>> entry : settings.entrySet()) {
            configFile.addDomain(entry.getKey(), entry.getValue());
        }

        configFile.write();

        final ConfigFileBackedConfigProvider identity = new ConfigFileBackedConfigProvider(file, false);
        addConfigProvider(identity);

        return identity;
    }

    /**
     * Gets a singleton instance of the Identity Manager.
     *
     * @return A singleton instance of the IdentityManager.
     * @deprecated Shouldn't use global state.
     */
    @Deprecated
    public static IdentityManager getIdentityManager() {
        return instance;
    }

    /**
     * Sets the singleton instance of the Identity Manager.
     *
     * @param identityManager The identity manager to use.
     * @deprecated Shouldn't use global state.
     */
    @Deprecated
    public static void setIdentityManager(final IdentityManager identityManager) {
        instance = identityManager;
    }

    /**
     * Finds and adds sources for the given manager, and adds it as an identity listener.
     *
     * @param configManager The manager to be initialised.
     */
    private void setUpConfigManager(final ConfigManager configManager) {
        List<ConfigProvider> sources = getIdentitiesForManager(configManager);

        for (ConfigProvider identity : sources) {
            log.trace("Found {}", identity);
            configManager.checkIdentity(identity);
        }

        registerIdentityListener(configManager);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProviderMigrator createMigratableConfig(final String protocol,
            final String ircd, final String network, final String server) {
        final ConfigManager configManager = new ConfigManager(protocol, ircd, network, server);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigProviderMigrator createMigratableConfig(final String protocol,
            final String ircd, final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(protocol, ircd, network, server, channel);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server) {
        final ConfigManager configManager = new ConfigManager(protocol, ircd, network, server);
        setUpConfigManager(configManager);
        return configManager;
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(protocol, ircd, network, server, channel);
        setUpConfigManager(configManager);
        return configManager;
    }

}
