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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The config manager manages the various config sources for each entity.
 */
class ConfigManager implements ConfigChangeListener, ConfigProviderListener,
        AggregateConfigProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
    /** Temporary map for lookup stats. */
    private static final Map<String, Integer> STATS = new TreeMap<>();
    /** Magical domain to redirect to the version identity. */
    private static final String VERSION_DOMAIN = "version";
    /** A list of sources for this config manager. */
    private final List<ConfigProvider> sources = new ArrayList<>();
    /** The listeners registered for this manager. */
    private final Multimap<String, ConfigChangeListener> listeners = ArrayListMultimap.create();
    /** The config binder to use for this manager. */
    private final ConfigBinder binder;
    /** The manager to use to fetch global state. */
    private final IdentityManager manager;
    /** Client info object. */
    private final ClientInfo clientInfo;
    /** The protocol this manager is for. */
    private String protocol;
    /** The ircd this manager is for. */
    private String ircd;
    /** The network this manager is for. */
    private String network;
    /** The server this manager is for. */
    private String server;
    /** The channel this manager is for. */
    private String channel;

    /**
     * Creates a new instance of ConfigManager.
     *
     * @param manager  The manager to use to retrieve global state horribly.
     * @param protocol The protocol for this manager
     * @param ircd     The name of the ircd for this manager
     * @param network  The name of the network for this manager
     * @param server   The name of the server for this manager
     *
     * @since 0.6.3
     */
    ConfigManager(
            final ClientInfo clientInfo,
            final IdentityManager manager,
            final DMDircMBassador eventBus,
            final String protocol, final String ircd,
            final String network, final String server) {
        this(clientInfo, manager, eventBus, protocol, ircd, network, server, "<Unknown>");
    }

    /**
     * Creates a new instance of ConfigManager.
     *
     * @param manager  The manager to use to retrieve global state horribly.
     * @param protocol The protocol for this manager
     * @param ircd     The name of the ircd for this manager
     * @param network  The name of the network for this manager
     * @param server   The name of the server for this manager
     * @param channel  The name of the channel for this manager
     *
     * @since 0.6.3
     */
    ConfigManager(
            final ClientInfo clientInfo,
            final IdentityManager manager,
            final DMDircMBassador eventBus,
            final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        final String chanName = channel + '@' + network;

        this.clientInfo = clientInfo;
        this.manager = manager;
        this.protocol = protocol;
        this.ircd = ircd;
        this.network = network;
        this.server = server;
        this.channel = chanName;

        binder = new ConfigBinder(this, eventBus);
    }

    @Override
    public ConfigBinder getBinder() {
        return binder;
    }

    @Override
    public String getOption(final String domain, final String option,
            final Validator<String> validator) {
        doStats(domain, option);

        if (VERSION_DOMAIN.equals(domain)) {
            final String response = clientInfo.getVersionConfigSetting(VERSION_DOMAIN, option);
            if (response == null || validator.validate(response).isFailure()) {
                return null;
            }
            return response;
        }

        synchronized (sources) {
            for (ConfigProvider source : sources) {
                if (source.hasOption(domain, option, validator)) {
                    return source.getOption(domain, option, validator);
                }
            }
        }

        return null;
    }

    @Override
    public boolean hasOption(final String domain, final String option,
            final Validator<String> validator) {
        doStats(domain, option);

        if (VERSION_DOMAIN.equals(domain)) {
            final String response = clientInfo.getVersionConfigSetting(VERSION_DOMAIN, option);
            return response != null && !validator.validate(response).isFailure();
        }

        synchronized (sources) {
            for (ConfigProvider source : sources) {
                if (source.hasOption(domain, option, validator)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, String> getOptions(final String domain) {
        if (VERSION_DOMAIN.equals(domain)) {
            return manager.getVersionSettings().getOptions(domain);
        }

        final Map<String, String> res = new HashMap<>();

        synchronized (sources) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                res.putAll(sources.get(i).getOptions(domain));
            }
        }

        return res;
    }

    /**
     * Removes the specified identity from this manager.
     *
     * @param identity The identity to be removed
     */
    public void removeIdentity(final ConfigProvider identity) {
        if (!sources.contains(identity)) {
            return;
        }

        final Collection<String[]> changed = new ArrayList<>();

        // Determine which settings will have changed
        for (String domain : identity.getDomains()) {
            identity.getOptions(domain).keySet().stream()
                    .filter(option -> identity.equals(getScope(domain, option)))
                    .forEach(option -> changed.add(new String[]{domain, option}));
        }

        synchronized (sources) {
            identity.removeListener(this);
            sources.remove(identity);
        }

        // Fire change listeners
        for (String[] setting : changed) {
            configChanged(setting[0], setting[1]);
        }
    }

    /**
     * Retrieves the identity that currently defines the specified domain and option.
     *
     * @param domain The domain to search for
     * @param option The option to search for
     *
     * @return The identity that defines that setting, or null on failure
     */
    protected ConfigProvider getScope(final String domain, final String option) {
        if (VERSION_DOMAIN.equals(domain)) {
            return manager.getVersionSettings();
        }

        synchronized (sources) {
            for (ConfigProvider source : sources) {
                if (source.hasOptionString(domain, option)) {
                    return source;
                }
            }
        }

        return null;
    }

    /**
     * Checks whether the specified identity applies to this config manager.
     *
     * @param identity The identity to test
     *
     * @return True if the identity applies, false otherwise
     */
    public boolean identityApplies(final ConfigProvider identity) {
        final String comp;

        switch (identity.getTarget().getType()) {
            case PROTOCOL:
                comp = protocol;
                break;
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
            case CUSTOM:
                // We don't want custom identities
                comp = null;
                break;
            default:
                comp = "";
                break;
        }

        final boolean result = comp != null
                && identityTargetMatches(identity.getTarget().getData(), comp);

        LOG.trace("Checking if identity {} applies. Comparison: {}, target: {}, result: {}",
                identity, comp, identity.getTarget().getData(), result);

        return result;
    }

    /**
     * Determines whether the specified identity target matches the desired target. If the desired
     * target is prefixed with "re:", it is treated as a regular expression; otherwise the strings
     * are compared lexicographically to determine a match.
     *
     * @param desired The target string required by this config manager
     * @param actual  The target string supplied by the identity
     *
     * @return True if the identity should be applied, false otherwise
     *
     * @since 0.6.3m2
     */
    protected boolean identityTargetMatches(final String actual, final String desired) {
        return actual.startsWith("re:") ? desired.matches(actual.substring(3))
                : actual.equalsIgnoreCase(desired);
    }

    /**
     * Called whenever there is a new identity available. Checks if the identity is relevant for
     * this manager, and adds it if it is.
     *
     * @param identity The identity to be checked
     */
    public void checkIdentity(final ConfigProvider identity) {
        if (!sources.contains(identity) && identityApplies(identity)) {
            synchronized (sources) {
                sources.add(identity);
                identity.addListener(this);
                Collections.sort(sources, new ConfigProviderTargetComparator());
            }

            // Determine which settings will have changed
            for (String domain : identity.getDomains()) {
                identity.getOptions(domain).keySet().stream()
                        .filter(option -> identity.equals(getScope(domain, option)))
                        .forEach(option -> configChanged(domain, option));
            }
        }
    }

    @Override
    public Set<String> getDomains() {
        final Set<String> res = new HashSet<>();

        synchronized (sources) {
            for (ConfigProvider source : sources) {
                res.addAll(source.getDomains());
            }
        }

        return res;
    }

    @Override
    public List<ConfigProvider> getSources() {
        return new ArrayList<>(sources);
    }

    /**
     * Migrates this manager from its current configuration to the appropriate one for the specified
     * new parameters, firing listeners where settings have changed.
     *
     * <p>
     * This is package private - only callers with access to a {@link ConfigProviderMigrator}
     * should be able to migrate managers.
     *
     * @param protocol The protocol for this manager
     * @param ircd     The new name of the ircd for this manager
     * @param network  The new name of the network for this manager
     * @param server   The new name of the server for this manager
     * @param channel  The new name of the channel for this manager
     */
    void migrate(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        LOG.debug("Migrating from {{}, {}, {}, {}, {}} to {{}, {}, {}, {}, {}}", this.protocol,
                this.ircd, this.network, this.server, this.channel, protocol, ircd, network, server,
                channel);

        this.protocol = protocol;
        this.ircd = ircd;
        this.network = network;
        this.server = server;
        this.channel = channel + '@' + network;

        new ArrayList<>(sources).stream().filter(identity -> !identityApplies(identity))
                .forEach(identity -> {
                    LOG.debug("Removing identity that no longer applies: {}", identity);
                    removeIdentity(identity);
                });

        final List<ConfigProvider> newSources = manager.getIdentitiesForManager(this);
        for (ConfigProvider identity : newSources) {
            LOG.trace("Testing new identity: {}", identity);
            checkIdentity(identity);
        }

        LOG.debug("New identities: {}", sources);
    }

    /**
     * Records the lookup request for the specified domain and option.
     *
     * @param domain The domain that is being looked up
     * @param option The option that is being looked up
     */
    @SuppressWarnings("PMD.AvoidCatchingNPE")
    protected static void doStats(final String domain, final String option) {
        final String key = domain + '.' + option;

        try {
            STATS.put(key, 1 + (STATS.containsKey(key) ? STATS.get(key) : 0));
        } catch (NullPointerException ex) {
            // JVM bugs ftl.
        }
    }

    /**
     * Retrieves the statistic map.
     *
     * @return A map of config options to lookup counts
     */
    public static Map<String, Integer> getStats() {
        return STATS;
    }

    @Override
    public void addChangeListener(final String domain,
            final ConfigChangeListener listener) {
        addListener(domain, listener);
    }

    @Override
    public void addChangeListener(final String domain, final String key,
            final ConfigChangeListener listener) {
        addListener(domain + '.' + key, listener);
    }

    @Override
    public void removeListener(final ConfigChangeListener listener) {
        synchronized (listeners) {
            final Iterable<String> keys = new HashSet<>(listeners.keySet());
            keys.forEach(k -> listeners.remove(k, listener));
        }
    }

    /**
     * Adds the specified listener to the internal map/list.
     *
     * @param key      The key to use (domain or domain.key)
     * @param listener The listener to register
     */
    private void addListener(final String key,
            final ConfigChangeListener listener) {
        synchronized (listeners) {
            listeners.put(key, listener);
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        final Collection<ConfigChangeListener> targets = new ArrayList<>();

        if (listeners.containsKey(domain)) {
            targets.addAll(listeners.get(domain));
        }

        if (listeners.containsKey(domain + '.' + key)) {
            targets.addAll(listeners.get(domain + '.' + key));
        }

        for (ConfigChangeListener listener : targets) {
            listener.configChanged(domain, key);
        }
    }

    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        checkIdentity(configProvider);
    }

    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        removeIdentity(configProvider);
    }

}
