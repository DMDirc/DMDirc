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

import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.util.MapList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The config manager manages the various config sources for each entity.
 * 
 * @author chris
 */
public final class ConfigManager extends ConfigSource implements Serializable,
        ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Temporary map for lookup stats. */
    private static final Map<String, Integer> stats = new TreeMap<String, Integer>();
    
    /** A list of sources for this config manager. */
    private List<Identity> sources;
    
    /** The listeners registered for this manager. */
    private final MapList<String, ConfigChangeListener> listeners
            = new MapList<String, ConfigChangeListener>();
    
    /** The ircd this manager is for. */
    private final String ircd;
    /** The network this manager is for. */
    private final String network;
    /** The server this manager is for. */
    private final String server;
    /** The channel this manager is for. */
    private final String channel;
    
    /**
     * Creates a new instance of ConfigManager.
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     */
    public ConfigManager(final String ircd, final String network,
            final String server) {
        this(ircd, network, server, "<Unknown>");
    }
    
    /**
     * Creates a new instance of ConfigManager.
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     * @param channel The name of the channel for this manager
     */
    public ConfigManager(final String ircd, final String network,
            final String server, final String channel) {
        final String chanName = channel + "@" + network;
        sources = IdentityManager.getSources(ircd, network, server, chanName);
        
        for (Identity identity : sources) {
            identity.addListener(this);
        }
        
        this.ircd = ircd;
        this.network = network;
        this.server = server;
        this.channel = chanName;
        
        IdentityManager.addConfigManager(this);
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
    public boolean hasOption(final String domain, final String option) {
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
     * Returns the name of all known options.
     *
     * @return A list of options
     */
    public Set<String> getOptions() {
        final HashSet<String> res = new HashSet<String>();
        
        synchronized (sources) {
            for (Identity source : sources) {
                for (String key : source.getOptions()) {
                    res.add(key);
                }
            }
        }
        
        return res;
    }
    
    /**
     * Returns the name of all the options in the specified domain. If the
     * domain doesn't exist, an empty list is returned.
     *
     * @param domain The domain to search
     * @return A list of options in the specified domain
     */
    public List<String> getOptions(final String domain) {
        final ArrayList<String> res = new ArrayList<String>();
        
        for (String key : getOptions()) {
            if (key.startsWith(domain + ".")) {
                res.add(key.substring(domain.length() + 1));
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
        synchronized (sources) {
            sources.remove(identity);
        }
    }
    
    /**
     * Called whenever there is a new identity available. Checks if the
     * identity is relevant for this manager, and adds it if it is.
     * @param identity The identity to be checked
     */
    public void checkIdentity(final Identity identity) {
        String comp;
        
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
            // We don't want profiles
            comp = null;
            break;
        default:
            comp = "";
            break;
        }
        
        if (comp != null && comp.equalsIgnoreCase(identity.getTarget().getData())) {
            synchronized (sources) {
                sources.add(identity);
                identity.addListener(this);
                Collections.sort(sources);
            }
        }
    }
    
    /**
     * Returns the name of all domains known by this manager.
     *
     * @return A list of domains known to this manager
     */
    public List<String> getDomains() {
        final ArrayList<String> res = new ArrayList<String>();
        String domain;
        
        for (String key : getOptions()) {
            domain = key.substring(0, key.indexOf('.'));
            if (!res.contains(domain)) {
                res.add(domain);
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
     * Records the lookup request for the specified domain & option.
     *
     * @param domain The domain that is being looked up
     * @param option The option that is being looked up
     */
    private void doStats(final String domain, final String option) {
        final String key = domain + "." + option;
        
        stats.put(key, 1 + (stats.containsKey(key) ? stats.get(key) : 0));
    }
    
    /**
     * Retrieves the statistic map.
     *
     * @return A map of config options to lookup counts
     */
    public static Map<String, Integer> getStats() {
        return stats;
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
        listeners.removeFromAll(listener);
    }
    
    /**
     * Adds the specified listener to the internal map/list.
     *
     * @param key The key to use (domain or domain.key)
     * @param listener The listener to register
     */
    private void addListener(final String key,
            final ConfigChangeListener listener) {
        listeners.add(key, listener);
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
}
