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

package com.dmdirc.plugins;

import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.util.SimpleInjector;

import javax.inject.Inject;

/**
 * Utility class that can initialise a {@link SimpleInjector} for use by plugins.
 *
 * Eventually this should be replaced by using the same DI framework for plugins
 * as for the client.
 */
public class PluginInjectorInitialiser {

    private final ActionManager actionManager;
    private final PluginManager pluginManager;
    private final IdentityManager identityManager;
    private final ServerManager serverManager;
    private final CommandManager commandManager;
    private final MessageSinkManager messageSinkManager;
    private final WindowManager windowManager;
    private final StatusBarManager statusBarManager;
    private final PreferencesManager preferencesManager;
    private final PerformWrapper performWrapper;
    private final LifecycleController lifecycleController;

    /**
     * Creates a new {@link PluginInjectorInitialiser} which will inject all
     * of the given parameters.
     */
    @Inject
    public PluginInjectorInitialiser(
            final ActionManager actionManager,
            final PluginManager pluginManager,
            final IdentityManager identityManager,
            final ServerManager serverManager,
            final CommandManager commandManager,
            final MessageSinkManager messageSinkManager,
            final WindowManager windowManager,
            final StatusBarManager statusBarManager,
            final PreferencesManager preferencesManager,
            final PerformWrapper performWrapper,
            final LifecycleController lifecycleController) {
        this.actionManager = actionManager;
        this.pluginManager = pluginManager;
        this.identityManager = identityManager;
        this.serverManager = serverManager;
        this.commandManager = commandManager;
        this.messageSinkManager = messageSinkManager;
        this.windowManager = windowManager;
        this.statusBarManager = statusBarManager;
        this.preferencesManager = preferencesManager;
        this.performWrapper = performWrapper;
        this.lifecycleController = lifecycleController;
    }

    /**
     * Initialises the given injector with all of the known "global" managers.
     *
     * @param injector The injector to be initialised
     */
    public void initialise(final SimpleInjector injector) {
        injector.addParameter(Main.class, Main.mainInstance);
        injector.addParameter(actionManager);
        injector.addParameter(PluginManager.class, pluginManager);
        injector.addParameter(identityManager);
        injector.addParameter(ServerManager.class, serverManager);
        injector.addParameter(commandManager);
        injector.addParameter(MessageSinkManager.class, messageSinkManager);
        injector.addParameter(WindowManager.class, windowManager);
        injector.addParameter(statusBarManager);
        injector.addParameter(PreferencesManager.class, preferencesManager);
        injector.addParameter(PerformWrapper.class, performWrapper);
        injector.addParameter(LifecycleController.class, lifecycleController);
    }
}
