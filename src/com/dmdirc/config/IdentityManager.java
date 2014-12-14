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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.Precondition;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.collections.MapList;
import com.dmdirc.util.collections.WeakMapList;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.FileUtils;
import com.dmdirc.util.io.InvalidConfigFileException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class IdentityManager implements IdentityFactory, IdentityController {

    private static final Logger LOG = LoggerFactory.getLogger(IdentityManager.class);
    /** A regular expression that will match all characters illegal in file names. */
    private static final String ILLEGAL_CHARS = "[\\\\\"/:\\*\\?\"<>\\|]";
    /** The domain used for identity settings. */
    private static final String IDENTITY_DOMAIN = "identity";
    /** The domain used for profile settings. */
    private static final String PROFILE_DOMAIN = "profile";
    /** Base configuration directory where the main configuration file will be located. */
    private final Path configDirectory;
    /** Directory to save and load identities in. */
    private final Path identitiesDirectory;
    /**
     * The identities that have been loaded into this manager.
     *
     * Standard identities are inserted with a <code>null</code> key, custom identities use their
     * custom type as the key.
     */
    private final MapList<String, ConfigProvider> identities = new MapList<>();
    /** Map of paths to corresponding config providers, to facilitate reloading. */
    private final Map<Path, ConfigProvider> configProvidersByPath = new ConcurrentHashMap<>();
    /** The event bus to post events to. */
    private final DMDircMBassador eventBus;
    /**
     * The {@link ConfigProviderListener}s that have registered with this manager.
     *
     * Listeners for standard identities are inserted with a <code>null</code> key, listeners for a
     * specific custom type use their type as the key.
     */
    private final MapList<String, ConfigProviderListener> listeners = new WeakMapList<>();
    /** Client info objecty. */
    private final ClientInfo clientInfo;
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
     * @param baseDirectory       The BASE config directory.
     * @param identitiesDirectory The directory to store identities in.
     * @param eventBus            The event bus to post events to
     */
    public IdentityManager(final Path baseDirectory, final Path identitiesDirectory,
            final DMDircMBassador eventBus, final ClientInfo clientInfo) {
        this.configDirectory = baseDirectory;
        this.identitiesDirectory = identitiesDirectory;
        this.eventBus = eventBus;
        this.clientInfo = clientInfo;
    }

    /**
     * Loads all identity files.
     *
     * @throws InvalidIdentityFileException If there is an error with the config file.
     */
    public void initialise() throws InvalidIdentityFileException {
        identities.clear();

        loadVersionIdentity();
        loadDefaults();
        loadUserIdentities();
        loadConfig();

        // Set up the identity used for the addons defaults
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();
        target.setOrder(500000);

        final ConfigFile addonConfigFile = new ConfigFile((Path) null);
        final Map<String, String> addonSettings = new HashMap<>();
        addonSettings.put("name", "Addon defaults");
        addonConfigFile.addDomain("identity", addonSettings);

        addonConfig = new ConfigFileBackedConfigProvider(this, eventBus, addonConfigFile, target);
        addConfigProvider(addonConfig);
    }

    /** Loads the default (built in) identities. */
    private void loadDefaults() {
        try {
            loadIdentity(FileUtils.getPathForResource(getClass().getResource(
                    "defaults/default/defaults")));
            loadIdentity(FileUtils.getPathForResource(getClass().getResource(
                    "defaults/default/formatter")));
        } catch (URISyntaxException ex) {
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.FATAL, ex,
                    "Unable to load settings", ""));
        }

        final Path file = identitiesDirectory.resolve("modealiases");

        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException ex) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                        "Unable to create modealiases directory", "Please check file permissions " +
                        "for " + file));
            }
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(file)) {
            if (!directoryStream.iterator().hasNext()) {
                extractIdentities("modealiases");
            }
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.FATAL, ex,
                    "Unable to iterate required directory '" + file + "'. Please check " +
                            "file permissions or specify a different configuration " +
                            "directory.", ""));
            return;
        }

        loadUser(file);
    }

    /**
     * Extracts the specific set of default identities to the user's identity folder.
     *
     * @param target The target to be extracted
     */
    private void extractIdentities(final String target) {
        try {
            FileUtils.copyResources(getClass().getResource("defaults/" + target),
                    identitiesDirectory);
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Unable to extract default identities: " + ex.getMessage(), ""));
        }
    }

    @Override
    public void loadUserIdentities() {
        if (!Files.exists(identitiesDirectory)) {
            try {
                Files.createDirectories(identitiesDirectory);
            } catch (IOException ex) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                        "Unable to create identity dir", ""));
            }
        }

        loadUser(identitiesDirectory);
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
    private void loadUser(final Path dir) {
        checkNotNull(dir);
        checkArgument(Files.isDirectory(dir));

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path child : directoryStream) {
                if (Files.isDirectory(child)) {
                    loadUser(child);
                } else {
                    loadIdentity(child);
                }
            }
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to load user identity files from " + dir, ""));
        }
    }

    /**
     * Loads an identity from the specified file. If the identity already exists, it is told to
     * reload instead.
     *
     * @param file The file to load the identity from.
     */
    private void loadIdentity(final Path file) {
        synchronized (identities) {
            if (configProvidersByPath.containsKey(file)) {
                try {
                    configProvidersByPath.get(file).reload();
                } catch (IOException ex) {
                    eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                            "I/O error when reloading identity file: "
                                    + file + " (" + ex.getMessage() + ')', ""));
                } catch (InvalidConfigFileException ex) {
                    // Do nothing
                }
            }
        }

        try {
            final ConfigProvider provider = new ConfigFileBackedConfigProvider(this, eventBus,
                    file, false);
            addConfigProvider(provider);
            configProvidersByPath.put(file, provider);
        } catch (InvalidIdentityFileException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Invalid identity file: " + file + " (" + ex.getMessage() + ')', ""));
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "I/O error when reading identity file: " + file, ""));
        }
    }

    /**
     * Retrieves all known identities.
     *
     * @return A set of all known identities
     *
     * @since 0.6.4
     */
    private Iterable<ConfigProvider> getAllIdentities() {
        final Collection<ConfigProvider> res = new LinkedHashSet<>();

        for (Map.Entry<String, List<ConfigProvider>> entry : identities.entrySet()) {
            res.addAll(entry.getValue());
        }

        return res;
    }

    /**
     * Returns the "group" to which the specified identity belongs. For custom identities this is
     * the custom identity type, otherwise this is <code>null</code>.
     *
     * @param identity The identity whose group is being retrieved
     *
     * @return The group of the specified identity
     *
     * @since 0.6.4
     */
    private String getGroup(final ConfigProvider identity) {
        return identity.getTarget().getType() == ConfigTarget.TYPE.CUSTOM
                ? identity.getTarget().getData() : null;
    }

    @Override
    public void loadVersionIdentity() {
        try {
            versionConfig = new ConfigFileBackedConfigProvider(eventBus, IdentityManager.class.
                    getResourceAsStream("/com/dmdirc/version.config"), false);
            addConfigProvider(versionConfig);
        } catch (IOException | InvalidIdentityFileException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to load version information", ""));
        }
    }

    /**
     * Loads the config identity.
     *
     * @throws InvalidIdentityFileException if there is a problem with the config file.
     */
    private void loadConfig() throws InvalidIdentityFileException {
        try {
            final Path file = configDirectory.resolve("dmdirc.config");

            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            config = new ConfigFileBackedConfigProvider(this, eventBus, file, true);
            config.setOption("identity", "name", "Global config");
            configProvidersByPath.put(file, config);
            addConfigProvider(config);
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "I/O error when loading global config: " + ex.getMessage(), ""));
        }
    }

    @Override
    public ConfigProvider getUserSettings() {
        return config;
    }

    @Override
    public ConfigProvider getAddonSettings() {
        return addonConfig;
    }

    @Override
    public ConfigProvider getVersionSettings() {
        return versionConfig;
    }

    @Override
    public void saveAll() {
        synchronized (identities) {
            for (ConfigProvider identity : getAllIdentities()) {
                identity.save();
            }
        }
    }

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

        LOG.debug("Adding identity: {} (group: {})", new Object[]{identity, target});

        synchronized (listeners) {
            for (ConfigProviderListener listener : listeners.safeGet(target)) {
                listener.configProviderAdded(identity);
            }
        }
    }

    @Override
    public void removeConfigProvider(final ConfigProvider identity) {
        checkNotNull(identity);

        final String group = getGroup(identity);

        checkArgument(identities.containsValue(group, identity));

        Path path = null;
        for (Map.Entry<Path, ConfigProvider> entry : configProvidersByPath.entrySet()) {
            if (entry.getValue() == identity) {
                path = entry.getKey();
            }
        }

        if (path != null) {
            configProvidersByPath.remove(path);
        }

        synchronized (identities) {
            identities.remove(group, identity);
        }

        synchronized (listeners) {
            for (ConfigProviderListener listener : listeners.safeGet(group)) {
                listener.configProviderRemoved(identity);
            }
        }
    }

    @Override
    public void registerIdentityListener(final ConfigProviderListener listener) {
        registerIdentityListener(null, listener);
    }

    @Override
    public void unregisterIdentityListener(final ConfigProviderListener listener) {
        listeners.removeFromAll(listener);
    }

    @Override
    public void registerIdentityListener(final String type, final ConfigProviderListener listener) {
        checkNotNull(listener);

        synchronized (listeners) {
            listeners.add(type, listener);
        }
    }

    @Override
    public List<ConfigProvider> getProvidersByType(final String type) {
        return Collections.unmodifiableList(identities.safeGet(type));
    }

    /**
     * Retrieves a list of all config sources that should be applied to the specified config
     * manager.
     *
     * @param manager The manager requesting sources
     *
     * @return A list of all matching config sources
     */
    List<ConfigProvider> getIdentitiesForManager(final ConfigManager manager) {
        final List<ConfigProvider> sources = new ArrayList<>();

        synchronized (identities) {
            sources.addAll(identities.safeGet(null).stream()
                    .filter(manager::identityApplies)
                    .collect(Collectors.toList()));
        }

        Collections.sort(sources, new ConfigProviderTargetComparator());

        LOG.debug("Found {} source(s) for {}", sources.size(), manager);

        return sources;
    }

    @Override
    public synchronized AggregateConfigProvider getGlobalConfiguration() {
        if (globalconfig == null) {
            globalconfig = createAggregateConfig("", "", "", "");
        }

        return globalconfig;
    }

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

        final String myTarget = (channel + '@' + network).toLowerCase();

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

    @Override
    public ConfigProvider createCustomConfig(final String name, final String type) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<>(2));

        settings.get(IDENTITY_DOMAIN).put("name", name);
        settings.get(IDENTITY_DOMAIN).put("type", type);

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to create identity", ""));
            return null;
        }
    }

    @Override
    public ConfigProvider createProfileConfig(final String name) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<>(1));
        settings.put(PROFILE_DOMAIN, new HashMap<>(2));

        final String nick = System.getProperty("user.name").replace(' ', '_');

        settings.get(IDENTITY_DOMAIN).put("name", name);
        settings.get(PROFILE_DOMAIN).put("nicknames", nick);
        settings.get(PROFILE_DOMAIN).put("realname", nick);

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to create identity", ""));
            return null;
        }
    }

    @Override
    public ConfigProvider createConfig(final ConfigTarget target) {
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put(IDENTITY_DOMAIN, new HashMap<>(2));
        settings.get(IDENTITY_DOMAIN).put("name", target.getData());
        settings.get(IDENTITY_DOMAIN).put(target.getTypeName(), target.getData());

        try {
            return createIdentity(settings);
        } catch (InvalidIdentityFileException | IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to create identity", ""));
            return null;
        }
    }

    /**
     * Creates a new identity containing the specified properties.
     *
     * @param settings The settings to populate the identity with
     *
     * @return A new identity containing the specified properties
     *
     * @throws IOException                  If the file cannot be created
     * @throws InvalidIdentityFileException If the settings are invalid
     * @since 0.6.3m1
     */
    protected ConfigFileBackedConfigProvider createIdentity(
            final Map<String, Map<String, String>> settings)
            throws IOException, InvalidIdentityFileException {
        if (!settings.containsKey(IDENTITY_DOMAIN)
                || !settings.get(IDENTITY_DOMAIN).containsKey("name")
                || settings.get(IDENTITY_DOMAIN).get("name").isEmpty()) {
            throw new InvalidIdentityFileException("identity.name is not set");
        }

        final String name = settings.get(IDENTITY_DOMAIN).get("name").replaceAll(ILLEGAL_CHARS, "_");

        Path file = identitiesDirectory.resolve(name);
        int attempt = 1;

        while (Files.exists(file)) {
            file = identitiesDirectory.resolve(name + '-' + attempt);
            attempt++;
        }

        final ConfigFile configFile = new ConfigFile(file);

        for (Map.Entry<String, Map<String, String>> entry : settings.entrySet()) {
            configFile.addDomain(entry.getKey(), entry.getValue());
        }

        configFile.write();

        final ConfigFileBackedConfigProvider identity = new ConfigFileBackedConfigProvider(this,
                eventBus, file, false);
        addConfigProvider(identity);

        return identity;
    }

    /**
     * Finds and adds sources for the given manager, and adds it as an identity listener.
     *
     * @param configManager The manager to be initialised.
     */
    private void setUpConfigManager(final ConfigManager configManager) {
        final List<ConfigProvider> sources = getIdentitiesForManager(configManager);

        for (ConfigProvider identity : sources) {
            LOG.trace("Found {}", identity);
            configManager.checkIdentity(identity);
        }

        registerIdentityListener(configManager);
    }

    @Override
    public ConfigProviderMigrator createMigratableConfig(final String protocol,
            final String ircd, final String network, final String server) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, eventBus, protocol,
                ircd,
                network, server);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    @Override
    public ConfigProviderMigrator createMigratableConfig(final String protocol,
            final String ircd, final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, eventBus, protocol,
                ircd, network, server, channel);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, eventBus, protocol,
                ircd, network, server);
        setUpConfigManager(configManager);
        return configManager;
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, eventBus, protocol,
                ircd, network, server, channel);
        setUpConfigManager(configManager);
        return configManager;
    }

}
