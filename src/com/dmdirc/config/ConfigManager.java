/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.util.MapList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * The config manager manages the various config sources for each entity.
 *
 * @author chris
 */
public class ConfigManager extends ConfigSource implements ConfigChangeListener,
        IdentityListener {

    /** Temporary map for lookup stats. */
    private static final Map<String, Integer> STATS = new TreeMap<String, Integer>();

    /** A logger for this class. */
    private static final java.util.logging.Logger LOGGER = java.util.logging
            .Logger.getLogger(ConfigManager.class.getName());

    /** A list of sources for this config manager. */
    private final List<Identity> sources;

    /** The listeners registered for this manager. */
    private final MapList<String, ConfigChangeListener> listeners
            = new MapList<String, ConfigChangeListener>();

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
     * @param protocol The protocol for this manager
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     * @since 0.6.3
     */
    public ConfigManager(final String protocol, final String ircd,
            final String network, final String server) {
        this(protocol, ircd, network, server, "<Unknown>");
    }

    /**
     * Creates a new instance of ConfigManager.
     *
     * @param protocol The protocol for this manager
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     * @param channel The name of the channel for this manager
     * @since 0.6.3
     */
    public ConfigManager(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        final String chanName = channel + "@" + network;

        this.protocol = protocol;
        this.ircd = ircd;
        this.network = network;
        this.server = server;
        this.channel = chanName;

        sources = IdentityManager.getSources(this);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Found " + sources.size() + " source(s) for: "
                    + this.protocol + ", " + this.ircd + ", " + this.network
                    + ", " + this.server + ", " + this.channel);
        }

        for (Identity identity : sources) {
            identity.addListener(this);
        }

        IdentityManager.addIdentityListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getOption(final String domain, final String option) {
        doStats(domain, option);

        synchronized (sources) {
            for (Identity source : sources) {
                if (source.hasOption(domain, option)) {
                    return source.getOption(domain, option);
                }
            }
        }

        throw new IllegalArgumentException("Config option not found: " + domain + "." + option);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean hasOption(final String domain, final String option) {
        doStats(domain, option);

        synchronized (sources) {
            for (Identity source : sources) {
                if (source.hasOption(domain, option)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the name of all the options in the specified domain. If the
     * domain doesn't exist, an empty list is returned.
     *
     * @param domain The domain to search
     * @return A list of options in the specified domain
     */
    public Map<String, String> getOptions(final String domain) {
        final Map<String, String> res = new HashMap<String, String>();

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
    public void removeIdentity(final Identity identity) {
        if (!sources.contains(identity)) {
            return;
        }

        final List<String[]> changed = new ArrayList<String[]>();

        // Determine which settings will have changed
        for (String domain : identity.getDomains()) {
            for (String option : identity.getOptions(domain).keySet()) {
                if (identity.equals(getScope(domain, option))) {
                    changed.add(new String[]{domain, option});
                }
            }
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
     * Retrieves the identity that currently defines the specified domain and
     * option.
     *
     * @param domain The domain to search for
     * @param option The option to search for
     * @return The identity that defines that setting, or null on failure
     */
    protected Identity getScope(final String domain, final String option) {
        synchronized (sources) {
            for (Identity source : sources) {
                if (source.hasOption(domain, option)) {
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
     * @return True if the identity applies, false otherwise
     */
    public boolean identityApplies(final Identity identity) {
        String comp;

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

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Comparison: " + comp + ", target: "
                    + identity.getTarget().getData() + " (" + identity + "). Result: "
                    + result);
        }

        return result;
    }

    /**
     * Determines whether the specified identity target matches the desired
     * target. If the desired target is prefixed with "re:", it is treated
     * as a regular expression; otherwise the strings are compared
     * lexigraphically to determine a match.
     *
     * @param desired The target string required by this config manager
     * @param actual The target string supplied by the identity
     * @return True if the identity should be applied, false otherwise
     * @since 0.6.3m2
     */
    protected boolean identityTargetMatches(final String actual, final String desired) {
        return actual.startsWith("re:") ? desired.matches(actual.substring(3))
                : actual.equalsIgnoreCase(desired);
    }

    /**
     * Called whenever there is a new identity available. Checks if the
     * identity is relevant for this manager, and adds it if it is.
     *
     * @param identity The identity to be checked
     */
    public void checkIdentity(final Identity identity) {
        if (!sources.contains(identity) && identityApplies(identity)) {
            synchronized (sources) {
                sources.add(identity);
                identity.addListener(this);
                Collections.sort(sources);
            }

            // Determine which settings will have changed
            for (String domain : identity.getDomains()) {
                for (String option : identity.getOptions(domain).keySet()) {
                    if (identity.equals(getScope(domain, option))) {
                        configChanged(domain, option);
                    }
                }
            }
        }
    }

    /**
     * Returns the name of all domains known by this manager.
     *
     * @return A list of domains known to this manager
     */
    public Set<String> getDomains() {
        final Set<String> res = new HashSet<String>();

        synchronized (sources) {
            for (Identity source : sources) {
                res.addAll(source.getDomains());
            }
        }

        return res;
    }

    /**
     * Retrieves a list of sources for this config manager.
     * @return This config manager's sources.
     */
    public List<Identity> getSources() {
        return new ArrayList<Identity>(sources);
    }

    /**
     * Migrates this ConfigManager from its current configuration to the
     * appropriate one for the specified new parameters, firing listeners where
     * settings have changed.
     *
     * @param protocol The protocol for this manager
     * @param ircd The new name of the ircd for this manager
     * @param network The new name of the network for this manager
     * @param server The new name of the server for this manager
     * @since 0.6.3
     */
    public void migrate(final String protocol, final String ircd,
            final String network, final String server) {
        migrate(protocol, ircd, network, server, "<Unknown>");
    }

    /**
     * Migrates this ConfigManager from its current configuration to the
     * appropriate one for the specified new parameters, firing listeners where
     * settings have changed.
     *
     * @param protocol The protocol for this manager
     * @param ircd The new name of the ircd for this manager
     * @param network The new name of the network for this manager
     * @param server The new name of the server for this manager
     * @param channel The new name of the channel for this manager
     * @since 0.6.3
     */
    public void migrate(final String protocol, final String ircd,
            final String network, final String server, final String channel) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Migrating from " + this.protocol + ", " + this.ircd
                    + ", " + this.network + ", " + this.server + ", "
                    + this.channel + " to " + protocol + ", " + ircd + ", "
                    + network + ", " + server + ", " + channel);
        }

        this.protocol = protocol;
        this.ircd = ircd;
        this.network = network;
        this.server = server;
        this.channel = channel + "@" + network;

        for (Identity identity : new ArrayList<Identity>(sources)) {
            if (!identityApplies(identity)) {
                LOGGER.fine("Removing identity that no longer applies: " + identity);
                removeIdentity(identity);
            }
        }

        final List<Identity> newSources = IdentityManager.getSources(this);
        for (Identity identity : newSources) {
            LOGGER.fine("Testing new identity: " + identity);
            checkIdentity(identity);
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("New identities: " + sources);
        }
    }

    /**
     * Records the lookup request for the specified domain & option.
     *
     * @param domain The domain that is being looked up
     * @param option The option that is being looked up
     */
    protected static void doStats(final String domain, final String option) {
        final String key = domain + "." + option;

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

    /**
     * Adds a change listener for the specified domain.
     *
     * @param domain The domain to be monitored
     * @param listener The listener to register
     */
    public void addChangeListener(final String domain,
            final ConfigChangeListener listener) {
        addListener(domain, listener);
    }

    /**
     * Adds a change listener for the specified domain and key.
     *
     * @param domain The domain of the option
     * @param key The option to be monitored
     * @param listener The listener to register
     */
    public void addChangeListener(final String domain, final String key,
            final ConfigChangeListener listener) {
        addListener(domain + "." + key, listener);
    }

    /**
     * Removes the specified listener for all domains and options.
     *
     * @param listener The listener to be removed
     */
    public void removeListener(final ConfigChangeListener listener) {
        synchronized (listeners) {
            listeners.removeFromAll(listener);
        }
    }

    /**
     * Adds the specified listener to the internal map/list.
     *
     * @param key The key to use (domain or domain.key)
     * @param listener The listener to register
     */
    private void addListener(final String key,
            final ConfigChangeListener listener) {
        synchronized (listeners) {
            listeners.add(key, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        final List<ConfigChangeListener> targets
                = new ArrayList<ConfigChangeListener>();

        if (listeners.containsKey(domain)) {
            targets.addAll(listeners.get(domain));
        }

        if (listeners.containsKey(domain + "." + key)) {
            targets.addAll(listeners.get(domain + "." + key));
        }

        for (ConfigChangeListener listener : targets) {
            listener.configChanged(domain, key);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void identityAdded(final Identity identity) {
        checkIdentity(identity);
    }

    /** {@inheritDoc} */
    @Override
    public void identityRemoved(final Identity identity) {
        removeIdentity(identity);
    }

}
