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

package com.dmdirc.plugins;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.Version;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides helper methods for dealing with plugins.
 */
@Singleton
public class CorePluginHelper {

    private final ServiceManager serviceManager;

    @Inject
    public CorePluginHelper(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * Ensures that there is at least one provider of the specified service type by extracting
     * matching core plugins. Plugins must be named so that their file name starts with the service
     * type, and then an underscore.
     *
     * @param corePluginExtractor Extractor to use if the service doesn't exist
     * @param pm                  The plugin manager to use to access services
     * @param serviceType         The type of service that should exist
     */
    public void ensureExists(
            final CorePluginExtractor corePluginExtractor,
            final PluginManager pm,
            final String serviceType) {
        if (serviceManager.getServicesByType(serviceType).isEmpty()) {
            corePluginExtractor.extractCorePlugins(serviceType + "_");
            pm.refreshPlugins();
        }
    }

    /**
     * Checks whether the plugins bundled with this release of DMDirc are newer than the plugins
     * known by the specified {@link PluginManager}. If the bundled plugins are newer, they are
     * automatically extracted.
     *
     * @param corePluginExtractor Extractor to use if plugins need updating.
     * @param pm                  The plugin manager to use to check plugins
     * @param config              The configuration source for bundled versions
     */
    public void checkBundledPlugins(
            final CorePluginExtractor corePluginExtractor,
            final PluginManager pm,
            final AggregateConfigProvider config) {
        pm.getAllPlugins().stream().filter(plugin -> config
                .hasOptionString("bundledplugins_versions", plugin.getName())).forEach(plugin -> {
            final Version bundled =
                    new Version(config.getOption("bundledplugins_versions", plugin.getName()));
            final Version installed = plugin.getVersion();

            if (installed.compareTo(bundled) < 0) {
                corePluginExtractor.extractCorePlugins(plugin.getName());
            }
        });
    }

}
