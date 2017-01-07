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

package com.dmdirc.plugins;

import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.util.system.SystemLifecycleComponent;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.updater.manager.UpdateManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import static com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;

/**
 * Dagger module for plugin-related components.
 */
@SuppressWarnings("TypeMayBeWeakened")
@Module(library = true, complete = false)
public class PluginModule {

    @Provides
    @Singleton
    public PluginManager getPluginManager(
            final EventBus eventBus,
            final IdentityController identityController,
            final UpdateManager updateManager,
            final ObjectGraph objectGraph,
            final ServiceManager serviceManager,
            final CorePluginHelper pluginHelper,
            final PluginFileHandler fileHandler,
            @Directory(DirectoryType.PLUGINS) final String directory) {
        final PluginManager manager = new PluginManager(eventBus, serviceManager,
                identityController, updateManager, objectGraph, fileHandler, directory);
        manager.refreshPlugins();

        final CorePluginExtractor extractor = new CorePluginExtractor(manager, directory);
        pluginHelper.checkBundledPlugins(extractor, manager,
                identityController.getGlobalConfiguration());

        for (String service : new String[]{"ui", "tabcompletion", "parser"}) {
            pluginHelper.ensureExists(extractor, manager, service);
        }

        // The user may have an existing parser plugin (e.g. twitter) which
        // will satisfy the service existence check above, but will render the
        // client pretty useless, so we'll force IRC extraction for now.
        extractor.extractCorePlugins("parser_irc");
        manager.refreshPlugins();
        return manager;
    }

    @Provides
    public ServiceManager getServiceManager(final ServiceManagerImpl serviceManager) {
        return serviceManager;
    }

    @Provides
    public ServiceLocator getServiceLocator(final LegacyServiceLocator locator) {
        return locator;
    }

    @Provides(type = Provides.Type.SET)
    @Singleton
    public SystemLifecycleComponent getEventFormatManager(final PluginEventFormatManager manager) {
        return manager;
    }

}
