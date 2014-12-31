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

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;

/**
 * Facilitates migration of a {@link ConfigManager}.
 */
public class ConfigManagerMigrator implements ConfigProviderMigrator {

    /** The config manager that will be migrated. */
    private final ConfigManager configManager;

    /**
     * Creates a new instance of {@link ConfigManagerMigrator} that will act on the specified
     * manager.
     *
     * <p>
     * Package private - should only be created in specific circumstances by a
     * {@link IdentityManager}, and only with newly created {@link ConfigManager}s.
     *
     * @param configManager The config manager to migrate.
     */
    ConfigManagerMigrator(final ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void migrate(final String protocol, final String ircd, final String network,
            final String server) {
        migrate(protocol, ircd, network, server, "<Unknown>");
    }

    @Override
    public void migrate(final String protocol, final String ircd, final String network,
            final String server, final String channel) {
        configManager.migrate(protocol, ircd, network, server, channel);
    }

    @Override
    public AggregateConfigProvider getConfigProvider() {
        return configManager;
    }

}
