/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.FileUtils;
import com.dmdirc.util.io.InvalidConfigFileException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.FATAL_APP_ERROR;
import static com.dmdirc.util.LogUtils.FATAL_USER_ERROR;
import static com.dmdirc.util.LogUtils.USER_ERROR;
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
     * Standard identities are inserted with a {@code null} key, custom identities use their
     * custom type as the key.
     */
    private final Multimap<String, ConfigProvider> identities = ArrayListMultimap.create();
    /** Map of paths to corresponding config providers, to facilitate reloading. */
    private final Map<Path, ConfigProvider> configProvidersByPath = new ConcurrentHashMap<>();
    /**
     * The {@link ConfigProviderListener}s that have registered with this manager.
     *
     * Listeners for standard identities are inserted with a {@code null} key, listeners for a
     * specific custom type use their type as the key.
     */
    private final Multimap<String, WeakReference<ConfigProviderListener>> listeners =
            ArrayListMultimap.create();
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
     */
    public IdentityManager(final Path baseDirectory, final Path identitiesDirectory,
            final ClientInfo clientInfo) {
        this.configDirectory = baseDirectory;
        this.identitiesDirectory = identitiesDirectory;
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

        addonConfig = new ConfigFileBackedConfigProvider(this, addonConfigFile, target);
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
            LOG.error(FATAL_APP_ERROR, "Unable to load settings", ex);
        }

        final Path file = identitiesDirectory.resolve("modealiases");

        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException ex) {
                LOG.info(USER_ERROR, "Unable to create modealiases directory", file, ex);
            }
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(file)) {
            if (!directoryStream.iterator().hasNext()) {
                extractIdentities("modealiases");
            }
        } catch (IOException ex) {
            LOG.error(FATAL_USER_ERROR, "Unable to iterate required directory '{}'. Please check"
                    + "file permissions or specify a different configuration directory.", file, ex);
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
            LOG.warn(USER_ERROR, "Unable to extract default identities: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void loadUserIdentities() {
        if (!Files.exists(identitiesDirectory)) {
            try {
                Files.createDirectories(identitiesDirectory);
            } catch (IOException ex) {
                LOG.warn(USER_ERROR, "Unable to create identity dir", ex);
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
            LOG.warn(USER_ERROR, "Unable to load user identity files from: {}", dir, ex);
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
                    LOG.warn(USER_ERROR, "I/O error when reloading identity file: {} ({})",
                            file, ex.getMessage(), ex);
                } catch (InvalidConfigFileException ex) {
                    // Do nothing
                }
            }
        }

        try {
            final ConfigProvider provider = new ConfigFileBackedConfigProvider(this, file, false);
            addConfigProvider(provider);
            configProvidersByPath.put(file, provider);
        } catch (InvalidIdentityFileException ex) {
            LOG.warn(USER_ERROR, "Invalid identity file: {} ({})", file, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOG.warn(USER_ERROR, "I/O error when reading identity file: {}", file, ex);
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
        return identities.values();
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
            versionConfig = new ConfigFileBackedConfigProvider(IdentityManager.class.
                    getResourceAsStream("/com/dmdirc/version.config"), false);
            addConfigProvider(versionConfig);
        } catch (IOException | InvalidIdentityFileException ex) {
            LOG.warn(USER_ERROR, "Unable to load version information.", ex);
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

            config = new ConfigFileBackedConfigProvider(this, file, true);
            config.setOption("identity", "name", "Global config");
            configProvidersByPath.put(file, config);
            addConfigProvider(config);
        } catch (IOException ex) {
            LOG.warn(USER_ERROR, "I/O error when loading global config: {}", ex.getMessage(), ex);
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

        if (identities.containsEntry(target, identity)) {
            removeConfigProvider(identity);
        }

        synchronized (identities) {
            identities.put(target, identity);
        }

        LOG.debug("Adding identity: {} (group: {})", new Object[]{identity, target});

        synchronized (listeners) {
            listeners.get(target).stream()
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .forEach(l -> l.configProviderAdded(identity));
        }
    }

    @Override
    public void removeConfigProvider(final ConfigProvider identity) {
        checkNotNull(identity);

        final String group = getGroup(identity);

        checkArgument(identities.containsEntry(group, identity));

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
            listeners.get(group).stream()
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .forEach(l -> l.configProviderRemoved(identity));
        }
    }

    @Override
    public void registerIdentityListener(final ConfigProviderListener listener) {
        registerIdentityListener(null, listener);
    }

    @Override
    public void unregisterIdentityListener(final ConfigProviderListener listener) {
        synchronized (listeners) {
            listeners.entries().stream().filter(e -> {
                final ConfigProviderListener value = e.getValue().get();
                return value == null || value.equals(listener);
            }).forEach(e -> listeners.remove(e.getKey(), e.getValue()));
        }
    }

    @Override
    public void registerIdentityListener(final String type, final ConfigProviderListener listener) {
        checkNotNull(listener);

        synchronized (listeners) {
            listeners.put(type, new WeakReference<>(listener));
        }
    }

    @Override
    public Collection<ConfigProvider> getProvidersByType(final String type) {
        return Collections.unmodifiableCollection(identities.get(type));
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
            sources.addAll(identities.get(null).stream()
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
            for (ConfigProvider identity : identities.get(null)) {
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
            for (ConfigProvider identity : identities.get(null)) {
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
            for (ConfigProvider identity : identities.get(null)) {
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
            LOG.warn(USER_ERROR, "Unable to create identity", ex);
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
            LOG.warn(USER_ERROR, "Unable to create identity", ex);
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
            LOG.warn(USER_ERROR, "Unable to create identity", ex);
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
                file, false);
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
        final ConfigManager configManager = new ConfigManager(clientInfo, this, protocol,
                ircd, network, server);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    @Override
    public ConfigProviderMigrator createMigratableConfig(final String protocol,
            final String ircd, final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, protocol,
                ircd, network, server, channel);
        setUpConfigManager(configManager);
        return new ConfigManagerMigrator(configManager);
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, protocol,
                ircd, network, server);
        setUpConfigManager(configManager);
        return configManager;
    }

    @Override
    public AggregateConfigProvider createAggregateConfig(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        final ConfigManager configManager = new ConfigManager(clientInfo, this, protocol,
                ircd, network, server, channel);
        setUpConfigManager(configManager);
        return configManager;
    }

}
