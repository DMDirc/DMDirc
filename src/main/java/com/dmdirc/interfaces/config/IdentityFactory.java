/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.interfaces.config;

import com.dmdirc.Precondition;
import com.dmdirc.config.ConfigTarget;

/**
 * Defines methods implemented by a factory which can create useful identities.
 */
// TODO: Rename this to ConfigFactory or something along those lines.
public interface IdentityFactory {

    /**
     * Retrieves the config for the specified channel@network. The config is created if it doesn't
     * exist.
     *
     * @param network The name of the network
     * @param channel The name of the channel
     *
     * @return A config provider for the channel
     */
    @Precondition({"The specified network is non-null and not empty",
            "The specified channel is non-null and not empty"})
    ConfigProvider createChannelConfig(String network, String channel);

    /**
     * Retrieves the config for the specified network. The config is created if it doesn't exist.
     *
     * @param network The name of the network
     *
     * @return A config provider for the network
     */
    @Precondition("The specified network is non-null and not empty")
    ConfigProvider createNetworkConfig(String network);

    /**
     * Retrieves the config for the specified server. The config is created if it doesn't exist.
     *
     * @param server The name of the server
     *
     * @return A config provider for the server
     */
    @Precondition("The specified server is non-null and not empty")
    ConfigProvider createServerConfig(String server);

    /**
     * Creates a custom configuration with the specified name and type.
     *
     * @param name The name of the configuration.
     * @param type The custom type of the configuration.
     *
     * @return A custom config provider.
     */
    ConfigProvider createCustomConfig(String name, String type);

    /**
     * Creates a profile configuration with the specified name.
     *
     * @param name The name of the profile.
     *
     * @return A custom config provider.
     */
    ConfigProvider createProfileConfig(String name);

    /**
     * Creates a configuration for the specified target.
     *
     * @param target The target of the configuration.
     *
     * @return A config provider for the specified target.
     */
    ConfigProvider createConfig(ConfigTarget target);

    /**
     * Creates a new {@link AggregateConfigProvider} that may be migrated, with the given initial
     * configuration.
     *
     * @param protocol The protocol for the provider
     * @param ircd     The name of the ircd for the provider
     * @param network  The name of the network for the provider
     * @param server   The name of the server for the provider
     *
     * @return A new {@link ConfigProviderMigrator}.
     */
    ConfigProviderMigrator createMigratableConfig(String protocol, String ircd, String network,
            String server);

    /**
     * Creates a new {@link AggregateConfigProvider} that may be migrated, with the given initial
     * configuration.
     *
     * @param protocol The protocol for the provider
     * @param ircd     The name of the ircd for the provider
     * @param network  The name of the network for the provider
     * @param server   The name of the server for the provider
     * @param channel  The name of the channel for the provider
     *
     * @return A new {@link ConfigProviderMigrator}.
     */
    ConfigProviderMigrator createMigratableConfig(String protocol, String ircd, String network,
            String server, String channel);

    /**
     * Creates a new aggregate, read-only config.
     *
     * @param protocol The protocol for this provider.
     * @param ircd     The name of the ircd for this provider.
     * @param network  The name of the network for this provider.
     * @param server   The name of the server for this provider.
     *
     * @return A new {@link AggregateConfigProvider}.
     */
    AggregateConfigProvider createAggregateConfig(String protocol, String ircd, String network,
            String server);

    /**
     * Creates a new aggregate, read-only config.
     *
     * @param protocol The protocol for this provider.
     * @param ircd     The name of the ircd for this provider.
     * @param network  The name of the network for this provider.
     * @param server   The name of the server for this provider.
     * @param channel  The name of the channel for this provider.
     *
     * @return A new {@link AggregateConfigProvider}.
     */
    AggregateConfigProvider createAggregateConfig(String protocol, String ircd, String network,
            String server, String channel);

}
