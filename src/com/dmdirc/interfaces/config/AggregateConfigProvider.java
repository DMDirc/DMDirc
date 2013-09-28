/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.interfaces.config;

import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.ConfigChangeListener;

import java.util.List;
import java.util.Set;

/**
 * A configuration provider which aggregates an ordered list of other
 * {@link ConfigProvider}s.
 *
 * Because this provider exposes settings from multiple independent sources,
 * it cannot change any settings as the scope of such changes may be unclear.
 */
public interface AggregateConfigProvider extends ReadOnlyConfigProvider {

    /**
     * Adds a change listener for the specified domain.
     *
     * @param domain The domain to be monitored
     * @param listener The listener to register
     */
    void addChangeListener(String domain, ConfigChangeListener listener);

    /**
     * Adds a change listener for the specified domain and key.
     *
     * @param domain The domain of the option
     * @param key The option to be monitored
     * @param listener The listener to register
     */
    void addChangeListener(String domain, String key, ConfigChangeListener listener);

    /**
     * Returns the name of all domains known by this manager.
     *
     * @return A list of domains known to this manager
     */
    Set<String> getDomains();

    /**
     * Retrieves a list of sources for this config manager.
     * @return This config manager's sources.
     */
    List<Identity> getSources();

    /**
     * Removes the specified listener for all domains and options.
     *
     * @param listener The listener to be removed
     */
    void removeListener(ConfigChangeListener listener);

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
    void migrate(String protocol, String ircd, String network, String server);

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
    void migrate(String protocol, String ircd, String network, String server, String channel);

}
