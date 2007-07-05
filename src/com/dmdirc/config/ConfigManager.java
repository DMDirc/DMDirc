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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The config manager manages the various config sources for each entity.
 * @author chris
 */
public final class ConfigManager implements Serializable, ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** A list of sources for this config manager. */
    private List<Identity> sources;
    
    /** The listeners registered for this manager. */
    private final Map<String, List<ConfigChangeListener>> listeners
            = new HashMap<String, List<ConfigChangeListener>>();
    
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
    
    /**
     * Retrieves the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the option
     */
    public String getOption(final String domain, final String option) {
        for (Identity source : sources) {
            if (source.hasOption(domain, option)) {
                return source.getOption(domain, option);
            }
        }
        
        throw new IndexOutOfBoundsException("Config option not found: " + domain + "." + option);
    }
    
    /**
     * Retrieves the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback Value to use if the option isn't set
     * @return The value of the option
     */
    public String getOption(final String domain, final String option,
            final String fallback) {
        for (Identity source : sources) {
            if (source.hasOption(domain, option)) {
                return source.getOption(domain, option);
            }
        }
        
        return fallback;
    }
    
    /**
     * Retrieves a colour representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback The colour that should be used in case of error
     * @return The colour representation of the option
     */
    public Color getOptionColour(final String domain, final String option,
            final Color fallback) {
        if (!hasOption(domain, option)) {
            return fallback;
        }
        
        return ColourManager.parseColour(getOption(domain, option), fallback);
    }
    
    /**
     * Retrieves a boolean representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The boolean representation of the option
     */
    public boolean getOptionBool(final String domain, final String option) {
        if (!hasOption(domain, option)) {
            return false;
        }
        
        return Boolean.parseBoolean(getOption(domain, option));
    }
    
    /**
     * Retrieves an integral representation of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param fallback The value to use if the config isn't valud
     * @return The integer representation of the option
     */
    public int getOptionInt(final String domain, final String option,
            final int fallback) {
        if (!hasOption(domain, option)) {
            return fallback;
        }
        
        int res;
        
        try {
            res = Integer.parseInt(getOption(domain, option));
        } catch (NumberFormatException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Invalid number format for " + domain + "." + option);
            res = fallback;
        }
        
        return res;
    }
    
    /**
     * Returns the name of all known options.
     *
     * @return A list of options
     */
    public List<String> getOptions() {
        final ArrayList<String> res = new ArrayList<String>();
        
        for (Identity source : sources) {
            for (String key : source.getOptions()) {
                res.add(key);
            }
        }
        
        return res;
    }
    
    /**
     * Returns the name of all the options in the specified domain.
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
     * Returns the scope of the specified option.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The scope of the option
     */
    public ConfigTarget getOptionScope(final String domain, final String option) {
        for (Identity source : sources) {
            if (source.hasOption(domain, option)) {
                return source.getTarget();
            }
        }
        
        throw new IndexOutOfBoundsException("Config option not found: " + domain + "." + option);
    }
    
    /**
     * Determines if this manager has the specified option.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff the option exists, false otherwise.
     */
    public boolean hasOption(final String domain, final String option) {
        for (Identity source : sources) {
            if (source.hasOption(domain, option)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Called whenever there is a new identity available. Checks if the
     * identity is relevant for this manager, and adds it if it is.
     * @param identity The identity to be checked
     */
    public void checkIdentity(final Identity identity) {
        String comp = "";
        
        switch (identity.getTarget().getType()) {
        case ConfigTarget.TYPE_IRCD:
            comp = ircd;
            break;
        case ConfigTarget.TYPE_NETWORK:
            comp = network;
            break;
        case ConfigTarget.TYPE_SERVER:
            comp = server;
            break;
        case ConfigTarget.TYPE_CHANNEL:
            comp = channel;
            break;
        default:
            comp = "<Unknown>";
            break;
        }
        
        if (comp != null && comp.equalsIgnoreCase(identity.getTarget().getData())) {
            sources.add(identity);
            identity.addListener(this);
            Collections.sort(sources);
        }
    }
    
    /**
     * Retrieves a list of sources for this config manager.
     * @return This config manager's sources.
     */
    public List<Identity> getSources() {
        return sources;
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
       for (List<ConfigChangeListener> list : listeners.values()) {
           list.remove(listener);
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
        if (!listeners.containsKey(key)) {
            listeners.put(key, new ArrayList<ConfigChangeListener>());
        }
        
        listeners.get(key).add(listener);
    }
    
    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key,
            final String oldValue, final String newValue) {
        final List<ConfigChangeListener> targets
                = new ArrayList<ConfigChangeListener>();
        
        if (listeners.containsKey(domain)) {
            targets.addAll(listeners.get(domain));
        }
        
        if (listeners.containsKey(domain + "." + key)) {
            targets.addAll(listeners.get(domain + "." + key));            
        }
        
        for (ConfigChangeListener listener : targets) {
            listener.configChanged(domain, key, oldValue, newValue);
        }
    }
}
