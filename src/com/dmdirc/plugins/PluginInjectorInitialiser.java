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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.util.SimpleInjector;
import com.dmdirc.util.URLBuilder;

import javax.inject.Inject;

/**
 * Utility class that can initialise a {@link SimpleInjector} for use by plugins.
 *
 * Eventually this should be replaced by using the same DI framework for plugins as for the client.
 */
public class PluginInjectorInitialiser {

    private final PluginManager pluginManager;
    private final IdentityController identityController;
    private final ConnectionManager connectionManager;
    private final ThemeManager themeManager;
    private final CommandManager commandManager;
    private final MessageSinkManager messageSinkManager;
    private final WindowManager windowManager;
    private final PreferencesManager preferencesManager;
    private final LifecycleController lifecycleController;
    private final CorePluginExtractor corePluginExtractor;
    private final URLBuilder urlBuilder;
    private final ColourManager colourManager;
    private final DMDircMBassador eventBus;

    @Inject
    public PluginInjectorInitialiser(
            final PluginManager pluginManager,
            final IdentityController identityController,
            final ConnectionManager connectionManager,
            final ThemeManager themeManager,
            final CommandManager commandManager,
            final MessageSinkManager messageSinkManager,
            final WindowManager windowManager,
            final PreferencesManager preferencesManager,
            final LifecycleController lifecycleController,
            final CorePluginExtractor corePluginExtractor,
            final URLBuilder urlBuilder,
            @GlobalConfig final ColourManager colourManager,
            final DMDircMBassador eventBus) {
        this.pluginManager = pluginManager;
        this.identityController = identityController;
        this.connectionManager = connectionManager;
        this.themeManager = themeManager;
        this.commandManager = commandManager;
        this.messageSinkManager = messageSinkManager;
        this.windowManager = windowManager;
        this.preferencesManager = preferencesManager;
        this.lifecycleController = lifecycleController;
        this.corePluginExtractor = corePluginExtractor;
        this.urlBuilder = urlBuilder;
        this.colourManager = colourManager;
        this.eventBus = eventBus;
    }

    /**
     * Initialises the given injector with all of the known "global" managers.
     *
     * @param injector The injector to be initialised
     */
    public void initialise(final SimpleInjector injector) {
        injector.addParameter(PluginManager.class, pluginManager);
        injector.addParameter(identityController);
        injector.addParameter(ConnectionManager.class, connectionManager);
        injector.addParameter(commandManager);
        injector.addParameter(MessageSinkManager.class, messageSinkManager);
        injector.addParameter(WindowManager.class, windowManager);
        injector.addParameter(PreferencesManager.class, preferencesManager);
        injector.addParameter(LifecycleController.class, lifecycleController);
        injector.addParameter(corePluginExtractor);
        injector.addParameter(themeManager);
        injector.addParameter(urlBuilder);
        injector.addParameter(colourManager);
        injector.addParameter(eventBus);
    }

}
